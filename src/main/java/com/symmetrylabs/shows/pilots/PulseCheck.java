package com.symmetrylabs.shows.pilots;

import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.model.Strip;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;


public class PulseCheck extends SLPattern<SLModel> {
    public static final String GROUP_NAME = PilotsShow.SHOW_NAME;

    private final PilotsModel model = (PilotsModel)lx.model;
    private CompoundParameter hues[];
    private BooleanParameter enabled[];
    private BooleanParameter alpha;
    private DiscreteParameter vis;
    public final SinLFO pulse = new SinLFO("pulse", 10, 90, 1500);

    public PulseCheck(LX lx) {
        super(lx);
        this.hues = new CompoundParameter[model.carts.size()];
        this.enabled = new BooleanParameter[model.carts.size()];
        this.alpha = new BooleanParameter("alpha", true);
        this.vis = new DiscreteParameter("visible", 0, 0, 2);
        addModulator(pulse).start();
        addParameter(vis);
        addParameter(alpha);
        for (int i = 0; i < model.carts.size(); i++) {
            this.hues[i] = new CompoundParameter("cart" + i, 180, 0, 360);
            this.enabled[i] = new BooleanParameter("EN" + i, true);
            addParameter(hues[i]);
            addParameter(enabled[i]);
        }
    }

    public void run(double deltaMs) {
        setColors(0);
        for (int i = 0; i < model.carts.size(); i++) {
            PilotsModel.Cart cart = model.carts.get(i);
            for (PilotsModel.Cart.Dataline d : cart.datalines){
                for (Strip s : d.strips){
                    LXPoint p = s.getPoints().get(s.getPoints().size() - (1 + vis.getValuei()));
                    colors[p.index] = LXColor.hsba(hues[i].getValuef(), pulse.getValuef(), enabled[i].getValuef()* 100, enabled[i].getValuef() );

                    p = s.getPoints().get(0 + vis.getValuei());
                    colors[p.index] = LXColor.hsba(hues[i].getValuef(), pulse.getValuef(), enabled[i].getValuef()* 100, enabled[i].getValuef() );
                }
            }
        }
    }
}
