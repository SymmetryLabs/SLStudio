package com.symmetrylabs.slstudio.cue;

import heronarts.lx.LX;
import heronarts.lx.LXComponent;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.StringParameter;
import org.joda.time.DateTime;
import heronarts.lx.parameter.BoundedParameter;


public class Cue extends LXComponent {
    public final StringParameter startAtStr;
    public final CompoundParameter durationMs;
    public final CompoundParameter fadeTo;
    public final BoundedParameter cuedParameter;
    private DateTime startAt;

    public Cue(LX lx, BoundedParameter cuedParameter) {
        super(lx);
        this.cuedParameter = cuedParameter;

        startAtStr = new StringParameter("startAt", "00:00");
        startAt = DateTime.now().withTime(0, 0, 0, 0);
        durationMs = (CompoundParameter) new CompoundParameter("duration", 1000, 50, 30 * 60 * 1000)
            .setExponent(4)
            .setUnits(LXParameter.Units.MILLISECONDS);
        fadeTo = new CompoundParameter("fadeTo", cuedParameter.getValue(), cuedParameter.range.v0, cuedParameter.range.v1);

        startAtStr.addListener(p -> {
                String[] timebits = startAtStr.getString().split(":");
                if (timebits.length != 2 && timebits.length != 3) {
                    return;
                }
                int h, m, s;
                try {
                    h = Integer.parseInt(timebits[0]);
                    m = Integer.parseInt(timebits[1]);
                    s = timebits.length > 2 ? Integer.parseInt(timebits[2]) : 0;
                } catch (NumberFormatException e) {
                    return;
                }
                startAt = DateTime.now().withTime(h, m, s, 0);
            });

        addParameter(startAtStr);
        addParameter(durationMs);
        addParameter(fadeTo);
    }

    @Override
    public String toString() {
        return String.format("%.3f second transition on %s to %.3f starting at %02d:%02d",
                             durationMs.getValue() / 1000.0,
                             cuedParameter.getLabel(),
                             fadeTo.getValue(),
                             startAt.getHourOfDay(), startAt.getMinuteOfHour());
    }

    public DateTime getStartTime() {
        return startAt;
    }
}
