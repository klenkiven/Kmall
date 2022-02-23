package xyz.klenkiven.kmall.order.service.impl;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.util.Arrays;
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
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import xyz.klenkiven.kmall.common.constant.OrderConstant;
import xyz.klenkiven.kmall.common.to.SkuHasStockTO;
import xyz.klenkiven.kmall.common.to.UserLoginTO;
import xyz.klenkiven.kmall.common.utils.PageUtils;
import xyz.klenkiven.kmall.common.utils.Query;

import xyz.klenkiven.kmall.common.utils.Result;
import xyz.klenkiven.kmall.order.dao.OrderDao;
import xyz.klenkiven.kmall.order.entity.OrderEntity;
import xyz.klenkiven.kmall.order.feign.CartFeignService;
import xyz.klenkiven.kmall.order.feign.MemberFeignService;
import xyz.klenkiven.kmall.order.feign.WareFeignService;
import xyz.klenkiven.kmall.order.interceptor.UserLoginInterceptor;
import xyz.klenkiven.kmall.order.model.dto.MemberAddressDTO;
import xyz.klenkiven.kmall.order.model.dto.OrderItemDTO;
import xyz.klenkiven.kmall.order.model.form.OrderSubmitForm;
import xyz.klenkiven.kmall.order.model.vo.OrderConfirmVO;
import xyz.klenkiven.kmall.order.model.vo.SubmitResultVO;
import xyz.klenkiven.kmall.order.service.OrderService;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    private final MemberFeignService memberFeignService;
    private final CartFeignService cartFeignService;
    private final WareFeignService wareFeignService;

    private final StringRedisTemplate redisTemplate;
    private final Executor executor;

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
        }, executor).thenRunAsync(()->{
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

    @Override
    public SubmitResultVO submitOrder(OrderSubmitForm form) {
        SubmitResultVO result = new SubmitResultVO();
        UserLoginTO user = UserLoginInterceptor.loginUser.get();

        // Verify Idempotent Token with CAS Op
        String orderTokenKey = OrderConstant.USER_ORDER_TOKEN_PREFIX + user.getId();
        String redisCASScript =
                "if redis.call('get', KEYS[1]) == ARGV[1] then" +
                "    redis.call('del', KEYS[1])" +
                "    return 1" +
                "else" +
                "    return 0" +
                "end";
        Long redisCas = redisTemplate.execute(RedisScript.of(redisCASScript, Long.class),
                List.of(orderTokenKey),
                form.getOrderToken());
        // If Fail Return Fail Code
        if (redisCas == null || redisCas == 0L) {
            result.setCode(500);
            return result;
        }

        // Create Order

        // Verify Price

        // Lock Quantity in Stock

        return null;
    }


    public OrderServiceImpl(MemberFeignService memberFeignService,
                            CartFeignService cartFeignService,
                            WareFeignService wareFeignService,
                            StringRedisTemplate redisTemplate,
                            ThreadPoolExecutor executor) {
        this.memberFeignService = memberFeignService;
        this.cartFeignService = cartFeignService;
        this.wareFeignService = wareFeignService;
        this.redisTemplate = redisTemplate;
        this.executor = executor;
    }
}