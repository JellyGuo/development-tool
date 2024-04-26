package cache;

import java.util.Map;

public interface ILocalCache<K, V> {
    V get(K key);

    Map<? extends K, ? extends V> getAll(Iterable<? extends K> keys);

    Map<K, V> getAll();
}
