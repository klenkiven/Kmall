package xyz.klenkiven.kmall.order.service.impl;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public OrderConfirmVO confirmOrder() {
        OrderConfirmVO confirmVO = new OrderConfirmVO();
        UserLoginTO user = UserLoginInterceptor.loginUser.get();

        // [FEIGN] Member Address;
        Result<List<MemberAddressDTO>> address = memberFeignService.getAddress(user.getId());
        confirmVO.setAddress(address.getData());

        // [FEIGN] Member Cart Item which checked
        Result<List<OrderItemDTO>> checkedItem = cartFeignService.getCheckedItem();
        confirmVO.setItems(checkedItem.getData());

        // Get User Credit
        confirmVO.setIntegration(user.getIntegration());

        return confirmVO;
    }


    public OrderServiceImpl(MemberFeignService memberFeignService, CartFeignService cartFeignService) {
        this.memberFeignService = memberFeignService;
        this.cartFeignService = cartFeignService;
    }
}