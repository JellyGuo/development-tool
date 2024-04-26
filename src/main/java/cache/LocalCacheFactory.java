package cache;

import common.SpringContextUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;

public class LocalCacheFactory {

    private static final Map<Class<? extends ILocalCache<?, ?>>, ILocalCache<?, ?>> cacheMap = new ConcurrentHashMap<>();

    private static final LocalCacheFactory instance = new LocalCacheFactory();

    private LocalCacheFactory() {
    }

    public LocalCacheFactory getInstance() {
        return instance;
    }

    public static <K, V> ILocalCache<K, V> createCache(Class<? extends ILocalCache<K, V>> clazz) {
        ICache cache = clazz.getAnnotation(ICache.class);
        if (cache == null) {
            throw new RuntimeException("No cache annotation found");
        }
        if (clazz.isAnnotationPresent(ICache.class)
                && clazz.isAssignableFrom(ILocalCache.class)) {
            ILocalCache<K, V> localCache = (ILocalCache<K, V>) cacheMap.get(clazz);
            if (localCache == null) {
                // 对map对象加锁，获得锁的线程执行同步代码块
                // 此时其他线程依然可以从map中获取对象，但是不能修改map
                // 和synchronized修饰类一样，获取锁的线程执行同步代码块，但是不影响其他非同步代码块被其他线程执行
                synchronized (cacheMap) {
                    localCache = (ILocalCache<K, V>) cacheMap.get(clazz);
                    if (localCache == null) {
                        localCache = createCacheInstance(clazz);
                        cacheMap.put(clazz, localCache);
                    }
                }
            }
            return localCache;
        }
        throw new RuntimeException("No cache class found");
    }

    private static <K, V> ILocalCache<K, V> createCacheInstance(Class<? extends ILocalCache<K, V>> clazz) {
        try {
            Constructor<? extends ILocalCache<K, V>> constructor = clazz.getConstructor(Executor.class, ScheduledExecutorService.class);
            Executor cacheLoadExecutor = SpringContextUtil.getBean("cacheLoadExecutor", Executor.class);
            ScheduledExecutorService cacheRefreshExecutor = SpringContextUtil.getBean("cacheRefreshExecutor", ScheduledExecutorService.class);

            ILocalCache<K, V> target = constructor.newInstance(cacheLoadExecutor, cacheRefreshExecutor);
            Method method = clazz.getMethod("initialize", clazz);
            method.invoke(target);
            return target;
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new RuntimeException("Failed to create cache instance", e);
        }
    }
}
