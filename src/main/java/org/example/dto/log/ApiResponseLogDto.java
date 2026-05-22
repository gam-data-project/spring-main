package org.example.dto.log;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ApiResponseLogDto <T>{
    private boolean success;
    private String code;
    private String message;
    private T data;
    private LocalDateTime timestamp;

}
