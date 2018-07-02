package com.symmetrylabs.layouts.tree.ui;

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

import com.symmetrylabs.layouts.tree.*;
import com.symmetrylabs.layouts.tree.config.*;


public class UITreeModelingTool extends UICollapsibleSection {

    private final UI ui;

    private final float LIST_HEIGHT = 150;
    private final float KNOB_HEIGHT = 40;
    private final float KNOB_WIDTH = 40;

    public final BooleanParameter displayTwigIndices = new BooleanParameter("indices", false);

    private final TreeModelingTool modelingTool;

    private final LimbControls limbControls;
    private final BranchControls branchControls;
    private final TwigControls twigControls;

    public UITreeModelingTool(UI ui, TreeModelingTool modelingTool, float x, float y, float w) {
        super(ui, x, y, w, 613);
        this.ui = ui;
        this.modelingTool = modelingTool;
        setTitle("TREE MODEL           (Use 'TreeModelingPattern')");
        setPadding(5);

        this.limbControls = new LimbControls(ui,4, 20, getContentWidth(), 180, modelingTool.limbManipulator);
        addTopLevelComponent(limbControls);

        this.branchControls = new BranchControls(ui,4, 200, getContentWidth(), 285, modelingTool.branchManipulator);
        addTopLevelComponent(branchControls);

        this.twigControls = new TwigControls(ui,4, 485, getContentWidth(), 125, modelingTool.twigManipulator);
        addTopLevelComponent(twigControls);

        modelingTool.selectedLimb.addListener(parameter -> {
            modelingTool.setSelectedBranch(modelingTool.getSelectedLimb().getBranches().get(0));
            updateBranchControl();
            modelingTool.setSelectedTwig(modelingTool.getSelectedBranch().getTwigs().get(0));
        });

        modelingTool.selectedBranch.addListener(parameter -> {
            updateBranchControl();
            modelingTool.setSelectedTwig(modelingTool.getSelectedBranch().getTwigs().get(0));
        });
    }

    private void updateBranchControl() {
        List<BranchItem> items = modelingTool.getSelectedLimb().getBranches().stream()
            .map(branch -> new BranchItem(branch)).collect(Collectors.toList());
        branchControls.branches.setItems(items);
    }

    private class LimbControls extends UI2dContainer {
        public final UIItemList.ScrollList limbs;

        private LimbControls(UI ui, float x, float y, float w, float h, TreeModelingTool.LimbManipulator manipulator) {
            super(x, y, w, h);
            setBorderColor(0xff888888);
            setBorderRounding(5);
            setBorderWeight(1);
            setBorder(true);
            setChildMargin(5);
            setPadding(5);

            new UILabel(0, 0, w, 15).setLabel("Limbs")
                .setPadding(0, 5).addToContainer(this);

            this.limbs = new UIItemList.ScrollList(ui, 0, 25, w, 100);
            limbs.setSingleClickActivate(true);
            limbs.addToContainer(this);
            limbs.setItems(modelingTool.tree.getLimbs().stream()
                .map(limb -> new LimbItem(limb)).collect(Collectors.toList())
            );

            new UIKnobsPanel(6, LIST_HEIGHT-20, getContentWidth()-12, KNOB_HEIGHT, manipulator).addToContainer(this);
        }

        private class UIKnobsPanel extends UI2dContainer {
            UIKnobsPanel(float x, float y, float w, float h, TreeModelingTool.LimbManipulator manipulator) {
                super(x, y, w, h);

                new UIKnob(manipulator.length)
                    .setPosition(KNOB_WIDTH*0, 0)
                    .setSize(KNOB_WIDTH, KNOB_HEIGHT)
                    .addToContainer(this);

                new UIKnob(manipulator.height)
                    .setPosition(KNOB_WIDTH*1, 0)
                    .setSize(KNOB_WIDTH, KNOB_HEIGHT)
                    .addToContainer(this);

                new UI2dContainer(KNOB_WIDTH*2, 0, 1, KNOB_HEIGHT)
                    .setBackgroundColor(ui.theme.getDarkBackgroundColor())
                    .addToContainer(this);

                new UIKnob(manipulator.azimuth)
                    .setPosition(KNOB_WIDTH*2+1, 0)
                    .setSize(KNOB_WIDTH, KNOB_HEIGHT)
                    .addToContainer(this);

                new UIKnob(manipulator.elevation)
                    .setPosition(KNOB_WIDTH*3+1, 0)
                    .setSize(KNOB_WIDTH, KNOB_HEIGHT)
                    .addToContainer(this);
            }
        }
    }

