package org.example.domain.io;

import org.example.domain.Component;
import org.example.domain.OutputPort;

public class ClockSource extends Component {

    private int periodTicks = 1;    // number of simulation passes before toggle
    private int counter = 0;        // counts up to periodTicks
    private boolean state = false;  // current boolean output state

    /**
     * Default constructor with period 1 (toggles each tick).
     */
    public ClockSource() {
        this(1);
    }

    /**
     * Create a clock with the given period (in simulator ticks).
     * @param periodTicks number of simulator passes between toggles (must be >=1)
     */
    public ClockSource(int periodTicks) {
        if (periodTicks < 1) periodTicks = 1;
        this.periodTicks = periodTicks;

        // Clock is an output-only component
        outputs.add(new OutputPort(this));
    }

    /**
     * Called by the simulator each pass. Implements discrete tick behavior.
     * After periodTicks calls, flips the output value and resets the counter.
     */
    @Override
    public void evaluate() {
        counter++;
        if (counter >= periodTicks) {
            state = !state;
            outputs.get(0).setValue(state);
            counter = 0;
        } else {
            // keep the current state on the output (important if simulator checks outputs snapshot)
            outputs.get(0).setValue(state);
        }
    }

    /**
     * Force set the clock to a particular logical state immediately.
     * Useful for tests or when user manually sets initial clock state.
     */
    public void setState(boolean state) {
        this.state = state;
        outputs.get(0).setValue(state);
    }

    public boolean getState() {
        return state;
    }

    public int getPeriodTicks() {
        return periodTicks;
    }

    public void setPeriodTicks(int periodTicks) {
        if (periodTicks < 1) periodTicks = 1;
        this.periodTicks = periodTicks;
    }

    /**
     * Reset the internal counter (useful when re-initializing simulation).
     */
    public void reset() {
        this.counter = 0;
    }
}