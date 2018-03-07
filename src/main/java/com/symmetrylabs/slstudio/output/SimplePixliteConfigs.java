package com.symmetrylabs.slstudio.output;

import com.symmetrylabs.slstudio.model.CandyBar;
import com.symmetrylabs.slstudio.model.CubesModel;
import heronarts.lx.LX;
import heronarts.lx.model.LXFixture;

public class SimplePixliteConfigs {

    public static SimplePixlite setupPixlites(LX lx) {
//    NissanModel model = ((NissanModel)lx.model);
        CubesModel model = ((CubesModel) lx.model);

        /**
         * EXAMPLE
         * EXAMPLE
         * EXAMPLE
         * EXAMPLE
         */
        int pix_index = 0;
        int pix_base_host_address = 129;

        int NUM_PIX = 3;
        int OUTPUT_CHANNELS_PER_PIX = 6;
//        SimplePixlite[] pixlites = new SimplePixlite[NUM_PIX];

        SimplePixlite chain;
        SimplePixlite currentPixlite;

        currentPixlite = chain = new SimplePixlite(lx, "10.200.1.130");
        for (int i = pix_index*OUTPUT_CHANNELS_PER_PIX; i < (pix_index + 1)*OUTPUT_CHANNELS_PER_PIX; i ++){
            currentPixlite.addPixliteOutput(new PointsGrouping(Integer.toString(i%6)) // <- output index on pixlite
                    .addPoints(model.bars.get(i).getPoints()));
        }
        pix_index++;
//        pixlites[pix_index++] = currentPixlite;

        currentPixlite = new SimplePixlite(lx, "10.200.1.129");
        for (int i = pix_index*OUTPUT_CHANNELS_PER_PIX; i < (pix_index + 1)*OUTPUT_CHANNELS_PER_PIX; i ++){
            currentPixlite.addPixliteOutput(new PointsGrouping(Integer.toString(i%6)) // <- output index on pixlite
                .addPoints(model.bars.get(i).getPoints()));
        }
        pix_index++;
        chain.addChild(currentPixlite);
//        pixlites[pix_index++] = currentPixlite;

        currentPixlite = new SimplePixlite(lx, "10.200.1.131");
        for (int i = pix_index*OUTPUT_CHANNELS_PER_PIX; i < (pix_index + 1)*OUTPUT_CHANNELS_PER_PIX; i ++){
            currentPixlite.addPixliteOutput(new PointsGrouping(Integer.toString(i%6)) // <- output index on pixlite
                .addPoints(model.bars.get(i).getPoints()));
        }
        pix_index++;
        chain.addChild(currentPixlite);
//        pixlites[pix_index++] = currentPixlite;

        return chain;
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
