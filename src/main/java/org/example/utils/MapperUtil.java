package org.example.utils;

import org.example.domain.Circuit;
import org.example.domain.Component;
import org.example.entity.CircuitEntity;
import org.example.entity.ComponentEntity;

import java.util.*;

public class MapperUtil {

    /**
     * Converts a CircuitEntity (DB model) into a Circuit (Domain model).
     */
    public static Circuit toDomain(CircuitEntity entity) {

        Circuit circuit = new Circuit();

        // Maps componentEntityId -> domainComponent
        Map<Long, Component> compMap = new HashMap<>();

        // Step 1: Create all domain components
        for (ComponentEntity ce : entity.getComponents()) {

            Component dc = createDomainComponent(ce);
            dc.setId(String.valueOf(ce.getId()));
            dc.setLabel(ce.getLabel());

            compMap.put(ce.getId(), dc);
            circuit.addComponent(dc);
        }

        // Step 2: Create domain connectors
        for (ComponentEntity ce : entity.getComponents()) {
            Component domainComponent = compMap.get(ce.getId());

            // Map ports later
            // Nothing to do here yet
        }

        // Step 3: Create ports for each component
        Map<Long, InputPort> inputPortMap = new HashMap<>();
        Map<Long, OutputPort> outputPortMap = new HashMap<>();

        for (ComponentEntity ce : entity.getComponents()) {
            Component domainComp = compMap.get(ce.getId());

            for (PortEntity pe : ce.getPorts()) {
                if (pe.getType() == PortEntity.PortType.INPUT) {
                    InputPort ip = new InputPort(domainComp);
                    domainComp.getInputs().add(ip);
                    inputPortMap.put(pe.getId(), ip);
                } else {
                    OutputPort op = new OutputPort(domainComp);
                    domainComp.getOutputs().add(op);
                    outputPortMap.put(pe.getId(), op);
                }
            }
        }

        // Step 4: Create connectors (wires)
        for (ComponentEntity ce : entity.getComponents()) {
            for (PortEntity pe : ce.getPorts()) {
                for (ConnectorEntity conn : pe.getOutgoingConnections()) {
                    // Map JPA -> Domain
                    OutputPort src = outputPortMap.get(conn.getSourcePort().getId());
                    InputPort dest = inputPortMap.get(conn.getDestPort().getId());

                    if (src != null && dest != null) {
                        Connector dConn = new Connector(src, dest);
                        circuit.addConnector(dConn);
                        src.addConnection(dConn);
                    }
                }
            }
        }

        return circuit;
    }

    /**
     * Creates domain component from entity type.
     */
    private static Component createDomainComponent(ComponentEntity ce) {

        String type = ce.getType().toUpperCase();

        return switch (type) {
            case "AND" -> new AndGate();
            case "OR" -> new OrGate();
            case "NOT" -> new NotGate();
            case "XOR" -> new XorGate();
            case "NAND" -> new NandGate();
            case "NOR" -> new NorGate();
            case "SWITCH" -> new Switch();
            case "LED" -> new LED();
            case "SUBCIRCUIT" -> new Circuit();  // optional, for nested circuits
            default -> throw new RuntimeException("Unknown component type: " + type);
        };
    }

    /**
     * Optional – convert Domain -> Entity for saving positions, labels, etc.
     * Does NOT save simulation values.
     */
    public static void updateEntityFromDomain(ComponentEntity ce, Component domain) {
        ce.setLabel(domain.getLabel());
        // Could map more fields like posX, posY when implementing UI
    }
}