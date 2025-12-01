package org.example.views;

import org.example.controllers.MainController;
import org.example.views.ProjectExplorerView;
import org.example.views.ComponentPaletteView;
import org.example.views.CircuitEditorView;
import org.example.views.SimulationPanel;

import javax.swing.*;
import java.awt.*;

public class MainWindow extends JFrame {

    private ProjectExplorerView projectExplorer;
    private ComponentPaletteView palette;
    private CircuitEditorView circuitEditor;
    private SimulationPanel simulationPanel;

    // Later: update when project/circuit selection works
    private long currentCircuitId = 1L;

    public MainWindow(MainController controller) {

        setTitle("LogiSim - Logic Simulator");
        setSize(1300, 1300);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setLocationRelativeTo(null);

        // ==================================================
        // LEFT SIDE (Project Explorer + Palette)
        // ==================================================
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setPreferredSize(new Dimension(250, 800));

        // Project Explorer
        projectExplorer = new ProjectExplorerView();
        leftPanel.add(projectExplorer, BorderLayout.CENTER);

        // Component Palette
        palette = new ComponentPaletteView();
        leftPanel.add(palette, BorderLayout.SOUTH);

        add(leftPanel, BorderLayout.WEST);

        // ==================================================
        // CENTER CANVAS (Circuit Editor)
        // ==================================================
        circuitEditor = new CircuitEditorView(currentCircuitId);
        add(circuitEditor, BorderLayout.CENTER);

        // ==================================================
        // RIGHT SIDE (Simulation Panel)
        // ==================================================
        simulationPanel = new SimulationPanel(currentCircuitId);
        add(simulationPanel, BorderLayout.EAST);

        // ==================================================
        // PALETTE → EDITOR: Select gate type
        // ==================================================
        palette.setSelectionListener(evt -> {
            String type = evt.getActionCommand();
            circuitEditor.setPlacementType(type);
        });

        // ==================================================
        // SIMULATE BUTTON → Trigger canvas update
        // ==================================================
        simulationPanel.setOnSimulate(() -> circuitEditor.runSimulationAndRefresh());

        projectExplorer.setOnCircuitSelected(circuit -> {
            if (circuit == null) return;
            long cid = circuit.getId();
            if (cid == currentCircuitId && circuitEditor != null) return;
            currentCircuitId = cid;
            // recreate editor and sim panel for that circuit
            remove(circuitEditor);
            remove(simulationPanel);

            circuitEditor = new org.example.views.CircuitEditorView(cid);
            simulationPanel = new org.example.views.SimulationPanel(cid);

            simulationPanel.setOnSimulate(() -> circuitEditor.runSimulationAndRefresh());
            simulationPanel.setOnExport(() -> {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Export Circuit Image");
                if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                    java.io.File file = fileChooser.getSelectedFile();
                    if (!file.getName().toLowerCase().endsWith(".png")) {
                        file = new java.io.File(file.getAbsolutePath() + ".png");
                    }
                    circuitEditor.exportToImage(file);
                }
            });

            add(circuitEditor, BorderLayout.CENTER);
            add(simulationPanel, BorderLayout.EAST);
            revalidate();
            repaint();
        });

        // ==================================================
        // MENU BAR
        // ==================================================
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");

        JMenuItem openProjectItem = new JMenuItem("Open Project...");
        openProjectItem.addActionListener(e -> {
            java.util.List<org.example.entity.ProjectEntity> projects = new org.example.services.ProjectService().getAllProjects();
            if (projects == null || projects.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No projects found.");
                return;
            }
            
            // Create a custom dialog for project selection
            JDialog dialog = new JDialog(this, "Open Project", true);
            dialog.setLayout(new BorderLayout());
            dialog.setSize(400, 300);
            dialog.setLocationRelativeTo(this);

            DefaultListModel<org.example.entity.ProjectEntity> listModel = new DefaultListModel<>();
            projects.forEach(listModel::addElement);
            
            JList<org.example.entity.ProjectEntity> list = new JList<>(listModel);
            list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            list.setCellRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    if (value instanceof org.example.entity.ProjectEntity) {
                        setText(((org.example.entity.ProjectEntity) value).getName());
                    }
                    return this;
                }
            });

            dialog.add(new JScrollPane(list), BorderLayout.CENTER);

            JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton openBtn = new JButton("Open");
            JButton cancelBtn = new JButton("Cancel");
            
            openBtn.addActionListener(ev -> {
                org.example.entity.ProjectEntity selected = list.getSelectedValue();
                if (selected != null) {
                    projectExplorer.selectProjectAndFirstCircuit(selected);
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Please select a project.");
                }
            });
            
            cancelBtn.addActionListener(ev -> dialog.dispose());
            
            btnPanel.add(openBtn);
            btnPanel.add(cancelBtn);
            dialog.add(btnPanel, BorderLayout.SOUTH);
            
            dialog.setVisible(true);
        });
        fileMenu.add(openProjectItem);

        JMenuItem saveItem = new JMenuItem("Save Project");
        saveItem.addActionListener(e -> {
            // In a real app, we might want to trigger a specific save action
            // For now, since persistence happens on every action, we can just show a confirmation
            JOptionPane.showMessageDialog(this, "Project Saved (Auto-save is active).");
        });

        JMenuItem exportItem = new JMenuItem("Export to Image...");
        exportItem.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Export Circuit Image");
            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                java.io.File file = fileChooser.getSelectedFile();
                if (!file.getName().toLowerCase().endsWith(".png")) {
                    file = new java.io.File(file.getAbsolutePath() + ".png");
                }
                circuitEditor.exportToImage(file);
            }
        });

        fileMenu.add(saveItem);
        fileMenu.add(exportItem);
        menuBar.add(fileMenu);
        setJMenuBar(menuBar);

        setVisible(true);
    }
}
