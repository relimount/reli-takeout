package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;


import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;

@Aspect
@Component
@Slf4j
public class AutoFillAspect {

    @Pointcut("execution(* com.sky.mapper..*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointcut (){}

    @Before("autoFillPointcut()")
    public void autoFill(JoinPoint joinPoint) {
        log.info("自动填充公共字段...");
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class);
            if (autoFill == null) {
                log.debug("方法上没有@AutoFill注解，跳过自动填充");
                return;
            }
            
            OperationType operationType = autoFill.value();
            log.info("操作类型：{}", operationType);

            Object[] objects = joinPoint.getArgs();
            if(objects == null || objects.length == 0) {
                log.debug("方法参数为空，跳过自动填充");
                return;
            }
            Object obj = objects[0];
            if (obj == null) {
                log.debug("参数对象为空，跳过自动填充");
                return;
            }

            LocalDateTime currentTime = LocalDateTime.now();
            Long currentId = BaseContext.getCurrentId();
            if (currentId == null) {
                log.warn("当前用户ID为空，跳过自动填充");
                return;
            }

            log.info("准备设置字段: updateTime={}, updateUser={}", currentTime, currentId);
            
            //先设定更新时间和更新人，因为无论是新增还是修改都需要更新这两个字段
            obj.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class).invoke(obj, currentTime);
            obj.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class).invoke(obj, currentId);
            log.info("成功设置更新时间字段");

            if(operationType == OperationType.INSERT) {
                //如果是新增操作，还需要设置创建时间和创建人
                obj.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class).invoke(obj, currentTime);
                obj.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class).invoke(obj, currentId);
                log.info("成功设置创建时间字段");
            }
        } catch (Exception e) {
            log.error("自动填充公共字段失败", e);
        }
    }
}
