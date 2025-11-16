package org.example.repositories;

import jakarta.persistence.EntityManager;
import org.example.entity.ProjectEntity;

import java.util.List;

public class ProjectRepository {

    private final EntityManager em;

    public ProjectRepository(EntityManager em) {
        this.em = em;
    }

    public ProjectEntity findById(Long id) {
        return em.find(ProjectEntity.class, id);
    }

    public List<ProjectEntity> findAll() {
        return em.createQuery("SELECT p FROM ProjectEntity p", ProjectEntity.class)
                .getResultList();
    }

    public void save(ProjectEntity project) {
        em.getTransaction().begin();
        em.persist(project);
        em.getTransaction().commit();
    }

    public void update(ProjectEntity project) {
        em.getTransaction().begin();
        em.merge(project);
        em.getTransaction().commit();
    }

    public void delete(Long id) {
        ProjectEntity p = findById(id);
        if (p == null) return;
        em.getTransaction().begin();
        em.remove(p);
        em.getTransaction().commit();
    }
}