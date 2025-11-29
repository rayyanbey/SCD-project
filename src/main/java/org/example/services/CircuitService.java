package org.example.services;

import jakarta.persistence.EntityManager;

import java.util.List;
import org.example.config.JPAUtil;
import org.example.entity.CircuitEntity;
import org.example.repositories.CircuitRepository;

public class CircuitService {

    private final EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
    private final CircuitRepository repo = new CircuitRepository(em);

    public CircuitEntity getCircuit(Long id) {
        return repo.findById(id);
    }

    public List<CircuitEntity> getCircuitsByProject(Long projectId) {
        return repo.findByProject(projectId);
    }

    public void saveCircuit(CircuitEntity circuit) {
        repo.save(circuit);
    }

    public void updateCircuit(CircuitEntity circuit) {
        repo.update(circuit);
    }

    public void refreshCircuit(CircuitEntity circuit) {
        // Use EntityManager to refresh the entity state from the database
        if (circuit == null) return;
        em.refresh(circuit);
    }

    public void clearCache() {
        // Clear the persistence context
        em.clear();
    }
}