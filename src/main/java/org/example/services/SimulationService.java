package org.example.services;

import jakarta.persistence.EntityManager;
import org.example.config.JPAUtil;
import org.example.domain.Circuit;
import org.example.entity.CircuitEntity;
import org.example.repositories.CircuitRepository;
import org.example.simulations.CircuitSimulator;
import org.example.utils.MapperUtil;

public class SimulationService {

    private final EntityManager em;
    private final CircuitRepository circuitRepo;
    private final CircuitSimulator simulator;

    public SimulationService() {
        this.em = JPAUtil.getEntityManagerFactory().createEntityManager();
        this.circuitRepo = new CircuitRepository(em);
        this.simulator = new CircuitSimulator();
    }

    public Circuit runSimulation(Long circuitId) {

        // 1. Load entity from DB
        CircuitEntity entity = circuitRepo.findById(circuitId);
        if (entity == null)
            throw new RuntimeException("Circuit not found: " + circuitId);

        // 2. Convert to Domain Model
        Circuit domainCircuit = MapperUtil.toDomain(entity);

        // 3. Run simulation
        simulator.run(domainCircuit);

        // 4. Return domain object (UI uses LED results)
        return domainCircuit;
    }
}