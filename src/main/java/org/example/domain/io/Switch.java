package org.example.domain.io;

import org.example.domain.Component;
import org.example.domain.OutputPort;

public class Switch extends Component {

    private boolean isOn = false;

    public Switch() {
        outputs.add(new OutputPort(this));
    }

    public void setOn(boolean on) {
        this.isOn = on;
    }

    @Override
    public void evaluate() {
        outputs.get(0).setValue(isOn);
    }
}