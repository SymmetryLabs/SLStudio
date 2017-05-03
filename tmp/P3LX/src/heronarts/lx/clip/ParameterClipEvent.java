package heronarts.lx.clip;

import heronarts.lx.parameter.LXNormalizedParameter;

public class ParameterClipEvent extends LXClipEvent {

    public final LXNormalizedParameter parameter;
    private double normalized;

    ParameterClipEvent(LXClip clip, LXNormalizedParameter parameter) {
        this(clip, parameter, parameter.getNormalized());
    }

    ParameterClipEvent(LXClip clip, LXNormalizedParameter parameter, double normalized) {
        super(clip, parameter.getComponent());
        this.parameter = parameter;
        this.normalized = normalized;
    }

    public ParameterClipEvent setNormalized(double normalized) {
        this.normalized = normalized;
        return this;
    }

    public double getNormalized() {
        return this.normalized;
    }

    @Override
    public void execute() {
        this.parameter.setNormalized(this.normalized);
    }
}
