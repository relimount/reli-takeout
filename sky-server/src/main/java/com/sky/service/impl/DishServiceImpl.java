package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.mapper.DishFlavorsMapper;
import com.sky.mapper.DishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 菜品业务逻辑实现类
 * 处理菜品相关的业务操作
 */
@Service
@Slf4j
public class DishServiceImpl implements DishService {
    
    @Autowired
    private DishMapper dishMapper;
    
    @Autowired
    private DishFlavorsMapper dishFlavorsMapper;

    /**
     * 保存菜品及其口味信息
     * 采用事务管理确保数据一致性
     * @param dishDTO 菜品数据传输对象
     */
    @Override
    @Transactional
    public void saveDish(DishDTO dishDTO) {
        //转换DTO到实体对象
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        
        //保存菜品基本信息
        dishMapper.saveDish(dish);
        
        //获取生成的菜品ID
        Long dishId = dish.getId();
        log.info("菜品id:{}", dishId);
        
        //处理口味信息
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if(flavors == null || flavors.isEmpty()){
            return;
        }
        
        //为每个口味设置关联的菜品ID
        flavors.forEach(flavor -> flavor.setDishId(dishId));
        dishFlavorsMapper.insertBatch(flavors);
    }

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO 分页查询条件
     * @return 分页结果封装
     */
    @Override
    public PageResult page(DishPageQueryDTO dishPageQueryDTO) {
        //设置分页参数
        PageHelper.startPage(dishPageQueryDTO.getPage(),dishPageQueryDTO.getPageSize());
        
        //执行分页查询
        Page<Dish> page = dishMapper.page(dishPageQueryDTO);
        
        //封装分页结果
        return new PageResult(page.getTotal(), page.getResult());
    }
}
