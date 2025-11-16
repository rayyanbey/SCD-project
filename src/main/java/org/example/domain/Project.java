package org.example.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Project {

    private String id;            // optional domain id (could be entity id as string)
    private String name;
    private String description;

    private List<Circuit> circuits = new ArrayList<>();

    public Project() {}

    public Project(String name) {
        this.name = name;
    }

    public Project(String id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public void addCircuit(Circuit c) {
        if (c != null) circuits.add(c);
    }

    public List<Circuit> getCircuits() {
        return circuits;
    }

    public Optional<Circuit> findCircuitByName(String name) {
        return circuits.stream().filter(c -> name != null && name.equals(c.toString())).findFirst();
    }

    public Optional<Circuit> findCircuitByIndex(int idx) {
        if (idx < 0 || idx >= circuits.size()) return Optional.empty();
        return Optional.of(circuits.get(idx));
    }

    public Circuit getMainCircuit() {
        // convention: first circuit or search for one marked main (you can set a flag on Circuit if needed)
        if (circuits.isEmpty()) return null;
        return circuits.get(0);
    }

    // getters / setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}