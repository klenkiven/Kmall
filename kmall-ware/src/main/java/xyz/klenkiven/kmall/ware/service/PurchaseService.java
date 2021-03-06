package xyz.klenkiven.kmall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import xyz.klenkiven.kmall.common.utils.PageUtils;
import xyz.klenkiven.kmall.ware.entity.PurchaseEntity;
import xyz.klenkiven.kmall.ware.vo.MergeVO;
import xyz.klenkiven.kmall.ware.vo.PurchaseDoneVO;

import java.util.List;
import java.util.Map;

/**
 * 采购信息
 *
 * @author klenkiven
 * @email wzl709@outlook.com
 * @date 2021-10-06 19:38:05
 */
public interface PurchaseService extends IService<PurchaseEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * Query Unreceived Purchase
     */
    PageUtils queryUnreceivedPage(Map<String, Object> params);

    /**
     * Merge Purchase Method
     */
    void mergePurchase(MergeVO mergeVO);

    /**
     * Receive Purchase by Stuff
     */
    void receivePurchase(List<Long> purchaseList);

    /**
     * Purchase Done
     */
    void done(PurchaseDoneVO purchaseDoneVO);
}

