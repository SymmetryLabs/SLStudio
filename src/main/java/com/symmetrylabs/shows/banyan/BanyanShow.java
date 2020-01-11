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
import heronarts.lx.LXLoopTask;
import heronarts.lx.LXChannel;
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
        /* 1  */ new BranchConfig(false, 18-108, 16*FOOT, 108-75, 0, 0, 0, BRANCH), // Round B
        /* 2  */ new BranchConfig(false, 0, 0, 0, 0, 0, 0, BRANCH),
        /* 3  */ new BranchConfig(false, -86+92, 20*FOOT+1, 90-75, 0, 0, 0, BRANCH), // Round A
        /* 4  */ new BranchConfig(false, -6-108, 14*FOOT+7, 14-75, 0, 0, 0, BRANCH), // Round B
        /* 5  */ new BranchConfig(false, 68-108, 14*FOOT+9, 20-75, 0, 0, 0, BRANCH), // Round B
        /* 6  */ new BranchConfig(false, -95+92, 20*FOOT, -10-75, 0, 0, 0, BRANCH), // Round A
        /* 7  */ new BranchConfig(false, 0, 0, 0, 0, 0, 0, BRANCH),
        /* 8  */ new BranchConfig(false, 29-108, 17*FOOT+11, 32-75, 0, 0, 0, BRANCH), // Round B
        /* 9  */ new BranchConfig(false, 0, 0, 0, 0, 0, 0, BRANCH),
        /* 10 */ new BranchConfig(false, -30+92, 18*FOOT+10, 28-75, 0, 0, 0, BRANCH), // Round A

        // -------------------------------------------------------
        /* 11 */ new BranchConfig(false, -64+92, 16*FOOT+11, -91-75, 0, 0, 0, BRANCH), // Round A
        /* 12 */ new BranchConfig(false, 13-108, 16*FOOT+10, -75, 0, 0, 0, BRANCH), // Round B
        /* 13 */ new BranchConfig(false, 13+92, 16*FOOT, 26-75, 0, 0, 0, BRANCH), // Round A
        /* 14 */ new BranchConfig(false, -4-108, 16*FOOT+5, 85-75, 0, 0, 0, BRANCH), // Round B
        /* 15 */ new BranchConfig(false, 0, 0, 0, 0, 0, 0, BRANCH),
        /* 16 */ new BranchConfig(false, -13+92, 19*FOOT+2, 48-75, 0, 0, 0, BRANCH), // Round A
        /* 17 */ new BranchConfig(false, -14-108, 16*FOOT+5, 100-75, 0, 0, 0, BRANCH), // Round B
        /* 18 */ new BranchConfig(false, -20-108, 15*FOOT+10, 112-75, 0, 0, 0, BRANCH), // Round B
        /* 19 */ new BranchConfig(false, 34-108, 16*FOOT+7, 154-75, 0, 0, 0, BRANCH), // Round B
        /* 20 */ new BranchConfig(false, 41-108, 13*FOOT+8, 169-75, 0, 0, 0, BRANCH), // Round B

        // -------------------------------------------------------
        /* 21 */ new BranchConfig(false, 0, 0, 0, 0, 0, 0, BRANCH), // EMPTY!
        /* 22 */ new BranchConfig(false, 21-108, 16*FOOT+1, -31-75, 0, 0, 0, BRANCH), // Round B
        /* 23 */ new BranchConfig(false, 0, 0, 0, 0, 0, 0, BRANCH),
        /* 24 */ new BranchConfig(false, 119-108, 17*FOOT+1, 181-22-75, 0, 0, 0, BRANCH), // Round B
        /* 25 */ new BranchConfig(false, 90-108, 13*FOOT+8, 181-15-75, 0, 0, 0, BRANCH), // Round B1
        /* 26 */ new BranchConfig(false, 104-108, 17*FOOT, 181-6-75, 0, 0, 0, BRANCH), // Round B
        /* 27 */ new BranchConfig(false, 119-108, 18*8+8, 181-11-75, 0, 0, 0, BRANCH), // Round B
        /* 28 */ new BranchConfig(false, 73-108, 16*FOOT+7, 181-14-75, 0, 0, 0, BRANCH), // Round B
        /* 29 */ new BranchConfig(false, 73-108, 18*FOOT+8, 181-24-75, 0, 0, 0, BRANCH), // Round B
        /* 30 */ new BranchConfig(false, 0, 0, 0, 0, 0, 0, BRANCH),

        // -------------------------------------------------------
        /* 31 */ new BranchConfig(false, -18+92, 16*FOOT+8, 150-75, 0, 0, 0, BRANCH), // Round A
        /* 32 */ new BranchConfig(false,  -2+92, 17*FOOT+3, 111-75, 0, 0, 0, BRANCH), // Round A
        /* 33 */ new BranchConfig(false, 18+92, 18*FOOT, 63-75, 0, 0, 0, BRANCH), // Round A
        /* 34 */ new BranchConfig(false, 10-108, 14*FOOT+9, 153-75, 0, 0, 0, BRANCH), // Round B
        /* 35 */ new BranchConfig(false, -33+92, 19*FOOT, 140-75, 0, 0, 0, BRANCH), // Round A
        /* 36 */ new BranchConfig(false, -53+92, 20*FOOT, 126-75, 0, 0, 0, BRANCH), // Round A
        /* 37 */ new BranchConfig(false, -32+92, 14*FOOT+6, 82-75, 0, 0, 0, BRANCH), // Round A
        /* 38 */ new BranchConfig(false, 0, 0, 0, 0, 0, 0, BRANCH),
        /* 39 */ new BranchConfig(false, -107+92, 16*FOOT+8, 116-75, 0, 0, 0, BRANCH), // Round A
        /* 40 */ new BranchConfig(false, 47-108, 18*FOOT+8, 126-75, 0, 0, 0, BRANCH), // Round B

        // -------------------------------------------------------
        /* 41 */ new BranchConfig(false, 0, 0, 0, 0, 0, 0, BRANCH),
        /* 42 */ new BranchConfig(false, 39-108, 14*FOOT+1, 75-75, 0, 0, 0, BRANCH), // Round B
        /* 43 */ new BranchConfig(false, 0, 0, 0, 0, 0, 0, BRANCH),
        /* 44 */ new BranchConfig(false, 52-108, 14 *FOOT+7, 76-75, 0, 0, 0, BRANCH), // Round B
        /* 45 */ new BranchConfig(false, 73-108, 17*FOOT+2, 66-75, 0, 0, 0, BRANCH), // Round B
        /* 46 */ new BranchConfig(false, 67-108, 20*FOOT+3, 49-75, 0, 0, 0, BRANCH), // Round B
        /* 47 */ new BranchConfig(false, 0, 0, 0, 0, 0, 0, BRANCH),
        /* 48 */ new BranchConfig(false, 36-108, 19*FOOT+7, 93-75, 0, 0, 0, BRANCH), // Round B
        /* 49 */ new BranchConfig(false, -20+92, 20*FOOT+9, 93-75, 0, 0, 0, BRANCH), // Round A
        /* 50 */ new BranchConfig(false, -28+92, 21*FOOT+7, 79-75, 0, 0, 0, BRANCH), // Round A

        // -------------------------------------------------------
        /* 51 */ new BranchConfig(false, -74+92, 18*FOOT+7, 78-75, 0, 0, 0, BRANCH), // Round A
        /* 52 */ new BranchConfig(false, -65+92, 17*FOOT+6, 69-75, 0, 0, 0, BRANCH), // Round A
        /* 53 */ new BranchConfig(false, -101+92, 17*FOOT+3, 44-75, 0, 0, 0, BRANCH), // Round A
        /* 54 */ new BranchConfig(false, 55-108, 19*FOOT, 32-75, 0, 0, 0, BRANCH), // Round B
        /* 55 */ new BranchConfig(false, -91+92, 19*FOOT+4, 35-75, 0, 0, 0, BRANCH), // Round A
        /* 56 */ new BranchConfig(false, 0, 0, 0, 0, 0, 0, BRANCH),
        /* 57 */ new BranchConfig(false, -93+92, 11*FOOT+11, -14-75, 0, 0, 0, BRANCH), // Round A
        /* 58 */ new BranchConfig(false, -42+92, 14*FOOT+5,   10-75, 0, 0, 0, BRANCH), // Round A
        /* 59 */ new BranchConfig(false, 0, 0, 0, 0, 0, 0, BRANCH),
        /* 60 */ new BranchConfig(false, -14+92, 11*FOOT+10,  42-75, 0, 0, 0, BRANCH), // Round A

        // -------------------------------------------------------
        /* 61 */ new BranchConfig(false, -65+92, 13*FOOT+4, -26-75, 0, 0, 0, BRANCH), // Round A
        /* 62 */ new BranchConfig(false, -18+92, 13*FOOT+4,   0, 0, 0, 0, BRANCH), // Round A
        /* 63 */ new BranchConfig(false,  -6+92, 15*FOOT+2, -32-75, 0, 0, 0, BRANCH), // Round A
        /* 64 */ new BranchConfig(false, 0, 14*FOOT+10, 45-75, 0, 0, 0, BRANCH), // Round A
        /* 65 */ new BranchConfig(false, -12+92, 17*FOOT+2, 39-75, 0, 0, 0, BRANCH), // Round A
        /* 66 */ new BranchConfig(false,  -8+92, 17*FOOT+6, -15-75, 0, 0, 0, BRANCH), // Round A
        /* 67 */ new BranchConfig(false, -31+92, 15*FOOT+8, -60-75, 0, 0, 0, BRANCH), // Round A
        /* 68 */ new BranchConfig(false, -25+92, 17*FOOT+9, -60-75, 0, 0, 0, BRANCH), // Round A
        /* 69 */ new BranchConfig(false, 40-108, 18*FOOT, 9-75, 0, 0, 0, BRANCH), // Round B
        /* 70 */ new BranchConfig(false, 0, 0, 0, 0, 0, 0, BRANCH),

        // -------------------------------------------------------
        /* 71 */ new BranchConfig(false, 31-108, 12*FOOT, 110-75, 0, 0, 0, BRANCH), // Round B
        /* 72 */ new BranchConfig(false, 0-108, 18*FOOT+8, 68-75, 0, 0, 0, BRANCH), // Round B
        /* 73 */ new BranchConfig(false, 0, 0, 0, 0, 0, 0, BRANCH),
        /* 74 */ new BranchConfig(false, 43-108, 13*FOOT+2, 9-75, 0, 0, 0, BRANCH), // Round B
        /* 75 */ new BranchConfig(false, 93-108, 13*FOOT+1, 181-50, 0, 0, 0, BRANCH), // Round B1
        /* 76 */ new BranchConfig(false, -54+92, 19*FOOT+8, 29-75, 0, 0, 0, BRANCH), // Round A
        /* 77 */ new BranchConfig(false, -47+92, 18*FOOT+4, 20-75, 0, 0, 0, BRANCH), // Round A
        /* 78 */ new BranchConfig(false, -53+92, 19*FOOT+4, -59-75, 0, 0, 0, BRANCH), // Round A
        /* 79 */ new BranchConfig(false, -67+92, 20*FOOT, -24-75, 0, 0, 0, BRANCH), // Round A
        /* 80 */ new BranchConfig(false, 0, 0, 0, 0, 0, 0, BRANCH),

        /* 80 */ new BranchConfig(false, -86+92, 20*FOOT, 42-75, 0, 0, 0, BRANCH), // was 53B!

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

        // BanyanModel.Star.Config starConfig = new BanyanModel.Star.Config(0, 26*FOOT, 0, 220);

        BanyanModel banyanModel = new BanyanModel(SHOW_NAME, treeConfig);
        return banyanModel;

