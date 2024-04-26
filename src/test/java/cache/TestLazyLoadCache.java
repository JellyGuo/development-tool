package cache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;

@Component
@ICache(mode = CacheMod.lazyload, maximumSize = 1000, refreshTime = 1000)
public class TestLazyLoadCache extends AbstractCache<String, String> {
    @Autowired
    public TestLazyLoadCache(Executor cacheLoadExecutor, ScheduledExecutorService cacheRefreshExecutor) {
        super(cacheLoadExecutor, cacheRefreshExecutor);
    }

    @Override
    protected ICacheLoader<String, String> cacheLoader(ILocalCache<String, String> localCache) {
        return new ICacheLoader<String, String>(localCache) {
            @Override
            protected String load(String s) {
                return "test cache load";
            }

            @Override
            protected Map<String, String> loadAll(Iterable<? extends String> keys) {
                return super.loadAll(keys);
            }
        };
    }
}
