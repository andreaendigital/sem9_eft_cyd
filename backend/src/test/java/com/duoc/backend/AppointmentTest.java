package com.duoc.backend;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AppointmentTest {

    /**
     * Validar la integridad estructural de una entidad es importante porque evita
     * que datos corruptos o incompletos se propaguen al resto de la aplicación.
     * En términos de OWASP, una entidad bien probada reduce riesgos de fallos de
     * integridad de datos y comportamientos inesperados en capas de negocio y persistencia.
     */
    @Test
    void shouldPopulateAllFieldsUsingFullConstructorAndIdSetter() {
        LocalDate date = LocalDate.of(2026, 5, 15);
        LocalTime time = LocalTime.of(14, 30);

        Appointment appointment = new Appointment(42, date, time, "Consulta general", "Dr. Perez");
        appointment.setId(7);

        assertEquals(7, appointment.getId());
        assertEquals(42, appointment.getPatientId());
        assertEquals(date, appointment.getDate());
        assertEquals(time, appointment.getTime());
        assertEquals("Consulta general", appointment.getReason());
        assertEquals("Dr. Perez", appointment.getVeterinarian());
    }

    /**
     * Este test cubre el constructor vacío y todos los setters/getters.
     * Eso ayuda a detectar errores en el mapeo de datos antes de que lleguen
     * a controladores, servicios o la base de datos.
     */
    @Test
    void shouldSupportSettersAndGettersWithRealisticTemporalTypes() {
        Appointment appointment = new Appointment();

        LocalDate date = LocalDate.of(2026, 12, 24);
        LocalTime time = LocalTime.of(9, 45);

        appointment.setId(100);
        appointment.setPatientId(55);
        appointment.setDate(date);
        appointment.setTime(time);
        appointment.setReason("Vacunación anual");
        appointment.setVeterinarian("Dra. Gomez");

        assertNotNull(appointment);
        assertEquals(100, appointment.getId());
        assertEquals(55, appointment.getPatientId());
        assertEquals(date, appointment.getDate());
        assertEquals(time, appointment.getTime());
        assertEquals("Vacunación anual", appointment.getReason());
        assertEquals("Dra. Gomez", appointment.getVeterinarian());
    }
}
