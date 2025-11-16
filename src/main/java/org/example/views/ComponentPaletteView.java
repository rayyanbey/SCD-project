package org.example.views;

import javax.swing.*;
import java.awt.*;

public class ComponentPaletteView extends JPanel {

    public ComponentPaletteView() {
        setPreferredSize(new Dimension(150, 800));
        setLayout(new GridLayout(10, 1));

        add(new JButton("AND"));
        add(new JButton("OR"));
        add(new JButton("NOT"));
        add(new JButton("XOR"));
        add(new JButton("NAND"));
        add(new JButton("NOR"));
        add(new JButton("SWITCH"));
        add(new JButton("LED"));
    }
}