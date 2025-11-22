package org.example.simulations;

import org.example.domain.Circuit;
import org.example.domain.Component;
import org.example.domain.Connector;
import org.example.domain.OutputPort;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Queue;
import java.util.Set;
import java.util.HashSet;
import java.util.List;

public class CircuitSimulator {
    public void run(Circuit circuit) {

        int maxIterations = 100000; // safety cap
        int iter = 0;

        Queue<Component> queue = new ArrayDeque<>();
        Set<Component> inQueue = new HashSet<>();

        // Initially enqueue all components (ensures stable state after inputs change)
        for (Component c : circuit.getComponents()) {
            queue.add(c);
            inQueue.add(c);
        }

        while (!queue.isEmpty() && iter < maxIterations) {
            iter++;
            Component c = queue.poll();
            inQueue.remove(c);

            // snapshot outputs before evaluation
            List<Boolean> before = snapshotOutputs(c);

            c.evaluate();

            // if outputs changed, propagate and enqueue affected components
            if (!before.equals(snapshotOutputs(c))) {
                for (OutputPort out : c.getOutputs()) {
                    // propagate along each connector and enqueue destination components
                    for (Connector conn : out.getConnections()) {
                        conn.propagate();
                        Component dest = conn.getDest().getParent();
                        if (!inQueue.contains(dest)) {
                            queue.add(dest);
                            inQueue.add(dest);
                        }
                    }
                }
            }
        }

        if (iter >= maxIterations) {
            System.err.println("Circuit simulation stopped after reaching max iterations (possible oscillation).");
        }
    }

    private List<Boolean> snapshotOutputs(Component c) {
        List<Boolean> result = new ArrayList<>();
        for (OutputPort out : c.getOutputs())
            result.add(out.getValue());
        return result;
    }
}