package com.symmetrylabs.shows.tree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.symmetrylabs.shows.tree.ui.UITreeModelAxes;
import com.symmetrylabs.shows.tree.ui.UITreeTrunk;
import com.symmetrylabs.slstudio.SLStudio;
import com.symmetrylabs.slstudio.SLStudioLX;
import heronarts.lx.LX;

import com.symmetrylabs.shows.Show;


public abstract class TreeShow implements Show {
    public static final String SHOW_NAME = "tree";

    public final Map<String, AssignablePixlite> pixlites = new HashMap<>();
    public final List<AssignablePixlite.Port> pixlitePorts = new ArrayList<>();

    protected void addPixlite(LX lx, AssignablePixlite pixlite) {
        pixlites.put(pixlite.ipAddress, pixlite);
        pixlitePorts.addAll(pixlite.ports);
        lx.addOutput(pixlite);
    }

    public void setupLx(SLStudioLX lx) {
        lx.engine.registerComponent("treeModelingTool", TreeModelingTool.getInstance(lx));

        //anemometer = new Anemometer();
        //lx.engine.modulation.addModulator(anemometer.speedModulator);
        //lx.engine.modulation.addModulator(anemometer.directionModulator);
        //lx.engine.registerComponent("anemomter", anemometer);
        //lx.engine.addLoopTask(anemometer);
        //anemometer.start();
    }

    @Override
    public void setupUi(SLStudioLX lx, SLStudioLX.UI ui) {
        //ui.preview.addComponent(new UITreeTrunk(SLStudio.applet));

        ui.preview.addComponent(UITreeModelAxes.getInstance(lx));
    }

}
