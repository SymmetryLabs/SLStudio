package com.symmetrylabs.shows.banyan;

import com.symmetrylabs.shows.tree.ui.UITreeStructure;
import com.symmetrylabs.slstudio.SLStudio;
import com.symmetrylabs.slstudio.model.banyan.InsideShardPanel;

import com.symmetrylabs.slstudio.SLStudioLX;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.shows.tree.*;
import com.symmetrylabs.shows.tree.config.*;
import com.symmetrylabs.shows.tree.ui.UITenereControllers;
import com.symmetrylabs.slstudio.output.PointsGrouping;
import com.symmetrylabs.slstudio.output.SimplePixlite;
import heronarts.lx.LX;
import heronarts.lx.transform.LXTransform;

import java.net.SocketException;


public class BanyanShow extends TreeShow {
    public static final String SHOW_NAME = "banyan";

    public static final int NUM_BRANCHES = 80;

    final TwigConfig[] BRANCH = new TwigConfig[]{
        new TwigConfig(-22.367998f, 0.0f, 0.0f, 43.2f, 0.0f, 0.0f, 1),
        new TwigConfig(-19.487999f, 12.575999f, 0.0f, 30.599998f, 0.0f, 0.0f, 2),
        new TwigConfig(-12.479999f, 26.208f, 0.0f, 16.919998f, 0.0f, 0.0f, 3),
        new TwigConfig(-1.344f, 31.008f, 0.0f, 6.1199994f, 0.0f, 0.0f, 4),
        new TwigConfig(8.063999f, 31.104f, 0.0f, -10.799999f, 0.0f, 0.0f, 5),
        new TwigConfig(13.727999f, 20.159998f, 0.0f, -46.799995f, 0.0f, 0.0f, 6),
        new TwigConfig(18.24f, 8.9279995f, 0.0f, -45.359997f, 0.0f, 0.0f, 7),
        new TwigConfig(21.792f, 0.0f, 0.0f, -47.159996f, 0.0f, 0.0f, 8)
    };

    BranchConfig[] LIMB_TYPE = new BranchConfig[NUM_BRANCHES];

    public SLModel buildModel() {
        for (int i = 0; i < NUM_BRANCHES; i++) {
            LIMB_TYPE[i] = new BranchConfig(false, 0, 0, 0, 0, 0, 0, BRANCH, true);
        }

//        LimbConfig.lengthEnabled = false;
//        LimbConfig.heightEnabled = false;
//        LimbConfig.azimuthEnabled = false;
//        LimbConfig.elevationEnabled = false;

        TreeConfig.createLimbType("Limb 1", LIMB_TYPE);
        TreeConfig.createBranchType("Branch", BRANCH);

        TreeConfig treeConfig = new TreeConfig(new LimbConfig[] {
            // just one limb
            new LimbConfig(false, 0, 0, 0, 0, 0, LIMB_TYPE),
        });

        BanyanModel.Star.Config starConfig = new BanyanModel.Star.Config(0, 0, 100, 0);

        BanyanModel banyanModel = new BanyanModel(SHOW_NAME, treeConfig, starConfig);

        return banyanModel;
    }

    @Override
    public void setupLx(LX lx) {
        super.setupLx(lx);

        // static pixlite output for the star
        SimplePixlite starLite = new SimplePixlite(lx, "10.200.1.100");
        for (int portNum = 0; portNum < 16; portNum++){
            starLite.addPixliteOutput(new PointsGrouping( (portNum + 1) + "", BanyanModel.star.innerPanels.get( (portNum) % 8).getPoints()).reversePoints()
            .addPoints(BanyanModel.star.outerPanels.get( (portNum + 7/*left rotate 1*/)% 8).getPoints()));
        }
        lx.addOutput(starLite);

    }

    public void setupUi(SLStudioLX lx, SLStudioLX.UI ui) {
        super.setupUi(lx, ui);
        ui.preview.addComponent(new UITreeStructure((TreeModel) lx.model));

//        new UITenereControllers(lx, ui, 0, 0, ui.rightPane.utility.getContentWidth()).addToContainer(ui.rightPane.model);
    }
    
    @Override
    public String getShowName() {
        return SHOW_NAME;
    }
}
