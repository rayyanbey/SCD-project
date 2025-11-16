package org.example.domain.io;

import org.example.domain.Component;
import org.example.domain.OutputPort;

public class Switch extends Component {

    public Switch() {
        outputs.add(new OutputPort(this));
    }

    public void setOn(boolean on) {
        outputs.get(0).setValue(on);
    }

    @Override
    public void evaluate() {
        // Switch value stays what the user sets
    }
}