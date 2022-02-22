package xyz.klenkiven.kmall.ware.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import xyz.klenkiven.kmall.common.utils.R;

import java.util.List;

@FeignClient("kmall-member")
public interface MemberFeignService {

    /**
     * Member Receive Address
     */
    @GetMapping("/member/memberreceiveaddress/info/{id}")
    public R getAddress(@PathVariable Long id);

}
