package org.example.controllers;

import org.example.views.MainWindow;

public class MainController {

    private MainWindow mainWindow;

    public MainController() {
        mainWindow = new MainWindow(this);
    }

    public void start() {
        mainWindow.setVisible(true);
    }
}