package xyz.klenkiven.kmall.seckill.scheduled;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import xyz.klenkiven.kmall.seckill.service.SeckillService;

/**
 * Seckill Scheduled Service
 * <p>
 * <ul>
 * <li>upload Product that will be seckill in 3 days</li>
 * <li></li>
 * </ul>
 * </p>
 * @author klenkiven
 */
@Service
public class SeckillSkuScheduled {

    private static final Logger log = LoggerFactory.getLogger(SeckillSkuScheduled.class);

    private final SeckillService seckillService;

    /**
     * Upload Seckill SKU Latest 3 days
     */
    @Scheduled(cron = "0 * * * * ?")
    public void uploadSeckillSkuLatest3Day() {
        // Redundant Upload is ignored
        seckillService.uploadSeckillSkuLatest3Day();
    }

    public SeckillSkuScheduled(SeckillService seckillService) {
        this.seckillService = seckillService;
    }

}
