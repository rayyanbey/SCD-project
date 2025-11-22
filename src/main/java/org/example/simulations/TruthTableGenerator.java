package org.example.simulations;

import org.example.domain.Circuit;
import org.example.domain.Component;
import org.example.domain.io.LED;
import org.example.domain.io.Switch;
import org.example.domain.ClockSource;

import java.util.*;

public class TruthTableGenerator {

    private final CircuitSimulator simulator = new CircuitSimulator();

    public List<Map<String, Boolean>> generateTruthTable(Circuit circuit) {

        // Collect all switches = circuit inputs in deterministic order
        List<Switch> inputs = new ArrayList<>();
        for (Component c : circuit.getComponents())
            if (c instanceof Switch sw)
                inputs.add(sw);

        // Sort by component id when available to get deterministic ordering
        inputs.sort(Comparator.comparing(s -> Optional.ofNullable(s.getId()).orElse("")));

        int inputCount = inputs.size();
        int rows = (int) Math.pow(2, inputCount);

        List<Map<String, Boolean>> table = new ArrayList<>();

        for (int r = 0; r < rows; r++) {

            // Prepare a row
            Map<String, Boolean> row = new LinkedHashMap<>();

            // Reset clock sources to a known state to avoid oscillation/non-determinism
            for (Component c : circuit.getComponents()) {
                if (c instanceof ClockSource clk) {
                    clk.reset();
                    clk.setState(false);
                }
            }

            // Set switch values
            for (int i = 0; i < inputCount; i++) {
                boolean value = ((r >> i) & 1) == 1;
                Switch sw = inputs.get(i);
                sw.setOn(value);
                String colName = Optional.ofNullable(sw.getId()).orElse("IN" + i);
                row.put(colName, value);
            }

            // Run simulation
            simulator.run(circuit);

            // Extract LED outputs
            int outIndex = 0;
            for (Component c : circuit.getComponents()) {
                if (c instanceof LED led) {
                    String colName = Optional.ofNullable(led.getId()).orElse("OUT" + outIndex);
                    row.put(colName, led.isLit());
                    outIndex++;
                }
            }

            table.add(row);
        }

        return table;
    }
}