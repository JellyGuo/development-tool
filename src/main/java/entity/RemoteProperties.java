package entity;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "remote")
@Data
public class RemoteProperties {
    private List<Server> servers;
    @Data
    public static class Server {
        private String name;
        private String client;
        private String method;
        private long timeout;
    }
}
