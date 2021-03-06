package xyz.klenkiven.kmall.ware.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.transaction.annotation.Transactional;
import xyz.klenkiven.kmall.common.to.mq.StockDetailTO;
import xyz.klenkiven.kmall.common.to.mq.StockLockedTO;
import xyz.klenkiven.kmall.common.utils.PageUtils;
import xyz.klenkiven.kmall.common.utils.Query;

import xyz.klenkiven.kmall.common.utils.R;
import xyz.klenkiven.kmall.common.utils.Result;
import xyz.klenkiven.kmall.ware.dao.WareSkuDao;
import xyz.klenkiven.kmall.ware.entity.WareOrderTaskDetailEntity;
import xyz.klenkiven.kmall.ware.entity.WareOrderTaskEntity;
import xyz.klenkiven.kmall.ware.entity.WareSkuEntity;
import xyz.klenkiven.kmall.common.exception.NoStockException;
import xyz.klenkiven.kmall.ware.feign.MemberFeignService;
import xyz.klenkiven.kmall.ware.feign.OrderFeignService;
import xyz.klenkiven.kmall.ware.feign.SkuFeignService;
import xyz.klenkiven.kmall.ware.service.WareOrderTaskDetailService;
import xyz.klenkiven.kmall.ware.service.WareOrderTaskService;
import xyz.klenkiven.kmall.ware.service.WareSkuService;
import xyz.klenkiven.kmall.common.to.SkuHasStockTO;
import xyz.klenkiven.kmall.ware.vo.FareResp;
import xyz.klenkiven.kmall.ware.vo.MemberAddressDTO;
import xyz.klenkiven.kmall.common.to.mq.OrderTO;
import xyz.klenkiven.kmall.ware.vo.WareSkuLockDTO;


