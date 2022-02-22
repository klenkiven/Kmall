package xyz.klenkiven.kmall.order.service.impl;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import xyz.klenkiven.kmall.common.to.UserLoginTO;
import xyz.klenkiven.kmall.common.utils.PageUtils;
import xyz.klenkiven.kmall.common.utils.Query;

import xyz.klenkiven.kmall.common.utils.Result;
import xyz.klenkiven.kmall.order.dao.OrderDao;
import xyz.klenkiven.kmall.order.entity.OrderEntity;
import xyz.klenkiven.kmall.order.feign.CartFeignService;
import xyz.klenkiven.kmall.order.feign.MemberFeignService;
import xyz.klenkiven.kmall.order.interceptor.UserLoginInterceptor;
import xyz.klenkiven.kmall.order.model.dto.MemberAddressDTO;
import xyz.klenkiven.kmall.order.model.dto.OrderItemDTO;
import xyz.klenkiven.kmall.order.model.vo.OrderConfirmVO;
import xyz.klenkiven.kmall.order.service.OrderService;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    private final MemberFeignService memberFeignService;
    private final CartFeignService cartFeignService;

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
        // Make RequestAttribute to all of the thread
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

        // [FEIGN] Member Address;
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
        }, executor);

        // Get User Credit
        confirmVO.setIntegration(user.getIntegration());

        CompletableFuture.allOf(addressFuture, cartItemFuture).join();
        return confirmVO;
    }


    public OrderServiceImpl(MemberFeignService memberFeignService,
                            CartFeignService cartFeignService,
                            ThreadPoolExecutor executor) {
        this.memberFeignService = memberFeignService;
        this.cartFeignService = cartFeignService;
        this.executor = executor;
    }
}