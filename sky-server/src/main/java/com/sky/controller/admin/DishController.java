package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

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
    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;

    /**
     * 新增菜品
     * @param dishDTO 菜品数据传输对象
     * @return 操作结果
     */
    @PostMapping
    public Result saveDish(@RequestBody DishDTO dishDTO){
        log.info("新增菜品:{}",dishDTO);
        dishService.saveDish(dishDTO);
        String key = "dish_" + dishDTO.getCategoryId();
        cleanCache(key);
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
        cleanCache("dish_*");
        return Result.success();
    }

    /**
     * 根据ID查询菜品详情
     * @param id 菜品ID
     * @return 菜品详细信息（包含口味）
     */
    @GetMapping("{id}")
    public Result<DishVO> getDishById(@PathVariable Long id){
        DishVO dishVO = dishService.getDishById(id);
        return Result.success(dishVO);
    }

    /**
     * 修改菜品信息
     * @param dishDTO 菜品数据传输对象
     * @return 操作结果
     */
    @PutMapping
    public Result updateDish(@RequestBody DishDTO dishDTO){
        log.info("修改菜品:{}",dishDTO);
        dishService.updateDish(dishDTO);
        cleanCache("dish_*");
        return Result.success();
    }

    /**
     * 菜品起售停售
     * */
    @PostMapping("/status/{status}")
    public Result updateDishStatus(@PathVariable Integer status,@RequestParam String id){
        log.info("修改菜品状态:{}",id);
        dishService.updateDishStatus(status,id);
        cleanCache("dish_*");
        return Result.success();
    }

    /**
     * 根据分类ID查询菜品
     * */
    @GetMapping("/list")
    public Result<List<Dish>> listDishByCategoryId(@RequestParam Long categoryId){
        log.info("根据分类id查询菜品:{}",categoryId);
        List<Dish> dishList = dishService.listDishByCategoryId(categoryId);
        return Result.success(dishList);
    }

    //清理缓存
    private void cleanCache(String pattern) {
        Set keys = redisTemplate.keys(pattern);
        redisTemplate.delete(keys);
    }

}
