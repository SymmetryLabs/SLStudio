package com.symmetrylabs.shows.summerstage;

import com.symmetrylabs.slstudio.pattern.Screen;
import heronarts.lx.LX;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;

public class FUCK extends Screen {
    DiscreteParameter textSize = new DiscreteParameter("textSize", 72, 50, 500);
    DiscreteParameter textX;
    DiscreteParameter textY;

    CompoundParameter xScale = new CompoundParameter("xScale", 2.58, 0.1, 10.0);
    CompoundParameter yScale = new CompoundParameter("yScale", 0.89, 0.1, 10.0);


    public FUCK(LX lx) {
        super(lx);

        textX = new DiscreteParameter("textX", 96, 0, pWidth);
        textY = new DiscreteParameter("textY", 92, 0, pHeight);

        addParameter(textSize);
        addParameter(textX);
        addParameter(textY);

        addParameter(xScale);
        addParameter(yScale);

        sigma.setValue(0.0f);
    }

    @Override
    public void draw() {
        pg.background(0);
        pg.fill(255, 0, 0);
        pg.stroke(255, 0, 0);

        pg.scale(xScale.getValuef(), yScale.getValuef());

        pg.textSize(textSize.getValuei());
        pg.text("FUCK", textX.getValuei(), textY.getValuei());
    }

    @Override
    public void setup() {

    }


}
