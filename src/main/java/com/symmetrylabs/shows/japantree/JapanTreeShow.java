package com.symmetrylabs.shows.japantree;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.net.SocketException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import heronarts.lx.LXLoopTask;

import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;

import com.symmetrylabs.shows.Show;
import com.symmetrylabs.slstudio.SLStudio;
import com.symmetrylabs.slstudio.SLStudioLX;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.shows.tree.*;
import com.symmetrylabs.shows.tree.config.*;
import com.symmetrylabs.shows.tree.ui.*;
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

    TreeConfig config;

    @Override
    public SLModel buildModel() {
        config = CompiledTreeData.CONFIG;
        TreeModel tree = new TreeModel(config);
        return tree;
    }

    @Override
    public void setupLx(SLStudioLX lx) {
        super.setupLx(lx);

        TreeModel tree = (TreeModel) (lx.model);

        lx.engine.addLoopTask(new LXLoopTask() {
            @Override
            public void loop(double v) {
                if (lx.engine.output.brightness.getValuef() > 0.9f) {
                    lx.engine.output.brightness.setValue(0.9f);
                }
                if (lx.engine.framesPerSecond.getValuef() != 60) {
                    lx.engine.framesPerSecond.setValue(60);
                }
            }
        });

        int branchId = 0;
        for (TreeModel.Branch branch : tree.getBranches()) {
            try {
                AssignableTenereController controller = new AssignableTenereController(lx, branch);
                controllers.put(branch, controller);
                lx.addOutput(controller);
            } catch (SocketException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void setupUi(SLStudioLX lx, SLStudioLX.UI ui) {
        super.setupUi(lx, ui);
    }

    @Override
    public boolean readConfigFromDisk() {
        return false;
    }
}
