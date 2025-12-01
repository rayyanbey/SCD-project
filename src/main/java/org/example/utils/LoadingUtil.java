package org.example.utils;

import javax.swing.*;
import java.awt.*;

public class LoadingUtil {

    private static JDialog loadingDialog;

    public static void showLoading(Component parent, String message) {
        if (loadingDialog != null && loadingDialog.isVisible()) {
            return; // Already showing
        }

        Window window = SwingUtilities.getWindowAncestor(parent);
        loadingDialog = new JDialog(window != null ? (Frame) window : null, "Loading...", Dialog.ModalityType.APPLICATION_MODAL);
        loadingDialog.setUndecorated(true);
        
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        panel.setBackground(Color.WHITE);
        
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        
        JLabel label = new JLabel(message, JLabel.CENTER);
        label.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        panel.add(label, BorderLayout.NORTH);
        panel.add(progressBar, BorderLayout.CENTER);
        
        loadingDialog.add(panel);
        loadingDialog.pack();
        loadingDialog.setLocationRelativeTo(parent);
        
        // Show in a separate thread to avoid blocking the caller if called on EDT before worker starts? 
        // Actually, for modal dialogs, we usually want to show it. 
        // But since we are going to use SwingWorker, we should show this *before* execute() or have the worker show it?
        // Better pattern: Caller shows dialog (non-blocking? No, modal blocks).
        // If modal blocks, we can't start the worker after showing.
        // So we should start the worker, then show the dialog?
        // Or make the dialog non-modal but blocking input via glass pane?
        // Let's stick to a simple non-modal always-on-top or just use a glass pane approach?
        // A modal dialog blocks the EDT if shown on EDT.
        // So we must run the worker *before* showing the dialog if it's modal?
        // Actually, best way for SwingWorker + Modal Dialog:
        // 1. Create dialog.
        // 2. Start SwingWorker.
        // 3. Show dialog.
        // 4. In SwingWorker.done(), hide dialog.
        
        // However, if we call showLoading() on EDT, it blocks until hidden.
        // So we can't easily use a static helper that blocks.
        // Let's make it non-modal but disable parent? Or just use a simple approach:
        // We'll make it non-modal but setLocationRelativeTo parent.
        // And maybe disable the parent frame?
        
        loadingDialog.setModal(false); 
        // We want to block user interaction though.
        // Let's use a GlassPane approach or just a modal dialog that is shown *after* worker start?
        // But if I wrap it in a helper "show", I can't control when it returns.
        
        // Let's try a different approach: executeWithLoading(Component parent, Runnable backgroundTask, Runnable onDone)
        
    }
    
    public static void executeWithLoading(Component parent, String message, Runnable backgroundTask, Runnable onDone) {
        Window window = SwingUtilities.getWindowAncestor(parent);
        JDialog dialog = new JDialog(window != null ? (Frame) window : null, "Loading", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setUndecorated(true);
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        panel.setBackground(Color.WHITE);
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        JLabel label = new JLabel(message, JLabel.CENTER);
        label.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(label, BorderLayout.NORTH);
        panel.add(progressBar, BorderLayout.CENTER);
        dialog.add(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(parent);

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                backgroundTask.run();
                return null;
            }

            @Override
            protected void done() {
                dialog.dispose();
                if (onDone != null) {
                    onDone.run();
                }
            }
        };
        
        worker.execute();
        dialog.setVisible(true); // This blocks until dispose() is called
    }
}
