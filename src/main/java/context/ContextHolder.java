package context;

import com.alibaba.ttl.TransmittableThreadLocal;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;

import java.util.Map;

public class ContextHolder {
    static TransmittableThreadLocal<Context> contextHolder = new TransmittableThreadLocal<Context>(){
        /**
         * 把MDC中的内容放到子线程中，用于logback日志输出
         * MDC和TransmittableThreadLocal都可作为上下文传递工具，但是MDC不适用线程池
         */
        @Override
        protected void beforeExecute() {
            final Context mdc = get();
            mdc.getLogContext().forEach(MDC::put);
        }

        @Override
        protected void afterExecute() {
            MDC.clear();
        }

        @Override
        protected Context initialValue() {
            return new Context();
        }
    };

    public static Map<String, String> getLogContext() {
        return contextHolder.get().getLogContext();
    }

    public static Map<String, String> getScopeContext() {
        return contextHolder.get().getScopeContext();
    }

    public static void setLog(String key, String value) {
        contextHolder.get().getLogContext().put(key, value);
    }

    public static String getLog(String key) {
        return contextHolder.get().getLogContext().getOrDefault(key, StringUtils.EMPTY);
    }

    public static void set(String key, String value) {
        contextHolder.get().getScopeContext().put(key, value);
    }

    public static String get(String key) {
        return contextHolder.get().getScopeContext().getOrDefault(key, StringUtils.EMPTY);
    }

    public static void remove(){
        contextHolder.remove();
    }
}
