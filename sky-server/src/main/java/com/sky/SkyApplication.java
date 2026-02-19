package com.sky;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement //开启注解方式的事务管理
@Slf4j
public class SkyApplication {
    private static final Logger logger = LoggerFactory.getLogger(SkyApplication.class);
    public static void main(String[] args) {
        SpringApplication.run(SkyApplication.class, args);
        logger.info("server started");
    }
}
