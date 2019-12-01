package com.symmetrylabs.shows.empirewall.ui;

import java.util.List;
import java.util.stream.Collectors;

import static processing.core.PConstants.*;

import heronarts.lx.LX;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.color.LXColor;

import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI2dContainer;
import heronarts.p3lx.ui.studio.UICollapsibleSection;
import heronarts.p3lx.ui.component.UIKnob;
import heronarts.p3lx.ui.component.UILabel;
import heronarts.p3lx.ui.component.UIItemList;
import heronarts.p3lx.ui.component.UIToggleSet;
import heronarts.p3lx.ui.component.UIButton;
import heronarts.p3lx.ui.component.UITextBox;
import heronarts.p3lx.ui.component.UIIntegerBox;
import heronarts.p3lx.ui.component.UIDropMenu;

import com.symmetrylabs.shows.tree.*;
import com.symmetrylabs.shows.tree.config.*;
import com.symmetrylabs.shows.empirewall.*;
import com.symmetrylabs.shows.empirewall.config.*;
import com.symmetrylabs.slstudio.SLStudioLX;


public class UIVineWallModelingTool extends UICollapsibleSection {

    public static UIVineWallModelingTool instance = null;

    private final LX lx;
    private final UI ui;

    private final float LIST_HEIGHT = 150;
    private final float KNOB_HEIGHT = 40;
    private final float KNOB_WIDTH = 40;

    //public final BooleanParameter displayTwigIndices = new BooleanParameter("indices", false);

    private final VineWallModelingTool modelingTool;

    public final UIKnob leafSelectorKnob;

    //private final LeafControls leafControls;

    public static UIVineWallModelingTool getInstance(LX lx, UI ui, VineWallModelingTool modelingTool, float x, float y, float w) {
        if (instance == null) {
            instance = new UIVineWallModelingTool(lx, ui, modelingTool, x, y, w);
        }
        return instance;
    }

    public static UIVineWallModelingTool getInstance() {
        if (instance != null) {
            return instance;
        }
        return null;
    }

    private UIVineWallModelingTool(LX lx, UI ui, VineWallModelingTool modelingTool, float x, float y, float w) {
        super(ui, x, y, w, 800);
        this.lx = lx;
        this.ui = ui;
        this.modelingTool = modelingTool;
        setTitle("VINE WALL MODELER");
        setPadding(5);

        new UILabel(0, 0, 90, 15).setLabel("Selected Vine: ")
            .setPadding(0, 5)
            .addToContainer(this);

        final UIDropMenu selectedVine = new UIDropMenu(90, 3, 100, 18, modelingTool.selectedVine);
        selectedVine.addToContainer(this);

        VineWallConfig config = ((VineModel) lx.model).getConfig();
        String[] vineIds = new String[config.getVines().size()];
        for (int i = 0; i < vineIds.length; i++) vineIds[i] = config.getVines().get(i).id;
        selectedVine.setOptions(vineIds);




        UIButton save = new UIButton(200, 3, 50, 18);
        save.setParameter(modelingTool.onSave).setLabel("Save");
        save.addToContainer(this);


        new UILabel(0, 40, 50, 15).setLabel("Leaves")
            .setPadding(0, 5)
            .addToContainer(this);

        // final UILabel selectedLeafLabel = new UILabel(190, 50, 100, 15).setLabel("Selected Leaf: " + modelingTool.selectedLeaf.getOption());
        // selectedLeafLabel.setPadding(0, 5)
        //     .addToContainer(this);

        // modelingTool.selectedLeaf.addListener(parameter -> {
        //     selectedLeafLabel.setLabel("Selected Leaf: " + modelingTool.selectedLeaf.getOption());
        // });

        this.leafSelectorKnob = new UIKnob(modelingTool.selectedLeaves[0]);
        leafSelectorKnob.setPosition(KNOB_WIDTH*0, 70)
            .setSize(KNOB_WIDTH, KNOB_HEIGHT)
            .addToContainer(this);

        // modelingTool.selectedVine.addListener(parameter -> {
        //     leaf.setParameter(modelingTool.selectedLeaf);
        //     redrawWindow();
        // });

        new UI2dContainer(KNOB_WIDTH*1+3, 70, 30, KNOB_HEIGHT)
            .setBackgroundColor(ui.theme.getDarkBackgroundColor())
            .addToContainer(this);

        UIKnob xPos = new UIKnob(modelingTool.leafManipulator.x);
        xPos.setPosition(KNOB_WIDTH*1+3, 70)
            .setSize(KNOB_WIDTH, KNOB_HEIGHT)
            .addToContainer(this);

        UIKnob yPos = new UIKnob(modelingTool.leafManipulator.y);
        yPos.setPosition(KNOB_WIDTH*2+3 * 1, 70)
            .setSize(KNOB_WIDTH, KNOB_HEIGHT)
            .addToContainer(this);

        UIKnob zPos = new UIKnob(modelingTool.leafManipulator.z);
        zPos.setPosition(KNOB_WIDTH*3+3, 70)
            .setSize(KNOB_WIDTH, KNOB_HEIGHT)
            .addToContainer(this);

        new UI2dContainer(KNOB_WIDTH*4+3, 70, 1, KNOB_HEIGHT)
            .setBackgroundColor(ui.theme.getDarkBackgroundColor())
            .addToContainer(this);

        UIKnob xRot = new UIKnob(modelingTool.leafManipulator.xRot);
        xRot.setPosition(KNOB_WIDTH*4+4, 70)
            .setSize(KNOB_WIDTH, KNOB_HEIGHT)
            .addToContainer(this);

        UIKnob yRot = new UIKnob(modelingTool.leafManipulator.yRot);
        yRot.setPosition(KNOB_WIDTH*5+4, 70)
            .setSize(KNOB_WIDTH, KNOB_HEIGHT)
            .addToContainer(this);

        UIKnob zRot = new UIKnob(modelingTool.leafManipulator.zRot);
        zRot.setPosition(KNOB_WIDTH*6+4, 70)
            .setSize(KNOB_WIDTH, KNOB_HEIGHT)
            .addToContainer(this);


        // this.leafControls = new LeafControls(ui,4, 200, getContentWidth(), 305, modelingTool.leafManipulator);
        // addTopLevelComponent(leafControls);

        // modelingTool.selectedLimb.addListener(parameter -> {
        //     modelingTool.setSelectedBranch(modelingTool.getSelectedLimb().getBranches().get(0));
        //     updateBranchControl();
        //     modelingTool.setSelectedTwig(modelingTool.getSelectedBranch().getTwigs().get(0));
        // });

        // modelingTool.selectedBranch.addListener(parameter -> {
        //     updateBranchControl();
        //     modelingTool.setSelectedTwig(modelingTool.getSelectedBranch().getTwigs().get(0));
        // });

        // if (ui instanceof SLStudioLX.UI) {
        //     ((SLStudioLX.UI) ui).addSaveHook(modelingTool.store);
        // }
    }

