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
        int bar_index = 1; // burn one for the bar 'form' which has to be part of the model
        int pix_base_host_address = 129;

        int NUM_PIX = 3;
        int OUTPUT_CHANNELS_PER_PIX = 6;
//        SimplePixlite[] pixlites = new SimplePixlite[NUM_PIX];


        SimplePixlite chain;
        SimplePixlite currentPixlite;

        int[] pixlite_channel_mapping = new int[6];
        for (int i = 0; i < pixlite_channel_mapping.length; i++){
            pixlite_channel_mapping[i] = i+1; // default
        }
        // EXAMPLE:  How to swap a pixlite channel in case plugged in wrong
//        pixlite_channel_mapping[5] = 4;
//        pixlite_channel_mapping[4] = 5;
        // ALSO note that for whatever reason the

        currentPixlite = chain = new SimplePixlite(lx, "10.200.1.130");
        for (int i = 1; i <= 5; i ++){
            currentPixlite.addPixliteOutput(new PointsGrouping(Integer.toString(pixlite_channel_mapping[i - 1])) // <- output index on pixlite
                    .addPoints(model.bars.get(bar_index++).getPoints()));
        }
        pix_index++;
//        pixlites[pix_index++] = currentPixlite;

        pixlite_channel_mapping[0] = 2; // default
        pixlite_channel_mapping[1] = 1; // default
        currentPixlite = new SimplePixlite(lx, "10.200.1.129");
        for (int i = 1; i <= 6; i ++){
            currentPixlite.addPixliteOutput(new PointsGrouping(Integer.toString(pixlite_channel_mapping[i - 1])) // <- output index on pixlite
                .addPoints(model.bars.get(bar_index++).getPoints()));
        }
        pix_index++;
//        for (int i = 14; i >= 9; i--){
        for (int i = 11; i <= 16; i++){
            currentPixlite.addPixliteOutput(new PointsGrouping(Integer.toString(i)) // <- output index on pixlite
                .addPoints(model.bars.get(bar_index++).getPoints()));
        }
        pix_index++;
        chain.addChild(currentPixlite);
//        pixlites[pix_index++] = currentPixlite;

        for (int i = 0; i < pixlite_channel_mapping.length; i++){
            pixlite_channel_mapping[i] = i+1; // default
        }
        currentPixlite = new SimplePixlite(lx, "10.200.1.132");
        for (int i = 1; i <= 6; i ++){
            currentPixlite.addPixliteOutput(new PointsGrouping(Integer.toString(pixlite_channel_mapping[i - 1])) // <- output index on pixlite
                .addPoints(model.bars.get(bar_index++).getPoints()));
        }
        pix_index++;
        chain.addChild(currentPixlite);
//        pixlites[pix_index++] = currentPixlite;

        currentPixlite = new SimplePixlite(lx, "10.200.1.128");
        for (int i = 1; i <= 6; i ++){
            currentPixlite.addPixliteOutput(new PointsGrouping(Integer.toString(pixlite_channel_mapping[i - 1])) // <- output index on pixlite
                .addPoints(model.bars.get(bar_index++).getPoints()));
        }
        pix_index++;
        chain.addChild(currentPixlite);

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
