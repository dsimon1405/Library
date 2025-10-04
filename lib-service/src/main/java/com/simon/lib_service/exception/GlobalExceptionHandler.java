package com.simon.lib_service.exception;

import com.simon.exception.ExistsException;
import com.simon.exception.NotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.*;

import static com.simon.exception.GlobalExceptionHelper.createResponse;

@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler
    public ResponseEntity<Map<String, Object>> validationExceptions(MethodArgumentNotValidException ex, HttpServletRequest httpServletRequest) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return createResponse(HttpStatus.BAD_REQUEST, errors, httpServletRequest);
    }

    @ExceptionHandler
    public ResponseEntity<Map<String, Object>> existsException(ExistsException ex, HttpServletRequest httpServletRequest) {
        return createResponse(HttpStatus.CONFLICT, ex.getMessage(), httpServletRequest);
    }

    @ExceptionHandler
    public ResponseEntity<Map<String, Object>> notFoundException(NotFoundException ex, HttpServletRequest httpServletRequest) {
        return createResponse(HttpStatus.NOT_FOUND, ex.getMessage(), httpServletRequest);
    }

    @ExceptionHandler
    public ResponseEntity<Map<String, Object>> requestParameterException(RequestParameterException ex, HttpServletRequest httpServletRequest) {
        return createResponse(HttpStatus.CONFLICT, ex.getMessage(), httpServletRequest);
    }
}
