package org.example.domain.io;

import org.example.domain.InputPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for LED component following TDD approach.
 */
class LEDTest {

    private LED led;

    @BeforeEach
    void setUp() {
        led = new LED();
    }

    @Test
    void testLED_InitialState_NotLit() {
        assertFalse(led.isLit(), "LED should initially not be lit");
    }

    @Test
    void testLED_InputFalse_NotLit() {
        // Arrange
        led.getInputs().get(0).setValue(false);

        // Act
        led.evaluate();

        // Assert
        assertFalse(led.isLit(), "LED with false input should not be lit");
    }

    @Test
    void testLED_InputTrue_Lit() {
        // Arrange
        led.getInputs().get(0).setValue(true);

        // Act
        led.evaluate();

        // Assert
        assertTrue(led.isLit(), "LED with true input should be lit");
    }

    @Test
    void testLED_ToggleInput_StateChanges() {
        // Arrange & Act & Assert
        led.getInputs().get(0).setValue(true);
        led.evaluate();
        assertTrue(led.isLit(), "LED should be lit");

        led.getInputs().get(0).setValue(false);
        led.evaluate();
        assertFalse(led.isLit(), "LED should not be lit");
    }

    @Test
    void testLED_HasOneInput() {
        assertEquals(1, led.getInputs().size(), "LED should have 1 input");
    }

    @Test
    void testLED_HasNoOutputs() {
        assertEquals(0, led.getOutputs().size(), "LED should have no outputs");
    }
}
