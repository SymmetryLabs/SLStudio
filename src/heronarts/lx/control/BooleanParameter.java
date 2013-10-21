package heronarts.lx.control;

/**
 * A simple parameter that has a binary value of off or on
 */
public class BooleanParameter extends LXListenableParameter {

    private final String label;
    private boolean on, defaultOn;

    public BooleanParameter(String label) {
        this(label, false);
    }
    
    public BooleanParameter(String label, boolean on) {
        this.label = label;
        this.on = this.defaultOn = on;
    }
    
    @Override
    public LXParameter reset() {
        setOn(this.defaultOn);
        return this;
    }
    
    public boolean isOn() {
        return this.on;
    }
    
    public BooleanParameter setOn(boolean on) {
        setValue(on ? 1. : 0.);
        return this;
    }
    
    @Override
    protected void updateValue(double value) {
        this.on = (value > 0);
    }

    @Override
    public double getValue() {
        return this.on ? 1. : 0.;
    }

    @Override
    public String getLabel() {
        return this.label;
    }

}
