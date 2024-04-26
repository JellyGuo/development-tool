package cache;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class ThreadTest {
    private static final Map<Integer, String> map = new HashMap<>();
    private static final ReentrantLock lock = new ReentrantLock();

    static {
        map.put(1, "1");
    }

    public static String getValue(Integer key) {
        System.out.println("current thread: " + Thread.currentThread().getName());
        String value = map.get(key);
        if (value == null) {
            synchronized (map) {
                value = map.get(key);
                if (value == null) {
                    System.out.println("come into: " + Thread.currentThread().getName());
                    value = "" + key;
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    map.put(key, value);
                }
            }
        }
        return value;
    }

    public static void main(String[] args) {
        Thread t1 = new Thread(() -> {
            System.out.println(Thread.currentThread().getName() + ":" + getValue(1));
        });
        Thread t2 = new Thread(() -> {
            System.out.println(Thread.currentThread().getName() + ":" + getValue(2));
        });
        Thread t3 = new Thread(() -> {
            System.out.println(Thread.currentThread().getName() + ":" + getValue(3));
        });
        t2.start();
        t3.start();
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        t1.start();
    }
}
