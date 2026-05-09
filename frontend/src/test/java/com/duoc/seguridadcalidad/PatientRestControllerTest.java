package com.duoc.seguridadcalidad;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Security-focused controller test.
 * It verifies that the controller refuses to proceed when the token is missing and
 * that the happy path reaches the backend service only after token extraction succeeds.
 */
@WebMvcTest(PatientRestController.class)
@AutoConfigureMockMvc(addFilters = false)
class PatientRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BackendService backendService;

    @MockBean
    private JwtCookieService jwtCookieService;

    @Test
    @DisplayName("getAll should return 200 when a token exists")
    void getAll_shouldReturnOkWhenTokenExists() throws Exception {
        when(jwtCookieService.extractToken(any())).thenReturn("valid-token");
        when(backendService.getPatients("valid-token"))
                .thenReturn(List.of(Map.of("id", 1, "fullName", "Ana Perez")));

        mockMvc.perform(get("/api/patients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].fullName").value("Ana Perez"));

        verify(backendService).getPatients("valid-token");
    }

    @Test
    @DisplayName("getAll should return 401 when the token is missing")
    void getAll_shouldReturnUnauthorizedWhenTokenIsMissing() throws Exception {
        when(jwtCookieService.extractToken(any())).thenReturn(null);

        mockMvc.perform(get("/api/patients"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(""));

        verify(backendService, never()).getPatients(any());
    }

    @Test
    @DisplayName("create should return 200 when a token exists")
    void create_shouldReturnOkWhenTokenExists() throws Exception {
        when(jwtCookieService.extractToken(any())).thenReturn("valid-token");
        when(backendService.createPatient(eq("valid-token"), any()))
                .thenReturn(Map.of("id", 2, "fullName", "Luis Gomez"));

        mockMvc.perform(post("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fullName":"Luis Gomez","documentNumber":"12345678"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.fullName").value("Luis Gomez"));

        verify(backendService).createPatient(eq("valid-token"), any());
    }

    @Test
    @DisplayName("create should return 401 when the token is missing")
    void create_shouldReturnUnauthorizedWhenTokenIsMissing() throws Exception {
        when(jwtCookieService.extractToken(any())).thenReturn(null);

        mockMvc.perform(post("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fullName":"Luis Gomez","documentNumber":"12345678"}
                                """))
                .andExpect(status().isUnauthorized());

        verify(backendService, never()).createPatient(any(), any());
    }

    @Test
    @DisplayName("create should return 400 for malformed JSON and avoid leaking internals")
    void create_shouldReturnBadRequestForMalformedJson() throws Exception {
        mockMvc.perform(post("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid-json"))
                .andExpect(status().isBadRequest());
    }
}
