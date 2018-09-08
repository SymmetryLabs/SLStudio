package com.symmetrylabs.shows.summerstage;

import com.symmetrylabs.slstudio.pattern.Screen;
import heronarts.lx.LX;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;

public class FUCK extends Screen {
    public static final String GROUP_NAME = SummerStageShow.SHOW_NAME;

    DiscreteParameter textSize = new DiscreteParameter("textSize", 72, 50, 500);
    DiscreteParameter textX;
    DiscreteParameter textY;

    CompoundParameter xScale = new CompoundParameter("xScale", 2.58, 0.1, 10.0);
    CompoundParameter yScale = new CompoundParameter("yScale", 0.89, 0.1, 10.0);
    CompoundParameter hitLength = new CompoundParameter("hitLength", 300, 1, 2000);

    SinLFO hit = new SinLFO("hit", 0, 1, hitLength);
    BooleanParameter trigger = new BooleanParameter("trigger", false);
    BooleanParameter hold = new BooleanParameter("hold", false);



    public FUCK(LX lx) {
        super(lx);

        textX = new DiscreteParameter("textX", 96, 0, pWidth);
        textY = new DiscreteParameter("textY", 92, 0, pHeight);

        addParameter(textSize);
        addParameter(textX);
        addParameter(textY);

        addParameter(xScale);
        addParameter(yScale);

        hit.setLooping(false);
        startModulator(hit);

        sigma.setValue(0.0f);

        trigger.setMode(BooleanParameter.Mode.MOMENTARY);
        hold.setMode(BooleanParameter.Mode.MOMENTARY);

        trigger.addListener(lxParameter -> {
            if (trigger.isOn()) {
                hit.trigger();
            }
        });

        addParameter(hitLength);
        addParameter(trigger);
        addParameter(hold);

    }

    @Override
    public void draw() {
        float b = hit.getValuef();
        if (hold.isOn()) {
            b = 1.0f;
        }
        pg.background(0);
        pg.fill(255 * b, 0, 0);
        pg.stroke(255 * b, 0, 0);

        pg.scale(xScale.getValuef(), yScale.getValuef());

        pg.textSize(textSize.getValuei());
        pg.text("FUCK", textX.getValuei(), textY.getValuei());
    }

    @Override
    public void setup() {

    }


}
