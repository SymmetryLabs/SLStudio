package com.symmetrylabs.slstudio.warp;

import com.symmetrylabs.shows.tree.TreeModel;

import java.util.ArrayList;
import java.util.Arrays;

import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.transform.LXVector;
import heronarts.lx.warp.LXWarp;

public class TreeStructure extends SLWarp<TreeModel> {
    public TreeStructure(LX lx) {
        super(lx);
    }

    @Override
    public boolean run(double deltaMs, boolean inputVectorsChanged) {
        if (inputVectorsChanged) {
            if (model instanceof TreeModel) {
                System.out.println("Recomputing TreeStructure warp (" + inputVectors.length + " vectors)...");
                TreeModel tree = (TreeModel) model;

                float na = 0, nb = 0, nc = 0, nd = 0, ne = 0;
                na = tree.limbs.size();
                for (TreeModel.Limb limb : tree.limbs) {
                    nb = Math.max(nb, limb.branches.size());
                    for (TreeModel.Branch branch : limb.branches) {
                        nc = Math.max(nc, branch.twigs.size());
                        for (TreeModel.Twig twig : branch.twigs) {
                            nd = Math.max(nd, twig.leaves.size());
                            for (TreeModel.Leaf leaf : twig.leaves) {
                                ne = Math.max(ne, leaf.points.length);
                            }
                        }
                    }
                }

                // Include only points for which inputVectors[i] is non-null.
                Arrays.fill(outputVectors, null);

                int a = 0;
                for (TreeModel.Limb limb : tree.limbs) {
                    int b = 0;
                    for (TreeModel.Branch branch : limb.branches) {
                        int c = 0;
                        for (TreeModel.Twig twig : branch.twigs) {
                            int d = 0;
                            for (TreeModel.Leaf leaf : twig.leaves) {
                                int e = 0;
                                for (LXPoint point : leaf.points) {
                                    if (inputVectors[point.index] != null) {
                                        LXVector ov = new LXVector(point);
                                        ov.x = tree.xMin + tree.xRange * (a / na + 0.1f * d / nd);
                                        ov.y = tree.yMin + tree.yRange * (c / nc);
                                        ov.z = tree.zMin + tree.zRange * (b / nb + 0.1f * e / ne);
                                        outputVectors[point.index] = ov;
                                    }
                                    e++;
                                }
                                d++;
                            }
                            c++;
                        }
                        b++;
                    }
                    a++;
                }
                return true;
            } else {
                System.out.println("Cannot apply TreeStructure warp to non-Tree model");
            }
        }
        return false;
    }
}
