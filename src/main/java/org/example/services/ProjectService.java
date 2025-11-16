package org.example.services;

import jakarta.persistence.EntityManager;

import java.util.List;
import  org.example.config.JPAUtil;
import org.example.entity.ProjectEntity;
import org.example.repositories.ProjectRepository;

public class ProjectService {

    private final EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
    private final ProjectRepository repo = new ProjectRepository(em);

    public List<ProjectEntity> getAllProjects() {
        return repo.findAll();
    }

    public void createProject(String name, String desc) {
        ProjectEntity p = new ProjectEntity();
        p.setName(name);
        p.setDescription(desc);
        repo.save(p);
    }

    public ProjectEntity getProject(Long id) {
        return repo.findById(id);
    }
}