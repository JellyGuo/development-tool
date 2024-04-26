package cache;

import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;

@Component
@ICache(mode = CacheMod.preload, refreshTime = 5)
public class TestPreLoadCache extends AbstractCache<Integer, List<String>> {
    @Autowired
    public TestPreLoadCache(Executor cacheLoadExecutor, ScheduledExecutorService cacheRefreshExecutor) {
        super(cacheLoadExecutor, cacheRefreshExecutor);
    }

    @Override
    protected ICacheLoader<Integer, List<String>> cacheLoader(ILocalCache<Integer, List<String>> localCache) {
        return new ICacheLoader<Integer, List<String>>(localCache) {
            @Override
            protected Map<Integer, List<String>> loadAll() {
                System.out.println("preload scheduled thread" + Thread.currentThread().getName());
                Map<Integer, List<String>> map = new HashMap<>();
                map.put(1, Lists.newArrayList("preload1", "preload2"));
                map.put(2, Lists.newArrayList("preload3", "preload4"));
                return map;
            }
        };
    }
}
