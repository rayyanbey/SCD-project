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

    private final SimulationService simulationService;
    private final long circuitId;

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

        simulateBtn.addActionListener(e -> {
            runSimulation();
            if (onSimulateCallback != null) onSimulateCallback.run();
        });

        truthBtn.addActionListener(e -> showTruthTable());
    }

    public void setOnSimulate(Runnable r) {
        this.onSimulateCallback = r;
    }

    private void runSimulation() {
        outputArea.setText("");
        Circuit domain = simulationService.runSimulation(circuitId);
        domain.getComponents().stream()
                .filter(c -> c instanceof org.example.domain.io.LED)
                .forEach(c -> {
                    boolean lit = ((org.example.domain.io.LED) c).isLit();
                    outputArea.append("LED " + c.getId() + " = " + lit + "\n");
                });
    }

    private void showTruthTable() {
        List<Map<String, Boolean>> table = simulationService.generateTruthTable(circuitId);
        if (table == null || table.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No inputs or outputs detected.");
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

    public void displayOutput(Circuit domainCircuit) {
        outputArea.setText("");

        domainCircuit.getComponents().stream()
                .filter(c -> c instanceof org.example.domain.io.LED)
                .forEach(c -> {
                    boolean lit = ((org.example.domain.io.LED) c).isLit();
                    outputArea.append("LED " + c.getId() + " = " + lit + "\n");
                });
    }

}