    private class BranchControls extends UI2dContainer {
        private final TreeModelingTool.BranchManipulator manipulator;
        public final UIItemList.ScrollList branches;

        private BranchControls(UI ui, float x, float y, float w, float h, TreeModelingTool.BranchManipulator manipulator) {
            super(x, y, w, h);
            this.manipulator = manipulator;

            setBorderColor(0xff888888);
            setBorderRounding(5);
            setBorderWeight(1);
            setBorder(true);
            setChildMargin(5);
            setPadding(5);

            new UILabel(0, 0, w, 15).setLabel("Branches")
                .setPadding(0, 5).addToContainer(this);

            this.branches = new UIItemList.ScrollList(ui, 0, 23, w, 100);
            branches.setSingleClickActivate(true);
            branches.addToContainer(this);
            branches.setItems(modelingTool.getSelectedLimb().getBranches().stream()
                .map(branch -> new BranchItem(branch)).collect(Collectors.toList())
            );

            new UILabel(7, 125, getContentWidth(), 16)
                .setLabel("Pixlite").addToContainer(this);
            new UINetworkPanel(6, 147, getContentWidth()-13, 53).addToContainer(this);

            new UILabel(7, 202, getContentWidth(), 18)
                .setLabel("Configuration").addToContainer(this);
            new UIKnobsPanel(5, 235, getContentWidth()-12, KNOB_HEIGHT).addToContainer(this);

            new UIDropMenu(130, 207, 110, 18, manipulator.type).addToContainer(this);
        }

        private class UINetworkPanel extends UI2dContainer {
            UINetworkPanel(float x, float y, float w, float h) {
                super(x, y, w, h);

                new UI2dContainer(0, 0, getContentWidth(), getContentHeight())
                    .setBackgroundColor(ui.theme.getDarkBackgroundColor())
                    .setBorderRounding(4)
                    .addToContainer(this);

                new UILabel(5, 0, (getContentWidth()*0.6f), 20)
                    .setLabel("ip address")
                    .setPadding(5, 45)
                    .setBackgroundColor(ui.theme.getDarkBackgroundColor())
                    .addToContainer(this);

                new UITextBox(5, 21, (getContentWidth()*0.6f), 25)
                    .setParameter(manipulator.ipAddress)
                    .addToContainer(this);

                new UILabel((getContentWidth()*0.6f)+9, 0, 79, 20)
                    .setLabel("output")
                    .setPadding(5, 16)
                    .setBackgroundColor(ui.theme.getDarkBackgroundColor())
                    .addToContainer(this);

                new UIIntegerBox((getContentWidth()*0.6f)+9, 21, 79, 25)
                    .setParameter(manipulator.channel)
                    .addToContainer(this);
            }
        }

