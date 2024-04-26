package entity;

import constant.Constant;
import context.ContextHolder;
import enums.ResponseStatusEnum;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;

@ToString
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class Response<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    @Getter
    @Setter
    private int code;

    @Getter
    @Setter
    private String msg;


    @Getter
    @Setter
    private T data;

    @Getter
    @Setter
    private String traceLogId = ContextHolder.getLog(Constant.TRACE_LOG_ID);

    public Boolean isOk() {
        return code == 0;
    }

    public static <T> Response<T> success() {
        return restResult(null, 0, null);
    }

    public static <T> Response<T> success(T data) {
        return restResult(data, ResponseStatusEnum.SUCCESS.getCode(), null);
    }

    public static <T> Response<T> success(T data, String msg) {
        return restResult(data, ResponseStatusEnum.SUCCESS.getCode(), msg);
    }

    public static <T> Response<T> failed() {
        return restResult(null, ResponseStatusEnum.FAIL.getCode(), null);
    }

    public static <T> Response<T> failed(String msg) {
        return restResult(null, ResponseStatusEnum.FAIL.getCode(), msg);
    }

    public static <T> Response<T> failed(T data) {
        return restResult(data, ResponseStatusEnum.FAIL.getCode(), null);
    }

    public static <T> Response<T> failed(T data, String msg) {
        return restResult(data, ResponseStatusEnum.FAIL.getCode(), msg);
    }

    public static <T> Response<T> failed(T data, int code, String msg) {
        return restResult(data, code, msg);
    }

    public static <T> Response<T> failed(int code, String msg) {
        return restResult(null, code, msg);
    }

    private static <T> Response<T> restResult(T data, int code, String msg) {
        Response<T> apiResult = new Response<>();
        apiResult.setCode(code);
        apiResult.setData(data);
        apiResult.setMsg(msg);
        return apiResult;
    }
}
