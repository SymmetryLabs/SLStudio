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
import com.symmetrylabs.slstudio.ApplicationState;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;

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
    final TwigConfig[] twigBranch;
    final BranchConfig[] limbType1;
    final BranchConfig[] limbType2;
    final BranchConfig[] limbType3;
    TreeConfig config;

    private static BranchConfig[] loadFromPly(String fileName, int limb, HashMap<Integer, TwigConfig[]> branchTypes) {
        List<BranchConfig> configs = new ArrayList<>();
        try {
            PlyReaderFile ply = new PlyReaderFile(fileName);
            ElementReader plyReader = ply.nextElementReader();
            for (Element elem = plyReader.readElement(); elem != null; elem = plyReader.readElement()) {
                if (elem.getInt("limb") != limb) {
                    continue;
                }
                Integer branchType = elem.getInt("branchtype");
                if (!branchTypes.containsKey(branchType)) {
                    ApplicationState.setWarning("JapanTree", "bad branch type " + branchType + " in limb " + limb);
                    continue;
                }
                configs.add(
                    new BranchConfig(
                        false,
                        (float) elem.getDouble("x"), (float) elem.getDouble("y"), (float) elem.getDouble("z"),
                        (float) elem.getDouble("azimuth"), (float) elem.getDouble("elevation"), (float) elem.getDouble("tilt"),
                        branchTypes.get(branchType)));
            }
            ply.close();
        } catch (IOException e) {
            System.out.println("unable to load point cloud for limb, exiting");
            throw new RuntimeException(e);
        }
        BranchConfig res[] = new BranchConfig[configs.size()];
        configs.toArray(res);
        return res;
    }

    public JapanTreeShow() {
        List<TwigConfig> branchTwigs = new ArrayList<>();
        try {
            PlyReaderFile ply = new PlyReaderFile("shows/japantree/branch.ply");
            ElementReader plyReader = ply.nextElementReader();
            for (Element elem = plyReader.readElement(); elem != null; elem = plyReader.readElement()) {
                branchTwigs.add(
                    new TwigConfig(
                        (float) elem.getDouble("x"), (float) elem.getDouble("y"), (float) elem.getDouble("z"),
                        (float) elem.getDouble("azimuth"), (float) elem.getDouble("elevation"), (float) elem.getDouble("tilt"),
                        elem.getInt("channel")));
            }
            ply.close();
        } catch (IOException e) {
            System.out.println("unable to load point cloud for branch, exiting");
            throw new RuntimeException(e);
        }
        branch = new TwigConfig[branchTwigs.size()];
        branchTwigs.toArray(branch);

        twigBranch = new TwigConfig[] {
            new TwigConfig(0, 0, 0, 0, 0, 0, 1),
        };

        HashMap<Integer, TwigConfig[]> branchTypes = new HashMap<>();
        branchTypes.put(0, branch);
        branchTypes.put(1, twigBranch);
        limbType1 = loadFromPly("shows/japantree/limbs.ply", 0, branchTypes);
        limbType2 = loadFromPly("shows/japantree/limbs.ply", 1, branchTypes);
        limbType3 = loadFromPly("shows/japantree/limbs.ply", 2, branchTypes);
    }

    @Override
    public SLModel buildModel() {
        TreeConfig.createLimbType("Limb/L1", limbType1);
        TreeConfig.createLimbType("Limb/L2", limbType2);
        TreeConfig.createLimbType("Limb/L3", limbType3);
        TreeConfig.createBranchType("Branch", branch);

        config = new TreeConfig(new LimbConfig[] {
                new LimbConfig(false, 0, 0, 0, 0, 0, limbType1),
                new LimbConfig(false, 0, 0, 0, 0, 0, limbType2),
                new LimbConfig(false, 0, 0, 0, 0, 0, limbType3),
            });
        TreeModel tree = new TreeModel(config);
        return tree;
    }

    @Override
    public void setupLx(LX lx) {
        super.setupLx(lx);

        try (FileOutputStream out = new FileOutputStream("src/main/java/com/symmetrylabs/shows/japantree/CompiledTreeData.java")) {
            OutputStreamWriter w = new OutputStreamWriter(out);
            new TreeCompiler(config, "com.symmetrylabs.shows.japantree", false).emit(w);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }

    @Override
    public void setupUi(SLStudioLX lx, SLStudioLX.UI ui) {
        super.setupUi(lx, ui);
        new UITenereControllers(lx, ui, 0, 0, ui.rightPane.utility.getContentWidth()).addToContainer(ui.rightPane.model);
        new UIPixlites(lx, ui, 0, 0, ui.rightPane.utility.getContentWidth()).addToContainer(ui.rightPane.model);
    }
}
