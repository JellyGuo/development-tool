package common;

import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * 获取配置文件中的配置，静态类初始化时使用
 */
@Component
public class EnvUtil implements EnvironmentAware {
    private static Environment env;

    @Override
    public void setEnvironment(Environment environment) {
        env = environment;
    }

    public static String getString(String key) {
        return env.getProperty(key);
    }
}
