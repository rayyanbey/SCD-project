package org.example.views;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class ComponentPaletteView extends JPanel {

    private String[] types = {"AND","OR","NOT","XOR","NAND","NOR","SWITCH","LED","CLOCK"};
    private ActionListener listener;

    public ComponentPaletteView() {
        setPreferredSize(new Dimension(120, 800));
        setLayout(new GridLayout(types.length, 1, 4, 4));
        for (String t : types) {
            JButton b = new JButton(t);
            b.setActionCommand(t);
            b.addActionListener(e -> {
                if (listener != null) listener.actionPerformed(e);
            });
            add(b);
        }
    }

    public void setSelectionListener(ActionListener l) {
        this.listener = l;
    }
}