package org.example.repositories;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.example.entity.ConnectorEntity;

import java.util.List;

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

    public void update(ConnectorEntity conn) {
        em.getTransaction().begin();
        em.merge(conn);
        em.getTransaction().commit();
    }

    public List<ConnectorEntity> findAllByCircuitId(Long circuitId) {
        String jpql = "SELECT c FROM ConnectorEntity c " +
                      "JOIN FETCH c.sourcePort sp " +
                      "JOIN FETCH sp.component spc " +
                      "JOIN FETCH c.destPort dp " +
                      "JOIN FETCH dp.component dpc " +
                      "WHERE spc.circuit.id = :cid";
        TypedQuery<ConnectorEntity> q = em.createQuery(jpql, ConnectorEntity.class);
        q.setParameter("cid", circuitId);
        return q.getResultList();
    }
}