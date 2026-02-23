package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜品管理控制器
 * 处理菜品相关的HTTP请求
 */
@RestController
@Slf4j
@RequestMapping("/admin/dish")
public class DishController {
    
    @Autowired
    private DishService dishService;
    
    /**
     * 新增菜品
     * @param dishDTO 菜品数据传输对象
     * @return 操作结果
     */
    @PostMapping
    public Result saveDish(@RequestBody DishDTO dishDTO){
        log.info("新增菜品:{}",dishDTO);
        dishService.saveDish(dishDTO);
        return Result.success();
    }

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO 分页查询条件
     * @return 分页结果
     */
    @GetMapping("/page")
    public Result<PageResult> pageDish(DishPageQueryDTO dishPageQueryDTO){
        PageResult pageResult = dishService.page(dishPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 批量删除菜品
     * @param ids 要删除的菜品ID列表
     * @return 操作结果
     */
    @DeleteMapping
    public Result DeleteDish(@RequestParam List<Long> ids){
        log.info("删除菜品:{}",ids);
        dishService.deleteBatch(ids);
        return Result.success();
    }
}
