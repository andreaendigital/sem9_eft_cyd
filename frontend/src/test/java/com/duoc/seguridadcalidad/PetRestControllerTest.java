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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * OWASP A01/A07 coverage-focused MVC test.
 * This slice keeps the controller isolated, bypasses security filters, and forces the HTTP
 * flow to execute through MockMvc so JaCoCo records the real controller branches.
 */
@WebMvcTest(PetRestController.class)
@AutoConfigureMockMvc(addFilters = false)
class PetRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BackendService backendService;

    @MockBean
    private JwtCookieService jwtCookieService;

    @Test
    @DisplayName("getAll should return the list of pets")
    void getAll_shouldReturnPets() throws Exception {
        when(backendService.getPets()).thenReturn(List.of(Map.of("id", 1, "name", "Nala")));

        mockMvc.perform(get("/api/pets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Nala"));

        verify(backendService).getPets();
    }

    @Test
    @DisplayName("search should forward filter parameters to the backend service")
    void search_shouldReturnFilteredPets() throws Exception {
        when(backendService.searchPets("dog", "female", "santiago", 3, "available"))
                .thenReturn(List.of(Map.of("id", 2, "species", "dog", "status", "available")));

        mockMvc.perform(get("/api/pets/search")
                        .param("species", "dog")
                        .param("gender", "female")
                        .param("location", "santiago")
                        .param("age", "3")
                        .param("status", "available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].species").value("dog"))
                .andExpect(jsonPath("$[0].status").value("available"));

        verify(backendService).searchPets("dog", "female", "santiago", 3, "available");
    }

    @Test
    @DisplayName("create should return 201 when the request contains a valid token")
    void create_shouldReturnCreatedWhenTokenExists() throws Exception {
        when(jwtCookieService.extractToken(any())).thenReturn("valid-token");
        when(backendService.createPet(eq("valid-token"), any()))
                .thenReturn(Map.of("id", 10, "name", "Luna"));

        mockMvc.perform(post("/api/pets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Luna","species":"cat"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.name").value("Luna"));

        verify(jwtCookieService).extractToken(any());
        verify(backendService).createPet(eq("valid-token"), any());
    }

    @Test
    @DisplayName("create should return 401 when the request is missing a token")
    void create_shouldReturnUnauthorizedWhenTokenIsMissing() throws Exception {
        when(jwtCookieService.extractToken(any())).thenReturn(null);

        mockMvc.perform(post("/api/pets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Luna","species":"cat"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(""));

        verify(backendService, never()).createPet(any(), any());
    }

    @Test
    @DisplayName("create should return 400 for malformed JSON without exposing internals")
    void create_shouldReturnBadRequestForMalformedJson() throws Exception {
        mockMvc.perform(post("/api/pets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid-json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("update should return 200 when the token exists")
    void update_shouldReturnOkWhenTokenExists() throws Exception {
        when(jwtCookieService.extractToken(any())).thenReturn("valid-token");
        when(backendService.updatePet(eq("valid-token"), eq(7), any()))
                .thenReturn(Map.of("id", 7, "name", "Milo"));

        mockMvc.perform(put("/api/pets/7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Milo","species":"dog"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.name").value("Milo"));

        verify(backendService).updatePet(eq("valid-token"), eq(7), any());
    }

    @Test
    @DisplayName("update should return 401 when the token is missing")
    void update_shouldReturnUnauthorizedWhenTokenIsMissing() throws Exception {
        when(jwtCookieService.extractToken(any())).thenReturn(null);

        mockMvc.perform(put("/api/pets/7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Milo","species":"dog"}
                                """))
                .andExpect(status().isUnauthorized());

        verify(backendService, never()).updatePet(any(), any(), any());
    }

    @Test
    @DisplayName("delete should return 200 when the token exists")
    void delete_shouldReturnOkWhenTokenExists() throws Exception {
        when(jwtCookieService.extractToken(any())).thenReturn("valid-token");
        when(backendService.deletePet(eq("valid-token"), eq(11)))
                .thenReturn(Map.of("deleted", true));

        mockMvc.perform(delete("/api/pets/11"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deleted").value(true));

        verify(backendService).deletePet(eq("valid-token"), eq(11));
    }

    @Test
    @DisplayName("delete should return 401 when the token is missing")
    void delete_shouldReturnUnauthorizedWhenTokenIsMissing() throws Exception {
        when(jwtCookieService.extractToken(any())).thenReturn(null);

        mockMvc.perform(delete("/api/pets/11"))
                .andExpect(status().isUnauthorized());

        verify(backendService, never()).deletePet(any(), any());
    }
}
