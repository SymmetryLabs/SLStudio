package com.symmetrylabs.shows.tree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.symmetrylabs.shows.tree.ui.UIPixlites;
import com.symmetrylabs.shows.tree.ui.UITreeModelAxes;
import com.symmetrylabs.shows.tree.ui.UITreeModelingTool;
import com.symmetrylabs.shows.tree.ui.UITreeTrunk;
import com.symmetrylabs.slstudio.SLStudio;
import com.symmetrylabs.slstudio.SLStudioLX;
import heronarts.lx.LX;

import com.symmetrylabs.shows.Show;


public abstract class TreeShow implements Show {
    public static final String SHOW_NAME = "tree";

    public final Map<String, AssignablePixlite> pixlites = new HashMap<>();
    public final List<AssignablePixlite.Port> pixlitePorts = new ArrayList<>();
    public final Map<TreeModel.Branch, AssignableTenereController> controllers = new HashMap<>();

    protected void addPixlite(LX lx, AssignablePixlite pixlite) {
        pixlites.put(pixlite.ipAddress, pixlite);
        pixlitePorts.addAll(pixlite.ports);
        lx.addOutput(pixlite);
    }

    public Map<TreeModel.Branch, AssignableTenereController> getTenereControllers() {
        return controllers;
    }

    protected boolean readConfigFromDisk() {
        return true;
    }
}
