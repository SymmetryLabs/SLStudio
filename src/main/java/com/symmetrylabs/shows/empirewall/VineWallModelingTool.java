package com.symmetrylabs.shows.empirewall;

import java.util.ArrayList;
import java.util.List;
import com.symmetrylabs.slstudio.SLStudioLX;

import com.google.gson.JsonObject;

import heronarts.lx.LX;
import heronarts.lx.LXComponent;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.ObjectParameter;
import heronarts.lx.parameter.EnumParameter;
import heronarts.lx.parameter.StringParameter;
import heronarts.lx.parameter.BooleanParameter;

import com.symmetrylabs.slstudio.SLStudio;
import com.symmetrylabs.shows.Show;
import com.symmetrylabs.shows.empirewall.config.*;
import com.symmetrylabs.shows.empirewall.*;
import com.symmetrylabs.shows.tree.TreeModel;
import com.symmetrylabs.shows.empirewall.ui.UIVineWallModelingTool;


public class VineWallModelingTool extends LXComponent {

    public final LX lx;

    private static VineWallModelingTool instance = null;

    public final VineModel vineWall;
    public final VineWallConfigStore store;

    public final ObjectParameter selectedVine;
    public final DiscreteParameter[] selectedLeaves;

    public final LeafManipulator leafManipulator;

    public final BooleanParameter onSave = new BooleanParameter("save", false).setMode(BooleanParameter.Mode.MOMENTARY);

    private VineWallModelingTool(LX lx, boolean readConfigFromDisk) {
        super(lx, "VineModelingTool");
        this.lx = lx;
        this.vineWall = (VineModel) lx.model;
        this.store = new VineWallConfigStore(lx, readConfigFromDisk);

        this.leafManipulator = new LeafManipulator(lx);
        addSubcomponent(leafManipulator);

        this.selectedVine = new ObjectParameter<VineModel.Vine>("selectedVine", vineWall.getVinesArray());
        this.selectedLeaves = new DiscreteParameter[vineWall.vines.size()];

        onSave.addListener(parameter -> {
            store.writeConfig();
        });

        for (int i = 0; i < vineWall.vines.size(); i++) {
            VineModel.Vine vine = vineWall.vines.get(i);
            selectedLeaves[i] = new DiscreteParameter("selectedLeaf", 0, 0, vine.getLeavesArray().length);

            int numLeaves = vine.getLeavesArray().length;
            String[] numLeavesOptions = new String[numLeaves];
            for (int j = 0; j < numLeaves; j++) {
                numLeavesOptions[j] = Integer.toString(j+1);
            }
            selectedLeaves[i].setOptions(numLeavesOptions); 
            selectedLeaves[i].addListener(parameter -> {
                leafManipulator.repurposeParameters();
            });
        }

        // String[] optionNames = new String[getSelectedBranch().getTwigsArray().length];
        // for (int i = 0; i < optionNames.length; i++) {
        //     optionNames[i] = Integer.toString(i);
        // }
        // selectedTwig.setOptions(optionNames);

        selectedVine.addListener(parameter -> {
            // selectedLeaf = new ObjectParameter<TreeModel_v2.Leaf>("selectedLeaf", getSelectedVine().getLeavesArray());

            // int numLeaves = getSelectedVine().getLeavesArray().length;
            // String[] numLeavesOptions = new String[numLeaves];
            // for (int i = 0; i < numLeaves; i++) {
            //     numLeavesOptions[i] = Integer.toString(i+1);
            // }
            // selectedLeaf.setOptions(numLeavesOptions);
            // leafManipulator.repurposeParameters();

            if (UIVineWallModelingTool.getInstance() != null) {
                UIVineWallModelingTool.getInstance().leafSelectorKnob.setParameter(selectedLeaves[selectedVine.getValuei()]);
            }

            // selectedLeaf.addListener(parameter1 -> {
            //     leafManipulator.repurposeParameters();
            //     System.out.println("Current Leaf Selected: " + selectedLeaf.getValuei());
            // });
        });

        leafManipulator.repurposeParameters();
    }

