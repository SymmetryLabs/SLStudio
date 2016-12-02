package heronarts.lx.color;

import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.parameter.LXListenableParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;

public class ColorParameter extends LXListenableParameter implements LXParameterListener {

    public final BoundedParameter hue;
    public final BoundedParameter saturation;
    public final BoundedParameter brightness;

    private int color;
    private boolean internalValueUpdate = false;

    public ColorParameter(String label) {
        this(label, 0xff000000);
    }

    public ColorParameter(String label, int color) {
        super(label, Double.longBitsToDouble(color));
        double h = LXColor.h(color);
        this.hue = new BoundedParameter(label+"-Hue", Double.isNaN(h) ? 0 : h, 0, 359);
        this.saturation = new BoundedParameter(label+"-Saturation", LXColor.s(color), 0, 100);
        this.brightness = new BoundedParameter(label+"-Brightness", LXColor.b(color), 0, 100);
        this.hue.addListener(this);
        this.saturation.addListener(this);
        this.brightness.addListener(this);
        this.color = color;
    }

    public int getColor() {
        return this.color;
    }

    public ColorParameter setColor(int color) {
        setValue(Double.longBitsToDouble(color));
        return this;
    }

    @Override
    protected double updateValue(double value) {
        this.internalValueUpdate = true;
        this.color = (int) Double.doubleToRawLongBits(value);
        this.saturation.setValue(LXColor.s(this.color));
        this.brightness.setValue(LXColor.b(this.color));
        double h = LXColor.h(this.color);
        if (!Double.isNaN(h)) {
            this.hue.setValue(LXColor.h(this.color));
        }
        this.internalValueUpdate = false;
        return value;
    }

    @Override
    public void onParameterChanged(LXParameter parameter) {
        if (!this.internalValueUpdate) {
            setColor(LXColor.hsb(
                this.hue.getValue(),
                this.saturation.getValue(),
                this.brightness.getValue())
            );
        }
    }

}
