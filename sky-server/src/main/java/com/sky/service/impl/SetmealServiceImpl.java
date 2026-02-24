package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class SetmealServiceImpl implements SetmealService {
    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Autowired
    private SetmealService setmealService;

    @Override
    public void saveSetmeal(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);
        setmealMapper.saveSetmeal(setmeal);
        List<SetmealDish> dishList = setmealDTO.getSetmealDishes();
        Long id = setmeal.getId();
        dishList.forEach(e->e.setSetmealId(id));
        setmealDishMapper.saveSetmealDish(dishList);
    }

    @Override
    public PageResult page(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageHelper.startPage(setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize());
        Page<Setmeal> page = setmealMapper.page(setmealPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public void deleteBatch(Long[] ids) {
        setmealMapper.deleteBatch(ids);
    }

    @Override
    public SetmealVO getById(Long id) {
        SetmealVO setmealVO= setmealMapper.getById(id);
        List<SetmealDish> dishList = setmealDishMapper.getBySetmealId(id);
        setmealVO.setSetmealDishes(dishList);
        return setmealVO;
    }

    @Transactional
    @Override
    public void update(SetmealDTO setmealDTO) {
        // 更新套餐基本信息
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmealMapper.update(setmeal);
        
        // 更新套餐菜品关系
        List<SetmealDish> dishList = setmealDTO.getSetmealDishes();
        Long id = setmealDTO.getId();
        
        // 先删除原有的套餐菜品关系
        setmealDishMapper.deleteBySetmealId(id);
        
        // 如果有新的菜品关系，则插入
        if(dishList != null && !dishList.isEmpty()){
            dishList.forEach(e -> e.setSetmealId(id));
            setmealDishMapper.saveSetmealDish(dishList);
        }
    }

    @Override
    public void changeStatus(Integer status, Long id) {
        Setmeal setmeal = new Setmeal();
        setmeal.setId(id);
        setmeal.setStatus(status);
        setmealMapper.update(setmeal);
    }

}
