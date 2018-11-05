package com.symmetrylabs.shows.pilots;

import heronarts.lx.LX;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.model.LXPoint;
import heronarts.lx.color.LXColor;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import com.symmetrylabs.slstudio.model.Strip;

public class PilotsTestDatalineSelector extends SLPattern<SLModel> {
    public static final String GROUP_NAME = PilotsShow.SHOW_NAME;

    private final PilotsModel model;
    private final DiscreteParameter selectedDataline;

    private final CompoundParameter pix = new CompoundParameter("pix", 0, 0, 400);

    public PilotsTestDatalineSelector(LX lx) {
        super(lx);
        this.model = (PilotsModel)lx.model;
        this.selectedDataline = new DiscreteParameter("cart", 1, 1, model.carts.size())
            .setOptions(new String[] {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15"});
        addParameter(selectedDataline);
        addParameter(pix);
    }

    public void run(double deltaMs) {
        setColors(0);
        for (PilotsModel.Cart cart : model.carts) {
            PilotsModel.Cart.Dataline dataline = cart.getDatalineByChannel(selectedDataline.getOption());

            int i = 0;
            for (LXPoint p : dataline.getPoints()) {
                if (i < pix.getValuef()){
                    colors[p.index] = LXColor.GREEN;
                }
                else{
                    colors[p.index] = LXColor.RED;
                }
                i++;
            }
        }
    }
}
