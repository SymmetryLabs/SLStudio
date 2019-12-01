package com.symmetrylabs.shows.banyan;

import com.symmetrylabs.shows.base.SLShow;
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
    final float FOOT = 12;

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

    BranchConfig[] LIMB_TYPE = new BranchConfig[] {

        // -------------------------------------------------------
        /* 1  */ new BranchConfig(false, 0, 0, 0, 0, 0, 0, BRANCH),
        /* 2  */ new BranchConfig(false, 0, 0, 0, 0, 0, 0, BRANCH),
        /* 3  */ new BranchConfig(true, -86+92, 20*FOOT+1, 90-75, 0, 0, 0, BRANCH),
        /* 4  */ new BranchConfig(false, 0, 0, 0, 0, 0, 0, BRANCH),
        /* 5  */ new BranchConfig(false, 0, 0, 0, 0, 0, 0, BRANCH),
        /* 6  */ new BranchConfig(true, -95+92, 20*FOOT, -10-75, 0, 0, 0, BRANCH),
        /* 7  */ new BranchConfig(false, 0, 0, 0, 0, 0, 0, BRANCH),
        /* 8  */ new BranchConfig(false, 0, 0, 0, 0, 0, 0, BRANCH),
        /* 9  */ new BranchConfig(false, 0, 0, 0, 0, 0, 0, BRANCH),
        /* 10 */ new BranchConfig(true, -30+92, 18*FOOT+10, 28-75, 0, 0, 0, BRANCH),

        // -------------------------------------------------------
        /* 11 */ new BranchConfig(true, -64+92, 16*FOOT+11, -91-75, 0, 0, 0, BRANCH),
        /* 12 */ new BranchConfig(false, 0, 0, 0, 0, 0, 0, BRANCH),
        /* 13 */ new BranchConfig(true, 13+92, 16*FOOT, 26-75, 0, 0, 0, BRANCH),
        /* 14 */ new BranchConfig(false, 0, 0, 0, 0, 0, 0, BRANCH),
        /* 15 */ new BranchConfig(false, 0, 0, 0, 0, 0, 0, BRANCH),
        /* 16 */ new BranchConfig(true, -13+92, 19*FOOT+2, 48-75, 0, 0, 0, BRANCH),
        /* 17 */ new BranchConfig(false, 0, 0, 0, 0, 0, 0, BRANCH),
        /* 18 */ new BranchConfig(false, 0, 0, 0, 0, 0, 0, BRANCH),
        /* 19 */ new BranchConfig(false, 0, 0, 0, 0, 0, 0, BRANCH),
        /* 20 */ new BranchConfig(false, 0, 0, 0, 0, 0, 0, BRANCH),

        // -------------------------------------------------------
        /* 21 */ // new BranchConfig(false, 0, 0, 0, 0, 0, 0, BRANCH), EMPTY!
        /* 22 */ new BranchConfig(false, 0, 0, 0, 0, 0, 0, BRANCH),
        /* 23 */ new BranchConfig(false, 0, 0, 0, 0, 0, 0, BRANCH),
        /* 24 */ new BranchConfig(false, 0, 0, 0, 0, 0, 0, BRANCH),
        /* 25 */ new BranchConfig(false, 0, 0, 0, 0, 0, 0, BRANCH),
        /* 26 */ new BranchConfig(false, 0, 0, 0, 0, 0, 0, BRANCH),
        /* 27 */ new BranchConfig(false, 0, 0, 0, 0, 0, 0, BRANCH),
        /* 28 */ new BranchConfig(false, 0, 0, 0, 0, 0, 0, BRANCH),
        /* 29 */ new BranchConfig(false, 0, 0, 0, 0, 0, 0, BRANCH),
        /* 30 */ new BranchConfig(false, 0, 0, 0, 0, 0, 0, BRANCH),

        // -------------------------------------------------------
        /* 31 */ new BranchConfig(true, -18+92, 16*FOOT+8, 150-75, 0, 0, 0, BRANCH),
        /* 32 */ new BranchConfig(true,  -2+92, 17*FOOT+3, 111-75, 0, 0, 0, BRANCH),
        /* 33 */ new BranchConfig(true, 18+92, 18*FOOT, 63-75, 0, 0, 0, BRANCH),
        /* 34 */ new BranchConfig(false, 0, 0, 0, 0, 0, 0, BRANCH),
        /* 35 */ new BranchConfig(true, -33+92, 19*FOOT, 140-75, 0, 0, 0, BRANCH),
        /* 36 */ new BranchConfig(true, -53+92, 20*FOOT, 126-75, 0, 0, 0, BRANCH),
        /* 37 */ new BranchConfig(true, -32+92, 14*FOOT+6, 82-75, 0, 0, 0, BRANCH),
        /* 38 */ new BranchConfig(false, 0, 0, 0, 0, 0, 0, BRANCH),
        /* 39 */ new BranchConfig(true, -107+92, 16*FOOT+8, 116-75, 0, 0, 0, BRANCH),
        /* 40 */ new BranchConfig(false, 0, 0, 0, 0, 0, 0, BRANCH),

        // -------------------------------------------------------
        /* 41 */ new BranchConfig(false, 0, 0, 0, 0, 0, 0, BRANCH),
        /* 42 */ new BranchConfig(false, 0, 0, 0, 0, 0, 0, BRANCH),
        /* 43 */ new BranchConfig(false, 0, 0, 0, 0, 0, 0, BRANCH),
        /* 44 */ new BranchConfig(false, 0, 0, 0, 0, 0, 0, BRANCH),
        /* 45 */ new BranchConfig(false, 0, 0, 0, 0, 0, 0, BRANCH),
        /* 46 */ new BranchConfig(false, 0, 0, 0, 0, 0, 0, BRANCH),
        /* 47 */ new BranchConfig(false, 0, 0, 0, 0, 0, 0, BRANCH),
        /* 48 */ new BranchConfig(false, 0, 0, 0, 0, 0, 0, BRANCH),
        /* 49 */ new BranchConfig(true, -20+92, 20*FOOT+9, 93-75, 0, 0, 0, BRANCH),
        /* 50 */ new BranchConfig(true, -28+92, 21*FOOT+7, 79-75, 0, 0, 0, BRANCH),

        // -------------------------------------------------------
        /* 51 */ new BranchConfig(true, -74+92, 18*FOOT+7, 78-75, 0, 0, 0, BRANCH),
        /* 52 */ new BranchConfig(true, -65+92, 17*FOOT+6, 69-75, 0, 0, 0, BRANCH),
        /* 53 */ new BranchConfig(true, -101+92, 17*FOOT+3, 44-75, 0, 0, 0, BRANCH),
        /* 54 */ new BranchConfig(false, 0, 0, 0, 0, 0, 0, BRANCH),
        /* 55 */ new BranchConfig(true, -91+92, 19*FOOT+4, 35-75, 0, 0, 0, BRANCH),
        /* 56 */ new BranchConfig(false, 0, 0, 0, 0, 0, 0, BRANCH),
        /* 57 */ new BranchConfig(true, -93+92, 11*FOOT+11, -14-75, 0, 0, 0, BRANCH),
        /* 58 */ new BranchConfig(true, -42+92, 14*FOOT+5,   10-75, 0, 0, 0, BRANCH),
        /* 59 */ new BranchConfig(false, 0, 0, 0, 0, 0, 0, BRANCH),
        /* 60 */ new BranchConfig(true, -14+92, 11*FOOT+10,  42-75, 0, 0, 0, BRANCH),

        // -------------------------------------------------------
        /* 61 */ new BranchConfig(true, -65+92, 13*FOOT+4, -26-75, 0, 0, 0, BRANCH),
        /* 62 */ new BranchConfig(true, -18+92, 13*FOOT+4,   0, 0, 0, 0, BRANCH),
        /* 63 */ new BranchConfig(true,  -6+92, 15*FOOT+2, -32-75, 0, 0, 0, BRANCH),
        /* 64 */ new BranchConfig(true, 0, 14*FOOT+10, 45-75, 0, 0, 0, BRANCH),
        /* 65 */ new BranchConfig(true, -12+92, 17*FOOT+2, 39-75, 0, 0, 0, BRANCH),
        /* 66 */ new BranchConfig(true,  -8+92, 17*FOOT+6, -15-75, 0, 0, 0, BRANCH),
        /* 67 */ new BranchConfig(true, -31+92, 15*FOOT+8, -60-75, 0, 0, 0, BRANCH),
        /* 68 */ new BranchConfig(true, -25+92, 17*FOOT+9, -60-75, 0, 0, 0, BRANCH),
        /* 69 */ new BranchConfig(false, 0, 0, 0, 0, 0, 0, BRANCH),
        /* 70 */ new BranchConfig(false, 0, 0, 0, 0, 0, 0, BRANCH),

        // -------------------------------------------------------
        /* 71 */ new BranchConfig(false, 0, 0, 0, 0, 0, 0, BRANCH),
        /* 72 */ new BranchConfig(false, 0, 0, 0, 0, 0, 0, BRANCH),
        /* 73 */ new BranchConfig(false, 0, 0, 0, 0, 0, 0, BRANCH),
        /* 74 */ new BranchConfig(false, 0, 0, 0, 0, 0, 0, BRANCH),
        /* 75 */ new BranchConfig(false, 0, 0, 0, 0, 0, 0, BRANCH),
        /* 76 */ new BranchConfig(true, -54+92, 19*FOOT+8, 29-75, 0, 0, 0, BRANCH),
        /* 77 */ new BranchConfig(true, -47+92, 18*FOOT+4, 20-75, 0, 0, 0, BRANCH),
        /* 78 */ new BranchConfig(true, -53+92, 19*FOOT+4, -59-75, 0, 0, 0, BRANCH),
        /* 79 */ new BranchConfig(true, -67+92, 20*FOOT, -24-75, 0, 0, 0, BRANCH),
        /* 80 */ new BranchConfig(false, 0, 0, 0, 0, 0, 0, BRANCH),
    };

    public SLModel buildModel() {
        // for (int i = 0; i < NUM_BRANCHES; i++) {
        //     LIMB_TYPE[i] = new BranchConfig(false, 0, 0, 0, 0, 0, 0, BRANCH, true);
        // }



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

        BanyanModel.Star.Config starConfig = new BanyanModel.Star.Config(0, 30*FOOT, 0, 220);

        BanyanModel banyanModel = new BanyanModel(SHOW_NAME, treeConfig, starConfig);
        return banyanModel;

//        InsideShardPanel shard = new InsideShardPanel(SHOW_NAME, new LXTransform());
//        return shard;

    }

    @Override
    public void setupLx(LX lx) {
        super.setupLx(lx);

//         static pixlite output for the star
        SimplePixlite starLite = new SimplePixlite(lx, "10.200.1.100");
        int ROTATE = 1;

        int get = 0;
        starLite.addPixliteOutput(new PointsGrouping( "1", BanyanModel.star.innerPanels.get(get).getPoints()).reversePoints()
            .addPoints(BanyanModel.star.outerPanels.get(get).getPoints()));
        get = 7;
        starLite.addPixliteOutput(new PointsGrouping( "2", BanyanModel.star.innerPanels.get(get).getPoints()).reversePoints()
            .addPoints(BanyanModel.star.outerPanels.get(get).getPoints()));
        get = 6;
        starLite.addPixliteOutput(new PointsGrouping( "3", BanyanModel.star.innerPanels.get(get).getPoints()).reversePoints()
            .addPoints(BanyanModel.star.outerPanels.get(get).getPoints()));
        get = 5;
        starLite.addPixliteOutput(new PointsGrouping( "4", BanyanModel.star.innerPanels.get(get).getPoints()).reversePoints()
            .addPoints(BanyanModel.star.outerPanels.get(get).getPoints()));
        get = 4;
        starLite.addPixliteOutput(new PointsGrouping( "5", BanyanModel.star.innerPanels.get(get).getPoints()).reversePoints()
            .addPoints(BanyanModel.star.outerPanels.get(get).getPoints()));
        get = 3;
        starLite.addPixliteOutput(new PointsGrouping( "6", BanyanModel.star.innerPanels.get(get).getPoints()).reversePoints()
            .addPoints(BanyanModel.star.outerPanels.get(get).getPoints()));
        get = 2;
        starLite.addPixliteOutput(new PointsGrouping( "7", BanyanModel.star.innerPanels.get(get).getPoints()).reversePoints()
            .addPoints(BanyanModel.star.outerPanels.get(get).getPoints()));
        get = 1;
        starLite.addPixliteOutput(new PointsGrouping( "8", BanyanModel.star.innerPanels.get(get).getPoints()).reversePoints()
            .addPoints(BanyanModel.star.outerPanels.get(get).getPoints()));

        get = 8%8;
        starLite.addPixliteOutput(new PointsGrouping( "9", BanyanModel.star.innerPanels.get(get).getPoints()).reversePoints()
            .addPoints(BanyanModel.star.outerPanels.get(get).getPoints()));
        get = 15%8;
        starLite.addPixliteOutput(new PointsGrouping( "10", BanyanModel.star.innerPanels.get(get).getPoints()).reversePoints()
            .addPoints(BanyanModel.star.outerPanels.get(get).getPoints()));
        get = 14%8;
        starLite.addPixliteOutput(new PointsGrouping( "11", BanyanModel.star.innerPanels.get(get).getPoints()).reversePoints()
            .addPoints(BanyanModel.star.outerPanels.get(get).getPoints()));
        get = 13%8;
        starLite.addPixliteOutput(new PointsGrouping( "12", BanyanModel.star.innerPanels.get(get).getPoints()).reversePoints()
            .addPoints(BanyanModel.star.outerPanels.get(get).getPoints()));
        get = 12%8;
        starLite.addPixliteOutput(new PointsGrouping( "13", BanyanModel.star.innerPanels.get(get).getPoints()).reversePoints()
            .addPoints(BanyanModel.star.outerPanels.get(get).getPoints()));
        get = 11%8;
        starLite.addPixliteOutput(new PointsGrouping( "14", BanyanModel.star.innerPanels.get(get).getPoints()).reversePoints()
            .addPoints(BanyanModel.star.outerPanels.get(get).getPoints()));
        get = 10%8;
        starLite.addPixliteOutput(new PointsGrouping( "15", BanyanModel.star.innerPanels.get(get).getPoints()).reversePoints()
            .addPoints(BanyanModel.star.outerPanels.get(get).getPoints()));
        get = 9%8;
        starLite.addPixliteOutput(new PointsGrouping( "16", BanyanModel.star.innerPanels.get(get).getPoints()).reversePoints()
            .addPoints(BanyanModel.star.outerPanels.get(get).getPoints()));


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
