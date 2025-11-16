package org.example.views;

import org.example.entity.ProjectEntity;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ProjectExplorerView extends JPanel {

    private DefaultListModel<String> listModel = new DefaultListModel<>();
    private JList<String> projectList = new JList<>(listModel);

    public ProjectExplorerView() {
        setPreferredSize(new Dimension(200, 800));
        setLayout(new BorderLayout());
        add(new JLabel("Projects"), BorderLayout.NORTH);
        add(new JScrollPane(projectList), BorderLayout.CENTER);
    }

    public void displayProjects(List<ProjectEntity> projects) {
        listModel.clear();
        for (ProjectEntity p : projects) {
            listModel.addElement(p.getId() + ": " + p.getName());
        }
    }
}