package xyz.klenkiven.kmall.order.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import xyz.klenkiven.kmall.common.utils.Result;
import xyz.klenkiven.kmall.order.model.dto.OrderItemDTO;

import java.util.List;

@FeignClient("kmall-cart")
public interface CartFeignService {

    /**
     * Get Current Order Item List
     */
    @GetMapping("/userCheckedItem")
    public Result<List<OrderItemDTO>> getCheckedItem();

}
