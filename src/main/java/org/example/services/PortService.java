package org.example.services;

import jakarta.persistence.EntityManager;

import java.util.List;
import org.example.config.JPAUtil;
import org.example.entity.ComponentEntity;
import org.example.entity.PortEntity;
import org.example.repositories.PortRepository;

public class PortService {

    private final EntityManager em;
    private final PortRepository repo;

    public PortService() {
        this.em = JPAUtil.getEntityManagerFactory().createEntityManager();
        this.repo = new PortRepository(em);
    }

    /**
     * Add a port to a component. The ComponentEntity must already exist.
     */
    public void addPort(PortEntity port) {
        if (port.getComponent() == null) {
            throw new RuntimeException("Port must be linked to a ComponentEntity before saving.");
        }
        repo.save(port);
    }

    /**
     * Create and attach a port to a given component (utility method).
     */
    public PortEntity createPort(ComponentEntity parent, String name,
                                 int portIndex, PortEntity.PortType type) {
        PortEntity p = new PortEntity();
        p.setName(name);
        p.setPortIndex(portIndex);
        p.setType(type);
        p.setComponent(parent);
        repo.save(p);
        return p;
    }

    /**
     * Return all ports belonging to a component.
     */
    public List<PortEntity> getPortsByComponent(ComponentEntity comp) {
        if (comp == null || comp.getId() == null) {
            throw new IllegalArgumentException("Component must not be null and must be saved first.");
        }

        return em.createQuery(
                "SELECT p FROM PortEntity p WHERE p.component.id = :cid",
                PortEntity.class
        ).setParameter("cid", comp.getId()).getResultList();
    }

    /**
     * Helpful when deleting components.
     */
    public void deletePort(Long id) {
        repo.delete(id);
    }

    /**
     * Update a port (not commonly needed, but available).
     */
    public void updatePort(PortEntity port) {
        em.getTransaction().begin();
        em.merge(port);
        em.getTransaction().commit();
    }

    /**
     * Returns a specific port.
     */
    public PortEntity getPort(Long id) {
        return repo.findById(id);
    }
}