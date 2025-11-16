package org.example.repositories;

import jakarta.persistence.EntityManager;
import org.example.entity.PortEntity;

public class PortRepository {

    private final EntityManager em;

    public PortRepository(EntityManager em) {
        this.em = em;
    }

    public PortEntity findById(Long id) {
        return em.find(PortEntity.class, id);
    }

    public void save(PortEntity port) {
        em.getTransaction().begin();
        em.persist(port);
        em.getTransaction().commit();
    }

    public void delete(Long id) {
        PortEntity p = findById(id);
        if (p == null) return;

        em.getTransaction().begin();
        em.remove(p);
        em.getTransaction().commit();
    }
}