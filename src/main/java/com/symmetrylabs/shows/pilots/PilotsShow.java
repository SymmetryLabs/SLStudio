package com.symmetrylabs.shows.pilots;

import com.google.gson.Gson;
import com.symmetrylabs.shows.HasWorkspace;
import com.symmetrylabs.shows.Show;
import com.symmetrylabs.slstudio.SLStudioLX;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.model.Strip;
import com.symmetrylabs.slstudio.workspaces.Workspace;
import heronarts.lx.LXChannel;
import heronarts.lx.LXEngine;
import heronarts.lx.color.LXColor;
import heronarts.lx.output.LXOutput;
import heronarts.lx.transform.LXVector;
import processing.data.JSONArray;
import processing.data.JSONObject;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This file creates and positions "carts" and creates one pixlite for each of them
 */
public class PilotsShow implements Show, HasWorkspace, CartConfigurator.ConfigChangedListener, LXEngine.Listener {
    static final String SHOW_NAME = "pilots";

    static final String IP_CONFIGS_FILENAME = "cart_IP_configs.txt";

    static final float RED_HUE = 0;
    static final float YELLOW_HUE = 51;
    static final int RED = LXColor.hsb(RED_HUE, 100, 100);
    static final int YELLOW = LXColor.hsb(YELLOW_HUE, 85, 100);

    private SLStudioLX lx;
    private CartConfigurator configurator;
    private List<PilotsPixlite> outputs = new ArrayList<>();
    private Workspace workspace;

    private PilotsModel.Cart[] carts = new PilotsModel.Cart[] {
        /* no extra cart spacing here because they're not actually adjacent;
         * FSL is in front of BSL. */
        new PilotsModel.Cart(CartConfig.FSL,  new LXVector(
            0, 0, 0)),

        new PilotsModel.Cart(CartConfig.BSL,  new LXVector(
            1 * PilotsModel.CART_X, 0, 3 * PilotsModel.STRIP_LENGTH)),

        new PilotsModel.Cart(CartConfig.BSCL, new LXVector(
            2 * PilotsModel.CART_X + PilotsModel.CART_SPACING, 0, 3 * PilotsModel.STRIP_LENGTH)),

        new PilotsModel.Cart(CartConfig.BSC,  new LXVector(
            3 * PilotsModel.CART_X + 2 * PilotsModel.CART_SPACING, 0, 3 * PilotsModel.STRIP_LENGTH)),

        new PilotsModel.Cart(CartConfig.BSCR, new LXVector(
            4 * PilotsModel.CART_X + 3 * PilotsModel.CART_SPACING, 0, 3 * PilotsModel.STRIP_LENGTH)),

        new PilotsModel.Cart(CartConfig.BSR,  new LXVector(
            5 * PilotsModel.CART_X + 4 * PilotsModel.CART_SPACING, 0, 3 * PilotsModel.STRIP_LENGTH)),

        new PilotsModel.Cart(CartConfig.FSR,  new LXVector(
            6 * PilotsModel.CART_X + 4 * PilotsModel.CART_SPACING, 0, 0))

        // Temporary, just to send the "same thing" to each cart
//        new PilotsModel.Cart(CartConfig.FSL,  new LXVector(0, 0, 0)),
//        new PilotsModel.Cart(CartConfig.BSL,  new LXVector(0, 0, 0)),
//        new PilotsModel.Cart(CartConfig.BSCL,  new LXVector(0, 0, 0)),
//        new PilotsModel.Cart(CartConfig.BSC,  new LXVector(0, 0, 0)),
//        new PilotsModel.Cart(CartConfig.BSCR,  new LXVector(0, 0, 0)),
//        new PilotsModel.Cart(CartConfig.BSR,  new LXVector(0, 0, 0)),
//        new PilotsModel.Cart(CartConfig.FSR,  new LXVector(0, 0, 0))
    };

    @Override
    public SLModel buildModel() {
        List<Strip> strips = new ArrayList<>();
        for (PilotsModel.Cart cart : carts) {
            strips.addAll(cart.strips);
        }
        return new PilotsModel(strips, Arrays.asList(carts));
    }

    @Override
    public void setupLx(SLStudioLX lx) {
        this.lx = lx;
        try {
            onConfigChanged(CartConfig.readConfigsFromFile());
        } catch (IOException e) {
            e.printStackTrace();
        }

        lx.engine.addListener(this);
        for (LXChannel c : lx.engine.channels) {
            c.autoDisable.setValue(true);
        }
    }

    @Override
    public void setupUi(SLStudioLX lx, SLStudioLX.UI ui) {
        workspace = new Workspace(lx, ui, "shows/pilots");

        configurator = new CartConfigurator(ui, 0, 0, ui.rightPane.utility.getContentWidth());
        configurator.setListener(this);

        try {
            configurator.applyConfigs(CartConfig.readConfigsFromFile());
        } catch (IOException e) {
            e.printStackTrace();
            // if we couldn't read from file apply the defaults..
            configurator.applyConfigs(CartConfig.defaultConfigs());
        }



        configurator.addToContainer(ui.rightPane.utility);
    }

    @Override
    public void onConfigChanged(CartConfig[] configs) {
        for (LXOutput output : outputs) {
            lx.removeOutput(output);
        }
        PilotsModel model = (PilotsModel) lx.model;
        outputs.clear();

        Gson gson = new Gson();

        for (CartConfig cc : configs) {
            outputs.add(new PilotsPixlite(lx, cc.address, model.getCartById(cc.modelId)));
        }
        for (LXOutput output : outputs) {
            lx.addOutput(output);
        }

        // encode the configs
        String jsonString = gson.toJson(configs);
        // write it to a file
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(IP_CONFIGS_FILENAME);
            fileWriter.write(jsonString);
            fileWriter.close();        // dump info to a file
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public Workspace getWorkspace() {
        return workspace;
    }

    @Override
    public void channelAdded(LXEngine lxEngine, LXChannel lxChannel) {
        lxChannel.autoDisable.setValue(true);
    }

    @Override
    public void channelRemoved(LXEngine lxEngine, LXChannel lxChannel) {
    }

    @Override
    public void channelMoved(LXEngine lxEngine, LXChannel lxChannel) {
    }
}
