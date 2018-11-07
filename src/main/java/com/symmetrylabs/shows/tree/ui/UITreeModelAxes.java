package com.symmetrylabs.shows.tree.ui;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;
import static processing.core.PConstants.*;

import heronarts.lx.LX;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI3dComponent;
import heronarts.lx.color.LXColor;

import com.symmetrylabs.slstudio.SLStudio;
import com.symmetrylabs.shows.tree.config.LimbConfig;
import com.symmetrylabs.shows.tree.config.BranchConfig;
import com.symmetrylabs.shows.tree.config.TwigConfig;
import com.symmetrylabs.shows.tree.TreeModel;
import com.symmetrylabs.shows.tree.TreeModelingTool;
import static com.symmetrylabs.util.DistanceConstants.*;

public class UITreeModelAxes extends UI3dComponent {

    private static UITreeModelAxes instance = null;

    private final TreeModelingTool modelingTool;

    private UITreeModelAxes(LX lx) {
        this.modelingTool = TreeModelingTool.getInstance(lx);
        visible.setValue(false);
    }

    public static UITreeModelAxes getInstance(LX lx) {
        if (instance == null)
            instance = new UITreeModelAxes(lx);

        return instance;
    }

    public static UITreeModelAxes getInstance() {
        if (instance != null)
            return instance;

        return null;
    }

    @Override
    protected void onDraw(UI ui, PGraphics pg) {

        final LimbConfig limbConfig = modelingTool.getSelectedLimb().getConfig();
        final BranchConfig branchConfig = modelingTool.getSelectedBranch().getConfig();
        final TwigConfig twigConfig = modelingTool.getSelectedTwig().getConfig();

        pg.strokeWeight(2);

        pg.pushMatrix();
        pg.rotateY(limbConfig.azimuth * PI / 180.f);
        pg.translate(0, limbConfig.height, 0);
        pg.rotateX(limbConfig.elevation * PI / 180.f);
        pg.rotateZ(limbConfig.tilt* PI / 180.f);

        switch (modelingTool.mode.getEnum()) {
            case LIMB:
                pg.stroke(LXColor.GREEN);
                pg.line(0, 0, 0, 0, limbConfig.MAX_LENGTH, 0);
                pg.translate(0, limbConfig.length, 0);

                // dotted lines
                pg.pushMatrix();
                pg.stroke(LXColor.RED);
                for (int i = 0; i < (-branchConfig.MIN_X+branchConfig.MAX_X)/10; i++) {
                    pg.line(branchConfig.MIN_X+i*10, 0, 0, branchConfig.MIN_X+i*10+3f, 0, 0);
                }
                pg.stroke(LXColor.GREEN);
                for (int i = 0; i < (-branchConfig.MIN_Y+branchConfig.MAX_Y)/10; i++) {
                    pg.line(0, branchConfig.MIN_Y+i*10, 0, 0, branchConfig.MIN_Y+i*10+3f, 0);
                }
                pg.stroke(LXColor.BLUE);
                for (int i = 0; i < (-branchConfig.MIN_Z+branchConfig.MAX_Z)/10; i++) {
                    pg.line(0, 0, branchConfig.MIN_Z+i*10, 0, 0, branchConfig.MIN_Z+i*10+3f);
                }
                pg.popMatrix();
                break;

            case BRANCH:
                pg.translate(0, limbConfig.length, 0);

                // dotted lines
                pg.pushMatrix();
                pg.stroke(LXColor.RED);
                for (int i = 0; i < (-branchConfig.MIN_X+branchConfig.MAX_X)/10; i++) {
                    pg.line(branchConfig.MIN_X+i*10, 0, 0, branchConfig.MIN_X+i*10+3f, 0, 0);
                }
                pg.stroke(LXColor.GREEN);
                for (int i = 0; i < (-branchConfig.MIN_Y+branchConfig.MAX_Y)/10; i++) {
                    pg.line(0, branchConfig.MIN_Y+i*10, 0, 0, branchConfig.MIN_Y+i*10+3f, 0);
                }
                pg.stroke(LXColor.BLUE);
                for (int i = 0; i < (-branchConfig.MIN_Z+branchConfig.MAX_Z)/10; i++) {
                    pg.line(0, 0, branchConfig.MIN_Z+i*10, 0, 0, branchConfig.MIN_Z+i*10+3f);
                }
                pg.popMatrix();

                pg.pushMatrix();
                pg.translate(branchConfig.x, branchConfig.y, branchConfig.z);
                pg.rotateX(branchConfig.elevation * PI / 180.f);
                pg.rotateY(branchConfig.tilt * PI / 180.f);
                pg.rotateZ(branchConfig.azimuth * PI / 180.f);

                pg.stroke(LXColor.RED);
                pg.line(twigConfig.MIN_X, 0, 0, twigConfig.MAX_X, 0, 0);
                pg.stroke(LXColor.GREEN);
                pg.line(0, twigConfig.MIN_Y, 0, 0, twigConfig.MAX_Y, 0);
                pg.stroke(LXColor.BLUE);
                pg.line(0, 0, twigConfig.MIN_Z, 0, 0, twigConfig.MAX_Z);
                pg.popMatrix();
                break;

            case TWIG:
                // nothing for now
                break;
        }

        pg.popMatrix();
    }
}

