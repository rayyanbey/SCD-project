package org.example.repositories;

import jakarta.persistence.EntityManager;
import org.example.entity.CircuitEntity;

import java.util.List;

public class CircuitRepository {

    private final EntityManager em;

    public CircuitRepository(EntityManager em) {
        this.em = em;
    }

    public CircuitEntity findById(Long id) {
        return em.find(CircuitEntity.class, id);
    }

    public List<CircuitEntity> findByProject(Long projectId) {
        return em.createQuery(
                        "SELECT c FROM CircuitEntity c WHERE c.project.id = :pid", CircuitEntity.class)
                .setParameter("pid", projectId)
                .getResultList();
    }

    public void save(CircuitEntity circuit) {
        em.getTransaction().begin();
        em.persist(circuit);
        em.getTransaction().commit();
    }

    public void update(CircuitEntity circuit) {
        em.getTransaction().begin();
        em.merge(circuit);
        em.getTransaction().commit();
    }

    public void delete(Long id) {
        CircuitEntity c = findById(id);
        if (c == null) return;
        em.getTransaction().begin();
        em.remove(c);
        em.getTransaction().commit();
    }

    public void refresh(CircuitEntity circuit) {
        em.refresh(circuit);
    }

    public void clear() {
        em.clear();
    }
}