package xyz.klenkiven.kmall.ware.dao;

import org.apache.ibatis.annotations.Param;
import xyz.klenkiven.kmall.ware.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 商品库存
 * 
 * @author klenkiven
 * @email wzl709@outlook.com
 * @date 2021-10-06 19:38:05
 */
@Mapper
public interface WareSkuDao extends BaseMapper<WareSkuEntity> {

    /**
     * Add Sku Stock DAO
     */
    void addStock(@Param("wareId") Long wareId, @Param("skuId") Long skuId, @Param("skuNum") Integer skuNum);

    /**
     * Get Sku Stock
     */
    Long getStockBySkuId(@Param("skuId") Long skuId);

    /**
     * Get all available ware for sku
     * @param skuId sku id
     * @param count item count
     * @return ware id
     */
    List<Long> listWareIdHasStock(@Param("skuId") Long skuId, @Param("count") Integer count);

    /**
     * Lock SKU Stock if effect row is 0, fail
     * @param skuId sku id
     * @param wareId ware id
     * @param count count
     * @return effect row
     */
    Long lockSkuStock(@Param("skuId") Long skuId, @Param("wareId") Long wareId, @Param("count") Integer count);

    /**
     * Unlock Stock for Ware
     * @param skuId sku Id
     * @param wareId ware id
     * @param num sku num
     */
    void unlockStock(@Param("skuId") Long skuId, @Param("wareId") Long wareId,
                     @Param("num") Integer num);
}
