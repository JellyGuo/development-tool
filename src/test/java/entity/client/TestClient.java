package entity.client;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestClient {
    public List<String> generateTestString(String str) {
        List<String> list = new ArrayList<>();
        list.add(str);
        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("test client normal");
        list.add("test client normal String");
        return list;
    }

    public Map<Integer, String> getResponse(String str) {
        try {
            Thread.sleep(14000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("test client time out: " + str);
        return new HashMap<>();
    }
}
