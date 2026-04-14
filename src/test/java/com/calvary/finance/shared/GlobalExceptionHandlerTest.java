package com.calvary.finance.shared;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleValidationExceptionReturnsBadRequestWithFieldErrors() throws Exception {
        Method method = DummyController.class.getDeclaredMethod("create", DummyPayload.class);
        MethodParameter parameter = new MethodParameter(method, 0);
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new DummyPayload(), "dummyPayload");
        bindingResult.addError(new FieldError("dummyPayload", "email", "must be valid"));
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(parameter, bindingResult);
        HttpServletRequest request = request("/users");

        ResponseEntity<ApiError> response = handler.handleValidationException(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getBody().getPath()).isEqualTo("/users");
        assertThat(response.getBody().getFieldErrors())
                .extracting(ApiError.FieldError::getField, ApiError.FieldError::getMessage)
                .containsExactly(org.assertj.core.groups.Tuple.tuple("email", "must be valid"));
    }

    @Test
    void handleEntityNotFoundExceptionReturnsBodyWithNotFoundStatus() {
        HttpServletRequest request = request("/users/42");
        EntityNotFoundException exception = new EntityNotFoundException("User not found");

        ResponseEntity<ApiError> response = handler.handleEntityNotFoundException(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(response.getBody().getError()).isEqualTo("User not found");
        assertThat(response.getBody().getPath()).isEqualTo("/users/42");
    }

    @Test
    void handleRuntimeExceptionReturnsBadRequestResponse() {
        HttpServletRequest request = request("/roles");
        RuntimeException exception = new RuntimeException("Role already exists");

        ResponseEntity<ApiError> response = handler.handleRuntimeException(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getBody().getError()).isEqualTo("Role already exists");
        assertThat(response.getBody().getPath()).isEqualTo("/roles");
    }

    private HttpServletRequest request(String uri) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI(uri);
        return request;
    }

    private static class DummyController {
        @SuppressWarnings("unused")
        void create(DummyPayload payload) {
        }
    }

    private static class DummyPayload {
    }
}
