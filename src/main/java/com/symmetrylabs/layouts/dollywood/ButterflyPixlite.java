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

        try {
            addChild(new ButterflyPixliteOutput(lx, ipAddress,
                new ButterflyPointsGrouping("1")
                    .addPoints(((DollywoodModel)lx.model).getWingById("butterfly_1_upper_left_wing").points)
                    .addPoints(((DollywoodModel)lx.model).getWingById("butterfly_1_upper_right_wing").points)
                    .addPoints(((DollywoodModel)lx.model).getWingById("butterfly_1_lower_left_wing").points)
                    .addPoints(((DollywoodModel)lx.model).getWingById("butterfly_1_lower_right_wing").points)

                    .addPoints(((DollywoodModel)lx.model).getWingById("butterfly_2_upper_left_wing").points)
                    .addPoints(((DollywoodModel)lx.model).getWingById("butterfly_2_upper_right_wing").points)
                    .addPoints(((DollywoodModel)lx.model).getWingById("butterfly_2_lower_left_wing").points)
                    .addPoints(((DollywoodModel)lx.model).getWingById("butterfly_2_lower_right_wing").points)

                    .addPoints(((DollywoodModel)lx.model).getWingById("butterfly_3_upper_left_wing").points)
                    .addPoints(((DollywoodModel)lx.model).getWingById("butterfly_3_upper_right_wing").points)
                    .addPoints(((DollywoodModel)lx.model).getWingById("butterfly_3_lower_left_wing").points)
                    .addPoints(((DollywoodModel)lx.model).getWingById("butterfly_3_lower_right_wing").points)
            ));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}