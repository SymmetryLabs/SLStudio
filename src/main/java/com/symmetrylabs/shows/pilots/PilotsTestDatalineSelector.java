package com.symmetrylabs.shows.pilots;

import heronarts.lx.LX;
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

    public PilotsTestDatalineSelector(LX lx) {
        super(lx);
        this.model = (PilotsModel)lx.model;
        this.selectedDataline = new DiscreteParameter("cart", 1, 1, model.carts.size())
            .setOptions(new String[] {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15"});
        addParameter(selectedDataline);
    }

    public void run(double deltaMs) {
        setColors(0);
        for (PilotsModel.Cart cart : model.carts) {
            PilotsModel.Cart.Dataline dataline = cart.getDatalineByChannel(selectedDataline.getOption());

            for (LXPoint p : dataline.getPoints()) {
                colors[p.index] = LXColor.GREEN;
            }
        }
    }
}
