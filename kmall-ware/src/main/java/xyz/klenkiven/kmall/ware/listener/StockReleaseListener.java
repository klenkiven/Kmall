package xyz.klenkiven.kmall.ware.listener;

import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import xyz.klenkiven.kmall.common.to.mq.StockLockedTO;
import xyz.klenkiven.kmall.ware.service.WareSkuService;
import xyz.klenkiven.kmall.common.to.mq.OrderTO;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Listener for Stock Release
 * @author klenkiven
 */
@Service
@RabbitListener(queues = "stock.release.stock.queue")
public class StockReleaseListener {

    private static final Logger log = LoggerFactory.getLogger(StockReleaseListener.class);
    private final WareSkuService wareSkuService;

    /**
     * Listen to RabbitMQ and Release Stock
     */
    @RabbitHandler
    public void handelStockLockRelease(StockLockedTO to, Message message, Channel channel) throws IOException {
        log.info("Task: {} is Fail, Rollback SKU: {}, SKU Count: {}, Message: {}",
                to.getTaskId(),
                to.getTaskDetail().getSkuId(),
                to.getTaskDetail().getSkuNum(),
                new String(message.getBody())
        );
        try {
            wareSkuService.releaseStockLock(to);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }
    }

    /**
     * Listen to RabbitMQ OrderEntity
     */
    @RabbitHandler
    public void handleOrderClose(OrderTO order, Message message, Channel channel) throws IOException, TimeoutException {
        log.info("OrderSn: {} is timeout, Rollback Message: {}",
                order.getOrderSn(),
                new String(message.getBody())
        );

        try {
            wareSkuService.unlockStock(order);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        } finally {
            channel.close();
        }
    }

    public StockReleaseListener(WareSkuService wareSkuService) {
        this.wareSkuService = wareSkuService;
    }

}
