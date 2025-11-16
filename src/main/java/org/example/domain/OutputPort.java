package org.example.domain;

import java.util.ArrayList;
import java.util.List;

public class OutputPort extends Port {

    private List<Connector> connections = new ArrayList<>();

    public OutputPort(Component parent) {
        super(parent);
    }

    public void addConnection(Connector conn) {
        connections.add(conn);
    }

    public List<Connector> getConnections() {
        return connections;
    }
}
