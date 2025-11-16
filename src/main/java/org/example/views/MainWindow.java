package org.example.views;

import org.example.controllers.MainController;

import javax.swing.*;
import java.awt.*;

import javax.swing.*;
import java.awt.*;

public class MainWindow extends JFrame {

    private ProjectExplorerView projectExplorer;
    private ComponentPaletteView palette;
    private CircuitEditorView circuitEditor;
    private SimulationPanel simulationPanel;

    private long currentCircuitId = 1L; // TODO: set dynamically when project loads

    public MainWindow(MainController controller) {
        setTitle("LogiSim - Logic Simulator");
        setSize(1300, 800);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setLocationRelativeTo(null);

        // ============= LEFT SIDE =============
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setPreferredSize(new Dimension(250, 800));

        // Project Explorer (top-left)
        projectExplorer = new ProjectExplorerView();
        leftPanel.add(projectExplorer, BorderLayout.CENTER);

        // Component Palette (bottom-left)
        palette = new ComponentPaletteView();
        leftPanel.add(palette, BorderLayout.SOUTH);

        add(leftPanel, BorderLayout.WEST);

        // ============= CENTER CANVAS =============
        circuitEditor = new CircuitEditorView(currentCircuitId);
        add(circuitEditor, BorderLayout.CENTER);

        // ============= RIGHT PANEL (Simulation) =============
        simulationPanel = new SimulationPanel(currentCircuitId);
        add(simulationPanel, BorderLayout.EAST);

        // ============= WIRING PALETTE → EDITOR =============
        palette.setSelectionListener(evt -> {
            String type = evt.getActionCommand();
            circuitEditor.setPlacementType(type);
        });

        // ============= WIRING SIMULATE BUTTON → CANVAS + PANEL =============
        simulationPanel.setOnSimulate(() -> {
            circuitEditor.runSimulationAndRefresh();
        });

        setVisible(true);
    }
}