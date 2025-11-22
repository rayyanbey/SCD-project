package org.example.utils;

import org.example.domain.*;
import org.example.domain.gates.*;
import org.example.domain.io.LED;
import org.example.domain.io.Switch;
import org.example.entity.CircuitEntity;
import org.example.entity.ComponentEntity;
import org.example.entity.ConnectorEntity;
import org.example.entity.PortEntity;

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

        switch (type) {
            case "AND": return new AndGate();
            case "OR": return new OrGate();
            case "NOT": return new NotGate();
            case "XOR": return new XorGate();
            case "NAND": return new NandGate();
            case "NOR": return new NorGate();
            case "SWITCH": {
                Switch sw = new Switch();
                // if entity stores initial switch state, apply it to domain output
                if (ce.getSwitchState() != null) sw.setOn(ce.getSwitchState());
                return sw;
            }
            case "LED": return new LED();
            case "CLOCK": {
                // if you saved a period value on the entity, use it
                int period = (ce.getClockPeriod() == null) ? 1 : ce.getClockPeriod();
                ClockSource clk = new ClockSource(period);
                return clk;
            }
            default:
                throw new RuntimeException("Unknown component type: " + type);
        }
    }


    public static void updateEntityFromDomain(ComponentEntity ce, Component domain) {
        ce.setLabel(domain.getLabel());
        // Could map more fields like posX, posY when implementing UI
    }
}