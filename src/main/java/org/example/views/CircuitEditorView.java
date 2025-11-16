package org.example.views;

import org.example.entity.CircuitEntity;

import javax.swing.*;
import java.awt.*;

public class CircuitEditorView extends JPanel {

    public CircuitEditorView() {
        setBackground(Color.WHITE);
        setLayout(null); // absolute positioning for drag-drop
    }

    public void loadCircuit(CircuitEntity entity) {
        removeAll();
        repaint();

        JLabel lbl = new JLabel("Editing Circuit: " + entity.getName());
        lbl.setBounds(20, 20, 300, 30);
        add(lbl);
    }
}