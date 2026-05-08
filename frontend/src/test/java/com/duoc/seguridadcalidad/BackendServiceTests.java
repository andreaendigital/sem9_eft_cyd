package com.duoc.seguridadcalidad;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BackendServiceTests {

    @Mock
    private RestTemplate restTemplate;

    private BackendService backendService;

    private static final String BASE_URL = "http://localhost:8080";
    private static final String TOKEN = "test-jwt-token";

    @BeforeEach
    void setUp() {
        backendService = new BackendService(restTemplate, BASE_URL);
    }

    // -------------------------------------------------------------------------
    // login
    // -------------------------------------------------------------------------

    @Test
    void loginShouldReturnTokenWhenBackendRespondsWithRawToken() {
        AuthRequest request = new AuthRequest();
        request.setUsername("user");
        request.setPassword("pass");

        when(restTemplate.postForEntity(BASE_URL + "/login", request, String.class))
                .thenReturn(ResponseEntity.ok("my-token"));

        AuthResponse response = backendService.login(request);

        assertEquals("my-token", response.getToken());
    }

    @Test
    void loginShouldStripBearerPrefixFromToken() {
        AuthRequest request = new AuthRequest();
        request.setUsername("user");
        request.setPassword("pass");

        when(restTemplate.postForEntity(BASE_URL + "/login", request, String.class))
                .thenReturn(ResponseEntity.ok("Bearer my-token"));

        AuthResponse response = backendService.login(request);

        assertEquals("my-token", response.getToken());
    }

    @Test
    void loginShouldReturnNullTokenWhenBodyIsNull() {
        AuthRequest request = new AuthRequest();

        when(restTemplate.postForEntity(BASE_URL + "/login", request, String.class))
                .thenReturn(ResponseEntity.ok(null));

        AuthResponse response = backendService.login(request);

        assertNull(response.getToken());
    }

    @Test
    void loginShouldPropagateHttpStatusCodeException() {
        AuthRequest request = new AuthRequest();

        when(restTemplate.postForEntity(BASE_URL + "/login", request, String.class))
                .thenThrow(HttpClientErrorException.create(HttpStatus.UNAUTHORIZED, "Unauthorized", null, null, null));

        assertThrows(HttpClientErrorException.class, () -> backendService.login(request));
    }

    // -------------------------------------------------------------------------
    // getPatients
    // -------------------------------------------------------------------------

    @Test
    @SuppressWarnings("unchecked")
    void getPatientsShouldReturnListOfPatients() {
        Map<String, Object> patient = Map.of("id", 1, "name", "John");

        when(restTemplate.exchange(eq(BASE_URL + "/patients"), eq(HttpMethod.GET), any(), eq(Map[].class)))
                .thenReturn(ResponseEntity.ok(new Map[]{patient}));

        List<Map<String, Object>> result = backendService.getPatients(TOKEN);

        assertEquals(1, result.size());
        assertEquals("John", result.get(0).get("name"));
    }

    @Test
    void getPatientsShouldReturnEmptyListWhenBodyIsNull() {
        when(restTemplate.exchange(eq(BASE_URL + "/patients"), eq(HttpMethod.GET), any(), eq(Map[].class)))
                .thenReturn(ResponseEntity.ok(null));

        List<Map<String, Object>> result = backendService.getPatients(TOKEN);

        assertTrue(result.isEmpty());
    }

    @Test
    void getPatientsShouldPropagateHttpStatusCodeException() {
        when(restTemplate.exchange(eq(BASE_URL + "/patients"), eq(HttpMethod.GET), any(), eq(Map[].class)))
                .thenThrow(HttpClientErrorException.create(HttpStatus.FORBIDDEN, "Forbidden", null, null, null));

        assertThrows(HttpClientErrorException.class, () -> backendService.getPatients(TOKEN));
    }

    // -------------------------------------------------------------------------
    // createPatient
    // -------------------------------------------------------------------------

    @Test
    @SuppressWarnings("unchecked")
    void createPatientShouldReturnCreatedPatient() {
        Map<String, Object> patient = Map.of("name", "Jane");
        Map<String, Object> created = Map.of("id", 2, "name", "Jane");

        when(restTemplate.postForObject(eq(BASE_URL + "/patients"), any(), eq(Map.class)))
                .thenReturn(created);

        Map<String, Object> result = backendService.createPatient(TOKEN, patient);

        assertEquals(2, result.get("id"));
    }

    @Test
    void createPatientShouldPropagateHttpStatusCodeException() {
        when(restTemplate.postForObject(eq(BASE_URL + "/patients"), any(), eq(Map.class)))
                .thenThrow(HttpClientErrorException.create(HttpStatus.BAD_REQUEST, "Bad Request", null, null, null));

        assertThrows(HttpClientErrorException.class, () -> backendService.createPatient(TOKEN, Map.of()));
    }

    // -------------------------------------------------------------------------
    // getAppointments
    // -------------------------------------------------------------------------

    @Test
    @SuppressWarnings("unchecked")
    void getAppointmentsShouldReturnListOfAppointments() {
        Map<String, Object> appt = Map.of("id", 10, "date", "2026-05-01");

        when(restTemplate.exchange(eq(BASE_URL + "/appointments"), eq(HttpMethod.GET), any(), eq(Map[].class)))
                .thenReturn(ResponseEntity.ok(new Map[]{appt}));

        List<Map<String, Object>> result = backendService.getAppointments(TOKEN);

        assertEquals(1, result.size());
        assertEquals(10, result.get(0).get("id"));
    }

    @Test
    void getAppointmentsShouldReturnEmptyListWhenBodyIsNull() {
        when(restTemplate.exchange(eq(BASE_URL + "/appointments"), eq(HttpMethod.GET), any(), eq(Map[].class)))
                .thenReturn(ResponseEntity.ok(null));

        assertTrue(backendService.getAppointments(TOKEN).isEmpty());
    }

    @Test
    void getAppointmentsShouldPropagateHttpStatusCodeException() {
        when(restTemplate.exchange(eq(BASE_URL + "/appointments"), eq(HttpMethod.GET), any(), eq(Map[].class)))
                .thenThrow(HttpClientErrorException.create(HttpStatus.UNAUTHORIZED, "Unauthorized", null, null, null));

        assertThrows(HttpClientErrorException.class, () -> backendService.getAppointments(TOKEN));
    }

    // -------------------------------------------------------------------------
    // createAppointment
    // -------------------------------------------------------------------------

    @Test
    @SuppressWarnings("unchecked")
    void createAppointmentShouldReturnCreatedAppointment() {
        Map<String, Object> appt = Map.of("date", "2026-05-01");
        Map<String, Object> created = Map.of("id", 11, "date", "2026-05-01");

        when(restTemplate.postForObject(eq(BASE_URL + "/appointments"), any(), eq(Map.class)))
                .thenReturn(created);

        Map<String, Object> result = backendService.createAppointment(TOKEN, appt);

        assertEquals(11, result.get("id"));
    }

    @Test
    void createAppointmentShouldPropagateHttpStatusCodeException() {
        when(restTemplate.postForObject(eq(BASE_URL + "/appointments"), any(), eq(Map.class)))
                .thenThrow(HttpClientErrorException.create(HttpStatus.BAD_REQUEST, "Bad Request", null, null, null));

        assertThrows(HttpClientErrorException.class, () -> backendService.createAppointment(TOKEN, Map.of()));
    }

    // -------------------------------------------------------------------------
    // getPets
    // -------------------------------------------------------------------------

    @Test
    @SuppressWarnings("unchecked")
    void getPetsShouldReturnListOfPets() {
        Map<String, Object> pet = Map.of("id", 5, "name", "Rex");

        when(restTemplate.exchange(eq(BASE_URL + "/pets"), eq(HttpMethod.GET), isNull(), eq(Map[].class)))
                .thenReturn(ResponseEntity.ok(new Map[]{pet}));

        List<Map<String, Object>> result = backendService.getPets();

        assertEquals(1, result.size());
        assertEquals("Rex", result.get(0).get("name"));
    }

    @Test
    void getPetsShouldReturnEmptyListWhenBodyIsNull() {
        when(restTemplate.exchange(eq(BASE_URL + "/pets"), eq(HttpMethod.GET), isNull(), eq(Map[].class)))
                .thenReturn(ResponseEntity.ok(null));

        assertTrue(backendService.getPets().isEmpty());
    }

    @Test
    void getPetsShouldPropagateHttpStatusCodeException() {
        when(restTemplate.exchange(eq(BASE_URL + "/pets"), eq(HttpMethod.GET), isNull(), eq(Map[].class)))
                .thenThrow(HttpClientErrorException.create(HttpStatus.INTERNAL_SERVER_ERROR, "Server Error", null, null, null));

        assertThrows(HttpClientErrorException.class, () -> backendService.getPets());
    }

    // -------------------------------------------------------------------------
    // getAvailablePets
    // -------------------------------------------------------------------------

    @Test
    @SuppressWarnings("unchecked")
    void getAvailablePetsShouldReturnListOfAvailablePets() {
        Map<String, Object> pet = Map.of("id", 6, "status", "available");

        when(restTemplate.exchange(eq(BASE_URL + "/pets/available"), eq(HttpMethod.GET), isNull(), eq(Map[].class)))
                .thenReturn(ResponseEntity.ok(new Map[]{pet}));

        List<Map<String, Object>> result = backendService.getAvailablePets();

        assertEquals(1, result.size());
        assertEquals("available", result.get(0).get("status"));
    }

    @Test
    void getAvailablePetsShouldReturnEmptyListWhenBodyIsNull() {
        when(restTemplate.exchange(eq(BASE_URL + "/pets/available"), eq(HttpMethod.GET), isNull(), eq(Map[].class)))
                .thenReturn(ResponseEntity.ok(null));

        assertTrue(backendService.getAvailablePets().isEmpty());
    }

    // -------------------------------------------------------------------------
    // searchPets
    // -------------------------------------------------------------------------

    @Test
    @SuppressWarnings("unchecked")
    void searchPetsShouldBuildUriWithProvidedParameters() {
        Map<String, Object> pet = Map.of("id", 7, "species", "dog");

        when(restTemplate.exchange(contains("/pets/search"), eq(HttpMethod.GET), isNull(), eq(Map[].class)))
                .thenReturn(ResponseEntity.ok(new Map[]{pet}));

        List<Map<String, Object>> result = backendService.searchPets("dog", null, null, null, null);

        assertEquals(1, result.size());
        assertEquals("dog", result.get(0).get("species"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void searchPetsShouldReturnEmptyListWhenBodyIsNull() {
        when(restTemplate.exchange(contains("/pets/search"), eq(HttpMethod.GET), isNull(), eq(Map[].class)))
                .thenReturn(ResponseEntity.ok(null));

        assertTrue(backendService.searchPets(null, null, null, null, null).isEmpty());
    }

    @Test
    void searchPetsShouldPropagateHttpStatusCodeException() {
        when(restTemplate.exchange(contains("/pets/search"), eq(HttpMethod.GET), isNull(), eq(Map[].class)))
                .thenThrow(HttpClientErrorException.create(HttpStatus.BAD_REQUEST, "Bad Request", null, null, null));

        assertThrows(HttpClientErrorException.class, () -> backendService.searchPets("dog", null, null, null, null));
    }

    // -------------------------------------------------------------------------
    // createPet
    // -------------------------------------------------------------------------

    @Test
    @SuppressWarnings("unchecked")
    void createPetShouldReturnCreatedPet() {
        Map<String, Object> pet = Map.of("name", "Buddy");
        Map<String, Object> created = Map.of("id", 8, "name", "Buddy");

        when(restTemplate.postForObject(eq(BASE_URL + "/pets"), any(), eq(Map.class)))
                .thenReturn(created);

        Map<String, Object> result = backendService.createPet(TOKEN, pet);

        assertEquals(8, result.get("id"));
    }

    @Test
    void createPetShouldPropagateHttpStatusCodeException() {
        when(restTemplate.postForObject(eq(BASE_URL + "/pets"), any(), eq(Map.class)))
                .thenThrow(HttpClientErrorException.create(HttpStatus.BAD_REQUEST, "Bad Request", null, null, null));

        assertThrows(HttpClientErrorException.class, () -> backendService.createPet(TOKEN, Map.of()));
    }

    // -------------------------------------------------------------------------
    // updatePet
    // -------------------------------------------------------------------------

    @Test
    @SuppressWarnings("unchecked")
    void updatePetShouldReturnUpdatedPet() {
        Map<String, Object> pet = Map.of("name", "Buddy Updated");
        Map<String, Object> updated = Map.of("id", 8, "name", "Buddy Updated");

        when(restTemplate.exchange(eq(BASE_URL + "/pets/8"), eq(HttpMethod.PUT), any(), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(updated));

        Map<String, Object> result = backendService.updatePet(TOKEN, 8, pet);

        assertEquals("Buddy Updated", result.get("name"));
    }

    @Test
    void updatePetShouldPropagateHttpStatusCodeException() {
        when(restTemplate.exchange(eq(BASE_URL + "/pets/8"), eq(HttpMethod.PUT), any(), eq(Map.class)))
                .thenThrow(HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found", null, null, null));

        assertThrows(HttpClientErrorException.class, () -> backendService.updatePet(TOKEN, 8, Map.of()));
    }

    // -------------------------------------------------------------------------
    // deletePet
    // -------------------------------------------------------------------------

    @Test
    @SuppressWarnings("unchecked")
    void deletePetShouldReturnResponseBody() {
        Map<String, Object> deleted = Map.of("message", "deleted");

        when(restTemplate.exchange(eq(BASE_URL + "/pets/8"), eq(HttpMethod.DELETE), any(), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(deleted));

        Map<String, Object> result = backendService.deletePet(TOKEN, 8);

        assertEquals("deleted", result.get("message"));
    }

    @Test
    void deletePetShouldPropagateHttpStatusCodeException() {
        when(restTemplate.exchange(eq(BASE_URL + "/pets/8"), eq(HttpMethod.DELETE), any(), eq(Map.class)))
                .thenThrow(HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found", null, null, null));

        assertThrows(HttpClientErrorException.class, () -> backendService.deletePet(TOKEN, 8));
    }

    // -------------------------------------------------------------------------
    // getInvoices
    // -------------------------------------------------------------------------

    @Test
    @SuppressWarnings("unchecked")
    void getInvoicesShouldReturnListOfInvoices() {
        Map<String, Object> invoice = Map.of("id", 100, "total", 50.0);

        when(restTemplate.exchange(eq(BASE_URL + "/invoices"), eq(HttpMethod.GET), any(), eq(Map[].class)))
                .thenReturn(ResponseEntity.ok(new Map[]{invoice}));

        List<Map<String, Object>> result = backendService.getInvoices(TOKEN);

        assertEquals(1, result.size());
        assertEquals(100, result.get(0).get("id"));
    }

    @Test
    void getInvoicesShouldReturnEmptyListWhenBodyIsNull() {
        when(restTemplate.exchange(eq(BASE_URL + "/invoices"), eq(HttpMethod.GET), any(), eq(Map[].class)))
                .thenReturn(ResponseEntity.ok(null));

        assertTrue(backendService.getInvoices(TOKEN).isEmpty());
    }

    @Test
    void getInvoicesShouldPropagateHttpStatusCodeException() {
        when(restTemplate.exchange(eq(BASE_URL + "/invoices"), eq(HttpMethod.GET), any(), eq(Map[].class)))
                .thenThrow(HttpClientErrorException.create(HttpStatus.UNAUTHORIZED, "Unauthorized", null, null, null));

        assertThrows(HttpClientErrorException.class, () -> backendService.getInvoices(TOKEN));
    }

    // -------------------------------------------------------------------------
    // getInvoiceById
    // -------------------------------------------------------------------------

    @Test
    @SuppressWarnings("unchecked")
    void getInvoiceByIdShouldReturnInvoice() {
        Map<String, Object> invoice = Map.of("id", 100L, "total", 50.0);

        when(restTemplate.exchange(eq(BASE_URL + "/invoices/100"), eq(HttpMethod.GET), any(), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(invoice));

        Map<String, Object> result = backendService.getInvoiceById(TOKEN, 100L);

        assertEquals(100L, result.get("id"));
    }

    @Test
    void getInvoiceByIdShouldPropagateHttpStatusCodeException() {
        when(restTemplate.exchange(eq(BASE_URL + "/invoices/100"), eq(HttpMethod.GET), any(), eq(Map.class)))
                .thenThrow(HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found", null, null, null));

        assertThrows(HttpClientErrorException.class, () -> backendService.getInvoiceById(TOKEN, 100L));
    }

    // -------------------------------------------------------------------------
    // getInvoiceByAppointmentId
    // -------------------------------------------------------------------------

    @Test
    @SuppressWarnings("unchecked")
    void getInvoiceByAppointmentIdShouldReturnInvoice() {
        Map<String, Object> invoice = Map.of("id", 100L, "appointmentId", 10L);

        when(restTemplate.exchange(eq(BASE_URL + "/invoices/appointment/10"), eq(HttpMethod.GET), any(), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(invoice));

        Map<String, Object> result = backendService.getInvoiceByAppointmentId(TOKEN, 10L);

        assertEquals(10L, result.get("appointmentId"));
    }

    @Test
    void getInvoiceByAppointmentIdShouldPropagateHttpStatusCodeException() {
        when(restTemplate.exchange(eq(BASE_URL + "/invoices/appointment/10"), eq(HttpMethod.GET), any(), eq(Map.class)))
                .thenThrow(HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found", null, null, null));

        assertThrows(HttpClientErrorException.class, () -> backendService.getInvoiceByAppointmentId(TOKEN, 10L));
    }

    // -------------------------------------------------------------------------
    // createInvoice
    // -------------------------------------------------------------------------

    @Test
    @SuppressWarnings("unchecked")
    void createInvoiceShouldReturnCreatedInvoice() {
        Map<String, Object> request = Map.of("total", 75.0);
        Map<String, Object> created = Map.of("id", 101L, "total", 75.0);

        when(restTemplate.postForObject(eq(BASE_URL + "/invoices/appointments/10"), any(), eq(Map.class)))
                .thenReturn(created);

        Map<String, Object> result = backendService.createInvoice(TOKEN, 10L, request);

        assertEquals(101L, result.get("id"));
    }

    @Test
    void createInvoiceShouldPropagateHttpStatusCodeException() {
        when(restTemplate.postForObject(eq(BASE_URL + "/invoices/appointments/10"), any(), eq(Map.class)))
                .thenThrow(HttpClientErrorException.create(HttpStatus.BAD_REQUEST, "Bad Request", null, null, null));

        assertThrows(HttpClientErrorException.class, () -> backendService.createInvoice(TOKEN, 10L, Map.of()));
    }
}
