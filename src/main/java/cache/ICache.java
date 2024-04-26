package cache;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ICache {
    /**
     * 缓存模式
     * preload:预加载，定期刷新
     * lazyload：懒加载，有最大数量和过期时间限制
     */
    CacheMod mode();

    /**
     * 缓存最大容量
     */
    long maximumSize() default 10000;
    /**
     * 缓存刷新时间
     */
    long refreshTime() default 300;
    /**
     * 缓存过期时间
     */
    long expireTime() default 300;
}
