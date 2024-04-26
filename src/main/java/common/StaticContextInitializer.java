package common;

import entity.RemoteProperties;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * 静态类中引用SpringBoot加载的配置文件，要等Spring启动完毕
 */
@Component
public class StaticContextInitializer {

    @Getter
    private static RemoteProperties remoteProperties;

    @Autowired
    public StaticContextInitializer(RemoteProperties remoteProperties) {
        StaticContextInitializer.remoteProperties = remoteProperties;
    }

    @PostConstruct
    public void init() {
        // Now you can use remoteProperties in static methods
    }

}