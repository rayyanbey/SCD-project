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
    private final JButton exportBtn = new JButton("Export Img");

    private final SimulationService simulationService;
    private final long circuitId;

    private Runnable onSimulateCallback;
    private Runnable onExportCallback;

    public SimulationPanel(long circuitId) {
        this.circuitId = circuitId;
        this.simulationService = new SimulationService();

        setPreferredSize(new Dimension(260, 800));
        setLayout(new BorderLayout());

        JPanel top = new JPanel(new FlowLayout());
        top.add(simulateBtn);
        top.add(truthBtn);
        top.add(exportBtn);
        add(top, BorderLayout.NORTH);
        add(new JScrollPane(outputArea), BorderLayout.CENTER);

        simulateBtn.addActionListener(e -> {
            runSimulation();
        });

        truthBtn.addActionListener(e -> showTruthTable());

        exportBtn.addActionListener(e -> {
            if (onExportCallback != null) onExportCallback.run();
        });
    }

    public void setOnSimulate(Runnable r) {
        this.onSimulateCallback = r;
    }

    public void setOnExport(Runnable r) {
        this.onExportCallback = r;
    }

    private void runSimulation() {
        simulateBtn.setEnabled(false);
        outputArea.setText("Simulating...");

        SwingWorker<Circuit, Void> worker = new SwingWorker<>() {
            @Override
            protected Circuit doInBackground() throws Exception {
                // Background thread
                return simulationService.runSimulation(circuitId);
            }

            @Override
            protected void done() {
                // EDT
                try {
                    Circuit domain = get();
                    outputArea.setText("");
                    domain.getComponents().stream()
                            .filter(c -> c instanceof org.example.domain.io.LED)
                            .forEach(c -> {
                                boolean lit = ((org.example.domain.io.LED) c).isLit();
                                outputArea.append("LED " + c.getId() + " = " + lit + "\n");
                            });

                    if (onSimulateCallback != null) onSimulateCallback.run();

                } catch (Exception e) {
                    outputArea.setText("Error: " + e.getMessage());
                    e.printStackTrace();
                } finally {
                    simulateBtn.setEnabled(true);
                }
            }
        };

        worker.execute();
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