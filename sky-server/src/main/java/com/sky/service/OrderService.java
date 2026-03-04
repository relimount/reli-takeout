package com.sky.service;

import com.github.pagehelper.Page;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.result.PageResult;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;

public interface OrderService {
    OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO);

    PageResult pageHistoryOrders(Integer page, Integer pageSize);

    OrderVO getOrderDetail(Long id);
}
