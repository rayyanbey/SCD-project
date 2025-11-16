package org.example.domain;

public class Connector {

    private OutputPort source;
    private InputPort dest;

    public Connector(OutputPort source, InputPort dest) {
        this.source = source;
        this.dest = dest;
    }

    public void propagate() {
        dest.setValue(source.getValue());
    }

    public OutputPort getSource() { return source; }
    public InputPort getDest() { return dest; }
}