@Service("wareSkuService")
@RequiredArgsConstructor
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    private static final Logger log = LoggerFactory.getLogger(WareSkuService.class);

    private final SkuFeignService skuFeignService;
    private final MemberFeignService memberFeignService;
    private final OrderFeignService orderFeignService;

    private final WareOrderTaskService taskService;
    private final WareOrderTaskDetailService taskDetailService;

    private final RabbitTemplate rabbitTemplate;

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
                .getData("memberReceiveAddress", new TypeReference<>() {
                });
        fareResp.setFare(new BigDecimal(addrId % 12));
        fareResp.setAddress(data);
        return fareResp;
    }

    @Override
    @Transactional
    public void releaseStockLock(StockLockedTO to) {
        Long taskId = to.getTaskId();
        StockDetailTO detail = to.getTaskDetail();
        Long skuId = detail.getSkuId();
        Long detailId = detail.getId();
        // Unlock Logic
        // 1. Query Stock Detail in DB
        //      Detail exist in DB:
        //          Order is Not-Exist: Unlock Stock
        //          Order is Exist:
        //              Order State[Canceled]: Unlock Stock
        //              Order State[Non-Canceled]: Need not to Rollback
        //      Detail non-exist in DB: Need not to Rollback
        WareOrderTaskDetailEntity detailEntity = taskDetailService.getById(detailId);
        if (detailEntity != null && detailEntity.getLockStatus() == 1) {
            WareOrderTaskEntity taskEntity = taskService.getById(taskId);
            String orderSn = taskEntity.getOrderSn();
            Result<OrderTO> orderResult = orderFeignService.getOrderStatus(orderSn);
            // Reject and requeue if Status normal
            if (orderResult == null || orderResult.getCode() != 0) {
                // Some Errors Occur
                throw new RuntimeException("Some Errors Occur!");
            }
            // order is not exist and order status = 4 [Canceled]
            OrderTO order = orderResult.getData();
            if (order == null || order.getStatus() == 4) {
                // Unlock Stock
                // TODO need CAS
                if (detailEntity.getLockStatus() == 1) {
                    unlockStock(skuId, detailEntity.getWareId(),
                            detailEntity.getId(), detailEntity.getSkuNum());
                }
            }
        }
    }

    @Override
    public void unlockStock(OrderTO order) {
        // Get newest task status
        OrderTO currentOrder =
                orderFeignService.getOrderStatus(order.getOrderSn()).getData();
        WareOrderTaskEntity taskEntity = taskService.getTaskByOrderSn(currentOrder.getOrderSn());

        // When Task Detail not execute, then do release
        List<WareOrderTaskDetailEntity> detailList =
                taskDetailService.getNotExecutedTaskDetailByTaskId(taskEntity.getId());
        for (WareOrderTaskDetailEntity taskDetailEntity : detailList) {
            unlockStock(
                    taskDetailEntity.getSkuId(),
                    taskDetailEntity.getWareId(),
                    taskDetailEntity.getId(),
                    taskDetailEntity.getSkuNum()
            );
        }
    }

    @Override
    public void orderLockStock(WareSkuLockDTO lock) {
        // Save Order Task
        WareOrderTaskEntity task = new WareOrderTaskEntity();
        task.setOrderSn(lock.getOrderSn());
        taskService.save(task);

        // Query all available ware
        List<SkuWareHasStock> wareHasStocks = lock.getLocks().stream().map(item -> {
            SkuWareHasStock stock = new SkuWareHasStock();
            stock.setSkuId(item.getSkuId());
            stock.setCount(item.getCount());
            List<Long> wareId = baseMapper.listWareIdHasStock(item.getSkuId(), item.getCount());
            stock.setWareId(wareId);
            return stock;
        }).collect(Collectors.toList());

        // Lock Stock
        for (SkuWareHasStock wareHasStock : wareHasStocks) {
            Long skuId = wareHasStock.getSkuId();
            List<Long> wareIds = wareHasStock.getWareId();
            if (wareIds == null || wareIds.size() == 0) {
                throw new NoStockException(skuId);
            }
            // Do Lock
            boolean locked = true;
            for (Long wareId : wareIds) {
                // Success return 1, Fail return 0
                Long effectRow = baseMapper.lockSkuStock(skuId, wareId, wareHasStock.getCount());
                if (effectRow == 1) {
                    // Success to Lock
                    WareOrderTaskDetailEntity taskDetail = new WareOrderTaskDetailEntity();
                    taskDetail.setSkuId(skuId);
                    taskDetail.setSkuName("");
                    taskDetail.setTaskId(task.getId());
                    taskDetail.setWareId(wareId);
                    taskDetail.setLockStatus(1); // Locked Success State
                    taskDetail.setSkuNum(wareHasStock.getCount());
                    taskDetailService.save(taskDetail);

                    // RabbitMQ Message
                    StockLockedTO stockLockedTO = new StockLockedTO();
                    stockLockedTO.setTaskId(task.getId());
                    StockDetailTO detailTO = new StockDetailTO();
                    BeanUtils.copyProperties(taskDetail, detailTO);
                    stockLockedTO.setTaskDetail(detailTO);
                    rabbitTemplate.convertAndSend(
                            "stock-event-exchange",
                            "stock.locked",
                            stockLockedTO
                    );
                    break;
                }
                locked = false;
            }

            // Fail to Lock
            if (!locked) {
                throw new NoStockException(skuId);
            }
        }
    }

    /**
     * Unlock Stock for Ware
     *
     * @param skuId        sku Id
     * @param wareId       ware id
     * @param taskDetailId task detail
     * @param num          sku num
     */
    private void unlockStock(Long skuId, Long wareId, Long taskDetailId, Integer num) {
        // Unlock for SKU
        this.baseMapper.unlockStock(skuId, wareId, num);
        // Change Status for Task
        WareOrderTaskDetailEntity taskDetail = taskDetailService.getById(taskDetailId);
        taskDetail.setLockStatus(2); // Set to Unlock Status
        taskDetailService.updateById(taskDetail);
    }

    @Data
    static class SkuWareHasStock {
        private Long skuId;
        private Integer count;
        private List<Long> wareId;
    }

}