    public static VineWallModelingTool getInstance(LX lx) {
        return getInstance(lx, true);
    }

    public static VineWallModelingTool getInstance(LX lx, boolean readConfigFromDisk) {
        if (instance == null) {
            instance = new VineWallModelingTool(lx, readConfigFromDisk);
        }
        return instance;
    }

    public static VineWallModelingTool getInstance() {
        if (instance != null) {
            return instance;
        }
        return null;
    }

    public VineModel.Vine getSelectedVine() {
        return (VineModel.Vine) selectedVine.getObject();
    }

    public void setSelectedVine(VineModel.Vine vine) {
        selectedVine.setValue(vine);
    }

    public TreeModel.Leaf getSelectedLeaf() {
        int i = selectedLeaves[selectedVine.getValuei()].getValuei();
        return vineWall.vines.get(selectedVine.getValuei()).leaves.get(i);
    }

    public LeafConfig getSelectedLeafConfig() {
        int i = getSelectedVine().leaves.indexOf(getSelectedLeaf());
        return getSelectedVine().getConfig().getLeaves().get(i);
    }

    // public void setSelectedLeaf(TreeModel_v2.Leaf leaf) {
    //     int i = selectedVine.getValuei();
    //     selectedLeaves[i].setObjects(getSelectedVine().getLeavesArray());
    //     selectedLeaves[i].setValue(leaf);
    // }

    @Override
    // this is just for now for now, we probably want as a separate saving event than the lxp
    public void save(LX lx, JsonObject obj) {
        store.writeConfig();
    }

    // (todo) make these a little more elegant
    public class LeafManipulator extends LXComponent {
        private boolean disableParameters = false;

        public final CompoundParameter x = new CompoundParameter("x", LeafConfig.DEFAULT_X, LeafConfig.MIN_X, LeafConfig.MAX_X);
        public final CompoundParameter y = new CompoundParameter("y", LeafConfig.DEFAULT_Y, LeafConfig.MIN_Y, LeafConfig.MAX_Y);
        public final CompoundParameter z    = new CompoundParameter("z", LeafConfig.DEFAULT_Z, LeafConfig.MIN_Z, LeafConfig.MAX_Z);
        public final CompoundParameter xRot = new CompoundParameter("xRot", LeafConfig.DEFAULT_X_ROT, LeafConfig.MIN_X_ROT, LeafConfig.MAX_X_ROT);
        public final CompoundParameter yRot = new CompoundParameter("yRot", LeafConfig.DEFAULT_Y_ROT, LeafConfig.MIN_Y_ROT, LeafConfig.MAX_Y_ROT);
        public final CompoundParameter zRot = new CompoundParameter("zRot", LeafConfig.DEFAULT_Z_ROT, LeafConfig.MIN_Z_ROT, LeafConfig.MAX_Z_ROT);

        private LeafManipulator(LX lx) {
            super(lx);
            addParameter(x);
            addParameter(y);
            addParameter(z);
            addParameter(xRot);
            addParameter(yRot);
            addParameter(zRot);
        }

        public void onParameterChanged(LXParameter parameter) {
            if (!disableParameters) {
                LeafConfig config = getSelectedLeafConfig();
                config.x = x.getValuef();
                config.y = y.getValuef();
                config.z = z.getValuef();
                config.xRot = xRot.getValuef();
                config.yRot = yRot.getValuef();
                config.zRot = zRot.getValuef();
                vineWall.reconfigure();
            }
        }

        public void repurposeParameters() {
            disableParameters = true;
            LeafConfig config = getSelectedLeafConfig();
            x.setValue(config.x);
            y.setValue(config.y);
            z.setValue(config.z);
            xRot.setValue(config.xRot);
            yRot.setValue(config.yRot);
            zRot.setValue(config.zRot);
            disableParameters = false;
        }
    }
}
