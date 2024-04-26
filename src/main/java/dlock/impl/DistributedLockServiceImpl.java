package dlock.impl;

import dlock.DistributedLockService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;



@Service
@Slf4j
public class DistributedLockServiceImpl implements DistributedLockService {

    @Autowired
    private RedissonClient redissonClient;

    @Override
    public <R> R runWithDLock(String lockKey, Long waitTime, Long leaseTime, TimeUnit timeUnit, Supplier<R> supplier) throws Exception {
        RLock lock = null;
        Boolean locked = Boolean.FALSE;
        try {
            lock = redissonClient.getLock(lockKey);
            locked = lock.tryLock(waitTime, leaseTime, timeUnit);
            if (locked) {
                return supplier.get();
            } else {
                log.error("Get Distributed Lock [{}] failure", lockKey);
                throw new RuntimeException("system.busy.error");
            }
        } catch (Exception e) {
            log.error("Executed With Distributed Lock error,", e);
            throw e;
        } finally {
            if (locked) {
                lock.unlock();
            }
        }
    }

    @Override
    public <R> R runWithDLockWithOutWaitingTime(String lockKey, Supplier<R> supplier) throws Exception {
        RLock lock = null;
        try {
            lock = redissonClient.getLock(lockKey);
            lock.lock(-1, TimeUnit.SECONDS);
            return supplier.get();
        } catch (Exception e) {
            log.error("Executed With Distributed Lock error,", e);
            throw e;
        } finally {
            if (Objects.nonNull(lock)) {
                lock.unlock();
            }
        }
    }

    @Override
    public <R> R runWithWatchDogDLock(String lockKey, Long waitTime, TimeUnit timeUnit, Supplier<R> supplier) throws Exception {
        RLock lock = null;
        Boolean locked = Boolean.FALSE;
        try {
            lock = redissonClient.getLock(lockKey);
            locked = lock.tryLock(waitTime, -1, timeUnit);
            if (locked) {
                return supplier.get();
            } else {
                log.error("Get Distributed Lock [{}] failure", lockKey);
                throw new RuntimeException("system.busy.error");
            }
        } catch (Exception e) {
            log.error("Executed With Distributed Lock error,", e);
            throw e;
        } finally {
            if (locked) {
                lock.unlock();
            }
        }
    }
}
