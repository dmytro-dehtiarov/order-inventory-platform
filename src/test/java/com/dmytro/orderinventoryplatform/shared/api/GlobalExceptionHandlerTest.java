package com.dmytro.orderinventoryplatform.shared.api;

import com.dmytro.orderinventoryplatform.shared.domain.ConflictException;
import com.dmytro.orderinventoryplatform.shared.domain.ResourceNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    record TestRequest(@NotBlank String name) {}

    @Test
    void resourceNotFoundException_mapsTo404() throws Exception {
        mockMvc.perform(get("/test-not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Test not found"));
    }
    @Test
    void conflictException_mapsTo409() throws Exception {
        mockMvc.perform(get("/test-conflict"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Conflict occurred"));
    }
    @Test
    void validationException_mapsTo400() throws Exception {
        mockMvc.perform(post("/test-validation")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @RestController
    private static class TestController {

        @GetMapping("/test-not-found")
        void triggerNotFound() {
            throw new ResourceNotFoundException("Test not found") {};
        }
        @GetMapping("/test-conflict")
        void triggerConflictException() {
            throw new ConflictException("Conflict occurred") {};
        }
        @PostMapping("/test-validation")
        void triggerValidation(@Valid @RequestBody TestRequest request) {
        }
    }
}