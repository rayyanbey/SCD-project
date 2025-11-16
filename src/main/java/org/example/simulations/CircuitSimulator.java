package org.example.simulations;

import org.example.domain.Circuit;
import org.example.domain.Component;
import org.example.domain.Connector;
import org.example.domain.OutputPort;

import java.util.HashSet;
import java.util.Set;

public class CircuitSimulator {
    public void run(Circuit circuit) {

        boolean changed = true;

        while (changed) {
            changed = false;

            // Evaluate all components
            for (Component c : circuit.getComponents()) {

                // Capture outputs before evaluation
                Set<Boolean> before = snapshotOutputs(c);

                c.evaluate();

                // If outputs changed, we must propagate again
                if (!before.equals(snapshotOutputs(c))) {
                    changed = true;
                }
            }

            // Propagate wire values
            for (Connector conn : circuit.getConnectors()) {
                conn.propagate();
            }
        }
    }

    private Set<Boolean> snapshotOutputs(Component c) {
        Set<Boolean> result = new HashSet<>();
        for (OutputPort out : c.getOutputs())
            result.add(out.getValue());
        return result;
    }
}