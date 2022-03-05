package xyz.klenkiven.kmall.seckill.scheduled;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import xyz.klenkiven.kmall.seckill.service.SeckillService;

import java.util.Date;
import java.util.concurrent.TimeUnit;

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
    private static final String UPLOAD_LOCK = "seckill:upload:lock";

    private final SeckillService seckillService;

    private final RedissonClient redissonClient;

    /**
     * Upload Seckill SKU Latest 3 days
     */
    @Scheduled(cron = "0 * * * * ?")
    public void uploadSeckillSkuLatest3Day() {
        log.info("Upload Seckill SKU at {}", new Date());
        // Distributed Lock
        // It makes first upload process is safe in concurrency circumstance
        // When first upload has been done, this lock is unnecessary.
        // In order to, guarantee the first upload safety, the lock is worthy.
        RLock lock = redissonClient.getLock(UPLOAD_LOCK);
        lock.lock(10, TimeUnit.SECONDS);
        try {
            // Redundant Upload is ignored
            seckillService.uploadSeckillSkuLatest3Day();
        } finally {
            lock.unlock();
        }
    }

    public SeckillSkuScheduled(SeckillService seckillService, RedissonClient redissonClient) {
        this.seckillService = seckillService;
        this.redissonClient = redissonClient;
    }

}
