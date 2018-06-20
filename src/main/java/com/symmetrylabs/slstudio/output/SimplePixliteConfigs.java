package com.symmetrylabs.slstudio.output;

import heronarts.lx.LX;
import heronarts.lx.model.LXFixture;

public class SimplePixliteConfigs {

    public static SimplePixlite[] setupPixlites(LX lx) {
//    NissanModel model = ((NissanModel)lx.model);

        /**
         * EXAMPLE
         * EXAMPLE
         * EXAMPLE
         * EXAMPLE
         */
        return new SimplePixlite[] {

            // Pixlite for 9 panels

            new SimplePixlite(lx, "10.200.1.127")
                // don't forget strips start at the bottom of windows
                .addPixliteOutput(new PointsGrouping("1") // <- output index on pixlite
                .addPoints(lx.model.getPoints())
            ),

//            new SimplePixlite(lx, "10.200.1.153")
//                // don't forget strips start at the bottom of windows
//                .addPixliteOutput(new PointsGrouping("1") // <- output index on pixlite
//                .addPoints(lx.model.getPoints())
//            ),

        };



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
