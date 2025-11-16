package org.example.controllers;

import org.example.domain.Circuit;
import org.example.services.SimulationService;
import org.example.views.SimulationPanel;

public class SimulationController {

    private final SimulationService simulationService;
    private final SimulationPanel simulationPanel;

    public SimulationController(SimulationPanel panel) {
        this.simulationPanel = panel;
        this.simulationService = new SimulationService();
    }

    public void simulateCircuit(Long circuitId) {
        Circuit domainCircuit = simulationService.runSimulation(circuitId);

        simulationPanel.displayOutput(domainCircuit);
    }
}