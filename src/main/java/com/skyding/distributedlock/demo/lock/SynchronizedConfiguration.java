package com.skyding.distributedlock.demo.lock;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.UUID;

/**
 * @author weichunhe
 * created at 2018/11/30
 */
@Aspect
@Component
public class SynchronizedConfiguration {

    private static Logger LOG = LoggerFactory.getLogger(SynchronizedConfiguration.class);

    private String LOCK_PREFIX = "synchronized:";

    @Autowired
    private DistributedLock lock;

    @Pointcut("@annotation(com.skyding.distributedlock.demo.lock.Synchronized)")
    public void pointcut() {
    }

    private String getKey(JoinPoint joinPoint, Synchronized sync) {
        String key = sync.value();
        if (StringUtils.isEmpty(key)) {
            key = getFullMethodName(joinPoint);
        }
        return LOCK_PREFIX + key;
    }

    @Around("pointcut()&&@annotation(sync)")
    public void around(ProceedingJoinPoint joinPoint, Synchronized sync) {
        String key = getKey(joinPoint, sync);
        String value = UUID.randomUUID().toString();
        String methodName = getFullMethodName(joinPoint);
        boolean locked = lock.lock(key, value, sync.timeoutSeconds());
        if (locked) {
            LOG.info("lock {} with {}", key, value);
            try {
                joinPoint.proceed(joinPoint.getArgs());
            } catch (Throwable throwable) {
                LOG.error("An error occurs while executing {} ", methodName, throwable);
            } finally {
                unlock(key, value);
            }
        } else {
            LOG.warn("It will do nothing because lock {} failed.", methodName);
        }
    }

    public void unlock(String key, String value) {
        if (lock.unlock(key, value)) {
            LOG.info("unlock {} with {}", key, value);
        } else {
            LOG.warn("unlock {} with {} failed. Maybe something is wrong.");
        }
    }

    public static String getFullMethodName(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        return joinPoint.getTarget().getClass().getName() + "." + signature.getName();
    }
}
