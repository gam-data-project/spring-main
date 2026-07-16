package org.example.dto.log;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ServiceResultLogDto <T>{
    private boolean success;
    private String code;    // OK, VALIDATION_ERROR, DUPLICATE ...
    private String message;
    private T data;

    public static <T> ServiceResultLogDto<T> ok(T data, String message) {
        return ServiceResultLogDto.<T>builder()
                .success(true).code("OK").message(message).data(data).build();
    }

    public static <T> ServiceResultLogDto<T> fail(String code, String message) {
        return ServiceResultLogDto.<T>builder()
                .success(false).code(code).message(message).build();
    }
}
