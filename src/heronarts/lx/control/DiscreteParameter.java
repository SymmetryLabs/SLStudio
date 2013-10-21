package heronarts.lx.control;

/**
 * Parameter type with a discrete set of possible integer values from [0, range-1].
 */
public class DiscreteParameter extends LXListenableParameter {

    private final String label;
    private int range;
    private int value;
    private int defaultValue;
    
    public DiscreteParameter(String label, int range) {
        this(label, range, 0);
    }
    
    public DiscreteParameter(String label, int range, int value) {
        this.label = label;
        this.range = range;
        this.value = this.defaultValue = value;
    }
    
    @Override
    public LXParameter reset() {
        setValue(this.defaultValue);
        return this;
    }
        
    @Override
    protected void updateValue(double value) {
        this.value = ((int)value % this.range);
    }

    @Override
    public double getValue() {
        return this.value;
    }

    @Override
    public String getLabel() {
        return this.label;
    }

}
