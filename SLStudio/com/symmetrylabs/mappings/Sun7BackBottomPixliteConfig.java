package com.symmetrylabs.mappings;

import com.symmetrylabs.model.Slice;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class Sun7BackBottomPixliteConfig {
    public Sun7BackBottomPixliteConfig(LX lx, Slice slice, String ipAddress, Pixlite pixlite) throws SocketException {

        pixlite.addChild(new PixliteOutput(lx, ipAddress,
            new PointsGrouping("9")
                .addPoints(slice.getStripById("46").points)
                .addPoints(slice.getStripById("47").points, PointsGrouping.REVERSE_ORDERING)
                .addPoints(slice.getStripById("48").points)
        ));

        pixlite.addChild(new PixliteOutput(lx, ipAddress,
            new PointsGrouping("10")
                .addPoints(slice.getStripById("49").points)
                .addPoints(slice.getStripById("50").points, PointsGrouping.REVERSE_ORDERING)
                .addPoints(slice.getStripById("51").points, PointsGrouping.Shift.LEFT)
                .addPoints(slice.getStripById("52").points, PointsGrouping.REVERSE_ORDERING, PointsGrouping.Shift.LEFT)
        ));

        pixlite.addChild(new PixliteOutput(lx, ipAddress,
            new PointsGrouping("5")
                .addPoints(slice.getStripById("53").points, PointsGrouping.Shift.RIGHT)
                .addPoints(slice.getStripById("54").points, PointsGrouping.REVERSE_ORDERING)
                .addPoints(slice.getStripById("55").points)
                .addPoints(slice.getStripById("56").points, PointsGrouping.REVERSE_ORDERING)
        ));

        pixlite.addChild(new PixliteOutput(lx, ipAddress,
            new PointsGrouping("4")
                .addPoints(slice.getStripById("57").points)
                .addPoints(slice.getStripById("58").points, PointsGrouping.REVERSE_ORDERING)
                .addPoints(slice.getStripById("59").points)
        ));

        pixlite.addChild(new PixliteOutput(lx, ipAddress,
            new PointsGrouping("3")
                .addPoints(slice.getStripById("60").points)
                .addPoints(slice.getStripById("61").points, PointsGrouping.REVERSE_ORDERING)
                .addPoints(slice.getStripById("62").points)
        ));

        pixlite.addChild(new PixliteOutput(lx, ipAddress,
            new PointsGrouping("2")
                .addPoints(slice.getStripById("63").points)
                .addPoints(slice.getStripById("64").points, PointsGrouping.REVERSE_ORDERING, PointsGrouping.Shift.RIGHT)
                .addPoints(slice.getStripById("65").points)
        ));

        pixlite.addChild(new PixliteOutput(lx, ipAddress,
            new PointsGrouping("1")
                .addPoints(slice.getStripById("66").points)
                .addPoints(slice.getStripById("67").points, PointsGrouping.REVERSE_ORDERING)
        ));
    }
}
