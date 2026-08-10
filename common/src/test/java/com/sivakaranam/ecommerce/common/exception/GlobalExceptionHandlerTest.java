package com.sivakaranam.ecommerce.common.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void accessDeniedException_mapsTo403_notTheGenericHandler() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/products");

        var response = handler.handleAccessDenied(new AccessDeniedException("Access Denied"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody().message()).isEqualTo("Access is denied");
    }

    @Test
    void resourceNotFoundException_mapsTo404() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/products/999");

        var response = handler.handleNotFound(new ResourceNotFoundException("No product with id 999"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void unexpectedException_fallsBackTo500() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/products");

        var response = handler.handleGeneric(new RuntimeException("boom"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