    private void redrawWindow() {
        redraw();
    }

    // private void updateBranchControl() {
    //     List<BranchItem> items = modelingTool.getSelectedLimb().getBranches().stream()
    //         .map(branch -> new BranchItem(branch)).collect(Collectors.toList());
    //     branchControls.branches.setItems(items);
    // }

    private class LeafControls extends UI2dContainer {
        private final VineWallModelingTool.LeafManipulator manipulator;
        private final UIKnobsPanel knobsPanel;

        private LeafControls(UI ui, float x, float y, float w, float h, VineWallModelingTool.LeafManipulator manipulator) {
            super(x, y, w, h);
            this.manipulator = manipulator;

            setBorderColor(0xff666666);
            setBorderRounding(5);
            setBorderWeight(1);
            setBorder(true);
            setChildMargin(5);
            setPadding(5);

            new UILabel(0, 0, 50, 15).setLabel("Leaves")
                .setPadding(0, 5)
                .addToContainer(this);

            // new UILabel(0, 60, 50, 15).setLabel("Index")
            //     .setPadding(0, 5)
            //     .addToContainer(this);

            this.knobsPanel = new UIKnobsPanel(6, 130, getContentWidth()-12, KNOB_HEIGHT);
            knobsPanel.addToContainer(this);

            // final UIButton flip = new UIButton(5, 130+KNOB_HEIGHT+10, getContentWidth()/4, 15) {
            //     protected void onToggle(boolean active) {
            //         if (active) {
            //             float value = (manipulator.tilt.getNormalizedf() + 0.5f) % 1;
            //             manipulator.tilt.setNormalized(value);
            //         }
            //     }
            // };

            // flip.setMomentary(true)
            //     .setLabel("flip")
            //     .setTextAlignment(CENTER, CENTER)
            //     .setBorderRounding(8)
            //     .addToContainer(this);

            // new UIButton(getContentWidth()-83, 130+KNOB_HEIGHT+10, 80, 15)
            //     .setParameter(displayTwigIndices)
            //     .setLabel("display indices")
            //     .setBorderRounding(8)
            //     .addToContainer(this);

            // new UILabel(0, 130+KNOB_HEIGHT+30, 80, 20)
            //     .setLabel("Disabled pixels:")
            //     .setTextAlignment(LEFT, CENTER)
            //     .addToContainer(this);
            // new UITextBox(85, 130+KNOB_HEIGHT+30, getContentWidth() - 90, 20)
            //     .setParameter(manipulator.disabledPixels)
            //     .addToContainer(this);

            // manipulator.locked.addListener(parameter -> {
            //     boolean locked = ((BooleanParameter) parameter).isOn();
            //     knobsPanel.xPosition.setEnabled(TwigConfig.isXEnabled() && !locked);
            //     knobsPanel.yPosition.setEnabled(TwigConfig.isYEnabled() && !locked);
            //     knobsPanel.zPosition.setEnabled(TwigConfig.isZEnabled() && !locked);
            //     knobsPanel.azimuth.setEnabled(TwigConfig.isAzimuthEnabled() && !locked);
            //     knobsPanel.elevation.setEnabled(TwigConfig.isElevationEnabled() && !locked);
            //     knobsPanel.tilt.setEnabled(TwigConfig.isTiltEnabled() && !locked);
            //     flip.setEnabled(!locked);
            // });
        }

