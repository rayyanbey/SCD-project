package org.example.domain;

import java.util.ArrayList;
import java.util.List;

public class Circuit {

    private List<Component> components = new ArrayList<>();
    private List<Connector> connectors = new ArrayList<>();

    public void addComponent(Component c) {
        components.add(c);
    }

    // Alias for test compatibility
    public void add(Component c) {
        addComponent(c);
    }

    public void addConnector(Connector conn) {
        connectors.add(conn);
    }

    public List<Component> getComponents() { return components; }
    public List<Connector> getConnectors() { return connectors; }
}