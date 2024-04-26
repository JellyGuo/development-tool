package enums;

public enum ResponseStatusEnum {
    SUCCESS("SUCCESS", 0),
    FAIL("FAIL", 1);

    private String name;
    private int code;

    ResponseStatusEnum(String name, int code) {
        this.name = name;
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}
