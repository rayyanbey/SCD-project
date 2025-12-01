package org.example.domain.gates;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for NotGate following TDD approach.
 */
class NotGateTest {

    private NotGate gate;

    @BeforeEach
    void setUp() {
        gate = new NotGate();
    }

    @Test
    void testNotGate_InputFalse_OutputTrue() {
        // Arrange
        gate.getInputs().get(0).setValue(false);

        // Act
        gate.evaluate();

        // Assert
        assertTrue(gate.getOutputs().get(0).getValue(), "NOT gate with false input should output true");
    }

    @Test
    void testNotGate_InputTrue_OutputFalse() {
        // Arrange
        gate.getInputs().get(0).setValue(true);

        // Act
        gate.evaluate();

        // Assert
        assertFalse(gate.getOutputs().get(0).getValue(), "NOT gate with true input should output false");
    }

    @Test
    void testNotGate_HasOneInput() {
        assertEquals(1, gate.getInputs().size(), "NOT gate should have 1 input");
    }

    @Test
    void testNotGate_HasOneOutput() {
        assertEquals(1, gate.getOutputs().size(), "NOT gate should have 1 output");
    }
}
