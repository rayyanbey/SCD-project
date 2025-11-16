package org.example.domain;

public abstract class Port {

    protected Component parent;
    protected Boolean value = false;

    public Port(Component parent) {
        this.parent = parent;
    }

    public Boolean getValue() { return value; }
    public void setValue(Boolean value) { this.value = value; }

    public Component getParent() { return parent; }
}