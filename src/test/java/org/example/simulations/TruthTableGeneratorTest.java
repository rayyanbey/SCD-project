package org.example.simulations;

import org.example.domain.*;
import org.example.domain.gates.*;
import org.example.domain.io.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;

/**
 * Unit tests for TruthTableGenerator following TDD approach.
 */
class TruthTableGeneratorTest {

    private TruthTableGenerator generator;
    private Circuit circuit;

    @BeforeEach
    void setUp() {
        generator = new TruthTableGenerator();
        circuit = new Circuit();
    }

    @Test
    void testTruthTable_SingleSwitch_TwoRows() {
        // Arrange: Switch -> LED
        Switch sw = new Switch();
        LED led = new LED();
        sw.setId("1");
        led.setId("2");

        circuit.addComponent(sw);
        circuit.addComponent(led);

        Connector conn = new Connector(sw.getOutputs().get(0), led.getInputs().get(0));
        circuit.addConnector(conn);
        sw.getOutputs().get(0).addConnection(conn);

        // Act
        List<Map<String, Boolean>> table = generator.generateTruthTable(circuit);

        // Assert
        assertEquals(2, table.size(), "Truth table should have 2 rows for 1 input");
        
        // Row 0: Switch OFF
        assertFalse(table.get(0).get("1"), "Row 0: Switch should be OFF");
        assertFalse(table.get(0).get("2"), "Row 0: LED should be OFF");
        
        // Row 1: Switch ON
        assertTrue(table.get(1).get("1"), "Row 1: Switch should be ON");
        assertTrue(table.get(1).get("2"), "Row 1: LED should be ON");
    }

    @Test
    void testTruthTable_AndGate_FourRows() {
        // Arrange: Switch1 -> AND -> LED
        //          Switch2 ->
        Switch sw1 = new Switch();
        Switch sw2 = new Switch();
        AndGate andGate = new AndGate();
        LED led = new LED();

        sw1.setId("A");
        sw2.setId("B");
        led.setId("OUT");

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

        // Act
        List<Map<String, Boolean>> table = generator.generateTruthTable(circuit);

        // Assert
        assertEquals(4, table.size(), "Truth table should have 4 rows for 2 inputs");

        // Verify AND gate truth table
        // Row 0: 0, 0 -> 0
        assertFalse(table.get(0).get("A"));
        assertFalse(table.get(0).get("B"));
        assertFalse(table.get(0).get("OUT"), "AND(0,0) = 0");

        // Row 1: 1, 0 -> 0
        assertTrue(table.get(1).get("A"));
        assertFalse(table.get(1).get("B"));
        assertFalse(table.get(1).get("OUT"), "AND(1,0) = 0");

        // Row 2: 0, 1 -> 0
        assertFalse(table.get(2).get("A"));
        assertTrue(table.get(2).get("B"));
        assertFalse(table.get(2).get("OUT"), "AND(0,1) = 0");

        // Row 3: 1, 1 -> 1
        assertTrue(table.get(3).get("A"));
        assertTrue(table.get(3).get("B"));
        assertTrue(table.get(3).get("OUT"), "AND(1,1) = 1");
    }

    @Test
    void testTruthTable_XorGate_CorrectOutput() {
        // Arrange: Switch1 -> XOR -> LED
        //          Switch2 ->
        Switch sw1 = new Switch();
        Switch sw2 = new Switch();
        XorGate xorGate = new XorGate();
        LED led = new LED();

        sw1.setId("A");
        sw2.setId("B");
        led.setId("OUT");

        circuit.addComponent(sw1);
        circuit.addComponent(sw2);
        circuit.addComponent(xorGate);
        circuit.addComponent(led);

        Connector c1 = new Connector(sw1.getOutputs().get(0), xorGate.getInputs().get(0));
        Connector c2 = new Connector(sw2.getOutputs().get(0), xorGate.getInputs().get(1));
        Connector c3 = new Connector(xorGate.getOutputs().get(0), led.getInputs().get(0));

        circuit.addConnector(c1);
        circuit.addConnector(c2);
        circuit.addConnector(c3);

        sw1.getOutputs().get(0).addConnection(c1);
        sw2.getOutputs().get(0).addConnection(c2);
        xorGate.getOutputs().get(0).addConnection(c3);

        // Act
        List<Map<String, Boolean>> table = generator.generateTruthTable(circuit);

        // Assert - XOR truth table
        assertFalse(table.get(0).get("OUT"), "XOR(0,0) = 0");
        assertTrue(table.get(1).get("OUT"), "XOR(1,0) = 1");
        assertTrue(table.get(2).get("OUT"), "XOR(0,1) = 1");
        assertFalse(table.get(3).get("OUT"), "XOR(1,1) = 0");
    }

    @Test
    void testTruthTable_NoInputs_EmptyTable() {
        // Arrange: Circuit with no switches
        LED led = new LED();
        circuit.addComponent(led);

        // Act
        List<Map<String, Boolean>> table = generator.generateTruthTable(circuit);

        // Assert
        assertEquals(1, table.size(), "Truth table should have 1 row for 0 inputs");
    }
}