        private class UIKnobsPanel extends UI2dContainer {
            UIKnobsPanel(float x, float y, float w, float h) {
                super(x, y, w, h);

                new UIKnob(manipulator.xPosition)
                    .setEnabled(BranchConfig.isXEnabled())
                    .setPosition(KNOB_WIDTH*0, 0)
                    .setSize(KNOB_WIDTH, KNOB_HEIGHT)
                    .addToContainer(this);

                new UIKnob(manipulator.yPosition)
                    .setEnabled(BranchConfig.isYEnabled())
                    .setPosition(KNOB_WIDTH*1, 0)
                    .setSize(KNOB_WIDTH, KNOB_HEIGHT)
                    .addToContainer(this);

                new UIKnob(manipulator.zPosition)
                    .setEnabled(BranchConfig.isZEnabled())
                    .setPosition(KNOB_WIDTH*2, 0)
                    .setSize(KNOB_WIDTH, KNOB_HEIGHT)
                    .addToContainer(this);

                new UI2dContainer(KNOB_WIDTH*3, 0, 1, KNOB_HEIGHT)
                    .setBackgroundColor(ui.theme.getDarkBackgroundColor())
                    .addToContainer(this);

                new UIKnob(manipulator.azimuth)
                    .setEnabled(BranchConfig.isAzimuthEnabled())
                    .setPosition(KNOB_WIDTH*3+1, 0)
                    .setSize(KNOB_WIDTH, KNOB_HEIGHT)
                    .addToContainer(this);

                new UIKnob(manipulator.elevation)
                    .setEnabled(BranchConfig.isElevationEnabled())
                    .setPosition(KNOB_WIDTH*4+1, 0)
                    .setSize(KNOB_WIDTH, KNOB_HEIGHT)
                    .addToContainer(this);

                new UIKnob(manipulator.tilt)
                    .setEnabled(BranchConfig.isTiltEnabled())
                    .setPosition(KNOB_WIDTH*5+1, 0)
                    .setSize(KNOB_WIDTH, KNOB_HEIGHT)
                    .addToContainer(this);
            }
        }
    }

    private class TwigControls extends UI2dContainer {
        private final TreeModelingTool.TwigManipulator manipulator;

        private TwigControls(UI ui, float x, float y, float w, float h, TreeModelingTool.TwigManipulator manipulator) {
            super(x, y, w, h);
            this.manipulator = manipulator;

            setBorderColor(0xff888888);
            setBorderRounding(5);
            setBorderWeight(1);
            setBorder(true);
            setChildMargin(5);
            setPadding(5);

            new UIButton(getContentWidth()-83, 5, 80, 15)
                .setParameter(displayTwigIndices)
                .setLabel("display indices")
                .setBorderRounding(8)
                .addToContainer(this);

            new UILabel(0, 0, 50, 15).setLabel("Twigs")
                .setPadding(0, 5)
                .addToContainer(this);

            new UILabel(0, 23, getContentWidth()/2, 14)
                .setLabel("A")
                .setTextAlignment(CENTER, CENTER)
                .setBackgroundColor(LXColor.scaleBrightness(LXColor.RED, 0.35f))
                .addToContainer(this);

            new UILabel(getContentWidth()/2, 23, getContentWidth()/2, 14)
                .setLabel("B")
                .setTextAlignment(CENTER, CENTER)
                .setBackgroundColor(LXColor.scaleBrightness(LXColor.BLUE, 0.35f))
                .addToContainer(this);

            new UIToggleSet(0, 37, getContentWidth(), 30)
                .setParameter(modelingTool.selectedTwig)
                .addToContainer(this);

            new UIKnobsPanel(6, 75, getContentWidth()-12, KNOB_HEIGHT).addToContainer(this);
        }

        private class UIKnobsPanel extends UI2dContainer {
            UIKnobsPanel(float x, float y, float w, float h) {
                super(x, y, w, h);

                new UIKnob(manipulator.xPosition)
                    .setEnabled(TwigConfig.isXEnabled())
                    .setPosition(KNOB_WIDTH*0, 0)
                    .setSize(KNOB_WIDTH, KNOB_HEIGHT)
                    .addToContainer(this);

                new UIKnob(manipulator.yPosition)
                    .setEnabled(TwigConfig.isYEnabled())
                    .setPosition(KNOB_WIDTH*1 * 1, 0)
                    .setSize(KNOB_WIDTH, KNOB_HEIGHT)
                    .addToContainer(this);

                new UIKnob(manipulator.zPosition)
                    .setEnabled(TwigConfig.isZEnabled())
                    .setPosition(KNOB_WIDTH*2, 0)
                    .setSize(KNOB_WIDTH, KNOB_HEIGHT)
                    .addToContainer(this);

                new UI2dContainer(KNOB_WIDTH*3, 0, 1, KNOB_HEIGHT)
                    .setBackgroundColor(ui.theme.getDarkBackgroundColor())
                    .addToContainer(this);

                new UIKnob(manipulator.azimuth)
                    .setEnabled(TwigConfig.isAzimuthEnabled())
                    .setPosition(KNOB_WIDTH*3+1, 0)
                    .setSize(KNOB_WIDTH, KNOB_HEIGHT)
                    .addToContainer(this);

                new UIKnob(manipulator.elevation)
                    .setEnabled(TwigConfig.isElevationEnabled())
                    .setPosition(KNOB_WIDTH*4+1, 0)
                    .setSize(KNOB_WIDTH, KNOB_HEIGHT)
                    .addToContainer(this);

                new UIKnob(manipulator.tilt)
                    .setEnabled(TwigConfig.isTiltEnabled())
                    .setPosition(KNOB_WIDTH*5+1, 0)
                    .setSize(KNOB_WIDTH, KNOB_HEIGHT)
                    .addToContainer(this);
            }
        }
    }

