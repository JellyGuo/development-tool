package common;

import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.DefaultPropertySourceFactory;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.lang.Nullable;

import java.io.IOException;
import java.util.List;

/**
 * 加载@PropertySource 标注的yml配置文件
 * 在实际的开发中使用 @PropertySource 注解无法加载 yml 配置文件问题，需要继承DefaultPropertySourceFactory类，重写createPropertySource方法
 */
public class MyPropertySourceFactory extends DefaultPropertySourceFactory {
    @Override
    public PropertySource<?> createPropertySource(@Nullable String name, EncodedResource resource) throws IOException {
        if (resource == null) {
            return super.createPropertySource(name, resource);
        }
        List<PropertySource<?>> sources = new YamlPropertySourceLoader().load(resource.getResource().getFilename(), resource.getResource());
        return sources.get(0);
    }
}
