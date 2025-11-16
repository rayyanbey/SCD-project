package org.example.services;

import jakarta.persistence.EntityManager;
import org.example.config.JPAUtil;
import org.example.entity.ConnectorEntity;
import org.example.entity.PortEntity;
import org.example.repositories.ConnectorRepository;

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
}