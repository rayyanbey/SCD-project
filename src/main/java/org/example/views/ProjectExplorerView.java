package org.example.views;

import org.example.entity.ProjectEntity;
import org.example.entity.CircuitEntity;
import org.example.services.ProjectService;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.function.Consumer;

public class ProjectExplorerView extends JPanel {

    private final JTree tree;
    private final DefaultMutableTreeNode root;
    private final ProjectService projectService = new ProjectService();
    private Consumer<CircuitEntity> onCircuitSelected;

    public ProjectExplorerView() {
        setLayout(new BorderLayout());
        root = new DefaultMutableTreeNode("Projects");
        tree = new JTree(root);
        add(new JScrollPane(tree), BorderLayout.CENTER);

        // Double-click or selection
        tree.addTreeSelectionListener(e -> {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
            if (node == null) return;
            Object obj = node.getUserObject();
            if (obj instanceof CircuitEntity) {
                if (onCircuitSelected != null) onCircuitSelected.accept((CircuitEntity) obj);
            }
        });

        // top-level toolbar (create project)
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton createBtn = new JButton("New Project");
        top.add(createBtn);
        createBtn.addActionListener(ev -> createProjectDialog());
        add(top, BorderLayout.NORTH);

        // Context menu
        JPopupMenu popup = new JPopupMenu();
        JMenuItem newCircuitItem = new JMenuItem("New Circuit");
        popup.add(newCircuitItem);

        tree.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    int row = tree.getClosestRowForLocation(e.getX(), e.getY());
                    tree.setSelectionRow(row);
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
                    if (node != null && node.getUserObject() instanceof ProjectEntity) {
                        popup.show(e.getComponent(), e.getX(), e.getY());
                    }
                }
            }
        });

        newCircuitItem.addActionListener(e -> {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
            if (node != null && node.getUserObject() instanceof ProjectEntity) {
                ProjectEntity p = (ProjectEntity) node.getUserObject();
                createCircuitDialog(p);
            }
        });

        reloadAll();
    }

    private void createCircuitDialog(ProjectEntity p) {
        String name = JOptionPane.showInputDialog(this, "Enter Circuit Name:");
        if (name != null && !name.trim().isEmpty()) {
            CircuitEntity c = new CircuitEntity();
            c.setName(name);
            c.setProject(p);
            new org.example.services.CircuitService().saveCircuit(c);
            reloadAll();
        }
    }

    public void setOnCircuitSelected(Consumer<CircuitEntity> cb) {
        this.onCircuitSelected = cb;
    }

    public void reloadAll() {
        root.removeAllChildren();
        List<ProjectEntity> projects = projectService.getAllProjects();
        if (projects != null) {
            for (ProjectEntity p : projects) {
                DefaultMutableTreeNode pnode = new DefaultMutableTreeNode(p);
                if (p.getCircuits() != null) {
                    for (CircuitEntity c : p.getCircuits()) {
                        pnode.add(new DefaultMutableTreeNode(c));
                    }
                }
                root.add(pnode);
            }
        }
        ((DefaultTreeModel) tree.getModel()).reload();
        tree.expandRow(0);
    }

    private void createProjectDialog() {
        JTextField name = new JTextField();
        JTextArea desc = new JTextArea(4, 20);
        JPanel p = new JPanel(new BorderLayout());
        p.add(new JLabel("Name:"), BorderLayout.NORTH);
        p.add(name, BorderLayout.CENTER);
        p.add(new JLabel("Description:"), BorderLayout.SOUTH);
        int ok = JOptionPane.showConfirmDialog(this, new Object[]{ "Project name:", name, "Description:", new JScrollPane(desc) }, "Create Project", JOptionPane.OK_CANCEL_OPTION);
        if (ok == JOptionPane.OK_OPTION) {
            projectService.createProject(name.getText(), desc.getText());
            reloadAll();
        }
    }

    // convenience used by controllers:
    public void displayProjects(List<ProjectEntity> projects) {
        root.removeAllChildren();
        if (projects != null) {
            for (ProjectEntity p : projects) {
                DefaultMutableTreeNode pNode = new DefaultMutableTreeNode(p);
                if (p.getCircuits() != null) {
                    for (CircuitEntity c : p.getCircuits()) {
                        pNode.add(new DefaultMutableTreeNode(c));
                    }
                }
                root.add(pNode);
            }
        }
        ((DefaultTreeModel) tree.getModel()).reload();
        tree.expandRow(0);
    }
}
