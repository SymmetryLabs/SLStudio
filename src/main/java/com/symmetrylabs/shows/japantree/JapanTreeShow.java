package com.symmetrylabs.shows.japantree;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.net.SocketException;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.RotationOrder;
import org.apache.commons.math3.geometry.euclidean.threed.RotationConvention;
import java.io.IOException;

import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;

import com.symmetrylabs.shows.Show;
import com.symmetrylabs.slstudio.SLStudio;
import com.symmetrylabs.slstudio.SLStudioLX;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.shows.tree.*;
import com.symmetrylabs.shows.tree.config.*;
import com.symmetrylabs.shows.tree.ui.*;
import com.symmetrylabs.shows.tree.ui.UITenereControllers;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI3dComponent;

import org.smurn.jply.PlyReaderFile;
import org.smurn.jply.ElementReader;
import org.smurn.jply.Element;

import static com.symmetrylabs.util.DistanceConstants.*;
import static com.symmetrylabs.util.MathConstants.*;
import static com.symmetrylabs.util.MathUtils.*;

import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI3dComponent;
import processing.core.PGraphics;


public class JapanTreeShow extends TreeShow {
    public static final String SHOW_NAME = "japantree";

    public final Map<TreeModel.Branch, AssignableTenereController> controllers = new HashMap<>();

    final TwigConfig[] branch;
    final BranchConfig[] limbType1;
    final BranchConfig[] limbType2;
    final BranchConfig[] limbType3;
    final BranchConfig[] limbTypeSingleBranch;

    public JapanTreeShow() {
        List<TwigConfig> branchTwigs = new ArrayList<>();
        try {
            PlyReaderFile ply = new PlyReaderFile("shows/japantree/branch.ply");
            ElementReader plyReader = ply.nextElementReader();
            int i = 1;
            for (Element elem = plyReader.readElement(); elem != null; elem = plyReader.readElement()) {
                branchTwigs.add(
                    new TwigConfig(
                        (float) elem.getDouble("x"), (float) elem.getDouble("y"), (float) elem.getDouble("z"),
                        (float) elem.getDouble("azimuth"), (float) elem.getDouble("elevation"), (float) elem.getDouble("tilt"),
                        i++));
            }
            ply.close();
        } catch (IOException e) {
            System.out.println("unable to load point cloud for branch, exiting");
            throw new RuntimeException(e);
        }
        branch = new TwigConfig[branchTwigs.size()];
        branchTwigs.toArray(branch);

        List<BranchConfig> limbABranches = new ArrayList<>();
        try {
            PlyReaderFile ply = new PlyReaderFile("shows/japantree/limb-A.ply");
            ElementReader plyReader = ply.nextElementReader();
            int i = 1;
            for (Element elem = plyReader.readElement(); elem != null; elem = plyReader.readElement()) {
                limbABranches.add(
                    new BranchConfig(
                        false,
                        (float) elem.getDouble("x"), (float) elem.getDouble("y"), (float) elem.getDouble("z"),
                        (float) elem.getDouble("azimuth"), (float) elem.getDouble("elevation"), (float) elem.getDouble("tilt"),
                        branch));
            }
            ply.close();
        } catch (IOException e) {
            System.out.println("unable to load point cloud for limb A, exiting");
            throw new RuntimeException(e);
        }
        limbType1 = new BranchConfig[limbABranches.size()];
        limbABranches.toArray(limbType1);

        List<BranchConfig> limbBBranches = new ArrayList<>();
        try {
            PlyReaderFile ply = new PlyReaderFile("shows/japantree/limb-B.ply");
            ElementReader plyReader = ply.nextElementReader();
            int i = 1;
            for (Element elem = plyReader.readElement(); elem != null; elem = plyReader.readElement()) {
                limbBBranches.add(
                    new BranchConfig(
                        false,
                        (float) elem.getDouble("x"), (float) elem.getDouble("y"), (float) elem.getDouble("z"),
                        (float) elem.getDouble("azimuth"), (float) elem.getDouble("elevation"), (float) elem.getDouble("tilt"),
                        branch));
            }
            ply.close();
        } catch (IOException e) {
            System.out.println("unable to load point cloud for limb A, exiting");
            throw new RuntimeException(e);
        }
        limbType2 = new BranchConfig[limbBBranches.size()];
        limbBBranches.toArray(limbType2);

        List<BranchConfig> limbCBranches = new ArrayList<>();
        try {
            PlyReaderFile ply = new PlyReaderFile("shows/japantree/limb-C.ply");
            ElementReader plyReader = ply.nextElementReader();
            int i = 1;
            for (Element elem = plyReader.readElement(); elem != null; elem = plyReader.readElement()) {
                limbCBranches.add(
                    new BranchConfig(
                        false,
                        (float) elem.getDouble("x"), (float) elem.getDouble("y"), (float) elem.getDouble("z"),
                        (float) elem.getDouble("azimuth"), (float) elem.getDouble("elevation"), (float) elem.getDouble("tilt"),
                        branch));
            }
            ply.close();
        } catch (IOException e) {
            System.out.println("unable to load point cloud for limb A, exiting");
            throw new RuntimeException(e);
        }
        limbType3 = new BranchConfig[limbCBranches.size()];
        limbCBranches.toArray(limbType3);

        limbTypeSingleBranch = new BranchConfig[] {
            new BranchConfig(false, 0, 0, 0, 0, 0, 0, branch),
        };
    }

    public SLModel buildModel() {
        TreeConfig.createLimbType("Limb/L1", limbType1);
        TreeConfig.createLimbType("Limb/L2", limbType2);
        TreeConfig.createLimbType("Limb/L3", limbType3);
        TreeConfig.createLimbType("Limb/Single Branch", limbTypeSingleBranch);
        TreeConfig.createBranchType("Branch", branch);

        TreeConfig config = new TreeConfig(new LimbConfig[] {
                new LimbConfig(false, 0, 0, 0, 0, 0, limbType1),
                new LimbConfig(false, 0, 0, 0, 0, 0, limbType2),
                new LimbConfig(false, 0, 0, 0, 0, 0, limbType3),
                //new LimbConfig(false, 0, 0, 0, 0, 0, limbTypeSingleBranch),
            });

        TreeModel tree = new TreeModel(config);
        return tree;
    }

    @Override
    protected boolean readConfigFromDisk() {
        return false;
    }
}
