package org.example;

import jakarta.persistence.EntityManager;
import org.example.config.JPAUtil;
import org.example.controllers.MainController;
import org.example.views.MainWindow;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {

        // ------------------------------------------
        // 1. FORCE TABLE CREATION (your original code)
        // ------------------------------------------
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        em.close();
        System.out.println("Tables created successfully!");

        // ------------------------------------------
        // 2. LAUNCH THE GUI APPLICATION
        // ------------------------------------------
        SwingUtilities.invokeLater(() -> {
            try {
                // Make UI look native
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}

            // Create controller
            MainController controller = new MainController();
            controller.start();
        });
    }
}