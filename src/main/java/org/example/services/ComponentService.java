package org.example.services;

import jakarta.persistence.EntityManager;
import org.example.config.JPAUtil;
import org.example.entity.ComponentEntity;
import org.example.repositories.ComponentRepository;

public class ComponentService {

    private final EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
    private final ComponentRepository repo = new ComponentRepository(em);

    public ComponentEntity getComponent(Long id) {
        return repo.findById(id);
    }

    public void saveComponent(ComponentEntity comp) {
        repo.save(comp);
    }

    public void updateComponent(ComponentEntity comp) {
        repo.update(comp);
    }

    public void deleteComponent(Long id) {
        repo.delete(id);
    }
}