package org.example.views;

import javax.swing.*;

public class TruthTableView extends JFrame {

    JTable table;

    public TruthTableView(Object[][] data, String[] columns) {
        table = new JTable(data, columns);
        add(new JScrollPane(table));
        setSize(600, 400);
        setVisible(true);
    }
}