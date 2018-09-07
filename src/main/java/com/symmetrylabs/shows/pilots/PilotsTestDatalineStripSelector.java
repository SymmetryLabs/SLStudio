package com.symmetrylabs.shows.pilots;

import heronarts.lx.LX;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.color.LXColor;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import com.symmetrylabs.slstudio.model.Strip;


public class PilotsTestDatalineStripSelector extends SLPattern<SLModel> {
    public static final String GROUP_NAME = PilotsShow.SHOW_NAME;

    private final PilotsModel model;
    private final DiscreteParameter selectedStrip;
    private final CompoundParameter brightness = new CompoundParameter("bri", 50, 0, 100);
    private final SinLFO pulse = new SinLFO("pulse", 50, 100, 1000);

    public PilotsTestDatalineStripSelector(LX lx) {
        super(lx);
        this.model = (PilotsModel)lx.model;

        this.selectedStrip = new DiscreteParameter("strip", 1, 1, 13);
        addParameter(selectedStrip);
        addParameter(brightness);
        addModulator(pulse).start();
    }

    public void run(double deltaMs) {
        setColors(0);
        for (PilotsModel.Cart cart : model.carts) {

            int hue = 0; // distinguish different datalines
            int di = 0;

            for (PilotsModel.Cart.Dataline dataline : cart.datalines) {
                int pi = 0;
                float sat = (di < 5) ? 0 : 100;

                for (LXPoint p : dataline.getPoints()) {
                    colors[p.index] = pi++ % 2 == 0 ? lx.hsb(hue, sat, brightness.getValuef()) : 0;
                }

                // highlight the selected strip
                if (selectedStrip.getValuei() < dataline.strips.size()+1) {
                    Strip strip = dataline.strips.get(selectedStrip.getValuei()-1);

                    for (LXPoint p : strip.points) {
                        colors[p.index] = lx.hsb(hue, sat, pulse.getValuef());
                    }
                }
                di++;
                hue += 120;
            }
        }
    }
}
