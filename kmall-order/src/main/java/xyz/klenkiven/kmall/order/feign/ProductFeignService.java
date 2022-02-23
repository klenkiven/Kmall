package xyz.klenkiven.kmall.order.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import xyz.klenkiven.kmall.common.utils.Result;
import xyz.klenkiven.kmall.order.model.dto.SpuInfoDTO;

@FeignClient("kmall-product")
public interface ProductFeignService {

    /**
     * [FEIGN] Get Spu info from sku
     */
    @GetMapping("/product/spuinfo/from-sku/{skuId}")
    public Result<SpuInfoDTO> getSpuFromSku(@PathVariable Long skuId);

}
