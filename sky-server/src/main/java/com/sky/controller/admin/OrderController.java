package com.sky.controller.admin;

import com.sky.dto.OrdersPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.service.impl.OrderServiceImpl;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin/order")
@Slf4j
public class OrderController {
    @Autowired
    private OrderService orderService;

    /*#
    * 订单搜索
    * @Param ordersPageQueryDTO 搜索条件（订单号、手机号、用户名、订单状态、下单时间）
    * */

    @GetMapping("/conditionSearch")
    public Result<PageResult> conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageResult pageResult = orderService.conditionSearch(ordersPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 各个状态的订单数量统计
     */
    @GetMapping("/statistics")
    public Result<OrderStatisticsVO> statistics(){
        OrderStatisticsVO orderStatisticsVO = orderService.statistics();
        return Result.success(orderStatisticsVO);
    }

    /**
     * 查询订单详情
     * */
    @GetMapping("/details/{id}")
    public Result<OrderVO> getOrderDetail(@PathVariable Long id){
        OrderVO orderVO = orderService.getOrderDetail(id);
        return Result.success(orderVO);
    }

    /**
     * 接单
     * @Param id 订单 id
     * */
    @PutMapping("/confirm")
    public Result confirm(@RequestBody Map<String, Long> params){
        log.info("接单，订单 id: {}", params.get("id"));
        orderService.confirm(params.get("id"));
        return Result.success();
    }

    /**
     * 拒单
     * */
    @PutMapping("/rejection")
    public Result rejection(@RequestBody Map<String, Object> params){
        Long id = ((Number) params.get("id")).longValue();
        String rejectionReason = (String) params.get("rejectionReason");
        log.info("拒单，订单 id: {}, 原因：{}", id, rejectionReason);
        orderService.rejection(id, rejectionReason);
      return Result.success();
    }

    /**
     * 取消订单
     * */
    @PutMapping("/cancel")
    public Result cancelOrder(@RequestBody Map<String, Object> params) {
        Long id = ((Number) params.get("id")).longValue();
        String cancelReason = (String) params.get("cancelReason");
        log.info("取消订单，订单 id: {}, 原因：{}", id, cancelReason);
        orderService.adminCancelOrder(id, cancelReason);
        return Result.success();
    }
    /**
     * 派送订单
     * */
    @PutMapping("/delivery/{id}")
    public Result delivery(@PathVariable Long id) {
        log.info("派送订单，订单 id: {}", id);
        orderService.delivery(id);
        return Result.success();
    }

    /**
     * 完成订单
     * @param id 订单id
     * */
    @PutMapping("/complete/{id}")
    public Result complete(@PathVariable Long id) {
        log.info("完成订单，订单 id: {}", id);
        orderService.complete(id);
        return Result.success();
    }
}
