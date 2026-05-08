package com.duoc.seguridadcalidad;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PatientRepositoryTests {

    private PatientRepository repository;

    @BeforeEach
    void setUp() {
        repository = new PatientRepository();
    }

    @Test
    void findAllShouldReturnEmptyListWhenNoPatientsSaved() {
        List<Patient> result = repository.findAll();
        assertTrue(result.isEmpty());
    }

    @Test
    void saveShouldAssignIdWhenPatientHasNoId() {
        Patient patient = new Patient(null, "Rex", "dog", "Labrador", 3, "John");
        Patient saved = repository.save(patient);
        assertNotNull(saved.getId());
    }

    @Test
    void saveShouldAssignSequentialIdsToMultiplePatients() {
        Patient p1 = repository.save(new Patient(null, "Rex", "dog", "Labrador", 3, "John"));
        Patient p2 = repository.save(new Patient(null, "Luna", "cat", "Siamese", 2, "Jane"));
        assertEquals(p1.getId() + 1, p2.getId());
    }

    @Test
    void saveShouldNotOverwriteExistingId() {
        Patient patient = new Patient(99L, "Rex", "dog", "Labrador", 3, "John");
        Patient saved = repository.save(patient);
        assertEquals(99L, saved.getId());
    }

    @Test
    void saveShouldAddPatientToRepository() {
        repository.save(new Patient(null, "Rex", "dog", "Labrador", 3, "John"));
        assertEquals(1, repository.findAll().size());
    }

    @Test
    void findAllShouldReturnAllSavedPatients() {
        repository.save(new Patient(null, "Rex", "dog", "Labrador", 3, "John"));
        repository.save(new Patient(null, "Luna", "cat", "Siamese", 2, "Jane"));
        assertEquals(2, repository.findAll().size());
    }

    @Test
    void findAllShouldReturnDefensiveCopy() {
        repository.save(new Patient(null, "Rex", "dog", "Labrador", 3, "John"));
        List<Patient> first = repository.findAll();
        List<Patient> second = repository.findAll();
        assertNotSame(first, second);
    }

    @Test
    void modifyingReturnedListShouldNotAffectRepository() {
        repository.save(new Patient(null, "Rex", "dog", "Labrador", 3, "John"));
        List<Patient> list = repository.findAll();
        list.clear();
        assertEquals(1, repository.findAll().size());
    }

    @Test
    void saveShouldReturnTheSamePatientInstance() {
        Patient patient = new Patient(null, "Rex", "dog", "Labrador", 3, "John");
        Patient saved = repository.save(patient);
        assertSame(patient, saved);
    }
}
