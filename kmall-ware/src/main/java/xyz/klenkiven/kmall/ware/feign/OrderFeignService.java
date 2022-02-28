package xyz.klenkiven.kmall.ware.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import xyz.klenkiven.kmall.common.utils.Result;
import xyz.klenkiven.kmall.common.to.mq.OrderTO;

/**
 * Order Feign Service
 * @author klenkiven
 */
@FeignClient("kmall-order")
public interface OrderFeignService {

    /**
     * [FEIGN] Get Order Status
     */
    @GetMapping("/order/order/status/{orderSn}")
    public Result<OrderTO> getOrderStatus(@PathVariable String orderSn);

}
