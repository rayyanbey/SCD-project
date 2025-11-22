package org.example.views;

import org.example.entity.ProjectEntity;
import org.example.entity.CircuitEntity;
import org.example.services.ProjectService;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
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

        reloadAll();
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
