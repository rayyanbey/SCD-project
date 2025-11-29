package org.example.domain;
public class InputPort extends Port {

    public InputPort(Component parent) {
        super(parent);
    }

    public void connect(OutputPort source) {
        Connector c = new Connector(source, this);
        source.addConnection(c);
    }
}