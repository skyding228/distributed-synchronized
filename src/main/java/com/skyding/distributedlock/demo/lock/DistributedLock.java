package com.skyding.distributedlock.demo.lock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * @author weichunhe
 * created at 18-12-27
 */
@Component
public class DistributedLock {

    private static final String LOCK_PREFIX = "DistributedLock:";

    /**
     * default timeout is 5 minutes
     */
    private long DEFAULT_TIMEOUT_SECONDS = 5 * 60L;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * lock a key with a value
     *
     * @param key
     * @param value
     * @param timeoutSeconds
     * @return
     */
    public boolean lock(String key, String value, Long timeoutSeconds) {
        RedisConnection connection = getConnection();
        byte[] bytes = getFullKey(key);
        boolean locked = connection.setNX(bytes, value.getBytes());
        if (timeoutSeconds == null || timeoutSeconds < 0) {
            timeoutSeconds = DEFAULT_TIMEOUT_SECONDS;
        }
        long ttl = connection.ttl(bytes);

        //must reset ttl ,because if crashed here last time,It will be locked forever.
        if (ttl < 0 || ttl > timeoutSeconds) {
            connection.expire(bytes, timeoutSeconds);
        }
        return locked;
    }

    /**
     * only if the locked value equals to the param value ,you can unlock it .
     * It means that only the person who locked it can unlock it, until time out.
     *
     * @param key
     * @param value
     */
    public boolean unlock(String key, String value) {
        byte[] keyByte = getFullKey(key);
        if (!value.equals(new String(getConnection().get(keyByte)))) {
            return false;
        }
        return getConnection().del(keyByte) == 1;
    }

    private byte[] getFullKey(String key) {
        return (LOCK_PREFIX + key).getBytes();
    }

    private RedisConnection getConnection() {
        return redisTemplate.getConnectionFactory().getConnection();
    }
}
