package common;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * 获取Spring容器中的Bean，静态方法使用
 * 静态代码块执行是在类初始化时期，Spring中bean装载是在容器启动阶段，所以静态代码块中不能使用Spring中的bean
 * 3种方案：
 * 1.postConstruct 见 StaticContextInitializer
 * 2.实现ApplicationContextAware接口
 * 3.在启动类中，把SpringApplication.run的返回值赋给一个静态变量
 */
@Component
public class SpringContextUtil implements ApplicationContextAware {

    private static ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        context = applicationContext;
    }

    public static <T> T getBean(Class<T> beanClass) {
        return context.getBean(beanClass);
    }

    public static <T> T getBean(String beanName, Class<T> clazz) {
        return context.getBean(beanName, clazz);
    }
}