    // (todo) make these a little more elegant...
    private class LimbItem extends UIItemList.AbstractItem {
        final TreeModel.Limb limb;

        LimbItem(TreeModel.Limb limb) {
            this.limb = limb;
        }

        public String getLabel() {
            return "Limb #" + modelingTool.tree.getLimbs().indexOf(limb);
        }

        public boolean isSelected() {
            List<TreeModel.Limb> limbs = modelingTool.tree.getLimbs();
            TreeModel.Limb selectedLimb = (TreeModel.Limb) modelingTool.getSelectedLimb();
            return limbs.indexOf(limb) == limbs.indexOf(selectedLimb);
        }

        @Override
        public boolean isActive() {
            return isSelected();
        }

        @Override
        public int getActiveColor(UI ui) {
            return isSelected() ? ui.theme.getPrimaryColor() : ui.theme.getSecondaryColor();
        }

        @Override
        public void onActivate() {
            modelingTool.setSelectedLimb(limb);
        }
    }

    private class BranchItem extends UIItemList.AbstractItem {
        final TreeModel.Branch branch;

        BranchItem(TreeModel.Branch branch) {
            this.branch = branch;
        }

        public String getLabel() {
            int index = modelingTool.getSelectedLimb().getBranches().indexOf(branch);
            String ipAddress = branch.getConfig().ipAddress;
            int output = branch.getConfig().channel;
            return (index+1) + "/  (ip: " + ipAddress + ")  [ch: " + output + "]";//  {type: custom}";
        }

        public boolean isSelected() {
            List<TreeModel.Branch> branches = modelingTool.getSelectedLimb().getBranches();
            TreeModel.Branch selectedBranch = (TreeModel.Branch) modelingTool.getSelectedBranch();
            return branches.indexOf(branch) == branches.indexOf(selectedBranch);
        }

        @Override
        public boolean isActive() {
            return isSelected();
        }

        @Override
        public int getActiveColor(UI ui) {
            return isSelected() ? ui.theme.getPrimaryColor() : ui.theme.getSecondaryColor();
        }

        @Override
        public void onActivate() {
            modelingTool.setSelectedBranch(branch);
        }
    }

    private class TwigItem extends UIItemList.AbstractItem {
        final TreeModel.Twig twig;

        TwigItem(TreeModel.Twig twig) {
            this.twig = twig;
        }

        public String getLabel() {
            return "Twig #" + modelingTool.getSelectedBranch().getTwigs().indexOf(twig);
        }

        public boolean isSelected() {
            List<TreeModel.Twig> twigs = modelingTool.getSelectedBranch().getTwigs();
            TreeModel.Twig selectedTwig = (TreeModel.Twig) modelingTool.getSelectedTwig();
            return twigs.indexOf(twig) == twigs.indexOf(selectedTwig);
        }

        @Override
        public boolean isActive() {
            return isSelected();
        }

        @Override
        public int getActiveColor(UI ui) {
            return isSelected() ? ui.theme.getPrimaryColor() : ui.theme.getSecondaryColor();
        }

        @Override
        public void onActivate() {
            modelingTool.setSelectedTwig(twig);
        }
    }
}
