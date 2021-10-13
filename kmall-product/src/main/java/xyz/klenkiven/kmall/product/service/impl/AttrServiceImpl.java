package xyz.klenkiven.kmall.product.service.impl;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.transaction.annotation.Transactional;
import xyz.klenkiven.kmall.common.utils.PageUtils;
import xyz.klenkiven.kmall.common.utils.Query;

import xyz.klenkiven.kmall.product.dao.AttrAttrgroupRelationDao;
import xyz.klenkiven.kmall.product.dao.AttrDao;
import xyz.klenkiven.kmall.product.dao.AttrGroupDao;
import xyz.klenkiven.kmall.product.dao.CategoryDao;
import xyz.klenkiven.kmall.product.entity.AttrAttrgroupRelationEntity;
import xyz.klenkiven.kmall.product.entity.AttrEntity;
import xyz.klenkiven.kmall.product.entity.AttrGroupEntity;
import xyz.klenkiven.kmall.product.entity.CategoryEntity;
import xyz.klenkiven.kmall.product.service.AttrService;
import xyz.klenkiven.kmall.product.vo.AttrRespVO;
import xyz.klenkiven.kmall.product.vo.AttrVO;


@Service("attrService")
@RequiredArgsConstructor
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {

    private final AttrAttrgroupRelationDao attrAttrgroupRelationDao;
    private final AttrGroupDao attrGroupDao;
    private final CategoryDao categoryDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                new QueryWrapper<AttrEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveVO(AttrVO attr) {
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attr, attrEntity);
        // After saving data, mbp will complete entity automatically.
        this.save(attrEntity);

        // Save Relation
        AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
        relationEntity.setAttrId(attrEntity.getAttrId());
        relationEntity.setAttrGroupId(attr.getAttrGroupId());
        attrAttrgroupRelationDao.insert(relationEntity);
    }

    @Override
    @Transactional(rollbackFor = {Exception.class})
    public PageUtils queryBasePage(Long catalogId, Map<String, Object> params) {
        IPage<AttrEntity> page = new Query<AttrEntity>().getPage(params);
        QueryWrapper<AttrEntity> queryWrapper = new QueryWrapper<>();
        if (catalogId != 0) {
            queryWrapper.eq("catelog_id", catalogId);
        }

        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            queryWrapper.and((query) -> {
                query.eq("attr_id", key);
                query.or();
                query.like("attr_name", "%" + key + "%");
            });
        }
        IPage<AttrEntity> iPage = this.page(page, queryWrapper);

        PageUtils result = new PageUtils(iPage);
        List<AttrEntity> records = iPage.getRecords();
        List<AttrRespVO> collect = records.stream()
                .map((record) -> {
                    AttrRespVO vo = new AttrRespVO();
                    BeanUtils.copyProperties(record, vo);

                    // SET Attribute Group Name
                    AttrAttrgroupRelationEntity relationEntity = attrAttrgroupRelationDao.selectOne(
                            new QueryWrapper<AttrAttrgroupRelationEntity>()
                                    .eq("attr_id", record.getAttrId())
                    );
                    AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(relationEntity.getAttrGroupId());
                    if (attrGroupEntity != null) {
                        vo.setGroupName(attrGroupEntity.getAttrGroupName());
                    }

                    // SET Catalog Name
                    CategoryEntity categoryEntity = categoryDao.selectById(record.getCatelogId());
                    if (categoryEntity != null) {
                        vo.setCatelogName(categoryEntity.getName());
                    }

                    return vo;
                })
                .collect(Collectors.toList());
        result.setList(collect);
        return result;
    }

}