package xyz.klenkiven.kmall.seckill.scheduled;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * Scheduled Job
 * <p>Scheduled Auto Configuration: {@link org.springframework.boot.autoconfigure.task.TaskSchedulingAutoConfiguration}</p>
 * <p>Async Task Auto Configuration: {@link org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration}</p>
 */
@Component
@EnableScheduling
@EnableAsync
public class HelloSchedule {

    public static final Logger log = LoggerFactory.getLogger(HelloSchedule.class);

    /**
     * Spring not support Year field
     * Week: Mon-Sun 1-7
     * Schedule is blocked, but it should be async
     *      1. CompletedFuture
     *      2. Thread Pool in Spring Schedule
     *          spring.task.scheduling.pool.size
     *      3. Make Scheduled Job Method run as Async
     */
    @Scheduled(cron = "* * * * * ?")
    @Async
    public void hello() {
        log.info("Schedule: {}", new Date());
    }

}
