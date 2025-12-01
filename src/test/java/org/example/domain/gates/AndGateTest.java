package org.example.domain.gates;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AndGate following TDD approach.
 */
class AndGateTest {

    private AndGate gate;

    @BeforeEach
    void setUp() {
        gate = new AndGate();
    }

    @Test
    void testAndGate_BothInputsFalse_OutputFalse() {
        // Arrange
        gate.getInputs().get(0).setValue(false);
        gate.getInputs().get(1).setValue(false);

        // Act
        gate.evaluate();

        // Assert
        assertFalse(gate.getOutputs().get(0).getValue(), "AND gate with both inputs false should output false");
    }

    @Test
    void testAndGate_FirstInputTrue_OutputFalse() {
        // Arrange
        gate.getInputs().get(0).setValue(true);
        gate.getInputs().get(1).setValue(false);

        // Act
        gate.evaluate();

        // Assert
        assertFalse(gate.getOutputs().get(0).getValue(), "AND gate with one input true should output false");
    }

    @Test
    void testAndGate_SecondInputTrue_OutputFalse() {
        // Arrange
        gate.getInputs().get(0).setValue(false);
        gate.getInputs().get(1).setValue(true);

        // Act
        gate.evaluate();

        // Assert
        assertFalse(gate.getOutputs().get(0).getValue(), "AND gate with one input true should output false");
    }

    @Test
    void testAndGate_BothInputsTrue_OutputTrue() {
        // Arrange
        gate.getInputs().get(0).setValue(true);
        gate.getInputs().get(1).setValue(true);

        // Act
        gate.evaluate();

        // Assert
        assertTrue(gate.getOutputs().get(0).getValue(), "AND gate with both inputs true should output true");
    }

    @Test
    void testAndGate_HasTwoInputs() {
        assertEquals(2, gate.getInputs().size(), "AND gate should have 2 inputs");
    }

    @Test
    void testAndGate_HasOneOutput() {
        assertEquals(1, gate.getOutputs().size(), "AND gate should have 1 output");
    }
}
