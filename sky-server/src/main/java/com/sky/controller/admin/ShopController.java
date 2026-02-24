package com.sky.controller.admin;


import com.sky.annotation.AutoFill;
import com.sky.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController("adminShopController")
@Slf4j
@RequestMapping("/admin/shop")
public class ShopController {
    private final RedisTemplate<Object, Object> redisTemplate;
    @Autowired
    public ShopController(RedisTemplate<Object, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 设置店铺状态
     * */
    @PutMapping("/{status}")
    public Result setShopStatus(@PathVariable Integer status){
        log.info("设置店铺状态:{}",status);
        redisTemplate.opsForValue().set("SHOP_STATUS",status);
        return Result.success();
    }

    /**
     * 获取店铺状态
     * */
    @GetMapping("/status")
    public Result getShopStatus(){
        Object shopStatus = redisTemplate.opsForValue().get("SHOP_STATUS");
        log.info("获取店铺状态:{}",shopStatus);
        return Result.success(shopStatus);
    }
}
