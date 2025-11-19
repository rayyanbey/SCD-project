package org.example.views;

import javax.swing.*;
import java.awt.*;

public class TruthTableView extends JFrame {

    public TruthTableView(Object[][] data, String[] columns) {
        setTitle("Truth Table");
        JTable table = new JTable(data, columns);
        add(new JScrollPane(table), BorderLayout.CENTER);
        setSize(700, 400);
        setLocationRelativeTo(null);
        setVisible(true);
    }
}