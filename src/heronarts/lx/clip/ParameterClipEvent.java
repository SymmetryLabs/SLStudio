package heronarts.lx.clip;

import heronarts.lx.LXUtils;
import heronarts.lx.parameter.LXNormalizedParameter;

public class ParameterClipEvent extends LXClipEvent {

    public final LXNormalizedParameter parameter;
    private double normalized;

    ParameterClipEvent(LXClipLane lane, LXNormalizedParameter parameter) {
        this(lane, parameter, parameter.getNormalized());
    }

    ParameterClipEvent(LXClipLane lane, LXNormalizedParameter parameter, double normalized) {
        super(lane, parameter.getComponent());
        this.parameter = parameter;
        this.normalized = normalized;
    }

    public ParameterClipEvent setNormalized(double normalized) {
        normalized = LXUtils.constrain(normalized, 0, 1);
        if (this.normalized != normalized) {
            this.normalized = normalized;
            this.lane.onChange.bang();
        }
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
