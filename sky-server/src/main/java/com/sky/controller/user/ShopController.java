package com.sky.controller.user;


import com.sky.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController("userShopController")
@Slf4j
@RequestMapping("/user/shop")
public class ShopController {
    private final RedisTemplate<Object, Object> redisTemplate;
    @Autowired
    public ShopController(RedisTemplate<Object, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
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
