package xyz.klenkiven.kmall.member.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import xyz.klenkiven.kmall.common.utils.PageUtils;
import xyz.klenkiven.kmall.common.utils.Result;

import java.util.Map;

@FeignClient("kmall-order")
public interface OrderFeignService {

    /**
     * [FEIGN] 列表
     */
    @PostMapping("/order/order/listWithItem")
    public Result<PageUtils> listOrderItem(@RequestBody Map<String, Object> params);

}
