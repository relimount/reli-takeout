package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class OrderTask {
    @Autowired
    OrderMapper orderMapper;

    /**
     * 定时处理超时订单
     * */
    @Scheduled(cron = "0 * * * * ?")//每分钟一次
    public void processTimeoutOrders() {
        log.info("Processing timeout orders...");

        List<Orders>timeoutOrders = orderMapper.processTimeoutOrders(Orders.DELIVERY_IN_PROGRESS, LocalDateTime.now().plusMinutes(-15));

        for (Orders order : timeoutOrders) {
            log.info("Processing timeout order: {}", order);
            order.setStatus(Orders.CONFIRMED);
            order.setCancelTime(LocalDateTime.now());
            order.setCancelReason("订单超时未处理");
            orderMapper.update(order);
        }
    }

    /**
     * 处理前一日派送中订单
     * */
    @Scheduled(cron = "0 0 0 * * ?")//每天凌晨一次
    public void processDeliveryOrders() {
        log.info("Processing delivery orders...");

        List<Orders>deliveryOrders = orderMapper.processTimeoutOrders(Orders.DELIVERY_IN_PROGRESS, LocalDateTime.now().minusDays(1));

        for (Orders order : deliveryOrders) {
            log.info("Processing delivery order: {}", order);
            order.setStatus(Orders.COMPLETED);
            order.setDeliveryTime(LocalDateTime.now());
            orderMapper.update(order);
        }
    }
}
