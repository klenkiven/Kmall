package xyz.klenkiven.kmall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import xyz.klenkiven.kmall.common.utils.PageUtils;
import xyz.klenkiven.kmall.product.entity.BrandEntity;

import java.util.List;
import java.util.Map;

/**
 * 品牌
 *
 * @author klenkiven
 * @email wzl709@outlook.com
 * @date 2021-08-29 16:07:27
 */
public interface BrandService extends IService<BrandEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * When brand name is changed, then update relation cascade
     * @param brand brand
     */
    void updateDetailById(BrandEntity brand);

    /**
     * Get Brand entities by ids
     * For Feign from search service
     * @param brandIds brandIds
     * @return entities
     */
    List<BrandEntity> getBrandByIds(List<Long> brandIds);
}

