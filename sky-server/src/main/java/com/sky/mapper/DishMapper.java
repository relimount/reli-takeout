package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 菜品数据访问层接口
 * 定义菜品相关的数据库操作方法
 */
@Mapper
public interface DishMapper {

    /**
     * 根据分类id查询菜品数量
     * @param categoryId 分类ID
     * @return 菜品数量
     */
    @Select("select count(id) from dish where category_id = #{categoryId}")
    Integer countByCategoryId(Long categoryId);
    
    /**
     * 保存菜品信息
     * @param dish 菜品实体对象
     */
    @AutoFill(OperationType.INSERT)
    void saveDish(Dish dish);

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO 分页查询条件
     * @return 分页结果
     */
    Page<DishVO> page(DishPageQueryDTO dishPageQueryDTO);

    /**
     * 批量删除菜品
     * @param ids 要删除的菜品ID列表
     */
    void deleteBatch(List<Long> ids);

    /**
     * 根据ID查询菜品基本信息
     * @param id 菜品ID
     * @return 菜品实体对象
     */
    @Select("SELECT * FROM dish WHERE id = #{id}")
    Dish getDishById(Long id);

    /**
     * 更新菜品基本信息
     * @param dish 菜品实体对象
     */
    @AutoFill(OperationType.UPDATE)
    void UpdateDish(Dish dish);
}
