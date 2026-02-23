package com.sky.mapper;

import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DishFlavorsMapper {
    void insertBatch(List<DishFlavor> flavors);

    /**
     * 根据菜品ID查询口味列表
     * @param dishId 菜品ID
     * @return 口味列表
     */
    @Select("SELECT * FROM dish_flavor WHERE dish_id = #{dishId}")
    List<DishFlavor> getFlavorsById(Long dishId);

    /**
     * 根据菜品ID删除口味信息
     * @param dishId 菜品ID
     */
    @Delete("DELETE from dish_flavor where dish_id = #{dishId}")
    void deleteBatchByDishId(Long dishId);
}
