package dlock;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 分布式锁
 */
public interface DistributedLockService {
    /**
     * 分布式锁
     *
     * @param lockKey
     * @param waitTime  等待时间
     * @param leaseTime 租期（失效时间）
     * @param timeUnit
     * @param supplier
     * @param <R>
     * @return
     * @throws Exception
     */
    <R> R runWithDLock(String lockKey, Long waitTime, Long leaseTime, TimeUnit timeUnit, Supplier<R> supplier) throws Exception;

    /**
     * 分布式锁(无等待)
     *
     * @param lockKey
     * @param supplier
     * @return
     * @param <R>
     * @throws Exception
     */
    <R> R runWithDLockWithOutWaitingTime(String lockKey, Supplier<R> supplier) throws Exception;

    /**
     * 分布式锁看门狗模式
     *
     * @param lockKey
     * @param waitTime
     * @param timeUnit
     * @param supplier
     * @param <R>
     * @return
     * @throws Exception
     */
    <R> R runWithWatchDogDLock(String lockKey, Long waitTime, TimeUnit timeUnit, Supplier<R> supplier) throws Exception;
}
