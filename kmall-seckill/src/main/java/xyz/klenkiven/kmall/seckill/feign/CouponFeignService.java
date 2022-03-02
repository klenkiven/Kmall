package xyz.klenkiven.kmall.seckill.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import xyz.klenkiven.kmall.common.utils.Result;
import xyz.klenkiven.kmall.seckill.model.dto.SeckillSessionDTO;

import java.util.List;

/**
 * Coupon Service Feign Client
 */
@FeignClient("kmall-coupon")
public interface CouponFeignService {

    /**
     * [FEIGN] Get Session in latest 3 days
     */
    @GetMapping("coupon/seckillsession/latest-3-days-session")
    public Result<List<SeckillSessionDTO>> latest3DaysSession();

}
