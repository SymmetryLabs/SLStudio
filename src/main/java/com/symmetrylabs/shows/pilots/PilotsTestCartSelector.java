package com.symmetrylabs.shows.pilots;

import heronarts.lx.LX;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.model.LXPoint;
import heronarts.lx.color.LXColor;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import com.symmetrylabs.slstudio.model.Strip;

public class PilotsTestCartSelector extends SLPattern<SLModel> {
    public static final String GROUP_NAME = PilotsShow.SHOW_NAME;

    private final PilotsModel model;
    private final DiscreteParameter selectedCart;

    public PilotsTestCartSelector(LX lx) {
        super(lx);
        this.model = (PilotsModel)lx.model;
        this.selectedCart = new DiscreteParameter("cart", 1, 1, model.carts.size()+1);
        addParameter(selectedCart);
    }

    public void run(double deltaMs) {
        setColors(0);
        PilotsModel.Cart cart = model.carts.get(selectedCart.getValuei()-1);
        for (Strip strip : cart.strips) {
            for (LXPoint p : strip.points) {
                colors[p.index] = LXColor.GREEN;
            }
        }
    }
}
