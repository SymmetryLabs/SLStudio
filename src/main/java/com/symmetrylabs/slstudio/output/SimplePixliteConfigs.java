package com.symmetrylabs.slstudio.output;

import com.symmetrylabs.slstudio.model.CandyBar;
import com.symmetrylabs.slstudio.model.CubesModel;
import heronarts.lx.LX;
import heronarts.lx.model.LXFixture;

public class SimplePixliteConfigs {

    public static SimplePixlite[] setupPixlites(LX lx) {
//    NissanModel model = ((NissanModel)lx.model);
        CubesModel model = ((CubesModel) lx.model);

        /**
         * EXAMPLE
         * EXAMPLE
         * EXAMPLE
         * EXAMPLE
         */
        int i = 0;
        int pix_index = 0;
        SimplePixlite[] fixtures = new SimplePixlite[2];

        SimplePixlite currentPixlite = new SimplePixlite(lx, "10.200.1.130");
        for (i = 0; i < 16; i ++){
            currentPixlite.addPixliteOutput(new PointsGrouping(Integer.toString(i)) // <- output index on pixlite
                    .addPoints(model.bars.get(i).getPoints()));
        }

        fixtures[pix_index++] = currentPixlite;

        fixtures[pix_index] = new SimplePixlite(lx, "10.200.1.128");
        for (; i < 32; i ++){
            fixtures[pix_index].addPixliteOutput(new PointsGrouping(Integer.toString(i)) // <- output index on pixlite
                .addPoints(model.bars.get(i).getPoints()));
        }
        return fixtures;
        // CAR 1
//        int BASE_HOST = 130;
//        int OFFSET = 0;
//        for (LXFixture bar : model.bars) {
//            fixtures[i++] = new SimplePixlite(lx, "10.200.1.".concat(Integer.toString(BASE_HOST + (OFFSET++ /16) )))
//                // don't forget strips start at the bottom of windows
//                .addPixliteOutput(new PointsGrouping(Integer.toString(OFFSET)) // <- output index on pixlite
//                    .addPoints(bar.getPoints()));
//        }
//        return fixtures;
    }

    public static SimplePixlite setupPixlite(LX lx, LXFixture fixture, String ip, int output) {
//    NissanModel model = ((NissanModel)lx.model);

        /**
         * EXAMPLE
         * EXAMPLE
         * EXAMPLE
         * EXAMPLE
         */

        return new SimplePixlite(lx, ip)
                // don't forget strips start at the bottom of windows
                .addPixliteOutput(new PointsGrouping(Integer.toString(output)) // <- output index on pixlite
                .addPoints(fixture.getPoints())
            );
    }
}
