package com.symmetrylabs.shows.tree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.symmetrylabs.shows.base.SLShow;
import com.symmetrylabs.shows.tree.ui.UIPixlites;
import com.symmetrylabs.shows.tree.ui.UITreeModelAxes;
import com.symmetrylabs.shows.tree.ui.UITreeModelingTool;
import com.symmetrylabs.shows.tree.ui.UITreeTrunk;
import com.symmetrylabs.slstudio.SLStudio;
import com.symmetrylabs.slstudio.SLStudioLX;
import com.symmetrylabs.slstudio.ApplicationState;
import heronarts.lx.LX;

import com.symmetrylabs.shows.Show;


public abstract class TreeShow extends SLShow implements Show {
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

    public void setupLx(LX lx) {
        super.setupLx(lx);
        boolean readConfig = readConfigFromDisk();
        if (!readConfig) {
            ApplicationState.setWarning("TreeShow", "show is set to not read tree model from disk, model changes will be saved but not loaded on restart");
        }
        lx.engine.registerComponent("treeModelingTool", TreeModelingTool.getInstance(lx, readConfig));
    }

    @Override
    public void setupUi(SLStudioLX lx, SLStudioLX.UI ui) {
        super.setupUi(lx, ui);
        ui.preview.addComponent(new UITreeTrunk(SLStudio.applet));

        ui.preview.addComponent(UITreeModelAxes.getInstance(lx));

        UITreeModelAxes uiTreeModelAxes = UITreeModelAxes.getInstance(lx);
        ui.preview.addComponent(uiTreeModelAxes);

        UITreeModelingTool.instance = new UITreeModelingTool(
            ui, TreeModelingTool.getInstance(lx), 0, 0, ui.rightPane.model.getContentWidth());
        UITreeModelingTool.instance.addToContainer(ui.rightPane.model);
    }

    protected boolean readConfigFromDisk() {
        return true; // need to be able to get this intelligently (i.e. does the file exist?)
    }
}
