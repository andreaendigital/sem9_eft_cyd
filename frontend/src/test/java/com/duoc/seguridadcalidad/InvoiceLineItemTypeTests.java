package com.duoc.seguridadcalidad;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InvoiceLineItemTypeTests {

    @Test
    void enumShouldHaveExactlyThreeValues() {
        assertEquals(3, InvoiceLineItemType.values().length);
    }

    @Test
    void enumShouldContainServiceValue() {
        assertNotNull(InvoiceLineItemType.valueOf("SERVICE"));
    }

    @Test
    void enumShouldContainMedicationValue() {
        assertNotNull(InvoiceLineItemType.valueOf("MEDICATION"));
    }

    @Test
    void enumShouldContainAdditionalChargeValue() {
        assertNotNull(InvoiceLineItemType.valueOf("ADDITIONAL_CHARGE"));
    }

    @Test
    void valueOfShouldThrowExceptionForUnknownName() {
        assertThrows(IllegalArgumentException.class, () -> InvoiceLineItemType.valueOf("UNKNOWN"));
    }

    @Test
    void ordinalsShouldBeInDeclarationOrder() {
        assertEquals(0, InvoiceLineItemType.SERVICE.ordinal());
        assertEquals(1, InvoiceLineItemType.MEDICATION.ordinal());
        assertEquals(2, InvoiceLineItemType.ADDITIONAL_CHARGE.ordinal());
    }

    @Test
    void nameShouldMatchConstantName() {
        assertEquals("SERVICE", InvoiceLineItemType.SERVICE.name());
        assertEquals("MEDICATION", InvoiceLineItemType.MEDICATION.name());
        assertEquals("ADDITIONAL_CHARGE", InvoiceLineItemType.ADDITIONAL_CHARGE.name());
    }
}
