package org.example.views;

import org.example.controllers.MainController;

import javax.swing.*;
import java.awt.*;

public class MainWindow extends JFrame {

    public MainWindow(MainController controller) {
        setTitle("LogiSim - Logic Simulator");
        setSize(1200, 800);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Left: Project Explorer
        ProjectExplorerView explorer = new ProjectExplorerView();
        add(explorer, BorderLayout.WEST);

        // Center: Circuit Editor
        CircuitEditorView editor = new CircuitEditorView();
        add(editor, BorderLayout.CENTER);

        // Right: Simulation Panel
        SimulationPanel simulationPanel = new SimulationPanel();
        add(simulationPanel, BorderLayout.EAST);

        setLocationRelativeTo(null);
    }

}