package com.symmetrylabs.shows.tree;

import java.util.List;

import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.EnumParameter;

import com.symmetrylabs.slstudio.SLStudio;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import com.symmetrylabs.shows.tree.*;
import com.symmetrylabs.shows.tree.ui.*;


public class TreeModelingPattern extends SLPattern<TreeModel> {

    private final int ORANGE = 0xffff8000;

    private final TreeModelingTool modelingTool;
    private final UITreeModelingTool uiModelingTool;
    private final UITreeModelAxes uiTreeModelAxes;

    public final CompoundParameter siblingBrightness = new CompoundParameter("SibBri", 0.4f);

    public TreeModelingPattern(LX lx) {
        super(lx);
        this.modelingTool = TreeModelingTool.getInstance();
        this.uiModelingTool = UITreeModelingTool.getInstance();
        this.uiTreeModelAxes = UITreeModelAxes.getInstance();
        addParameter(siblingBrightness);
    }

    public void onActive() {
        uiTreeModelAxes.visible.setValue(true);
    }

    public void onInactive() {
        uiTreeModelAxes.visible.setValue(false);
    }

    public void run(double deltaMs) {
        List<LXPoint> points = null;

        setColors(0);
        int i1 = 0, i2 = 0;

        switch (modelingTool.mode.getEnum()) {
            case LIMB:
                i1 = 0; i2 = 0;
                for (TreeModel.Limb limb : modelingTool.tree.getLimbs()) {
                    for (LXPoint p : limb.getPoints()) {
                        if (i2++ % 2 == 0) {
                            colors[p.index] = LXColor.scaleBrightness(
                                (i1 % 2 == 0) ? LXColor.GREEN : ORANGE, siblingBrightness.getValuef()
                            );
                        }
                    }
                    i1++;
                }

                i1 = 0;
                points = modelingTool.getSelectedLimb().getPoints();
                for (LXPoint p : points) {
                    colors[p.index] = i1++ > (points.size()-1)/2 ? LXColor.RED : LXColor.BLUE;
                }
                break;

            case BRANCH:
                i1 = 0; i2 = 0;
                for (TreeModel.Branch branch : modelingTool.getSelectedLimb().getBranches()) {
                    for (LXPoint p : branch.getPoints()) {
                        if (i2++ % 2 == 0) {
                            colors[p.index] = LXColor.scaleBrightness(
                                (i1 % 2 == 0) ? LXColor.GREEN : ORANGE, siblingBrightness.getValuef()
                            );
                        }
                    }
                    i1++;
                }

                i1 = 0;
                points =  modelingTool.getSelectedBranch().getPoints();
                for (LXPoint p : points) {
                    colors[p.index] = i1++ > (points.size()-1)/2 ? LXColor.BLUE : LXColor.RED;
                }

//                if (uiModelingTool.displayTwigIndices.isOn()) {
//                    int bi = 0;
//                    for (TreeModel.Twig twig : modelingTool.getSelectedBranch().getTwigs()) {
//                        int ti = -1;
//                        for (TreeModel.Leaf leaf : twig.getLeaves()) {
//                            if (bi > ti++) {
//                                for (LXPoint p : leaf.getPoints()) {
//                                    colors[p.index] = LXColor.GREEN;
//                                }
//                            }
//                        }
//                        bi++;
//                    }
//                }
                break;

            case TWIG:
                i1 = 0; i2 = 0;
                //for (TreeModel.Twig twig : modelingTool.getSelectedBranch().getTwigs()) {
                TreeModel.Branch branch = modelingTool.getSelectedBranch();


//                for (int i = 0; i < branch.getTwigs().size(); i++) {
//                     System.out.println("------------------------------");
//                     for (TreeModel.Twig twig : branch.getTwigs()) {
//                         System.out.println("twig index: " + twig.getConfig().index);
//                     }
//                }


                for (int i3 = 0; i3 < branch.getTwigs().size(); i3++) {
                    TreeModel.Twig twig = branch.getTwigByWiringIndex(i3+1);
                    if (twig != null) {
                        for (LXPoint p : twig.points) {
                            if (i2++ % 2 == 0) {
                                colors[p.index] = LXColor.scaleBrightness(
                                    (i1 % 2 == 0) ? LXColor.GREEN : ORANGE, siblingBrightness.getValuef()
                                );
                            }
                        }
                    }
                    i1++;
                }

                i1 = 0;
                points = modelingTool.getSelectedTwig().getPoints();
                for (LXPoint p : points) {
                    colors[p.index] = i1++ > (points.size()-1)/2 ? LXColor.BLUE : LXColor.RED;
                }

                if (uiModelingTool.displayTwigIndices.isOn()) {
                        TreeModel.Twig twig = modelingTool.getSelectedTwig();
                        int ti = twig.getConfig().index-1;
                        int ti1 = -1;
                        for (TreeModel.Leaf leaf : twig.getLeaves()) {
                            if (ti1++ < ti) {
                                for (LXPoint p : leaf.getPoints()) {
                                    colors[p.index] = LXColor.GREEN;
                                }
                            }
                        }
                }
                break;

            default:
                break;
        }
    }
}
