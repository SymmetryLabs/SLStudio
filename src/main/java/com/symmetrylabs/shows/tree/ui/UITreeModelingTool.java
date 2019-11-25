package com.symmetrylabs.shows.tree.ui;

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
import com.symmetrylabs.slstudio.SLStudioLX;


public class UITreeModelingTool extends UICollapsibleSection {

    public static UITreeModelingTool instance = null;

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
        super(ui, x, y, w, 800);
        this.ui = ui;
        this.modelingTool = modelingTool;
        setTitle("TREE MODEL           (Use 'TreeModelingPattern')");
        setPadding(5);

        this.limbControls = new LimbControls(ui,4, 20, getContentWidth(), 180, modelingTool.limbManipulator);
        addTopLevelComponent(limbControls);

        this.branchControls = new BranchControls(ui,4, 200, getContentWidth(), 305, modelingTool.branchManipulator);
        addTopLevelComponent(branchControls);

        this.twigControls = new TwigControls(ui,4, 505, getContentWidth(), 240, modelingTool.twigManipulator);
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

        if (ui instanceof SLStudioLX.UI) {
            ((SLStudioLX.UI) ui).addSaveHook(modelingTool.store);
        }
    }

    private void redrawWindow() {
        redraw();
    }

    private void updateBranchControl() {
        List<BranchItem> items = modelingTool.getSelectedLimb().getBranches().stream()
            .map(branch -> new BranchItem(branch)).collect(Collectors.toList());
        branchControls.branches.setItems(items);
    }

    private class LimbControls extends UI2dContainer {
        public final UIItemList.ScrollList limbs;
        private final UIKnobsPanel knobsPanel;

        private LimbControls(UI ui, float x, float y, float w, float h, TreeModelingTool.LimbManipulator manipulator) {
            super(x, y, w, h);
            setBorderColor(0xff888888);
            setBorderRounding(5);
            setBorderWeight(1);
            setBorder(true);
            setChildMargin(5);
            setPadding(5);

            new UILabel(0, 0, w, 15)
                .setLabel("Limbs")
                .setPadding(0, 5)
                .addToContainer(this);

            new UIButton(getContentWidth()-40, 5, 35, 15)
                .setParameter(manipulator.locked)
                .setActiveColor((LXColor.scaleBrightness(LXColor.RED, 0.35f)))
                .setLabel("lock")
                .addToContainer(this);

            this.limbs = new UIItemList.ScrollList(ui, 0, 25, w, 100);
            limbs.setSingleClickActivate(true);
            limbs.addToContainer(this);
            limbs.setItems(modelingTool.tree.getLimbs().stream()
                .map(limb -> new LimbItem(limb)).collect(Collectors.toList())
            );

            this.knobsPanel = new UIKnobsPanel(6, LIST_HEIGHT-20, getContentWidth()-12, KNOB_HEIGHT, manipulator);
            knobsPanel.addToContainer(this);

            manipulator.locked.addListener(parameter -> {
                boolean locked = ((BooleanParameter)parameter).isOn();
                knobsPanel.length.setEnabled(!locked);
                knobsPanel.height.setEnabled(!locked);
                knobsPanel.azimuth.setEnabled(!locked);
                knobsPanel.elevation.setEnabled(!locked);
                modelingTool.branchManipulator.locked.setValue(locked);
                redrawWindow();
            });
        }

        private class UIKnobsPanel extends UI2dContainer {
            protected final UIKnob length;
            protected final UIKnob height;
            protected final UIKnob azimuth;
            protected final UIKnob elevation;

            UIKnobsPanel(float x, float y, float w, float h, TreeModelingTool.LimbManipulator manipulator) {
                super(x, y, w, h);

                this.length = new UIKnob(manipulator.length);
                length.setEnabled(!manipulator.locked.isOn())
                    .setPosition(KNOB_WIDTH*0, 0)
                    .setSize(KNOB_WIDTH, KNOB_HEIGHT)
                    .addToContainer(this);

                this.height = new UIKnob(manipulator.height);
                height.setEnabled(!manipulator.locked.isOn())
                    .setPosition(KNOB_WIDTH*1, 0)
                    .setSize(KNOB_WIDTH, KNOB_HEIGHT)
                    .addToContainer(this);

                new UI2dContainer(KNOB_WIDTH*2, 0, 1, KNOB_HEIGHT)
                    .setBackgroundColor(ui.theme.getDarkBackgroundColor())
                    .addToContainer(this);

                this.azimuth = new UIKnob(manipulator.azimuth);
                azimuth.setEnabled(!manipulator.locked.isOn())
                    .setPosition(KNOB_WIDTH*2+1, 0)
                    .setSize(KNOB_WIDTH, KNOB_HEIGHT)
                    .addToContainer(this);

                this.elevation = new UIKnob(manipulator.elevation);
                elevation.setEnabled(!manipulator.locked.isOn())
                    .setPosition(KNOB_WIDTH*3+1, 0)
                    .setSize(KNOB_WIDTH, KNOB_HEIGHT)
                    .addToContainer(this);
            }
        }
    }

    private class BranchControls extends UI2dContainer {
        private final TreeModelingTool.BranchManipulator manipulator;
        public final UIItemList.ScrollList branches;
        private final UIKnobsPanel knobsPanel;

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

            new UIButton(getContentWidth()-40, 5, 35, 15)
                .setParameter(manipulator.locked)
                .setActiveColor((LXColor.scaleBrightness(LXColor.RED, 0.35f)))
                .setLabel("lock")
                .addToContainer(this);

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

            this.knobsPanel = new UIKnobsPanel(5, 235, getContentWidth()-12, KNOB_HEIGHT);
            knobsPanel.addToContainer(this);

            new UIDropMenu(130, 207, 110, 18, manipulator.type).addToContainer(this);

            final UIButton flip = (UIButton) new UIButton(5, 285, getContentWidth()/4, 15)
                .setLabel("flip")
                .setParameter(modelingTool.branchManipulator.flipped)
                .addToContainer(this);

            manipulator.locked.addListener(parameter -> {
                boolean locked = ((BooleanParameter)parameter).isOn();
                knobsPanel.xPosition.setEnabled(!locked);
                knobsPanel.yPosition.setEnabled(!locked);
                knobsPanel.zPosition.setEnabled(!locked);
                knobsPanel.azimuth.setEnabled(!locked);
                knobsPanel.elevation.setEnabled(!locked);
                knobsPanel.tilt.setEnabled(!locked);
                flip.setEnabled(locked);
                modelingTool.twigManipulator.locked.setValue(locked);
                redrawWindow();
            });
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
            protected final UIKnob xPosition;
            protected final UIKnob yPosition;
            protected final UIKnob zPosition;
            protected final UIKnob azimuth;
            protected final UIKnob elevation;
            protected final UIKnob tilt;

            UIKnobsPanel(float x, float y, float w, float h) {
                super(x, y, w, h);

                this.xPosition = new UIKnob(manipulator.xPosition);
                xPosition.setEnabled(BranchConfig.isXEnabled())
                    .setPosition(KNOB_WIDTH*0, 0)
                    .setSize(KNOB_WIDTH, KNOB_HEIGHT)
                    .addToContainer(this);

                this.yPosition = new UIKnob(manipulator.yPosition);
                yPosition.setEnabled(BranchConfig.isYEnabled())
                    .setPosition(KNOB_WIDTH*1, 0)
                    .setSize(KNOB_WIDTH, KNOB_HEIGHT)
                    .addToContainer(this);

                this.zPosition = new UIKnob(manipulator.zPosition);
                zPosition.setEnabled(BranchConfig.isZEnabled())
                    .setPosition(KNOB_WIDTH*2, 0)
                    .setSize(KNOB_WIDTH, KNOB_HEIGHT)
                    .addToContainer(this);

                new UI2dContainer(KNOB_WIDTH*3, 0, 1, KNOB_HEIGHT)
                    .setBackgroundColor(ui.theme.getDarkBackgroundColor())
                    .addToContainer(this);

                this.azimuth = new UIKnob(manipulator.azimuth);
                azimuth.setEnabled(BranchConfig.isAzimuthEnabled())
                    .setPosition(KNOB_WIDTH*3+1, 0)
                    .setSize(KNOB_WIDTH, KNOB_HEIGHT)
                    .addToContainer(this);

                this.elevation = new UIKnob(manipulator.elevation);
                elevation.setEnabled(BranchConfig.isElevationEnabled())
                    .setPosition(KNOB_WIDTH*4+1, 0)
                    .setSize(KNOB_WIDTH, KNOB_HEIGHT)
                    .addToContainer(this);

                this.tilt = new UIKnob(manipulator.tilt);
                tilt.setEnabled(BranchConfig.isTiltEnabled())
                    .setPosition(KNOB_WIDTH*5+1, 0)
                    .setSize(KNOB_WIDTH, KNOB_HEIGHT)
                    .addToContainer(this);
            }
        }
    }

    private class TwigControls extends UI2dContainer {
        private final TreeModelingTool.TwigManipulator manipulator;
        private final UIKnobsPanel knobsPanel;

        private TwigControls(UI ui, float x, float y, float w, float h, TreeModelingTool.TwigManipulator manipulator) {
            super(x, y, w, h);
            this.manipulator = manipulator;

            setBorderColor(0xff666666);
            setBorderRounding(5);
            setBorderWeight(1);
            setBorder(true);
            setChildMargin(5);
            setPadding(5);

            new UILabel(0, 0, 50, 15).setLabel("Twigs")
                .setPadding(0, 5)
                .addToContainer(this);

            UIToggleSet selectedTwig = new UIToggleSet(0, 23, getContentWidth(), 30);
            selectedTwig.setParameter(modelingTool.selectedTwig).addToContainer(this);
            selectedTwig.setOptions(new String[] {"1", "2", "3", "4", "5", "6", "7", "8"});

            new UILabel(0, 60, 50, 15).setLabel("Index")
                .setPadding(0, 5)
                .addToContainer(this);

            new UILabel(0, 79, getContentWidth()/2, 14)
                .setLabel("A")
                .setTextAlignment(CENTER, CENTER)
                .setBackgroundColor(LXColor.scaleBrightness(LXColor.BLUE, 0.35f))
                .addToContainer(this);

            new UILabel(getContentWidth()/2, 79, getContentWidth()/2, 14)
                .setLabel("B")
                .setTextAlignment(CENTER, CENTER)
                .setBackgroundColor(LXColor.scaleBrightness(LXColor.RED, 0.35f))
                .addToContainer(this);

            new UIToggleSet(0, 92, getContentWidth(), 30)
                .setParameter(manipulator.index)
                .addToContainer(this);

            this.knobsPanel = new UIKnobsPanel(6, 130, getContentWidth()-12, KNOB_HEIGHT);
            knobsPanel.addToContainer(this);

            final UIButton flip = new UIButton(5, 130+KNOB_HEIGHT+10, getContentWidth()/4, 15) {
                protected void onToggle(boolean active) {
                    if (active) {
                        float value = (manipulator.tilt.getNormalizedf() + 0.5f) % 1;
                        manipulator.tilt.setNormalized(value);
                    }
                }
            };
            flip.setMomentary(true)
                .setLabel("flip")
                .setTextAlignment(CENTER, CENTER)
                .setBorderRounding(8)
                .addToContainer(this);

            new UIButton(getContentWidth()-83, 130+KNOB_HEIGHT+10, 80, 15)
                .setParameter(displayTwigIndices)
                .setLabel("display indices")
                .setBorderRounding(8)
                .addToContainer(this);

            new UILabel(0, 130+KNOB_HEIGHT+30, 80, 20)
                .setLabel("Disabled pixels:")
                .setTextAlignment(LEFT, CENTER)
                .addToContainer(this);
            new UITextBox(85, 130+KNOB_HEIGHT+30, getContentWidth() - 90, 20)
                .setParameter(manipulator.disabledPixels)
                .addToContainer(this);

            manipulator.locked.addListener(parameter -> {
                boolean locked = ((BooleanParameter) parameter).isOn();
                knobsPanel.xPosition.setEnabled(TwigConfig.isXEnabled() && !locked);
                knobsPanel.yPosition.setEnabled(TwigConfig.isYEnabled() && !locked);
                knobsPanel.zPosition.setEnabled(TwigConfig.isZEnabled() && !locked);
                knobsPanel.azimuth.setEnabled(TwigConfig.isAzimuthEnabled() && !locked);
                knobsPanel.elevation.setEnabled(TwigConfig.isElevationEnabled() && !locked);
                knobsPanel.tilt.setEnabled(TwigConfig.isTiltEnabled() && !locked);
                flip.setEnabled(!locked);
            });
        }

        private class UIKnobsPanel extends UI2dContainer {
            protected final UIKnob xPosition;
            protected final UIKnob yPosition;
            protected final UIKnob zPosition;
            protected final UIKnob azimuth;
            protected final UIKnob elevation;
            protected final UIKnob tilt;

            UIKnobsPanel(float x, float y, float w, float h) {
                super(x, y, w, h);

                this.xPosition = new UIKnob(manipulator.xPosition);
                xPosition.setEnabled(TwigConfig.isXEnabled())
                    .setPosition(KNOB_WIDTH*0, 0)
                    .setSize(KNOB_WIDTH, KNOB_HEIGHT)
                    .addToContainer(this);

                this.yPosition = new UIKnob(manipulator.yPosition);
                yPosition.setEnabled(TwigConfig.isYEnabled())
                    .setPosition(KNOB_WIDTH*1 * 1, 0)
                    .setSize(KNOB_WIDTH, KNOB_HEIGHT)
                    .addToContainer(this);

                this.zPosition = new UIKnob(manipulator.zPosition);
                zPosition.setEnabled(TwigConfig.isZEnabled())
                    .setPosition(KNOB_WIDTH*2, 0)
                    .setSize(KNOB_WIDTH, KNOB_HEIGHT)
                    .addToContainer(this);

                new UI2dContainer(KNOB_WIDTH*3, 0, 1, KNOB_HEIGHT)
                    .setBackgroundColor(ui.theme.getDarkBackgroundColor())
                    .addToContainer(this);

                this.azimuth = new UIKnob(manipulator.azimuth);
                azimuth.setEnabled(TwigConfig.isAzimuthEnabled())
                    .setPosition(KNOB_WIDTH*3+1, 0)
                    .setSize(KNOB_WIDTH, KNOB_HEIGHT)
                    .addToContainer(this);

                this.elevation = new UIKnob(manipulator.elevation);
                elevation.setEnabled(TwigConfig.isElevationEnabled())
                    .setPosition(KNOB_WIDTH*4+1, 0)
                    .setSize(KNOB_WIDTH, KNOB_HEIGHT)
                    .addToContainer(this);

                this.tilt = new UIKnob(manipulator.tilt);
                tilt.setEnabled(TwigConfig.isTiltEnabled())
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
            String isLocked = limb.getConfig().locked ? " (LOCKED)" : "";
            return "Limb #" + modelingTool.tree.getLimbs().indexOf(limb) + isLocked;
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
            String isLocked = branch.getConfig().locked ? " (LOCKED)" : "";
            int index = modelingTool.getSelectedLimb().getBranches().indexOf(branch);
            String ipAddress = branch.getConfig().ipAddress;
            int output = branch.getConfig().channel;
            return (index+1) + "/  (ip: " + ipAddress + ")  [ch: " + output + "]" + isLocked;
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
