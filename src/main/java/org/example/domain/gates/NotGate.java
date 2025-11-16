package org.example.domain.gates;

import org.example.domain.InputPort;
import org.example.domain.OutputPort;

public class NotGate extends LogicGate {

    public NotGate() {
        inputs.add(new InputPort(this));
        outputs.add(new OutputPort(this));
    }

    @Override
    public void evaluate() {
        outputs.get(0).setValue(!inputs.get(0).getValue());
    }
}