package xyz.klenkiven.kmall.coupon.service.impl;

import org.springframework.stereotype.Service;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import xyz.klenkiven.kmall.common.utils.PageUtils;
import xyz.klenkiven.kmall.common.utils.Query;

import xyz.klenkiven.kmall.coupon.dao.SeckillSessionDao;
import xyz.klenkiven.kmall.coupon.entity.SeckillSessionEntity;
import xyz.klenkiven.kmall.coupon.entity.SeckillSkuRelationEntity;
import xyz.klenkiven.kmall.coupon.service.SeckillSessionService;
import xyz.klenkiven.kmall.coupon.service.SeckillSkuRelationService;


@Service("seckillSessionService")
public class SeckillSessionServiceImpl extends ServiceImpl<SeckillSessionDao, SeckillSessionEntity> implements SeckillSessionService {

    private final SeckillSkuRelationService relationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SeckillSessionEntity> page = this.page(
                new Query<SeckillSessionEntity>().getPage(params),
                new QueryWrapper<SeckillSessionEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<SeckillSessionEntity> getLatest3DaysSession() {
        // Get all session
        List<SeckillSessionEntity> seckillSessionList = this.list(
                new QueryWrapper<SeckillSessionEntity>()
                        .between("start_time", startTime(), endTime())
        );

        // Get Session's Seckill SKU
        if (seckillSessionList != null && seckillSessionList.size() > 0) {
            seckillSessionList = seckillSessionList.stream()
                    .peek((session) -> {
                        Long id = session.getId();
                        List<SeckillSkuRelationEntity> skus =
                                relationService.listRelationSkusBySessionId(id);
                        session.setRelationSkus(skus);
                    }).collect(Collectors.toList());
        }
        return seckillSessionList;
    }

    /**
     * Get start time
     */
    private String startTime() {
        LocalDate now = LocalDate.now();
        LocalDateTime startTime = LocalDateTime.of(now, LocalTime.MIN);
        return startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    /**
     * Get end time
     */
    private String endTime() {
        LocalDate now = LocalDate.now();
        LocalDate after3days = now.plus(Period.ofDays(3));
        LocalDateTime startTime = LocalDateTime.of(after3days, LocalTime.MAX);
        return startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }


    public SeckillSessionServiceImpl(SeckillSkuRelationService relationService) {
        this.relationService = relationService;
    }
}