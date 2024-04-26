package context;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class Context {
    private Map<String,String> scopeContext = new HashMap<>();
    private Map<String,String> logContext = new HashMap<>();
}
