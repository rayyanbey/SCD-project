package org.example.domain.gates;

import org.example.domain.InputPort;
import org.example.domain.OutputPort;

public class XorGate extends LogicGate {

    public XorGate() {
        inputs.add(new InputPort(this));
        inputs.add(new InputPort(this));
        outputs.add(new OutputPort(this));
    }

    @Override
    public void evaluate() {
        boolean a = inputs.get(0).getValue();
        boolean b = inputs.get(1).getValue();
        outputs.get(0).setValue(a ^ b);
    }
}