//        InsideShardPanel shard = new InsideShardPanel(SHOW_NAME, new LXTransform());
//        return shard;

    }

    @Override
    public void setupLx(LX lx) {
        super.setupLx(lx);

//         static pixlite output for the star
        // SimplePixlite starLite = new SimplePixlite(lx, "10.200.1.100");
        // int ROTATE = 1;

        // int get = 0;
        // starLite.addPixliteOutput(new PointsGrouping( "1", BanyanModel.star.innerPanels.get(get).getPoints()).reversePoints()
        //     .addPoints(BanyanModel.star.outerPanels.get(get).getPoints()));
        // get = 7;
        // starLite.addPixliteOutput(new PointsGrouping( "2", BanyanModel.star.innerPanels.get(get).getPoints()).reversePoints()
        //     .addPoints(BanyanModel.star.outerPanels.get(get).getPoints()));
        // get = 6;
        // starLite.addPixliteOutput(new PointsGrouping( "3", BanyanModel.star.innerPanels.get(get).getPoints()).reversePoints()
        //     .addPoints(BanyanModel.star.outerPanels.get(get).getPoints()));
        // get = 5;
        // starLite.addPixliteOutput(new PointsGrouping( "4", BanyanModel.star.innerPanels.get(get).getPoints()).reversePoints()
        //     .addPoints(BanyanModel.star.outerPanels.get(get).getPoints()));
        // get = 4;
        // starLite.addPixliteOutput(new PointsGrouping( "5", BanyanModel.star.innerPanels.get(get).getPoints()).reversePoints()
        //     .addPoints(BanyanModel.star.outerPanels.get(get).getPoints()));
        // get = 3;
        // starLite.addPixliteOutput(new PointsGrouping( "6", BanyanModel.star.innerPanels.get(get).getPoints()).reversePoints()
        //     .addPoints(BanyanModel.star.outerPanels.get(get).getPoints()));
        // get = 2;
        // starLite.addPixliteOutput(new PointsGrouping( "7", BanyanModel.star.innerPanels.get(get).getPoints()).reversePoints()
        //     .addPoints(BanyanModel.star.outerPanels.get(get).getPoints()));
        // get = 1;
        // starLite.addPixliteOutput(new PointsGrouping( "8", BanyanModel.star.innerPanels.get(get).getPoints()).reversePoints()
        //     .addPoints(BanyanModel.star.outerPanels.get(get).getPoints()));

        // get = 8%8;
        // starLite.addPixliteOutput(new PointsGrouping( "9", BanyanModel.star.innerPanels.get(get).getPoints()).reversePoints()
        //     .addPoints(BanyanModel.star.outerPanels.get(get).getPoints()));
        // get = 15%8;
        // starLite.addPixliteOutput(new PointsGrouping( "10", BanyanModel.star.innerPanels.get(get).getPoints()).reversePoints()
        //     .addPoints(BanyanModel.star.outerPanels.get(get).getPoints()));
        // get = 14%8;
        // starLite.addPixliteOutput(new PointsGrouping( "11", BanyanModel.star.innerPanels.get(get).getPoints()).reversePoints()
        //     .addPoints(BanyanModel.star.outerPanels.get(get).getPoints()));
        // get = 13%8;
        // starLite.addPixliteOutput(new PointsGrouping( "12", BanyanModel.star.innerPanels.get(get).getPoints()).reversePoints()
        //     .addPoints(BanyanModel.star.outerPanels.get(get).getPoints()));
        // get = 12%8;
        // starLite.addPixliteOutput(new PointsGrouping( "13", BanyanModel.star.innerPanels.get(get).getPoints()).reversePoints()
        //     .addPoints(BanyanModel.star.outerPanels.get(get).getPoints()));
        // get = 11%8;
        // starLite.addPixliteOutput(new PointsGrouping( "14", BanyanModel.star.innerPanels.get(get).getPoints()).reversePoints()
        //     .addPoints(BanyanModel.star.outerPanels.get(get).getPoints()));
        // get = 10%8;
        // starLite.addPixliteOutput(new PointsGrouping( "15", BanyanModel.star.innerPanels.get(get).getPoints()).reversePoints()
        //     .addPoints(BanyanModel.star.outerPanels.get(get).getPoints()));
        // get = 9%8;
        // starLite.addPixliteOutput(new PointsGrouping( "16", BanyanModel.star.innerPanels.get(get).getPoints()).reversePoints()
        //     .addPoints(BanyanModel.star.outerPanels.get(get).getPoints()));

        // starLite.enabled.setValue(true);
        // lx.addOutput(starLite);

       lx.engine.addLoopTask(new LXLoopTask() {
           @Override
           public void loop(double v) {
                for (LXChannel channel : lx.engine.getChannels()) {
                    if (channel.fader.getValue() == 0) {
                        channel.enabled.setValue(false);
                    } else {
                        if (!channel.enabled.isOn()) {
                            channel.enabled.setValue(true);
                        }
                    }
                }
           }
        });

    }

    public void setupUi(SLStudioLX lx, SLStudioLX.UI ui) {
        super.setupUi(lx, ui);
       // ui.preview.addComponent(new UITreeStructure((TreeModel) lx.model));

//        new UITenereControllers(lx, ui, 0, 0, ui.rightPane.utility.getContentWidth()).addToContainer(ui.rightPane.model);
    }
    
    @Override
    public String getShowName() {
        return SHOW_NAME;
    }
}
