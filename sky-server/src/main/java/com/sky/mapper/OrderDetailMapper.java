package com.sky.mapper;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.OrderDetail;
import org.apache.ibatis.annotations.Mapper;


import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OrderDetailMapper {
    void insertBatch(List<OrderDetail> orderDetails);

    List<OrderDetail> getByOrderId(Long orderId);
    
    /**
     * 根据时间范围统计菜品销量排名
     * @param beginTime 开始时间
     * @param endTime 结束时间
     * @return 菜品销量列表
     */
    List<GoodsSalesDTO> getSalesTop10(LocalDateTime beginTime, LocalDateTime endTime);
}
