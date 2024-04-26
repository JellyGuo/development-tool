package cache;

import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Scheduler;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class AbstractCache<K, V> implements ILocalCache<K, V> {

    private final Executor loadPool;
    private final ScheduledExecutorService refreshPool;

    public AbstractCache(Executor loadPool, ScheduledExecutorService refreshPool) {
        this.loadPool = loadPool;
        this.refreshPool = refreshPool;
    }

    private AsyncLoadingCache<K, V> cache;

    private ConcurrentHashMap<K, V> preloadData;

    private final TimeUnit timeUnit = TimeUnit.SECONDS;

    private final AtomicBoolean preload = new AtomicBoolean(false);

    @PostConstruct
    public void initialize() {
        ICache annotation = this.getClass().getAnnotation(ICache.class);
        CacheMod mod = annotation.mode();
        long refreshTime = annotation.refreshTime();
        long expireTime = annotation.refreshTime();
        long size = annotation.maximumSize();
        if (mod == CacheMod.preload) {
            preload.set(true);
            Runnable preloadTask = preload();
            preloadTask.run();
            refreshPool.scheduleAtFixedRate(preloadTask, refreshTime, refreshTime, timeUnit);
        } else {
            cache = Caffeine.newBuilder()
                    .expireAfterWrite(expireTime, timeUnit)
                    .maximumSize(size)
                    .scheduler(Scheduler.systemScheduler())
                    .executor(loadPool)
                    .buildAsync(cacheLoader(this));
        }

    }

    @Override
    public V get(K key) {
        if (preload.get()) {
            return preloadData.get(key);
        }
        return cache.get(key).join();
    }

    @Override
    public Map<K, V> getAll() {
        if (preload.get()) {
            return preloadData;
        }
        return cache.synchronous().asMap();
    }

    @Override
    public Map<K, V> getAll(Iterable<? extends K> keys) {
        if (preload.get()) {
            return preloadData;
        }
        return cache.getAll(keys).join();
    }

    protected abstract ICacheLoader<K, V> cacheLoader(ILocalCache<K, V> localCache);

    private Runnable preload() {
        return () -> {
            ICacheLoader<K, V> cacheLoader = cacheLoader(this);
            try {
                Map<K, V> data = cacheLoader.loadAll();
                preloadData = new ConcurrentHashMap<>(data);
            } catch (Exception e) {
                if (preloadData == null) {
                    preloadData = new ConcurrentHashMap<>();
                }
            }
        };
    }

}
