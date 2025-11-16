package org.example.domain.gates;

import org.example.domain.InputPort;
import org.example.domain.OutputPort;

public class NandGate extends LogicGate {

    public NandGate() {
        inputs.add(new InputPort(this));
        inputs.add(new InputPort(this));
        outputs.add(new OutputPort(this));
    }

    @Override
    public void evaluate() {
        boolean result = !(inputs.get(0).getValue() && inputs.get(1).getValue());
        outputs.get(0).setValue(result);
    }
}