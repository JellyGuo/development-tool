package common;


import cn.hutool.core.lang.Pair;
import entity.RemoteProperties;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 反射工具类
 */
public class RemoteClientFactory {

    private static final Map<String, Pair<Class<?>, Object>> instanceCache = new ConcurrentHashMap<>();
    private static final ReentrantLock lock = new ReentrantLock();

    static {
        RemoteProperties remoteProperties = StaticContextInitializer.getRemoteProperties();
        Optional.ofNullable(remoteProperties).map(RemoteProperties::getServers)
                .orElse(new ArrayList<>()).forEach(server -> {
                    if (StringUtils.isNotBlank(server.getClient())) {
                        try {
                            Class<?> clazz = Class.forName(server.getClient());
                            Object instance = clazz.getDeclaredConstructor().newInstance();
                            instanceCache.put(server.getClient(), Pair.of(clazz, instance));
                        } catch (Exception e) {
                            throw new RuntimeException("Failed to initialize reflection class", e);
                        }
                    }

                });
    }

    public static Object invokeMethod(String className, String methodName, Object... args) {
        try {
            Class<?> clazz;
            Object instance;
            Pair<Class<?>, Object> pair = instanceCache.get(className);
            if (pair == null) {
                lock.lock();
                try {
                    pair = instanceCache.get(className);
                    if (pair == null) {
                        clazz = Class.forName(className);
                        instance = clazz.getDeclaredConstructor().newInstance();
                        pair = Pair.of(clazz, instance);
                        instanceCache.put(className, pair);
                    }
                } finally {
                    lock.unlock();
                }
            }
            clazz = pair.getKey();
            instance = pair.getValue();
            Method method = clazz.getMethod(methodName, getClasses(args));
            return method.invoke(instance, args);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke method", e);
        }
    }

    private static Class<?>[] getClasses(Object... args) {
        return (Class<?>[]) Arrays.stream(args)
                .map(Object::getClass)
                .toArray(Class[]::new);
    }
}
