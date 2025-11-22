package org.example.services;

import jakarta.persistence.EntityManager;
import org.example.config.JPAUtil;
import org.example.entity.ConnectorEntity;
import org.example.entity.PortEntity;
import org.example.repositories.ConnectorRepository;

import java.util.List;

public class ConnectorService {

    private final EntityManager em;
    private final ConnectorRepository repo;

    public ConnectorService() {
        this.em = JPAUtil.getEntityManagerFactory().createEntityManager();
        this.repo = new ConnectorRepository(em);
    }

    public void connectPorts(PortEntity source, PortEntity dest, String color) {
        ConnectorEntity c = new ConnectorEntity();
        c.setSourcePort(source);
        c.setDestPort(dest);
        c.setColor(color);
        repo.save(c);
    }

    public void deleteConnector(Long id) {
        repo.delete(id);
    }

    public List<ConnectorEntity> getIncomingConnections(Long portId) {
        String jpql = "SELECT c FROM ConnectorEntity c WHERE c.destPort.id = :pid";

        return em.createQuery(jpql, ConnectorEntity.class)
                .setParameter("pid", portId)
                .getResultList();
    }

    public List<ConnectorEntity> getOutgoingConnections(Long portId) {
        String jpql = "SELECT c FROM ConnectorEntity c WHERE c.sourcePort.id = :pid";

        return em.createQuery(jpql, ConnectorEntity.class)
                .setParameter("pid", portId)
                .getResultList();
    }


}