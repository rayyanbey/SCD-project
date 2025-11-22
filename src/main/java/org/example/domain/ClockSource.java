package org.example.domain;

public class ClockSource extends Component {

    private int periodTicks = 1;
    private int counter = 0;
    private boolean state = false;

    public ClockSource() { this(1); }

    public ClockSource(int periodTicks) {
        if (periodTicks < 1) periodTicks = 1;
        this.periodTicks = periodTicks;
        outputs.add(new OutputPort(this));
    }

    @Override
    public void evaluate() {
        counter++;
        if (counter >= periodTicks) {
            state = !state;
            outputs.get(0).setValue(state);
            counter = 0;
        } else {
            outputs.get(0).setValue(state);
        }
    }

    public void setPeriodTicks(int periodTicks) { this.periodTicks = Math.max(1, periodTicks); }
    public int getPeriodTicks() { return periodTicks; }
    public void setState(boolean s) { this.state = s; outputs.get(0).setValue(s); }
    public boolean getState() { return state; }
    public void reset() { counter = 0; }
}