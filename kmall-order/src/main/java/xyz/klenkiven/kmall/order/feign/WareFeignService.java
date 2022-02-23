package xyz.klenkiven.kmall.order.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import xyz.klenkiven.kmall.common.to.SkuHasStockTO;
import xyz.klenkiven.kmall.common.utils.Result;
import xyz.klenkiven.kmall.order.model.dto.FareDTO;
import xyz.klenkiven.kmall.order.model.dto.WareSkuLockDTO;

import java.util.List;

@FeignClient("kmall-ware")
public interface WareFeignService {

    /**
     * [RPC] Query SKU has Stock
     * /ware/waresku/has-stock
     */
    @PostMapping("/ware/waresku/has-stock")
    public Result<List<SkuHasStockTO>> getSkuHasStock(@RequestBody List<Long> skuIds);

    /**
     * [FEIGN] Query User Fare
     * /ware/waresku/fare
     */
    @PostMapping("/ware/waresku/fare")
    public Result<FareDTO> getFare(@RequestParam Long addrId);

    /**
     * [FEIGN] Ware Lock SKU for Order
     */
    @PostMapping("/ware/waresku/lock/order")
    public Result<Boolean> lockOrder(@RequestBody WareSkuLockDTO lock);

}
