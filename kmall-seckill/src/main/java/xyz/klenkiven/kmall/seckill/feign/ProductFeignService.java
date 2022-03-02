package xyz.klenkiven.kmall.seckill.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import xyz.klenkiven.kmall.common.utils.R;

@FeignClient("kmall-product")
public interface ProductFeignService {

    /**
     * SKU信息
     */
    @RequestMapping("/product/skuinfo/info/{skuId}")
    public R getSkuInfo(@PathVariable("skuId") Long skuId);

}
