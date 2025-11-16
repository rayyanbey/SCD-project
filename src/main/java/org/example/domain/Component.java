package org.example.domain;

import java.util.ArrayList;
import java.util.List;

public abstract class Component {

    protected String id;
    protected String label;

    protected List<InputPort> inputs = new ArrayList<>();
    protected List<OutputPort> outputs = new ArrayList<>();

    public abstract void evaluate();   // main logic of component

    public List<InputPort> getInputs() { return inputs; }
    public List<OutputPort> getOutputs() { return outputs; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
}