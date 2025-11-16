package org.example.views;

import org.example.domain.Circuit;
import org.example.domain.Component;
import org.example.domain.io.LED;
import org.example.services.SimulationService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;


/**
 * Right-hand panel with simulate and truth table controls.
 */
public class SimulationPanel extends JPanel {

    private final JTextArea outputArea = new JTextArea();
    private final JButton simulateBtn = new JButton("Simulate");
    private final JButton truthBtn = new JButton("Truth Table");

    private SimulationService simulationService;
    private long circuitId;

    private Runnable onSimulateCallback;

    public SimulationPanel(long circuitId) {
        this.circuitId = circuitId;
        this.simulationService = new SimulationService();

        setPreferredSize(new Dimension(260, 800));
        setLayout(new BorderLayout());

        JPanel top = new JPanel(new FlowLayout());
        top.add(simulateBtn);
        top.add(truthBtn);
        add(top, BorderLayout.NORTH);

        add(new JScrollPane(outputArea), BorderLayout.CENTER);

        // SIMULATE button ▶ calls internal simulation AND external callback (canvas)
        simulateBtn.addActionListener(e -> {
            runSimulation();
            if (onSimulateCallback != null) {
                onSimulateCallback.run();  // Notify CircuitEditorView
            }
        });

        // TRUTH TABLE button
        truthBtn.addActionListener(e -> showTruthTable());
    }

    /**
     * Allows MainWindow to wire this panel to the CircuitEditorView.
     */
    public void setOnSimulate(Runnable callback) {
        this.onSimulateCallback = callback;
    }

    /**
     * Runs backend simulation through SimulationService and prints LED results.
     */
    private void runSimulation() {
        outputArea.setText("");

        Circuit domainCircuit = simulationService.runSimulation(circuitId);

        domainCircuit.getComponents().stream()
                .filter(c -> c instanceof org.example.domain.io.LED)
                .forEach(c -> {
                    boolean lit = ((org.example.domain.io.LED) c).isLit();
                    outputArea.append("LED " + c.getId() + ": " + lit + "\n");
                });
    }

    /**
     * Display truth table in a JTable popup window.
     */
    private void showTruthTable() {
        List<Map<String, Boolean>> table = simulationService.generateTruthTable(circuitId);

        if (table == null || table.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No valid inputs or LEDs found.");
            return;
        }

        Map<String, Boolean> first = table.get(0);
        String[] cols = first.keySet().toArray(new String[0]);
        Object[][] data = new Object[table.size()][cols.length];

        for (int r = 0; r < table.size(); r++) {
            Map<String, Boolean> row = table.get(r);
            for (int c = 0; c < cols.length; c++) {
                data[r][c] = row.get(cols[c]);
            }
        }

        new TruthTableView(data, cols);
    }
}