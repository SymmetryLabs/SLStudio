package com.symmetrylabs.slstudio.pixlites;

import heronarts.lx.LX;
import com.symmetrylabs.slstudio.pixlites.NissanPixlite;
import com.symmetrylabs.slstudio.pixlites.PointsGrouping;
import com.symmetrylabs.slstudio.model.NissanModel;

public class NissanPixliteConfigs {
    
    public static NissanPixlite[] setupPixlites(LX lx) {
        NissanModel model = ((NissanModel)lx.model);

        /**
         * EXAMPLE
         * EXAMPLE
         * EXAMPLE
         * EXAMPLE
         */
        return new NissanPixlite[] {

            new NissanPixlite(lx, "10.200.1.10")
                // don't forget strips start at the bottom of windows
                .addPixliteOutput(new PointsGrouping("1") // <- output index on pixlite
                    .addPoints(model.getStripById("car1-driver-side-front-strip1").getPoints(), PointsGrouping.REVERSE_ORDERING)
                    .addPoints(model.getStripById("car1-driver-side-front-strip2").getPoints())
                    .addPoints(model.getStripById("car1-driver-side-front-strip3").getPoints(), PointsGrouping.REVERSE_ORDERING)
                    .addPoints(model.getStripById("car1-driver-side-front-strip4").getPoints())
                ),


        };
    }
}