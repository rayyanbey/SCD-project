package org.example.services;

import jakarta.persistence.EntityManager;
import org.example.config.JPAUtil;
import org.example.domain.Circuit;
import org.example.entity.CircuitEntity;
import org.example.repositories.CircuitRepository;
import org.example.simulations.CircuitSimulator;
import org.example.simulations.TruthTableGenerator;
import org.example.utils.MapperUtil;

import java.util.List;
import java.util.Map;

public class SimulationService {

    private final EntityManager em;
    private final CircuitRepository circuitRepo;
    private final CircuitSimulator simulator;
    private final TruthTableGenerator truthGen;

    public SimulationService() {
        this.em = JPAUtil.getEntityManagerFactory().createEntityManager();
        this.circuitRepo = new CircuitRepository(em);
        this.simulator = new CircuitSimulator();
        this.truthGen = new TruthTableGenerator();
    }

    public Circuit runSimulation(Long circuitId) {
        CircuitEntity entity = circuitRepo.findByIdWithComponents(circuitId);
        if (entity == null) throw new RuntimeException("Circuit not found: " + circuitId);
        Circuit domain = MapperUtil.toDomain(entity);
        simulator.run(domain);
        return domain;
    }

    public List<Map<String, Boolean>> generateTruthTable(Long circuitId) {
        CircuitEntity entity = circuitRepo.findByIdWithComponents(circuitId);
        if (entity == null) throw new RuntimeException("Circuit not found: " + circuitId);
        Circuit domain = MapperUtil.toDomain(entity);
        return truthGen.generateTruthTable(domain);
    }
}