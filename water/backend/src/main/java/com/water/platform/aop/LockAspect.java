package com.water.platform.aop;

import com.water.platform.annotation.Lock;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 * @author ：devon
 * @date ：2024/11/16 14:15
 * @description：功能描述
 * @version: 1.0
 */
@Aspect
@Component
@Slf4j
public class LockAspect {
    @Pointcut("@annotation(lock)")
    public void lockPointcut(Lock lock) {}

    @Around("lockPointcut(lock)")
    public Object around(ProceedingJoinPoint joinPoint, Lock lock) throws Throwable {
        Class<?> lockClass = lock.lockClass();
        synchronized (lockClass) {
            log.warn("get lock success");
            return joinPoint.proceed();
        }
    }
}
