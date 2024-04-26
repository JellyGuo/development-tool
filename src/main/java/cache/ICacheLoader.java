package cache;

import com.github.benmanes.caffeine.cache.AsyncCacheLoader;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public abstract class ICacheLoader<K, V> implements AsyncCacheLoader<K, V> {

    private final boolean preload;

    public ICacheLoader(ILocalCache<K, V> localCache) {
        this.preload = localCache.getClass().getAnnotation(ICache.class).mode() == CacheMod.preload;

    }

    protected V load(K k) {
        if (preload) {
            return null;
        }
        throw new UnsupportedOperationException();
    }

    protected Map<K, V> loadAll(Iterable<? extends K> keys) {
        if (preload) {
            return null;
        }
        throw new UnsupportedOperationException();
    }

    protected Map<K, V> loadAll() {
        if (preload) throw new UnsupportedOperationException();
        return null;
    }


    @Override
    public @NonNull CompletableFuture<V> asyncLoad(@NonNull K k, @NonNull Executor executor) {
        return CompletableFuture.supplyAsync(() -> load(k), executor);
    }

    @Override
    public @NonNull CompletableFuture<Map<@NonNull K, @NonNull V>> asyncLoadAll(@NonNull Iterable<? extends @NonNull K> keys, @NonNull Executor executor) {
        return CompletableFuture.supplyAsync(() -> loadAll(keys), executor);
    }

}
