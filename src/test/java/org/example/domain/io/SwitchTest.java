package org.example.domain.io;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Switch component following TDD approach.
 */
class SwitchTest {

    private Switch switchComponent;

    @BeforeEach
    void setUp() {
        switchComponent = new Switch();
    }

    @Test
    void testSwitch_InitialState_OutputFalse() {
        // Act
        switchComponent.evaluate();

        // Assert
        assertFalse(switchComponent.getOutputs().get(0).getValue(), "Switch should initially output false");
    }

    @Test
    void testSwitch_SetOn_OutputTrue() {
        // Arrange
        switchComponent.setOn(true);

        // Act
        switchComponent.evaluate();

        // Assert
        assertTrue(switchComponent.getOutputs().get(0).getValue(), "Switch set to ON should output true after evaluate");
    }

    @Test
    void testSwitch_SetOff_OutputFalse() {
        // Arrange
        switchComponent.setOn(false);

        // Act
        switchComponent.evaluate();

        // Assert
        assertFalse(switchComponent.getOutputs().get(0).getValue(), "Switch set to OFF should output false after evaluate");
    }

    @Test
    void testSwitch_ToggleOnOff_OutputChanges() {
        // Arrange & Act & Assert
        switchComponent.setOn(true);
        switchComponent.evaluate();
        assertTrue(switchComponent.getOutputs().get(0).getValue(), "Switch should be ON");

        switchComponent.setOn(false);
        switchComponent.evaluate();
        assertFalse(switchComponent.getOutputs().get(0).getValue(), "Switch should be OFF");
    }

    @Test
    void testSwitch_HasNoInputs() {
        assertEquals(0, switchComponent.getInputs().size(), "Switch should have no inputs");
    }

    @Test
    void testSwitch_HasOneOutput() {
        assertEquals(1, switchComponent.getOutputs().size(), "Switch should have 1 output");
    }
}
