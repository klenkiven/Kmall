package xyz.klenkiven.kmall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import xyz.klenkiven.kmall.common.utils.PageUtils;
import xyz.klenkiven.kmall.product.entity.CategoryBrandRelationEntity;
import xyz.klenkiven.kmall.product.vo.BrandRespVO;

import java.util.List;
import java.util.Map;

/**
 * 品牌分类关联
 *
 * @author klenkiven
 * @email wzl709@outlook.com
 * @date 2021-08-29 16:07:27
 */
public interface CategoryBrandRelationService extends IService<CategoryBrandRelationEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * Save Redundant Data brand name and category name
     * @param categoryBrandRelation entity with brand and category id
     */
    void saveDetail(CategoryBrandRelationEntity categoryBrandRelation);

    /**
     * When Category Name was changed, execute this method
     * @param catId catalog id
     * @param name new category name
     */
    void updateCategory(Long catId, String name);

    /**
     * When Brand Name was changed, execute this method
     * @param brandId brand id
     * @param name new brand name
     */
    void updateBrand(Long brandId, String name);

    /**
     * Get all brand that related with category
     */
    List<BrandRespVO> listAllBrands(Long catId);
}

