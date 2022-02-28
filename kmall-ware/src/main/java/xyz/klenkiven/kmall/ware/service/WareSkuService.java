package xyz.klenkiven.kmall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import xyz.klenkiven.kmall.common.to.mq.StockLockedTO;
import xyz.klenkiven.kmall.common.utils.PageUtils;
import xyz.klenkiven.kmall.ware.entity.WareSkuEntity;
import xyz.klenkiven.kmall.common.to.SkuHasStockTO;
import xyz.klenkiven.kmall.ware.vo.FareResp;
import xyz.klenkiven.kmall.common.to.mq.OrderTO;
import xyz.klenkiven.kmall.ware.vo.WareSkuLockDTO;

import java.util.List;
import java.util.Map;

/**
 * 商品库存
 *
 * @author klenkiven
 * @email wzl709@outlook.com
 * @date 2021-10-06 19:38:05
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * Add Sku Stock in Certain Ware
     */
    void addStock(Long wareId, Long skuId, Integer skuNum);

    /**
     * Query SKU has Stock
     */
    List<SkuHasStockTO> getSkuHasStock(List<Long> skuIds);

    /**
     * Get Fare
     */
    FareResp getFare(Long addrId);

    /**
     * Lock Stock for Order
     * @param lock order
     */
    void orderLockStock(WareSkuLockDTO lock);

    /**
     * Release SKU Stock
     * @param to Stock Locked TO
     */
    void releaseStockLock(StockLockedTO to);

    /**
     * Order Close Stock
     * <p>If order is closed and order service in stuck, Order will be never closed.</p>
     * @param order order
     */
    void unlockStock(OrderTO order);
}

