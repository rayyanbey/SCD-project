package org.example.views;

import org.example.domain.Component;
import org.example.domain.io.LED;

import javax.swing.*;
import java.awt.*;

public class SimulationPanel extends JPanel {

    JTextArea outputArea = new JTextArea();

    public SimulationPanel() {
        setPreferredSize(new Dimension(250, 800));
        setLayout(new BorderLayout());
        add(new JLabel("Simulation Output"), BorderLayout.NORTH);
        add(new JScrollPane(outputArea), BorderLayout.CENTER);
    }

    public void displayOutput(org.example.domain.Circuit circuit) {
        outputArea.setText("");

        for (Component c : circuit.getComponents()) {
            if (c instanceof LED led) {
                outputArea.append("LED: " + led.isLit() + "\n");
            }
        }
    }
}