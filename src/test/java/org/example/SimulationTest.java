package org.example;

import org.example.domain.Circuit;
import org.example.domain.gates.AndGate;
import org.example.domain.gates.NotGate;
import org.example.domain.gates.OrGate;
import org.example.domain.io.LED;
import org.example.domain.io.Switch;
import org.example.simulations.CircuitSimulator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SimulationTest {

    @Test
    public void testAndGate() {
        Circuit circuit = new Circuit();
        Switch s1 = new Switch(); s1.setId("S1");
        Switch s2 = new Switch(); s2.setId("S2");
        AndGate and = new AndGate();
        LED led = new LED(); led.setId("LED1");

        circuit.add(s1); circuit.add(s2); circuit.add(and); circuit.add(led);

        // Connect: S1 -> AND.IN1, S2 -> AND.IN2, AND.OUT -> LED
        and.getInputs().get(0).connect(s1.getOutputs().get(0));
        and.getInputs().get(1).connect(s2.getOutputs().get(0));
        led.getInputs().get(0).connect(and.getOutputs().get(0));

        CircuitSimulator sim = new CircuitSimulator();

        // 0 0 -> 0
        s1.setOn(false); s2.setOn(false);
        sim.run(circuit);
        assertFalse(led.isLit(), "0 AND 0 should be false");

        // 1 0 -> 0
        s1.setOn(true); s2.setOn(false);
        sim.run(circuit);
        assertFalse(led.isLit(), "1 AND 0 should be false");

        // 1 1 -> 1
        s1.setOn(true); s2.setOn(true);
        sim.run(circuit);
        assertTrue(led.isLit(), "1 AND 1 should be true");
    }

    @Test
    public void testOrGate() {
        Circuit circuit = new Circuit();
        Switch s1 = new Switch();
        Switch s2 = new Switch();
        OrGate or = new OrGate();
        LED led = new LED();

        circuit.add(s1); circuit.add(s2); circuit.add(or); circuit.add(led);

        or.getInputs().get(0).connect(s1.getOutputs().get(0));
        or.getInputs().get(1).connect(s2.getOutputs().get(0));
        led.getInputs().get(0).connect(or.getOutputs().get(0));

        CircuitSimulator sim = new CircuitSimulator();

        s1.setOn(false); s2.setOn(false);
        sim.run(circuit);
        assertFalse(led.isLit(), "0 OR 0 should be false");

        s1.setOn(true); s2.setOn(false);
        sim.run(circuit);
        assertTrue(led.isLit(), "1 OR 0 should be true");
    }

    @Test
    public void testComplexCircuit() {
        Circuit circuit = new Circuit();
        Switch a = new Switch(); a.setId("A");
        Switch b = new Switch(); b.setId("B");
        NotGate not = new NotGate();
        AndGate and = new AndGate();
        LED led = new LED();

        circuit.add(a); circuit.add(b); circuit.add(not); circuit.add(and); circuit.add(led);

        // A -> NOT -> AND.IN1
        not.getInputs().get(0).connect(a.getOutputs().get(0));
        and.getInputs().get(0).connect(not.getOutputs().get(0));

        // B -> AND.IN2
        and.getInputs().get(1).connect(b.getOutputs().get(0));

        // AND -> LED
        led.getInputs().get(0).connect(and.getOutputs().get(0));

        CircuitSimulator sim = new CircuitSimulator();

        // A=0, B=1 => NOT(0) AND 1 => 1 AND 1 => 1
        a.setOn(false); b.setOn(true);
        sim.run(circuit);
        assertTrue(led.isLit(), "NOT(0) AND 1 should be true");

        // A=1, B=1 => NOT(1) AND 1 => 0 AND 1 => 0
        a.setOn(true); b.setOn(true);
        sim.run(circuit);
        assertFalse(led.isLit(), "NOT(1) AND 1 should be false");
    }
}
