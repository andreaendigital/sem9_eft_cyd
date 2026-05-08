package com.duoc.seguridadcalidad;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PetControllerTests {

    private final PetController petController = new PetController();

    @Test
    void listPetsShouldReturnPetsView() {
        assertEquals("pets", petController.listPets());
    }

    @Test
    void showCreateFormShouldReturnNewPetView() {
        assertEquals("new_pet", petController.showCreateForm());
    }
}
