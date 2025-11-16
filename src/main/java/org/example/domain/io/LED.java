package org.example.domain.io;

import org.example.domain.Component;
import org.example.domain.InputPort;

public class LED extends Component {

    private boolean lit = false;

    public LED() {
        inputs.add(new InputPort(this));
    }

    @Override
    public void evaluate() {
        lit = inputs.get(0).getValue();
    }

    public boolean isLit() {
        return lit;
    }
}