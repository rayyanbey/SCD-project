package org.example.domain.gates;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for OrGate following TDD approach.
 */
class OrGateTest {

    private OrGate gate;

    @BeforeEach
    void setUp() {
        gate = new OrGate();
    }

    @Test
    void testOrGate_BothInputsFalse_OutputFalse() {
        // Arrange
        gate.getInputs().get(0).setValue(false);
        gate.getInputs().get(1).setValue(false);

        // Act
        gate.evaluate();

        // Assert
        assertFalse(gate.getOutputs().get(0).getValue(), "OR gate with both inputs false should output false");
    }

    @Test
    void testOrGate_FirstInputTrue_OutputTrue() {
        // Arrange
        gate.getInputs().get(0).setValue(true);
        gate.getInputs().get(1).setValue(false);

        // Act
        gate.evaluate();

        // Assert
        assertTrue(gate.getOutputs().get(0).getValue(), "OR gate with one input true should output true");
    }

    @Test
    void testOrGate_SecondInputTrue_OutputTrue() {
        // Arrange
        gate.getInputs().get(0).setValue(false);
        gate.getInputs().get(1).setValue(true);

        // Act
        gate.evaluate();

        // Assert
        assertTrue(gate.getOutputs().get(0).getValue(), "OR gate with one input true should output true");
    }

    @Test
    void testOrGate_BothInputsTrue_OutputTrue() {
        // Arrange
        gate.getInputs().get(0).setValue(true);
        gate.getInputs().get(1).setValue(true);

        // Act
        gate.evaluate();

        // Assert
        assertTrue(gate.getOutputs().get(0).getValue(), "OR gate with both inputs true should output true");
    }

    @Test
    void testOrGate_HasTwoInputs() {
        assertEquals(2, gate.getInputs().size(), "OR gate should have 2 inputs");
    }

    @Test
    void testOrGate_HasOneOutput() {
        assertEquals(1, gate.getOutputs().size(), "OR gate should have 1 output");
    }
}
