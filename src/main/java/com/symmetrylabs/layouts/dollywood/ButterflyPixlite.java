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
                new ButterflyPointsGrouping("1").addPoints(((DollywoodModel)lx.model).getWingById("butterfly_1_upper_left_wing").points)
            ));
            addChild(new ButterflyPixliteOutput(lx, ipAddress,
                new ButterflyPointsGrouping("2").addPoints(((DollywoodModel)lx.model).getWingById("butterfly_1_upper_right_wing").points)
            ));
            addChild(new ButterflyPixliteOutput(lx, ipAddress,
                new ButterflyPointsGrouping("3").addPoints(((DollywoodModel)lx.model).getWingById("butterfly_1_lower_left_wing").points)
            ));
            addChild(new ButterflyPixliteOutput(lx, ipAddress,
                new ButterflyPointsGrouping("4").addPoints(((DollywoodModel)lx.model).getWingById("butterfly_1_lower_right_wing").points)
            ));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}