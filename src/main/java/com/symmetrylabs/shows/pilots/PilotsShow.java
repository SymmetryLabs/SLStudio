package com.symmetrylabs.shows.pilots;

import com.symmetrylabs.shows.Show;
import com.symmetrylabs.slstudio.SLStudioLX;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.model.Strip;
import com.symmetrylabs.slstudio.model.StripsModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.transform.LXVector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * This file creates and positions "carts" and creates one pixlite for each of them
 */
public class PilotsShow implements Show {
    static final String SHOW_NAME = "pilots";

    private PilotsModel.Cart[] carts = new PilotsModel.Cart[] {
        /* no extra cart spacing here because they're not actually adjacent;
         * FSL is in front of BSL. */
        new PilotsModel.Cart("FSL",  new LXVector(
            24 * PilotsModel.STRIP_LENGTH + 4 * PilotsModel.CART_SPACING, 0, 0)),

        new PilotsModel.Cart("BSL",  new LXVector(
            20 * PilotsModel.STRIP_LENGTH + 4 * PilotsModel.CART_SPACING, 0, 3 * PilotsModel.STRIP_LENGTH)),

        new PilotsModel.Cart("BSCL", new LXVector(
            16 * PilotsModel.STRIP_LENGTH + 3 * PilotsModel.CART_SPACING, 0, 3 * PilotsModel.STRIP_LENGTH)),

        new PilotsModel.Cart("BSC",  new LXVector(
            12 * PilotsModel.STRIP_LENGTH + 2 * PilotsModel.CART_SPACING, 0, 3 * PilotsModel.STRIP_LENGTH)),

        new PilotsModel.Cart("BSCR", new LXVector(
            8 * PilotsModel.STRIP_LENGTH + PilotsModel.CART_SPACING, 0, 3 * PilotsModel.STRIP_LENGTH)),

        new PilotsModel.Cart("BSR",  new LXVector(
            4 * PilotsModel.STRIP_LENGTH, 0, 3 * PilotsModel.STRIP_LENGTH)),

        new PilotsModel.Cart("FSR",  new LXVector(
            0, 0, 0)),
    };

    @Override
    public SLModel buildModel() {
        List<Strip> strips = new ArrayList<>();
        for (PilotsModel.Cart cart : carts) {
            strips.addAll(cart.createStrips());
        }
        return new PilotsModel(strips, Arrays.asList(carts));
    }

    @Override
    public void setupLx(SLStudioLX lx) {
        lx.addOutput(
            new PilotsPixlite(lx, "10.200.1.10", ((PilotsModel)lx.model).getCartById("FSL"))
        );
    }

    @Override
    public void setupUi(SLStudioLX lx, SLStudioLX.UI ui) {
    }
}