        private class UIKnobsPanel extends UI2dContainer {
            protected final UIKnob x;
            protected final UIKnob y;
            protected final UIKnob z;
            protected final UIKnob xRot;
            protected final UIKnob yRot;
            protected final UIKnob zRot;

            UIKnobsPanel(float x, float y, float w, float h) {
                super(x, y, w, h);

                this.x = new UIKnob(manipulator.x);
                this.x.setPosition(KNOB_WIDTH*0, 0)
                    .setSize(KNOB_WIDTH, KNOB_HEIGHT)
                    .addToContainer(this);

                this.y = new UIKnob(manipulator.y);
                this.y.setPosition(KNOB_WIDTH*1 * 1, 0)
                    .setSize(KNOB_WIDTH, KNOB_HEIGHT)
                    .addToContainer(this);

                this.z = new UIKnob(manipulator.z);
                this.z.setPosition(KNOB_WIDTH*2, 0)
                    .setSize(KNOB_WIDTH, KNOB_HEIGHT)
                    .addToContainer(this);

                new UI2dContainer(KNOB_WIDTH*3, 0, 1, KNOB_HEIGHT)
                    .setBackgroundColor(ui.theme.getDarkBackgroundColor())
                    .addToContainer(this);

                this.xRot = new UIKnob(manipulator.xRot);
                this.xRot.setPosition(KNOB_WIDTH*3+1, 0)
                    .setSize(KNOB_WIDTH, KNOB_HEIGHT)
                    .addToContainer(this);

                this.yRot = new UIKnob(manipulator.yRot);
                this.yRot.setPosition(KNOB_WIDTH*4+1, 0)
                    .setSize(KNOB_WIDTH, KNOB_HEIGHT)
                    .addToContainer(this);

                this.zRot = new UIKnob(manipulator.zRot);
                this.zRot.setPosition(KNOB_WIDTH*5+1, 0)
                    .setSize(KNOB_WIDTH, KNOB_HEIGHT)
                    .addToContainer(this);
            }
        }
    }

    // // (todo) make these a little more elegant...
    // getPrimaryColorvate class LeafItem extends UIItemList.AbstractItem {
    //     final TreeModel.Leaf leaf;

    //     LeafItem(TreeModel.Leaf leaf) {
    //         this.leaf = leaf;
    //     }

    //     public String getLabel() {
    //         return "Leaf #" + modelingTool.getSelectedVine().leaves.indexOf(leaf);
    //     }

    //     public boolean isSelected() {
    //         // List<TreeModel.Limb> limbs = modelingTool.tree.getLimbs();
    //         // TreeModel.Limb selectedLimb = (TreeModel.Limb) modelingTool.getSelectedLimb();
    //         // return limbs.indexOf(limb) == limbs.indexOf(selectedLimb);
    //         return false;
    //     }

    //     @Override
    //     public boolean isActive() {
    //         return isSelected();
    //     }

    //     @Override
    //     public int getActiveColor(UI ui) {
    //         return isSelected() ? ui.theme.getPrimaryColor() : ui.theme.getSecondaryColor();
    //     }

    //     @Override
    //     public void onActivate() {
    //         modelingTool.setSelectedLeaf(leaf);
    //     }
    // }
}
