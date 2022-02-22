package xyz.klenkiven.kmall.order.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import xyz.klenkiven.kmall.common.to.SkuHasStockTO;
import xyz.klenkiven.kmall.common.utils.Result;

import java.util.List;

@FeignClient("kmall-ware")
public interface WareFeignService {

    /**
     * [RPC] Query SKU has Stock
     * /ware/waresku/has-stock
     */
    @PostMapping("/ware/waresku/has-stock")
    public Result<List<SkuHasStockTO>> getSkuHasStock(@RequestBody List<Long> skuIds);

}
