package com.sky.controller.admin;

import com.github.pagehelper.Page;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/admin/setmeal")
public class SetmealController {
    @Autowired
    private SetmealService setmealService;

    /**
     * 添加套餐
     * */
    @PostMapping
    public Result saveSetmeal(@RequestBody SetmealDTO setmealDTO) {
        setmealService.saveSetmeal(setmealDTO);
        return Result.success();
    }

    /**
     * 套餐分页查询
     * */
    @GetMapping("/page")
    public Result pageSetmeal(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageResult page = setmealService.page(setmealPageQueryDTO);
        return Result.success(page);
    }


}
