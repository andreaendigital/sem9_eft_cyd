package com.duoc.seguridadcalidad;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Entity integrity test.
 * Validating constructor and accessors is important because corrupted entity state can
 * cascade into authorization, persistence and business rule failures elsewhere in the app.
 */
class AppointmentTest {

    @Test
    @DisplayName("constructor should populate all fields")
    void constructor_shouldPopulateAllFields() {
        LocalDate date = LocalDate.of(2026, 6, 1);
        LocalTime time = LocalTime.of(9, 15);

        Appointment appointment = new Appointment(5L, 22L, date, time, "Control anual", "Dr. Mora");

        assertNotNull(appointment);
        assertEquals(5L, appointment.getId());
        assertEquals(22L, appointment.getPatientId());
        assertEquals(date, appointment.getDate());
        assertEquals(time, appointment.getTime());
        assertEquals("Control anual", appointment.getReason());
        assertEquals("Dr. Mora", appointment.getVeterinarian());
    }

    @Test
    @DisplayName("setters and getters should preserve values")
    void settersAndGetters_shouldPreserveValues() {
        Appointment appointment = new Appointment();
        LocalDate date = LocalDate.of(2026, 12, 24);
        LocalTime time = LocalTime.of(14, 30);

        appointment.setId(9L);
        appointment.setPatientId(33L);
        appointment.setDate(date);
        appointment.setTime(time);
        appointment.setReason("Vacunacion");
        appointment.setVeterinarian("Dra. Rojas");

        assertEquals(9L, appointment.getId());
        assertEquals(33L, appointment.getPatientId());
        assertEquals(date, appointment.getDate());
        assertEquals(time, appointment.getTime());
        assertEquals("Vacunacion", appointment.getReason());
        assertEquals("Dra. Rojas", appointment.getVeterinarian());
    }
}
