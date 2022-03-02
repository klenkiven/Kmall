package xyz.klenkiven.kmall.product.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import xyz.klenkiven.kmall.common.to.SeckillSkuRedisTO;
import xyz.klenkiven.kmall.common.utils.Result;

import java.util.List;

@FeignClient("kmall-seckill")
public interface SeckillFeignService {

    /**
     * [FEIGN] Get All Current Seckill SKU
     */
    @GetMapping("/getCurrentSeckillSkus")
    public Result<List<SeckillSkuRedisTO>> getCurrentSeckillSkus();

    /**
     * [FEIGN] Get Seckill SKU in Redis
     */
    @GetMapping("/sku/seckill/{skuID}")
    public Result<SeckillSkuRedisTO> getSkuSeckill(@PathVariable Long skuID);

}
