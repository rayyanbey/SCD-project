package org.example.domain.gates;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for XorGate following TDD approach.
 */
class XorGateTest {

    private XorGate gate;

    @BeforeEach
    void setUp() {
        gate = new XorGate();
    }

    @Test
    void testXorGate_BothInputsFalse_OutputFalse() {
        // Arrange
        gate.getInputs().get(0).setValue(false);
        gate.getInputs().get(1).setValue(false);

        // Act
        gate.evaluate();

        // Assert
        assertFalse(gate.getOutputs().get(0).getValue(), "XOR gate with both inputs false should output false");
    }

    @Test
    void testXorGate_FirstInputTrue_OutputTrue() {
        // Arrange
        gate.getInputs().get(0).setValue(true);
        gate.getInputs().get(1).setValue(false);

        // Act
        gate.evaluate();

        // Assert
        assertTrue(gate.getOutputs().get(0).getValue(), "XOR gate with one input true should output true");
    }

    @Test
    void testXorGate_SecondInputTrue_OutputTrue() {
        // Arrange
        gate.getInputs().get(0).setValue(false);
        gate.getInputs().get(1).setValue(true);

        // Act
        gate.evaluate();

        // Assert
        assertTrue(gate.getOutputs().get(0).getValue(), "XOR gate with one input true should output true");
    }

    @Test
    void testXorGate_BothInputsTrue_OutputFalse() {
        // Arrange
        gate.getInputs().get(0).setValue(true);
        gate.getInputs().get(1).setValue(true);

        // Act
        gate.evaluate();

        // Assert
        assertFalse(gate.getOutputs().get(0).getValue(), "XOR gate with both inputs true should output false");
    }

    @Test
    void testXorGate_HasTwoInputs() {
        assertEquals(2, gate.getInputs().size(), "XOR gate should have 2 inputs");
    }

    @Test
    void testXorGate_HasOneOutput() {
        assertEquals(1, gate.getOutputs().size(), "XOR gate should have 1 output");
    }
}
