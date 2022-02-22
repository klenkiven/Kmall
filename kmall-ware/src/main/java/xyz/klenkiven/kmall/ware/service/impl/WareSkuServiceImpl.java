package xyz.klenkiven.kmall.ware.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import xyz.klenkiven.kmall.common.utils.PageUtils;
import xyz.klenkiven.kmall.common.utils.Query;

import xyz.klenkiven.kmall.common.utils.R;
import xyz.klenkiven.kmall.ware.dao.WareSkuDao;
import xyz.klenkiven.kmall.ware.entity.WareSkuEntity;
import xyz.klenkiven.kmall.ware.feign.MemberFeignService;
import xyz.klenkiven.kmall.ware.feign.SkuFeignService;
import xyz.klenkiven.kmall.ware.service.WareSkuService;
import xyz.klenkiven.kmall.common.to.SkuHasStockTO;
import xyz.klenkiven.kmall.ware.vo.FareResp;
import xyz.klenkiven.kmall.ware.vo.MemberAddressDTO;


@Service("wareSkuService")
@RequiredArgsConstructor
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    private final SkuFeignService skuFeignService;
    private final MemberFeignService memberFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {

        QueryWrapper<WareSkuEntity> wareSkuEntityQueryWrapper = new QueryWrapper<>();

        String wareId = (String) params.get("wareId");
        wareSkuEntityQueryWrapper.eq(!StringUtils.isEmpty(wareId), "ware_id", wareId);
        String skuId = (String) params.get("skuId");
        wareSkuEntityQueryWrapper.eq(!StringUtils.isEmpty(skuId), "sku_id", skuId);

        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                wareSkuEntityQueryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void addStock(Long wareId, Long skuId, Integer skuNum) {
        List<WareSkuEntity> entities = baseMapper.selectList(
                new QueryWrapper<WareSkuEntity>()
                        .eq("ware_id", wareId)
                        .or().eq("sku_id", skuId)
        );
        if (entities == null || entities.size() == 0) {
            WareSkuEntity wareSkuEntity = new WareSkuEntity();
            wareSkuEntity.setSkuId(skuId);
            wareSkuEntity.setWareId(wareId);
            wareSkuEntity.setStock(skuNum);
            wareSkuEntity.setStockLocked(0);
            wareSkuEntity.setSkuName("");
            R r = skuFeignService.infoSku(skuId);
            if (r.getCode() == 0) {
                Map<String, Object> skuInfo = (Map<String, Object>) r.get("skuInfo");
                wareSkuEntity.setSkuName((String) skuInfo.get("skuName"));
            }
            baseMapper.insert(wareSkuEntity);
        } else {
            baseMapper.addStock(wareId, skuId, skuNum);
        }
    }

    @Override
    public List<SkuHasStockTO> getSkuHasStock(List<Long> skuIds) {
        if (skuIds == null || skuIds.size() == 0) return new ArrayList<>();

        return skuIds.stream()
                .map(skuId -> {
                    SkuHasStockTO hasStockVO = new SkuHasStockTO();
                    hasStockVO.setSkuId(skuId);
                    Long stock = baseMapper.getStockBySkuId(skuId);
                    hasStockVO.setHasStock(stock != null && stock > 0);
                    return hasStockVO;
                }).collect(Collectors.toList());
    }

    @Override
    public FareResp getFare(Long addrId) {
        FareResp fareResp = new FareResp();
        MemberAddressDTO data = memberFeignService.getAddress(addrId)
                        .getData("memberReceiveAddress", new TypeReference<>() {});
        fareResp.setFare(new BigDecimal(addrId % 12));
        fareResp.setAddress(data);
        return fareResp;
    }

}