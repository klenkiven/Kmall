package xyz.klenkiven.kmall.order.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import xyz.klenkiven.kmall.common.constant.OrderConstant;
import xyz.klenkiven.kmall.common.exception.NoStockException;
import xyz.klenkiven.kmall.common.to.SkuHasStockTO;
import xyz.klenkiven.kmall.common.to.UserLoginTO;
import xyz.klenkiven.kmall.common.to.mq.OrderTO;
import xyz.klenkiven.kmall.common.utils.PageUtils;
import xyz.klenkiven.kmall.common.utils.Query;

import xyz.klenkiven.kmall.common.utils.Result;
import xyz.klenkiven.kmall.order.dao.OrderDao;
import xyz.klenkiven.kmall.order.dao.OrderItemDao;
import xyz.klenkiven.kmall.order.entity.OrderEntity;
import xyz.klenkiven.kmall.order.entity.OrderItemEntity;
import xyz.klenkiven.kmall.order.enume.OrderStatusEnum;
import xyz.klenkiven.kmall.order.feign.CartFeignService;
import xyz.klenkiven.kmall.order.feign.MemberFeignService;
import xyz.klenkiven.kmall.order.feign.ProductFeignService;
import xyz.klenkiven.kmall.order.feign.WareFeignService;
import xyz.klenkiven.kmall.order.interceptor.UserLoginInterceptor;
import xyz.klenkiven.kmall.order.model.dto.*;
import xyz.klenkiven.kmall.order.model.form.OrderSubmitForm;
import xyz.klenkiven.kmall.order.model.vo.OrderConfirmVO;
import xyz.klenkiven.kmall.order.model.vo.SubmitResultVO;
import xyz.klenkiven.kmall.order.service.OrderService;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    private final OrderItemDao orderItemDao;

    private final MemberFeignService memberFeignService;
    private final CartFeignService cartFeignService;
    private final WareFeignService wareFeignService;
    private final ProductFeignService productFeignService;

    private final StringRedisTemplate redisTemplate;
    private final RabbitTemplate rabbitTemplate;
    private final Executor executor;

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);
    private final ThreadLocal<OrderSubmitForm> orderSubmitForm = new ThreadLocal<>();

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<>()
        );

        return new PageUtils(page);
    }

    @Override
    public OrderConfirmVO confirmOrder() {
        OrderConfirmVO confirmVO = new OrderConfirmVO();
        UserLoginTO user = UserLoginInterceptor.loginUser.get();
        // Make RequestAttribute to all of threads
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

        // [FEIGN] Member Address
        CompletableFuture<Void> addressFuture = CompletableFuture.runAsync(() -> {
            RequestContextHolder.setRequestAttributes(requestAttributes);
            Result<List<MemberAddressDTO>> address = memberFeignService.getAddress(user.getId());
            confirmVO.setAddress(address.getData());
        }, executor);

        // [FEIGN] Member Cart Item which checked
        CompletableFuture<Void> cartItemFuture = CompletableFuture.runAsync(() -> {
            RequestContextHolder.setRequestAttributes(requestAttributes);
            Result<List<OrderItemDTO>> checkedItem = cartFeignService.getCheckedItem();
            confirmVO.setItems(checkedItem.getData());
        }, executor).thenRunAsync(() -> {
            // [FEIGN] Has Stock Bundled Query
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<Long> skuIdList = confirmVO.getItems().stream()
                    .map(OrderItemDTO::getSkuId).collect(Collectors.toList());
            Map<Long, Boolean> hasStockMap = wareFeignService.getSkuHasStock(skuIdList).getData().stream()
                    .collect(Collectors.toMap(SkuHasStockTO::getSkuId, SkuHasStockTO::getHasStock));
            confirmVO.setHasStockMap(hasStockMap);
        });

        // Get User Credit
        confirmVO.setIntegration(user.getIntegration());

        // Idempotent Token and Save to Redis
        String token = UUID.randomUUID().toString().replace("-", "");
        confirmVO.setToken(token);
        redisTemplate.opsForValue().set(
                OrderConstant.USER_ORDER_TOKEN_PREFIX + user.getId(),
                token,
                30, TimeUnit.MINUTES
        );

        CompletableFuture.allOf(addressFuture, cartItemFuture).join();
        return confirmVO;
    }

    // @GlobalTransactional(rollbackFor = {Exception.class})
    @Transactional(rollbackFor = {Exception.class})
    @Override
    public SubmitResultVO submitOrder(OrderSubmitForm form) {
//        log.info("start SEATA Global Transaction XID: {}, and Branch Type: {}", RootContext.getXID(), RootContext.getBranchType());
        SubmitResultVO result = new SubmitResultVO();
        UserLoginTO user = UserLoginInterceptor.loginUser.get();
        orderSubmitForm.set(form);

        // Verify Idempotent Token with CAS Op
        String orderTokenKey = OrderConstant.USER_ORDER_TOKEN_PREFIX + user.getId();
        String redisCASScript =
                "if redis.call('get', KEYS[1]) == ARGV[1] then" +
                        "    redis.call('del', KEYS[1])" +
                        "    return 1" +
                        " else" +
                        "    return 0" +
                        " end";
        Long redisCas = redisTemplate.execute(RedisScript.of(redisCASScript, Long.class),
                List.of(orderTokenKey),
                form.getOrderToken());

        // If Fail Return Fail Code
        if (redisCas == null || redisCas == 0L) {
            log.warn("redisCAS: {}, User {} Press submit too frequency.", redisCas, user.getUsername() + ":" + user.getId());
            result.setCode(1);
            return result;
        }

        // Create Order
        OrderCreateDTO orderCreate = createOrder();
        log.warn("orderCreate successfully!");

        // Verify Pay Price
        BigDecimal payAmount = orderCreate.getOrder().getPayAmount();
        BigDecimal payPrice = form.getPayPrice();
        if (Math.abs(payAmount.subtract(payAmount).doubleValue()) >= 0.01) {
            // Fail to Verify Price
            log.warn("Price is changed!");
            result.setCode(2);
            return result;
        }

        // Save Order and OrderItem List
        this.saveOrUpdate(orderCreate.getOrder());
        orderCreate.getOrderItems().forEach(orderItemDao::insert);
        log.info("Save Order and OrderItem successfully!");

        // Lock Quantity in Stock [Throw Exception for rollback]
        WareSkuLockDTO wareSkuLockDTO = new WareSkuLockDTO();
        wareSkuLockDTO.setOrderSn(orderCreate.getOrder().getOrderSn());
        List<OrderItemDTO> checkedItem = orderCreate.getOrderItems().stream()
                .map((item) -> {
                    OrderItemDTO orderItemDTO = new OrderItemDTO();
                    orderItemDTO.setCount(item.getSkuQuantity());
                    orderItemDTO.setSkuId(item.getSkuId());
                    orderItemDTO.setTitle(item.getSkuName());
                    return orderItemDTO;
                }).collect(Collectors.toList());
        wareSkuLockDTO.setLocks(checkedItem);
        Result<Boolean> stockResp = wareFeignService.lockOrder(wareSkuLockDTO);
        if (stockResp.getCode() != 0) {
            // Fail to lock and rollback
            log.warn("There is no Stock!");
            throw  new NoStockException();
        }

        // Success and Send to MQ
        result.setOrder(orderCreate.getOrder());
        result.setCode(0);
        rabbitTemplate.convertAndSend(
                "order-event-exchange",
                "order.create.order",
                orderCreate.getOrder()
        );
        return result;
    }

    @Override
    public OrderEntity getOrderByOrderSn(String orderSn) {
        return this.getOne(
                new QueryWrapper<OrderEntity>()
                        .eq("order_sn", orderSn)
        );
    }

    @Override
    public void closeOrder(OrderEntity order) {
        // Query for Order Current Status
        OrderEntity currentOrder = this.getById(order.getId());
        // Close Order and Set Order Status
        if (OrderStatusEnum.CREATE_NEW.getCode().equals(currentOrder.getStatus())) {
            OrderEntity update = new OrderEntity();
            update.setId(order.getId());
            update.setStatus(OrderStatusEnum.CANCLED.getCode());
            this.updateById(update);
            // Release Order and Notify Ware service to Unlock Stock
            OrderTO orderTO = new OrderTO();
            BeanUtils.copyProperties(currentOrder, orderTO);
            rabbitTemplate.convertAndSend(
                    "order-event-exchange",
                    "order.release.other",
                    orderTO
            );
        }
    }

    /**
     * Do Create Order
     *
     * @return create data
     */
    private OrderCreateDTO createOrder() {
        OrderCreateDTO orderCreate = new OrderCreateDTO();

        // Gen OrderSn
        String orderSn = IdWorker.getTimeId();
        // Build Order Entity and Set
        OrderEntity orderEntity = buildOrder(orderSn);
        orderCreate.setOrder(orderEntity);

        // Set Delivery Fare
        orderCreate.setFare(orderEntity.getFreightAmount());

        // Build order items
        List<OrderItemEntity> orderItemEntities = buildOrderItemList(orderSn);
        orderCreate.setOrderItems(orderItemEntities);

        // Compute Pay Price
        computePrice(orderEntity, orderItemEntities);

        return orderCreate;
    }

    /**
     * Compute Price For Order
     *
     * @param orderEntity       order entity
     * @param orderItemEntities order item entity
     */
    private void computePrice(OrderEntity orderEntity, List<OrderItemEntity> orderItemEntities) {
        // Item Price
        BigDecimal couponTotal = new BigDecimal("0.0");
        BigDecimal integrationTotal = new BigDecimal("0.0");
        BigDecimal promotionTotal = new BigDecimal("0.0");
        BigDecimal realTotal = new BigDecimal("0.0");
        Integer giftIntegrationTotal = 0;
        Integer giftGrowthTotal = 0;

        for (OrderItemEntity entity : orderItemEntities) {
            BigDecimal couponAmount = entity.getCouponAmount();
            BigDecimal integrationAmount = entity.getIntegrationAmount();
            BigDecimal promotionAmount = entity.getPromotionAmount();
            BigDecimal realAmount = entity.getRealAmount();
            Integer giftIntegration = entity.getGiftIntegration();
            Integer giftGrowth = entity.getGiftGrowth();
            couponTotal = couponTotal.add(couponAmount);
            integrationTotal = integrationTotal.add(integrationAmount);
            promotionTotal = promotionTotal.add(promotionAmount);
            realTotal = realTotal.add(realAmount);
            giftGrowthTotal += giftGrowth;
            giftIntegrationTotal += giftIntegration;
        }
        // Price Related
        orderEntity.setCouponAmount(couponTotal);
        orderEntity.setIntegrationAmount(integrationTotal);
        orderEntity.setPromotionAmount(promotionTotal);
        orderEntity.setTotalAmount(realTotal);
        orderEntity.setPayAmount(realTotal.add(orderEntity.getFreightAmount()));

        // Credit Related
        orderEntity.setGrowth(giftGrowthTotal);
        orderEntity.setIntegration(giftIntegrationTotal);

    }

    /**
     * Build Order Entity
     */
    private OrderEntity buildOrder(String orderSn) {
        UserLoginTO user = UserLoginInterceptor.loginUser.get();
        OrderSubmitForm form = this.orderSubmitForm.get();

        // Order Entity
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderSn(orderSn);

        // Set Order Status
        orderEntity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());

        // Member Id
        orderEntity.setMemberId(user.getId());
        orderEntity.setMemberUsername(user.getUsername());

        // [FEIGN] Get Delivery Fare Info
        Long addrId = form.getAddrId();
        FareDTO fare = wareFeignService.getFare(addrId).getData();

        // Set Delivery Fare
        orderEntity.setFreightAmount(fare.getFare());

        // Set Address
        MemberAddressDTO address = fare.getAddress();
        orderEntity.setReceiverCity(address.getCity());
        orderEntity.setReceiverDetailAddress(address.getDetailAddress());
        orderEntity.setReceiverName(address.getName());
        orderEntity.setReceiverPhone(address.getPhone());
        orderEntity.setReceiverPostCode(address.getPostCode());
        orderEntity.setReceiverProvince(address.getProvince());
        orderEntity.setReceiverRegion(address.getRegion());

        // Set Delete Status
        orderEntity.setDeleteStatus(0);

        return orderEntity;
    }

    /**
     * Build order items
     *
     * @return order item entity
     */
    private List<OrderItemEntity> buildOrderItemList(String orderSn) {
        // [FEIGN] Get all of items
        List<OrderItemDTO> orderItemVOs = cartFeignService.getCheckedItem().getData();
        return orderItemVOs.stream()
                .map((dto) -> {
                    OrderItemEntity entity = buildOrderItem(dto);
                    entity.setOrderSn(orderSn);
                    return entity;
                }).collect(Collectors.toList());
    }

    /**
     * Build OrderItem Entity
     *
     * @param dto item in cart
     * @return Entity
     */
    private OrderItemEntity buildOrderItem(OrderItemDTO dto) {
        OrderItemEntity entity = new OrderItemEntity();
        // Order Id
        // SPU Info
        SpuInfoDTO spuFromSku = productFeignService.getSpuFromSku(dto.getSkuId()).getData();
        entity.setSpuId(spuFromSku.getId());
        entity.setSpuBrand(spuFromSku.getBrandId().toString());
        entity.setSpuName(spuFromSku.getSpuName());
        entity.setCategoryId(spuFromSku.getCatalogId());

        // SKU Info
        entity.setSkuId(dto.getSkuId());
        entity.setSkuName(dto.getTitle());
        entity.setSkuPic(dto.getImage());
        entity.setSkuPrice(dto.getPrice());
        String skuAttr = StringUtils.collectionToDelimitedString(dto.getSkuAttrValues(), ";");
        entity.setSkuAttrsVals(skuAttr);
        entity.setSkuQuantity(dto.getCount());

        // Credit Info
        entity.setGiftGrowth(dto.getPrice().multiply(new BigDecimal(dto.getCount())).intValue());
        entity.setGiftIntegration(dto.getPrice().multiply(new BigDecimal(dto.getCount())).intValue());

        // Price Info
        entity.setPromotionAmount(new BigDecimal(0));
        entity.setCouponAmount(new BigDecimal(0));
        entity.setIntegrationAmount(new BigDecimal(0));

        // Real Price
        BigDecimal origin = entity.getSkuPrice()
                .multiply(new BigDecimal(entity.getSkuQuantity().toString()));
        BigDecimal real = origin.subtract(entity.getCouponAmount())
                .subtract(entity.getPromotionAmount())
                .subtract(entity.getIntegrationAmount());
        entity.setRealAmount(real);

        // [Omit] Reduce Info

        return entity;
    }


    public OrderServiceImpl(OrderItemDao orderItemDao,
                            MemberFeignService memberFeignService,
                            CartFeignService cartFeignService,
                            WareFeignService wareFeignService,
                            ProductFeignService productFeignService,
                            StringRedisTemplate redisTemplate,
                            RabbitTemplate rabbitTemplate,
                            ThreadPoolExecutor executor) {
        this.orderItemDao = orderItemDao;
        this.memberFeignService = memberFeignService;
        this.cartFeignService = cartFeignService;
        this.wareFeignService = wareFeignService;
        this.productFeignService = productFeignService;
        this.redisTemplate = redisTemplate;
        this.rabbitTemplate = rabbitTemplate;
        this.executor = executor;
    }
}