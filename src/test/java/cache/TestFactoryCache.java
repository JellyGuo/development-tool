package cache;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;

@ICache(mode = CacheMod.lazyload, maximumSize = 100000, refreshTime = 300, expireTime = 3600)
public class TestFactoryCache extends AbstractCache<Integer, String> {

    private TestFactoryCache instance;

    public TestFactoryCache getInstance() {
        if (instance == null) {
            instance = (TestFactoryCache) LocalCacheFactory.createCache(TestFactoryCache.class);
        }
        return instance;
    }

    public TestFactoryCache(Executor loadPool, ScheduledExecutorService refreshPool) {
        super(loadPool, refreshPool);
    }

    @Override
    protected ICacheLoader<Integer, String> cacheLoader(ILocalCache<Integer, String> localCache) {
        return new ICacheLoader<Integer, String>(localCache) {
            @Override
            protected String load(Integer integer) {
                return "1";
            }

            @Override
            protected Map<Integer, String> loadAll(Iterable<? extends Integer> keys) {
                return new HashMap<>();
            }
        };
    }
}
