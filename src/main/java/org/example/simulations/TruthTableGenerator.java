package org.example.simulations;

import org.example.domain.Circuit;
import org.example.domain.Component;
import org.example.domain.io.LED;
import org.example.domain.io.Switch;

import java.util.*;

public class TruthTableGenerator {

    private final CircuitSimulator simulator = new CircuitSimulator();

    public List<Map<String, Boolean>> generateTruthTable(Circuit circuit) {

        // Collect all switches = circuit inputs
        List<Switch> inputs = new ArrayList<>();
        for (Component c : circuit.getComponents())
            if (c instanceof Switch sw)
                inputs.add(sw);

        int inputCount = inputs.size();
        int rows = (int) Math.pow(2, inputCount);

        List<Map<String, Boolean>> table = new ArrayList<>();

        for (int r = 0; r < rows; r++) {

            // Prepare a row
            Map<String, Boolean> row = new LinkedHashMap<>();

            // Set switch values
            for (int i = 0; i < inputCount; i++) {
                boolean value = ((r >> i) & 1) == 1;
                inputs.get(i).setOn(value);
                row.put("IN" + i, value);
            }

            // Run simulation
            simulator.run(circuit);

            // Extract LED outputs
            int outIndex = 0;
            for (Component c : circuit.getComponents()) {
                if (c instanceof LED led) {
                    row.put("OUT" + outIndex, led.isLit());
                    outIndex++;
                }
            }

            table.add(row);
        }

        return table;
    }
}