package org.example.repositories;

import jakarta.persistence.EntityManager;
import org.example.entity.ConnectorEntity;

public class ConnectorRepository {

    private final EntityManager em;

    public ConnectorRepository(EntityManager em) {
        this.em = em;
    }

    public ConnectorEntity findById(Long id) {
        return em.find(ConnectorEntity.class, id);
    }

    public void save(ConnectorEntity conn) {
        em.getTransaction().begin();
        em.persist(conn);
        em.getTransaction().commit();
    }

    public void delete(Long id) {
        ConnectorEntity c = findById(id);
        if (c == null) return;

        em.getTransaction().begin();
        em.remove(c);
        em.getTransaction().commit();
    }
}