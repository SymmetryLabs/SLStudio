package com.symmetrylabs.shows.hblamp;

import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.model.Strip;
import com.symmetrylabs.slstudio.model.StripsModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.transform.LXVector;
import java.util.ArrayList;
import java.util.List;
import heronarts.lx.model.StripModel;
import heronarts.lx.transform.LXTransform;

public class HbLampModel extends StripsModel<Strip> {
    private static final float LED_PER_M = 60.0f;
    private static final float LED_PITCH_CM = 100.0f / LED_PER_M;
    private static final float CORE_RADIUS = 5.0f;
    private static final int LEDS_PER_STRIP = 108;

    public HbLampModel() {
        this(new ArrayList<Strip>());
    }

    protected HbLampModel(List<Strip> strips) {
        super(HbLampShow.SHOW_NAME, strips);
    }

    public static HbLampModel create() {
        List<Strip> strips = new ArrayList<>();
        Strip.Metrics m = new Strip.Metrics(LEDS_PER_STRIP, LED_PITCH_CM);

        LXTransform t1 = new LXTransform()
            .translate(CORE_RADIUS, 0, 0)
            .rotateZ(Math.PI / 2);
        strips.add(new Strip("1", m, t1));

        LXTransform t2 = new LXTransform()
            .translate(0, LED_PITCH_CM * (LEDS_PER_STRIP - 1), CORE_RADIUS)
            .rotateZ(3 * Math.PI / 2);
        strips.add(new Strip("2", m, t2));

        LXTransform t3 = new LXTransform()
            .translate(-CORE_RADIUS, 0, 0)
            .rotateZ(Math.PI / 2);
        strips.add(new Strip("3", m, t3));

        LXTransform t4 = new LXTransform()
            .translate(0, LED_PITCH_CM * (LEDS_PER_STRIP - 1), -CORE_RADIUS)
            .rotateZ(3 * Math.PI / 2);
        strips.add(new Strip("4", m, t4));

        return new HbLampModel(strips);
    }
}
