package com.simon.exception;

import com.simon.name.Headers;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

public class GlobalExceptionHelper {

    public static ResponseEntity<Map<String, Object>> createResponse(HttpStatus httpStatus, Object errorMessages,
                                                               HttpServletRequest httpServletRequest) {
        Map<String, Object> responseBody = new LinkedHashMap<>();
        String headerOriginalPath = httpServletRequest.getHeader(Headers.ORIGINAL_PATH);
        responseBody.put("timestamp", new Date());
        responseBody.put("status", httpStatus.value());
        responseBody.put("error", errorMessages);
        responseBody.put("path", headerOriginalPath == null ? httpServletRequest.getRequestURL() : headerOriginalPath);
        return new ResponseEntity<>(responseBody, httpStatus);
    }
}
