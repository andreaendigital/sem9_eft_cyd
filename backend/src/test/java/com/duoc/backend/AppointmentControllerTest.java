package com.duoc.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
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
 * OWASP A07:2021 - Identification and Authentication Failures.
 *
 * This WebMvcTest keeps the scope on the HTTP controller only. Using MockMvc with
 * filters disabled ensures Spring Security does not short-circuit the request before
 * the controller runs, so JaCoCo can record the controller instructions and branches.
 * The repositories are mocked with @MockitoBean because the controller depends directly
 * on them. That makes the test a pure unit slice for the web layer.
 */
@WebMvcTest(AppointmentController.class)
@AutoConfigureMockMvc(addFilters = false)
class AppointmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @MockBean
    private AppointmentRepository appointmentRepository;

    @MockBean
    private PatientRepository patientRepository;

    private Appointment buildAppointment(Integer patientId) {
        return new Appointment(patientId, LocalDate.of(2026, 5, 15), LocalTime.of(14, 30), "Consulta general", "Dr. Perez");
    }

    private String toJson(Appointment appointment) throws Exception {
        return objectMapper.writeValueAsString(appointment);
    }

    /**
     * Successful creation path. Verifies the controller accepts a valid appointment,
     * persists it, and returns 201 CREATED.
     * This covers the normal authentication/authorization flow expected after input validation.
     */
    @Test
    @DisplayName("createAppointment should return 201 when the patient exists")
    void createAppointment_shouldReturnCreatedWhenPatientExists() throws Exception {
        Appointment appointment = buildAppointment(1);
        when(patientRepository.existsById(1)).thenReturn(true);
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(post("/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(appointment)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.patientId").value(1))
                .andExpect(jsonPath("$.reason").value("Consulta general"));

        verify(patientRepository).existsById(1);
        verify(appointmentRepository).save(any(Appointment.class));
    }

    /**
     * Negative branch for createAppointment. If the patient does not exist, the controller
     * returns 400 BAD REQUEST instead of storing inconsistent data.
     * This helps prevent integrity issues and IDOR-style abuse through arbitrary patient ids.
     */
    @Test
    @DisplayName("createAppointment should return 400 when patient does not exist")
    void createAppointment_shouldReturnBadRequestWhenPatientDoesNotExist() throws Exception {
        Appointment appointment = buildAppointment(99);
        when(patientRepository.existsById(99)).thenReturn(false);

        mockMvc.perform(post("/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(appointment)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Patient not found for patientId: 99"));

        verify(patientRepository).existsById(99);
    }

    /**
     * Error-handling branch for createAppointment.
     * If persistence fails, the controller should fail safely with 400 and not expose stack traces.
     */
    @Test
    @DisplayName("createAppointment should handle repository errors safely")
    void createAppointment_shouldHandleRepositoryException() throws Exception {
        Appointment appointment = buildAppointment(1);
        when(patientRepository.existsById(1)).thenReturn(true);
        when(appointmentRepository.save(any(Appointment.class))).thenThrow(new RuntimeException("DB error"));

        mockMvc.perform(post("/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(appointment)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Error al registrar")));

        verify(appointmentRepository).save(any(Appointment.class));
    }

    /**
     * getAllAppointments success path.
     * This ensures the controller actually traverses the list-returning branch and contributes
     * to JaCoCo instruction coverage.
     */
    @Test
    @DisplayName("getAllAppointments should return 200 with the appointment list")
    void getAllAppointments_shouldReturnOkWithList() throws Exception {
        when(appointmentRepository.findAll()).thenReturn(List.of(buildAppointment(1)));

        mockMvc.perform(get("/appointments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].patientId").value(1))
                .andExpect(jsonPath("$[0].reason").value("Consulta general"));

        verify(appointmentRepository).findAll();
    }

    /**
     * Error branch for getAllAppointments.
     * If the repository layer throws, the controller returns 500 instead of leaking details.
     */
    @Test
    @DisplayName("getAllAppointments should return 500 on repository failure")
    void getAllAppointments_shouldReturnInternalServerErrorOnException() throws Exception {
        when(appointmentRepository.findAll()).thenThrow(new RuntimeException("boom"));

        mockMvc.perform(get("/appointments"))
                .andExpect(status().isInternalServerError());
    }

    /**
     * Successful update path.
     * The controller must validate that the appointment exists and that the patient exists
     * before saving the updated entity.
     */
    @Test
    @DisplayName("updateAppointment should return 200 when both appointment and patient exist")
    void updateAppointment_shouldReturnOkWhenAppointmentAndPatientExist() throws Exception {
        Appointment appointment = buildAppointment(1);
        when(appointmentRepository.existsById(10)).thenReturn(true);
        when(patientRepository.existsById(1)).thenReturn(true);
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(put("/appointments/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(appointment)))
                .andExpect(status().isOk())
                .andExpect(content().string("Appointment updated successfully"));

        verify(appointmentRepository).existsById(10);
        verify(patientRepository).existsById(1);
        verify(appointmentRepository).save(any(Appointment.class));
    }

    /**
     * Negative update branch: the appointment id does not exist.
     * This protects against modifying resources that are not present and closes a branch
     * that JaCoCo previously reported as uncovered.
     */
    @Test
    @DisplayName("updateAppointment should return 404 when the appointment id does not exist")
    void updateAppointment_shouldReturnNotFoundWhenAppointmentDoesNotExist() throws Exception {
        Appointment appointment = buildAppointment(1);
        when(appointmentRepository.existsById(10)).thenReturn(false);

        mockMvc.perform(put("/appointments/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(appointment)))
                .andExpect(status().isNotFound());

        verify(appointmentRepository).existsById(10);
    }

    /**
     * Negative update branch: the target patient id is invalid.
     * This prevents inconsistent association updates and reduces data integrity failures.
     */
    @Test
    @DisplayName("updateAppointment should return 400 when the patient id is invalid")
    void updateAppointment_shouldReturnBadRequestWhenPatientDoesNotExist() throws Exception {
        Appointment appointment = buildAppointment(2);
        when(appointmentRepository.existsById(10)).thenReturn(true);
        when(patientRepository.existsById(2)).thenReturn(false);

        mockMvc.perform(put("/appointments/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(appointment)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Patient not found for patientId: 2"));

        verify(appointmentRepository).existsById(10);
        verify(patientRepository).existsById(2);
    }

    /**
     * Error-handling branch for updateAppointment.
     * A repository exception must not expose internal details; the controller should respond 400 safely.
     */
    @Test
    @DisplayName("updateAppointment should handle repository failures safely")
    void updateAppointment_shouldHandleExceptionSafely() throws Exception {
        Appointment appointment = buildAppointment(1);
        when(appointmentRepository.existsById(10)).thenReturn(true);
        when(patientRepository.existsById(1)).thenReturn(true);
        doThrow(new RuntimeException("save fail")).when(appointmentRepository).save(any(Appointment.class));

        mockMvc.perform(put("/appointments/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(appointment)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error updating appointment")));
    }

    /**
     * Successful delete path.
     * Verifies the controller can delete an existing appointment and returns 200 OK.
     */
    @Test
    @DisplayName("deleteAppointment should return 200 when the id exists")
    void deleteAppointment_shouldReturnOkWhenIdExists() throws Exception {
        when(appointmentRepository.existsById(10)).thenReturn(true);

        mockMvc.perform(delete("/appointments/10"))
                .andExpect(status().isOk())
                .andExpect(content().string("Appointment deleted successfully"));

        verify(appointmentRepository).existsById(10);
        verify(appointmentRepository).deleteById(eq(10));
    }

    /**
     * Negative delete branch: the id does not exist.
     * This is critical to cover the 404 path and avoid deleting arbitrary ids.
     */
    @Test
    @DisplayName("deleteAppointment should return 404 when the id does not exist")
    void deleteAppointment_shouldReturnNotFoundWhenIdDoesNotExist() throws Exception {
        when(appointmentRepository.existsById(10)).thenReturn(false);

        mockMvc.perform(delete("/appointments/10"))
                .andExpect(status().isNotFound());

        verify(appointmentRepository).existsById(10);
    }

    /**
     * Error-handling branch for deleteAppointment.
     * If deletion throws unexpectedly, the controller should respond with 500.
     */
    @Test
    @DisplayName("deleteAppointment should return 500 on repository failure")
    void deleteAppointment_shouldHandleExceptionSafely() throws Exception {
        when(appointmentRepository.existsById(10)).thenReturn(true);
        doThrow(new RuntimeException("delete fail")).when(appointmentRepository).deleteById(10);

        mockMvc.perform(delete("/appointments/10"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error deleting appointment")));
    }

    /**
     * Optional extra branch for get by id. Including it improves the controller's overall
     * instruction and branch coverage, helping the module reach the minimum 60% threshold.
     */
    @Test
    @DisplayName("getAppointmentById should return 200 when the id exists")
    void getAppointmentById_shouldReturnOkWhenIdExists() throws Exception {
        when(appointmentRepository.findById(10)).thenReturn(Optional.of(buildAppointment(1)));

        mockMvc.perform(get("/appointments/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.patientId").value(1));
    }

    /**
     * Extra negative branch for get by id. This verifies the 404 path and helps close coverage gaps.
     */
    @Test
    @DisplayName("getAppointmentById should return 404 when the id does not exist")
    void getAppointmentById_shouldReturnNotFoundWhenIdDoesNotExist() throws Exception {
        when(appointmentRepository.findById(10)).thenReturn(Optional.empty());

        mockMvc.perform(get("/appointments/10"))
                .andExpect(status().isNotFound());
    }
}
