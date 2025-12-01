package org.example.simulations;

import org.example.domain.*;
import org.example.domain.gates.*;
import org.example.domain.io.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CircuitSimulator following TDD approach.
 * Tests simple circuits to verify simulation propagation.
 */
class CircuitSimulatorTest {

    private CircuitSimulator simulator;
    private Circuit circuit;

    @BeforeEach
    void setUp() {
        simulator = new CircuitSimulator();
        circuit = new Circuit();
    }

    @Test
    void testSimulator_SimpleSwitch_LEDFollowsSwitch() {
        // Arrange: Switch -> LED
        Switch sw = new Switch();
        LED led = new LED();
        circuit.addComponent(sw);
        circuit.addComponent(led);

        Connector conn = new Connector(sw.getOutputs().get(0), led.getInputs().get(0));
        circuit.addConnector(conn);
        sw.getOutputs().get(0).addConnection(conn);

        // Act: Switch ON
        sw.setOn(true);
        simulator.run(circuit);

        // Assert
        assertTrue(led.isLit(), "LED should be lit when switch is ON");

        // Act: Switch OFF
        sw.setOn(false);
        simulator.run(circuit);

        // Assert
        assertFalse(led.isLit(), "LED should not be lit when switch is OFF");
    }

    @Test
    void testSimulator_AndGate_CorrectOutput() {
        // Arrange: Switch1 -> AND -> LED
        //          Switch2 ->
        Switch sw1 = new Switch();
        Switch sw2 = new Switch();
        AndGate andGate = new AndGate();
        LED led = new LED();

        circuit.addComponent(sw1);
        circuit.addComponent(sw2);
        circuit.addComponent(andGate);
        circuit.addComponent(led);

        Connector c1 = new Connector(sw1.getOutputs().get(0), andGate.getInputs().get(0));
        Connector c2 = new Connector(sw2.getOutputs().get(0), andGate.getInputs().get(1));
        Connector c3 = new Connector(andGate.getOutputs().get(0), led.getInputs().get(0));

        circuit.addConnector(c1);
        circuit.addConnector(c2);
        circuit.addConnector(c3);

        sw1.getOutputs().get(0).addConnection(c1);
        sw2.getOutputs().get(0).addConnection(c2);
        andGate.getOutputs().get(0).addConnection(c3);

        // Test: Both OFF
        sw1.setOn(false);
        sw2.setOn(false);
        simulator.run(circuit);
        assertFalse(led.isLit(), "LED should be OFF when both switches are OFF");

        // Test: One ON
        sw1.setOn(true);
        sw2.setOn(false);
        simulator.run(circuit);
        assertFalse(led.isLit(), "LED should be OFF when only one switch is ON");

        // Test: Both ON
        sw1.setOn(true);
        sw2.setOn(true);
        simulator.run(circuit);
        assertTrue(led.isLit(), "LED should be ON when both switches are ON");
    }

    @Test
    void testSimulator_NotGate_InvertsSignal() {
        // Arrange: Switch -> NOT -> LED
        Switch sw = new Switch();
        NotGate notGate = new NotGate();
        LED led = new LED();

        circuit.addComponent(sw);
        circuit.addComponent(notGate);
        circuit.addComponent(led);

        Connector c1 = new Connector(sw.getOutputs().get(0), notGate.getInputs().get(0));
        Connector c2 = new Connector(notGate.getOutputs().get(0), led.getInputs().get(0));

        circuit.addConnector(c1);
        circuit.addConnector(c2);

        sw.getOutputs().get(0).addConnection(c1);
        notGate.getOutputs().get(0).addConnection(c2);

        // Test: Switch OFF -> LED ON
        sw.setOn(false);
        simulator.run(circuit);
        assertTrue(led.isLit(), "LED should be ON when switch is OFF (inverted)");

        // Test: Switch ON -> LED OFF
        sw.setOn(true);
        simulator.run(circuit);
        assertFalse(led.isLit(), "LED should be OFF when switch is ON (inverted)");
    }

    @Test
    void testSimulator_EmptyCircuit_NoError() {
        // Act & Assert
        assertDoesNotThrow(() -> simulator.run(circuit), "Simulator should handle empty circuit");
    }
}
