package com.calvary.finance.shared;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
public class ApiError {
    private Instant timestamp = Instant.now();
    private int status;
    private String error;
    private String path;
    private List<FieldError> fieldErrors;

    @Data
    @NoArgsConstructor
    public static class FieldError {
        private String field;
        private String message;

        public FieldError(String field, String message) {
            this.field = field;
            this.message = message;
        }
    }
}
