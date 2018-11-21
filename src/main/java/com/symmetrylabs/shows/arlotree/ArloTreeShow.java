package com.symmetrylabs.shows.arlotree;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.net.SocketException;


import heronarts.lx.model.LXPoint;

import com.symmetrylabs.shows.Show;
import com.symmetrylabs.slstudio.SLStudio;
import com.symmetrylabs.slstudio.SLStudioLX;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.shows.tree.*;
import com.symmetrylabs.shows.tree.config.*;
import com.symmetrylabs.shows.tree.ui.*;
import com.symmetrylabs.shows.tree.ui.UIScheduler;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI3dComponent;

import static com.symmetrylabs.util.DistanceConstants.*;

import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI3dComponent;
import processing.core.PGraphics;
import static com.symmetrylabs.util.DistanceConstants.*;


public class ArloTreeShow extends TreeShow {

    public static final String SHOW_NAME = "arlotree";

    // female
    final TwigConfig[] BRANCH_TYPE_A = new TwigConfig[]{
        new TwigConfig( 0.0f,  37.4f, 0.0f, -32.4f, 0.0f, 0.0f, 1), // 8
        new TwigConfig(-16.3f, 36.5f, 0.0f,   6.8f, 0.0f, 0.0f, 2), // 7
        new TwigConfig(-29.8f, 25.9f, 0.0f,  28.8f, 0.0f, 0.0f, 3), // 6
        new TwigConfig(-37.4f, 10.5f, 0.0f,  28.8f, 0.0f, 0.0f, 4), // 5
        new TwigConfig( -5.7f,  9.5f, 0.0f,  14.4f, 0.0f, 0.0f, 5), // 4
        new TwigConfig( 14.4f, 19.2f, 0.0f,  18.0f, 0.0f, 0.0f, 6), // 3
        new TwigConfig( 24.5f, 16.3f, 0.0f, -36.0f, 0.0f, 0.0f, 7), // 2
        new TwigConfig( 16.3f,  0.0f, 0.0f, -61.2f, 0.0f, 0.0f, 8), // 1
    };

    // male
    final TwigConfig[] BRANCH_TYPE_B = new TwigConfig[] {
        new TwigConfig( 14.2f,  3.8f, 0.0f, -57.0f, 0.0f, 0.0f, 1), // 8
        new TwigConfig(  4.0f, 15.4f, 0.0f,  50.4f, 0.0f, 0.0f, 2), // 7
        new TwigConfig( 20.2f, 19.3f, 0.0f, -28.8f, 0.0f, 0.0f, 3), // 6
        new TwigConfig( 11.5f, 24.0f, 0.0f, -28.8f, 0.0f, 0.0f, 4), // 5
        new TwigConfig(-32.0f,  9.5f, 0.0f,  34.5f, 0.0f, 0.0f, 5), // 1
        new TwigConfig(-25.0f, 13.4f, 0.0f,   7.2f, 0.0f, 0.0f, 6), // 2
        new TwigConfig(-18.4f, 34.5f, 0.0f,  39.5f, 0.0f, 0.0f, 7), // 3
        new TwigConfig(  0.0f, 37.5f, 0.0f,   0.0f, 0.0f, 0.0f, 8)  // 4
    };

    public SLModel buildModel() {
        TreeConfig config = new TreeConfig(new LimbConfig[] {
            new LimbConfig(false, 50, 13*FEET, -210.0f, -90, 0, new BranchConfig[] {
                new BranchConfig(false, "0.0.0.0", 0, 0, 0, 0, 0, 0, 0, BRANCH_TYPE_A),
                new BranchConfig(false, "0.0.0.0", 200, 0, 0, 0, 0, 0, 0, BRANCH_TYPE_A),

            }),
        });

        return new TreeModel(config);
    }

    public void setupLx(SLStudioLX lx) {
        super.setupLx(lx);
        //lx.engine.framesPerSecond.setValue(30);

        final String[] ipAddresses = new String[] {
            "10.200.1.100",
        };

        for (int i = 0; i < ipAddresses.length; i++) {
            AssignablePixlite pixlite = new AssignablePixlite(lx, ipAddresses[i]);
            pixlites.put(ipAddresses[i], pixlite);
            pixlitePorts.addAll(pixlite.ports);
            lx.addOutput(pixlite);
        }

        //System.out.println("------------------------------");
        for (AssignablePixlite.Port port : pixlitePorts) {
            for (TreeModel.Branch branch : ((TreeModel)lx.model).getBranches()) {
                if (port.ipAddress.equals(branch.getConfig().ipAddress)
                    && port.index == branch.getConfig().channel) {
                    //System.out.println(port.index + " - " + branch.getConfig().channel);
                    port.setBranch(branch);
                }
            }
        }
    }
}
