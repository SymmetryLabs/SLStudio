package com.symmetrylabs.shows.pilots;

import com.symmetrylabs.shows.Show;
import com.symmetrylabs.slstudio.SLStudioLX;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.model.Strip;
import com.symmetrylabs.slstudio.ui.UIOverriddenRightPane;
import com.symmetrylabs.slstudio.ui.UIWorkspaces;
import com.symmetrylabs.slstudio.workspaces.Workspaces;
import heronarts.lx.color.LXColor;
import heronarts.lx.output.LXOutput;
import heronarts.lx.transform.LXVector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This file creates and positions "carts" and creates one pixlite for each of them
 */
public class PilotsShow implements Show, CartConfigurator.ConfigChangedListener {
    static final String SHOW_NAME = "pilots";

    static final float RED_HUE = 0;
    static final float YELLOW_HUE = 51;
    static final int RED = LXColor.hsb(RED_HUE, 100, 100);
    static final int YELLOW = LXColor.hsb(YELLOW_HUE, 85, 100);

    private SLStudioLX lx;
    private CartConfigurator configurator;
    private List<PilotsPixlite> outputs = new ArrayList<>();
    private Workspaces workspaces;

    private PilotsModel.Cart[] carts = new PilotsModel.Cart[] {
        /* no extra cart spacing here because they're not actually adjacent;
         * FSL is in front of BSL. */
        new PilotsModel.Cart(CartConfig.FSL,  new LXVector(
            24 * PilotsModel.STRIP_LENGTH + 4 * PilotsModel.CART_SPACING, 0, 0)),

        new PilotsModel.Cart(CartConfig.BSL,  new LXVector(
            20 * PilotsModel.STRIP_LENGTH + 4 * PilotsModel.CART_SPACING, 0, 3 * PilotsModel.STRIP_LENGTH)),

        new PilotsModel.Cart(CartConfig.BSCL, new LXVector(
            16 * PilotsModel.STRIP_LENGTH + 3 * PilotsModel.CART_SPACING, 0, 3 * PilotsModel.STRIP_LENGTH)),

        new PilotsModel.Cart(CartConfig.BSC,  new LXVector(
            12 * PilotsModel.STRIP_LENGTH + 2 * PilotsModel.CART_SPACING, 0, 3 * PilotsModel.STRIP_LENGTH)),

        new PilotsModel.Cart(CartConfig.BSCR, new LXVector(
            8 * PilotsModel.STRIP_LENGTH + PilotsModel.CART_SPACING, 0, 3 * PilotsModel.STRIP_LENGTH)),

        new PilotsModel.Cart(CartConfig.BSR,  new LXVector(
            4 * PilotsModel.STRIP_LENGTH, 0, 3 * PilotsModel.STRIP_LENGTH)),

        new PilotsModel.Cart(CartConfig.FSR,  new LXVector(
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
        this.lx = lx;
        onConfigChanged(CartConfig.defaultConfigs());

        workspaces = new Workspaces(lx, "shows/pilots");
    }

    @Override
    public void setupUi(SLStudioLX lx, SLStudioLX.UI ui) {
        configurator = new CartConfigurator(ui, 0, 0, ui.rightPane.utility.getContentWidth());
        configurator.setListener(this);
        configurator.applyConfigs(CartConfig.defaultConfigs());
        configurator.addToContainer(ui.rightPane.utility);

        UIWorkspaces wsui = new UIWorkspaces(ui, lx, workspaces, 0, 0, ui.leftPane.global.getContentWidth());
        wsui.addToContainer(ui.leftPane.global);
    }

    @Override
    public void onConfigChanged(CartConfig[] configs) {
        for (LXOutput output : outputs) {
            lx.removeOutput(output);
        }
        PilotsModel model = (PilotsModel) lx.model;
        outputs.clear();
        for (CartConfig cc : configs) {
            outputs.add(new PilotsPixlite(lx, cc.address, model.getCartById(cc.modelId)));
        }
        for (LXOutput output : outputs) {
            lx.addOutput(output);
        }
    }
}
