package com.sky.controller.user;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import com.sky.result.Result;
import com.sky.service.ShoppingCartService;
import org.apache.ibatis.annotations.Delete;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/shoppingCart")
public class ShoppingCartController {
    @Autowired
    private ShoppingCartService shoppingCartService;

    @PostMapping("/add")
    public Result addShoppingCart(@RequestBody ShoppingCartDTO shoppingCartDTO) {
        shoppingCartService.addShoppingCart(shoppingCartDTO);
        return Result.success();
    }

    @GetMapping("/list")
    public Result<List<ShoppingCart>>list(){
        List<ShoppingCart> list = shoppingCartService.list();
        return Result.success(list);
    }

    @PostMapping("/sub")
    public Result deleteShoppingCart(@RequestBody ShoppingCartDTO shoppingCartDTO){
        shoppingCartService.deleteShoppingCart(shoppingCartDTO);
        return Result.success();
    }
    @DeleteMapping("/clean")
    public Result clean(){
        shoppingCartService.clean();
        return Result.success();
    }
}
