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
    // Removed long-lived service to ensure fresh data
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
            org.example.utils.LoadingUtil.executeWithLoading(this, "Creating Circuit...", () -> {
                CircuitEntity c = new CircuitEntity();
                c.setName(name);
                c.setProject(p);
                new org.example.services.CircuitService().saveCircuit(c);
            }, this::reloadAll);
        }
    }

    public void setOnCircuitSelected(Consumer<CircuitEntity> cb) {
        this.onCircuitSelected = cb;
    }

    public void reloadAll() {
        org.example.utils.LoadingUtil.executeWithLoading(this, "Loading Projects...", () -> {
            root.removeAllChildren();
            // Use fresh service to get latest data
            List<ProjectEntity> projects = new ProjectService().getAllProjects();
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
        }, () -> {
            ((DefaultTreeModel) tree.getModel()).reload();
            tree.expandRow(0);
        });
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
            org.example.utils.LoadingUtil.executeWithLoading(this, "Creating Project...", () -> {
                new ProjectService().createProject(name.getText(), desc.getText());
            }, this::reloadAll);
        }
    }

    // convenience used by controllers:
    public void displayProjects(List<ProjectEntity> projects) {
        // This is usually called with already fetched data, but let's just reload to be safe or just update UI
        // Since reloadAll fetches data, we can just call that if we want consistency, or just update tree here.
        // But displayProjects seems to be used by MainController? Let's check usage.
        // Actually, let's just update the tree on EDT since projects are passed in.
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

    public void selectProjectAndFirstCircuit(ProjectEntity project) {
        // Reload to ensure we have latest
        // We need to chain these async calls.
        // reloadAll is async now.
        
        org.example.utils.LoadingUtil.executeWithLoading(this, "Loading Project...", () -> {
             // Background: Fetch latest project data? 
             // reloadAll does fetching.
             // But we need to wait for reloadAll to finish before selecting.
             // So we can't call reloadAll() directly inside here if reloadAll is also async/modal.
             // Actually, executeWithLoading blocks the UI but runs in background.
             // If we call reloadAll(), it will try to open another dialog?
             // No, reloadAll() calls executeWithLoading which opens a dialog.
             // Nested modal dialogs are tricky.
             // Let's implement a specific background task for this.
             
             // We'll do the fetching here manually to avoid nesting dialogs.
             root.removeAllChildren();
             List<ProjectEntity> projects = new ProjectService().getAllProjects();
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
        }, () -> {
            // Done on EDT
            ((DefaultTreeModel) tree.getModel()).reload();
            tree.expandRow(0);
            
            // Now find and select
            int childCount = root.getChildCount();
            for (int i = 0; i < childCount; i++) {
                DefaultMutableTreeNode pNode = (DefaultMutableTreeNode) root.getChildAt(i);
                Object userObj = pNode.getUserObject();
                if (userObj instanceof ProjectEntity && ((ProjectEntity) userObj).getId().equals(project.getId())) {
                    // Found project, select it
                    javax.swing.tree.TreePath pPath = new javax.swing.tree.TreePath(pNode.getPath());
                    tree.setSelectionPath(pPath);
                    tree.expandPath(pPath);
                    
                    // Check if it has children (circuits)
                    if (pNode.getChildCount() == 0) {
                         // No circuits! Create a default one.
                         // This needs another background task?
                         // Let's do it inline with a separate loading call
                         createDefaultCircuitAndSelect((ProjectEntity) userObj);
                         return;
                    }

                    // Select first circuit if available
                    if (pNode.getChildCount() > 0) {
                        DefaultMutableTreeNode cNode = (DefaultMutableTreeNode) pNode.getChildAt(0);
                        javax.swing.tree.TreePath cPath = new javax.swing.tree.TreePath(cNode.getPath());
                        tree.setSelectionPath(cPath);
                    }
                    return;
                }
            }
        });
    }
    
    private void createDefaultCircuitAndSelect(ProjectEntity p) {
        org.example.utils.LoadingUtil.executeWithLoading(this, "Creating Default Circuit...", () -> {
             CircuitEntity defaultCircuit = new CircuitEntity();
             defaultCircuit.setName("Main");
             defaultCircuit.setMain(true);
             defaultCircuit.setProject(p);
             new org.example.services.CircuitService().saveCircuit(defaultCircuit);
        }, () -> {
             // Recurse/Retry selection
             selectProjectAndFirstCircuit(p);
        });
    }
}
