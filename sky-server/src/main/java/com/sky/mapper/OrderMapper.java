package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.vo.OrderVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OrderMapper {

    void insert(Orders orders);

    @Select("select * from orders where user_id = #{currentId} order by order_time desc")
    Page<Orders> pageHistoryOrders(Long currentId);

    @Select("select * from orders where id = #{id}")
    OrderVO getOrder(Long id);
}
