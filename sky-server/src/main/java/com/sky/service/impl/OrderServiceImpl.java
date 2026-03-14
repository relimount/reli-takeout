package com.sky.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import com.sky.websoket.WebSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WeChatPayUtil weChatPayUtil;
    @Autowired
    private WebSocketServer webSocketServer;

    @Override
    @Transactional
    public OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO) {
        //处理异常（地址不存在，购物车为空）
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if (addressBook == null) {
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }

        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(BaseContext.getCurrentId());
        List<ShoppingCart>list = shoppingCartMapper.list(shoppingCart);
        if (list == null || list.isEmpty()) {
            throw new AddressBookBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        //插入订单数据
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO,orders);
        orders.setUserId(BaseContext.getCurrentId());
        orders.setPayStatus(Orders.UN_PAID);
        orders.setOrderTime(LocalDateTime.now());
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setConsignee(addressBook.getConsignee());
        orders.setPhone(addressBook.getPhone());

        orderMapper.insert(orders);

        //插入订单详情数据
        List<OrderDetail> orderDetails = new ArrayList<>();
        for(ShoppingCart cart : list){
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(cart,orderDetail);
            orderDetail.setOrderId(orders.getId());
            orderDetails.add(orderDetail);
        }

        orderDetailMapper.insertBatch(orderDetails);
        //清空购物车
        shoppingCartMapper.delete(shoppingCart);

        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .id(orders.getId())
                .orderTime(orders.getOrderTime())
                .orderNumber(orders.getNumber())
                .orderAmount(orders.getAmount())
                .build();

        return orderSubmitVO;
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);

        /*//调用微信支付接口，生成预支付交易单
        JSONObject jsonObject = weChatPayUtil.pay(
                ordersPaymentDTO.getOrderNumber(), //商户订单号
                new BigDecimal(0.01), //支付金额，单位 元
                "苍穹外卖订单", //商品描述
                user.getOpenid() //微信用户的openid
        );*/
        JSONObject jsonObject = new JSONObject();

        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
            throw new OrderBusinessException("该订单已支付");
        }

        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));

        return vo;
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);

        //通过websoket向客户浏览器推送消息
        Map map = new HashMap<>();
        map.put("type", 1);
        map.put("orderId",ordersDB.getId());
        map.put("content:","订单号：" + outTradeNo);
        String json = JSONObject.toJSONString(map);
        webSocketServer.sendToAllClient(json);
    }
    /**
     * 商家端订单搜索
     * */
    @Override
    public PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        Page<Orders> ordersList = orderMapper.conditionSearch(ordersPageQueryDTO);
        return new PageResult(ordersList.getTotal(), ordersList.getResult());
    }
    /**
     * 各个状态的订单数量统计
     */
    @Override
    public OrderStatisticsVO statistics() {
        Integer confirmed = orderMapper.statistics(Orders.CONFIRMED);
        Integer toBeConfirmed = orderMapper.statistics(Orders.TO_BE_CONFIRMED);
        Integer deliveryInProgress = orderMapper.statistics(Orders.DELIVERY_IN_PROGRESS);
        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        orderStatisticsVO.setConfirmed(confirmed);
        orderStatisticsVO.setToBeConfirmed(toBeConfirmed);
        orderStatisticsVO.setDeliveryInProgress(deliveryInProgress);
        return orderStatisticsVO;
    }
    /**
     * 接单
     * @Param id 订单id
     * */
    @Override
    public void confirm(Long id) {
        Orders orders = new Orders();
        orders.setId(id);
        orders.setStatus(Orders.CONFIRMED);
        orderMapper.update(orders);
    }

    /**
     * 拒单
     * */
    @Override
    public void rejection(Long id, String rejectionReason) {
        log.info("拒单，订单 id: {}, 原因：{}", id, rejectionReason);
        // 1. 查询订单
        Orders orders = orderMapper.getById(id);
    
        // 2. 校验订单是否存在
        if (orders == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
    
        // 3. 构建更新对象
        Orders updateOrders = Orders.builder()
                .id(id)
                .status(Orders.CANCELLED)          // 设置为已取消
                .rejectionReason(rejectionReason)   // 设置拒单原因
                .cancelTime(LocalDateTime.now())    // 设置取消时间
                .build();
    
        // 4. 如果已支付，需要设置退款状态
        if (Objects.equals(orders.getPayStatus(), Orders.PAID)) {
            updateOrders.setPayStatus(Orders.REFUND);
        }
    
        // 5. 执行更新
        orderMapper.update(updateOrders);
    }

    /**
     * 订单取消
     * */
    @Override
    public void adminCancelOrder(Long id, String cancelReason) {
        // 1. 查询订单
        Orders orders = orderMapper.getById(id);
        // 2. 校验订单是否存在
        if(orders == null){
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        // 3. 构建更新对象
        Orders updateOrders = Orders.builder()
                .id(id)
                .status(Orders.CANCELLED)          // 设置为已取消
                .cancelReason(cancelReason)   // 设置取消原因
                .cancelTime(LocalDateTime.now())    // 设置取消时间
                .build();
        // 4. 如果已支付，需要设置退款状态
        if(Objects.equals(orders.getPayStatus(), Orders.PAID)){
            updateOrders.setPayStatus(Orders.REFUND);
        }
        // 5. 执行更新
        orderMapper.update(updateOrders);
    }

    /**
     * 订单派送
     * @param id 订单id
     * */
    @Override
    public void delivery(Long id) {
        // 1. 查询订单
        Orders orders = orderMapper.getById(id);
        // 2. 校验订单是否存在
        if(orders == null){
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        // 3. 校验订单状态是否正确
        if(!Objects.equals(orders.getStatus(), Orders.CONFIRMED)){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        // 4. 构建更新对象
        Orders updateOrders = Orders.builder()
                .id(id)
                .status(Orders.DELIVERY_IN_PROGRESS)          // 设置为派送中
                .build();
        // 5. 执行更新
        orderMapper.update(updateOrders);
    }
    /**
     * 完成订单
     * @param id 订单id
     * */
    @Override
    public void complete(Long id) {
        // 1. 查询订单
        Orders orders = orderMapper.getById(id);
        // 2. 校验订单是否存在
        if(orders == null){
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        // 3. 校验订单状态是否正确
        if(!Objects.equals(orders.getStatus(), Orders.DELIVERY_IN_PROGRESS)){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        // 4. 构建更新对象
        Orders updateOrders = Orders.builder()
                .id(id)
                .status(Orders.COMPLETED)          // 设置为已完成
                .build();
        // 5. 执行更新
        orderMapper.update(updateOrders);
    }

    @Override
    public void reminder(Long id) {
        log.info("催单，订单 id: {}", id);
        // 1. 查询订单
        Orders orders = orderMapper.getById(id);
        log.info("订单号: {}", orders.getNumber());
        // 2. 校验订单是否存在
        if(orders == null){
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        // 3. 构建消息
        Map<String, Object> map = new HashMap<>();
        map.put("type", 2);
        map.put("orderId", id);
        map.put("content", "您的订单号为：" + orders.getNumber() + "，请尽快处理。");
        // 4. 发送消息
        webSocketServer.sendToAllClient(JSONObject.toJSONString(map));
    }

    @Override
    public PageResult pageHistoryOrders(OrdersPageQueryDTO ordersPageQueryDTO) {
        // 设置当前用户 ID
        ordersPageQueryDTO.setUserId(BaseContext.getCurrentId());
        
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        Page<Orders> ordersList = orderMapper.pageHistoryOrders(ordersPageQueryDTO);
        
        // 将 Orders 转换为 OrderVO，并填充订单详情
        List<OrderVO> orderVOList = new ArrayList<>();
        for (Orders orders : ordersList.getResult()) {
            // 如果查询待付款状态，过滤掉已取消的订单
            if (ordersPageQueryDTO.getStatus() != null && 
                Objects.equals(ordersPageQueryDTO.getStatus(), Orders.PENDING_PAYMENT) &&
                Objects.equals(orders.getStatus(), Orders.CANCELLED)) {
                continue;
            }
            
            OrderVO orderVO = new OrderVO();
            BeanUtils.copyProperties(orders, orderVO);
            
            // 查询订单详情
            List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(orders.getId());
            orderVO.setOrderDetailList(orderDetailList);
            
            orderVOList.add(orderVO);
        }
        
        return new PageResult(ordersList.getTotal(), orderVOList);
    }
    /**
     * 查询订单详情
     * @param id 订单id
     * @return orderVO 订单详情
     * */
    @Override
    public OrderVO getOrderDetail(Long id) {
        log.info("查询订单详情，订单id：{}", id);
        OrderVO orderVO = orderMapper.getOrder(id);
        List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(id);
        orderVO.setOrderDetailList(orderDetails);
        return orderVO;
    }

    /**
     * 取消订单
     * @param id 订单 ID
     * */
    @Override
    @Transactional
    public void cancelOrder(Long id) {
        log.info("取消订单，订单id：{}", id);
        // 1. 查询订单
        Orders orders = orderMapper.getById(id);
    
        // 2. 校验订单是否存在
        if (orders == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
    
        // 3. 校验订单状态（只有待付款或待接单状态可以取消）
        if (Objects.equals(orders.getStatus(), Orders.COMPLETED)
                || Objects.equals(orders.getStatus(), Orders.CANCELLED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
    
        // 4. 校验是否为当前用户的订单
        if (!Objects.equals(orders.getUserId(), BaseContext.getCurrentId())) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
    
        // 5. 构建更新对象
        Orders updateOrders = Orders.builder()
                .id(id)
                .status(Orders.CANCELLED)          // 设置为已取消
                .cancelTime(LocalDateTime.now())    // 设置取消时间
                .build();
    
        // 6. 如果已支付，需要设置退款状态
        if (Objects.equals(orders.getPayStatus(), Orders.PAID)) {
            updateOrders.setPayStatus(Orders.REFUND);
        }
    
        // 7. 执行更新
        orderMapper.update(updateOrders);
    }

    /**
     * 再来一单
     * @param id 订单 ID
     * */
    @Override
    public void repetition(Long id) {
        log.info("再来一单，订单 id：{}", id);
        
        // 1. 查询订单详情
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(id);
        
        // 2. 将订单详情添加到购物车
        Long userId = BaseContext.getCurrentId();
        for (OrderDetail orderDetail : orderDetailList) {
            ShoppingCart shoppingCart = new ShoppingCart();
            shoppingCart.setUserId(userId);
            
            // 根据菜品 ID 或套餐 ID 判断是菜品还是套餐
            if (orderDetail.getDishId() != null) {
                // 是菜品
                shoppingCart.setDishId(orderDetail.getDishId());
                shoppingCart.setDishFlavor(orderDetail.getDishFlavor());
            } else if (orderDetail.getSetmealId() != null) {
                // 是套餐
                shoppingCart.setSetmealId(orderDetail.getSetmealId());
            }
            
            // 检查购物车中是否已存在该商品
            List<ShoppingCart> cartList = shoppingCartMapper.list(shoppingCart);
            
            if (cartList != null && !cartList.isEmpty()) {
                // 已存在，数量增加
                ShoppingCart cart = cartList.get(0);
                cart.setNumber(cart.getNumber() + orderDetail.getNumber());
                shoppingCartMapper.updateNumber(cart);
            } else {
                // 不存在，新增
                shoppingCart.setName(orderDetail.getName());
                shoppingCart.setImage(orderDetail.getImage());
                shoppingCart.setAmount(orderDetail.getAmount());
                shoppingCart.setNumber(orderDetail.getNumber());
                shoppingCart.setCreateTime(LocalDateTime.now());
                shoppingCartMapper.insert(shoppingCart);
            }
        }
    }

}
