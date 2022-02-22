package xyz.klenkiven.kmall.order.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import xyz.klenkiven.kmall.common.utils.Result;
import xyz.klenkiven.kmall.order.model.dto.MemberAddressDTO;

import java.util.List;

/**
 * Kmall Member Service Feign Client
 * @author klenkiven
 */
@FeignClient("kmall-member")
public interface MemberFeignService {

    /**
     * Member Receive Address
     */
    @GetMapping("/member/memberreceiveaddress/{memberId}/addresses")
    public Result<List<MemberAddressDTO>> getAddress(@PathVariable Long memberId);

}
