package org.example.views;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class ComponentPaletteView extends JPanel {

    private final String[] TYPES = {"AND","OR","NOT","XOR","NAND","NOR","SWITCH","LED","CLOCK"};
    private ActionListener listener;

    public ComponentPaletteView() {
        setPreferredSize(new Dimension(140, 800));
        setLayout(new GridLayout(TYPES.length, 1, 6, 6));
        for (String t : TYPES) {
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