package org.example.repositories;

import jakarta.persistence.EntityManager;
import org.example.entity.ComponentEntity;

public class ComponentRepository {

    private final EntityManager em;

    public ComponentRepository(EntityManager em) {
        this.em = em;
    }

    public ComponentEntity findById(Long id) {
        return em.find(ComponentEntity.class, id);
    }

    public void save(ComponentEntity comp) {
        em.getTransaction().begin();
        em.persist(comp);
        em.getTransaction().commit();
    }

    public void update(ComponentEntity comp) {
        em.getTransaction().begin();
        em.merge(comp);
        em.getTransaction().commit();
    }

    public void delete(Long id) {
        ComponentEntity c = findById(id);
        if (c == null) return;
        em.getTransaction().begin();
        em.remove(c);
        em.getTransaction().commit();
    }
}
