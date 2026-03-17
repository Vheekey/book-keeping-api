package com.calvary.finance.shared;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        ApiError error = new ApiError();
        error.setStatus(HttpStatus.BAD_REQUEST.value());
        error.setError(HttpStatus.BAD_REQUEST.getReasonPhrase());
        error.setPath(request.getRequestURI());

        List<ApiError.FieldError> fields = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::toApiFieldError)
                .collect(Collectors.toList());
        error.setFieldErrors(fields);

        return ResponseEntity.badRequest().body(error);
    }

    private ApiError.FieldError toApiFieldError(FieldError fieldError) {
        return new ApiError.FieldError(fieldError.getField(), fieldError.getDefaultMessage());
    }
}
