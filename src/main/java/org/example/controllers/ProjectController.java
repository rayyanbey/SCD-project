package org.example.controllers;

import org.example.entity.ProjectEntity;
import org.example.services.ProjectService;
import org.example.views.ProjectExplorerView;

import java.util.List;

public class ProjectController {

    private final ProjectService service;
    private final ProjectExplorerView view;

    public ProjectController(ProjectExplorerView view, ProjectService service) {
        this.view = view;
        this.service = service;
    }

    public void loadProjects() {
        List<ProjectEntity> projects = service.getAllProjects();
        view.displayProjects(projects);
    }

    public void createProject(String name, String desc) {
        service.createProject(name, desc);
        loadProjects();
    }
}