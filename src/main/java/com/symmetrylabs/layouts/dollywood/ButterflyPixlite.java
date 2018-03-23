package com.symmetrylabs.layouts.dollywood;

import heronarts.lx.LX;
import heronarts.lx.output.LXDatagramOutput;
import heronarts.lx.output.LXOutput;
import heronarts.lx.output.LXOutputGroup;
 
public class ButterflyPixlite extends LXOutputGroup {
    public final String ipAddress;

    public ButterflyPixlite(LX lx, String ipAddress) {
        super(lx);
        this.ipAddress = ipAddress;

        for (DollywoodModel.Wing wing : ((DollywoodModel)lx.model).getWings()) {
            System.out.println(wing.id);
        }

        try {
            addChild(new ButterflyPixliteOutput(lx, ipAddress,
                new ButterflyPointsGrouping("1")
                    .addPoints(((DollywoodModel)lx.model).getWingById("butterfly1__lower_right_wing").points)
                    .addPoints(((DollywoodModel)lx.model).getWingById("butterfly1__upper_right_wing").points)
                    .addPoints(((DollywoodModel)lx.model).getWingById("butterfly1__upper_left_wing").points)
                    .addPoints(((DollywoodModel)lx.model).getWingById("butterfly1__lower_left_wing").points)

                    .addPoints(((DollywoodModel)lx.model).getWingById("butterfly2__lower_right_wing").points)
                    .addPoints(((DollywoodModel)lx.model).getWingById("butterfly2__upper_right_wing").points)
                    .addPoints(((DollywoodModel)lx.model).getWingById("butterfly2__upper_left_wing").points)
                    .addPoints(((DollywoodModel)lx.model).getWingById("butterfly2__lower_left_wing").points)

                    .addPoints(((DollywoodModel)lx.model).getWingById("butterfly3__lower_right_wing").points)
                    .addPoints(((DollywoodModel)lx.model).getWingById("butterfly3__upper_right_wing").points)
                    .addPoints(((DollywoodModel)lx.model).getWingById("butterfly3__upper_left_wing").points)
                    .addPoints(((DollywoodModel)lx.model).getWingById("butterfly3__lower_left_wing").points)
            ));

            addChild(new ButterflyPixliteOutput(lx, ipAddress,
                new ButterflyPointsGrouping("2")
                    .addPoints(((DollywoodModel)lx.model).getWingById("butterfly4__left_wing").points)
                    .addPoints(((DollywoodModel)lx.model).getWingById("butterfly4__right_wing").points)
            ));

            // addChild(new ButterflyPixliteOutput(lx, ipAddress,
            //   new ButterflyPointsGrouping("3")
            //     .addPoints(((DollywoodModel)lx.model).getWingById("butterfly5__left_wing").points)
            //     .addPoints(((DollywoodModel)lx.model).getWingById("butterfly5__right_wing").points)
            // ));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}