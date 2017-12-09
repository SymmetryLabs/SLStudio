package com.symmetrylabs.mappings;

import com.symmetrylabs.model.Slice;
import com.symmetrylabs.pixlites.Pixlite;
import com.symmetrylabs.pixlites.PixliteOutput;
import com.symmetrylabs.pixlites.PointsGrouping;
import heronarts.lx.LX;

import java.net.SocketException;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */ // 10.200.1.28
public class Sun8FrontBottomPixliteConfig {
    public Sun8FrontBottomPixliteConfig(LX lx, Slice slice, String ipAddress, Pixlite pixlite) throws SocketException {
        // shift everyone but the last one more
        pixlite.addChild(new PixliteOutput(lx, ipAddress,
            new PointsGrouping("9")
                .addPoints(slice.getStripById("46").points, PointsGrouping.Shift.LEFT_TWICE)
                .addPoints(slice.getStripById("47").points, PointsGrouping.REVERSE_ORDERING, PointsGrouping.Shift.RIGHT_TWICE)
                .addPoints(slice.getStripById("48").points, PointsGrouping.Shift.LEFT_TWICE)
        ));

        pixlite.addChild(new PixliteOutput(lx, ipAddress,
            new PointsGrouping("10")
                .addPoints(slice.getStripById("49").points, PointsGrouping.Shift.LEFT_TWICE)
                .addPoints(slice.getStripById("50").points, PointsGrouping.REVERSE_ORDERING, PointsGrouping.Shift.RIGHT)
                .addPoints(slice.getStripById("51").points, PointsGrouping.Shift.LEFT_TWICE)
                .addPoints(slice.getStripById("52").points, PointsGrouping.REVERSE_ORDERING, PointsGrouping.Shift.RIGHT_TWICE)
        ));

        pixlite.addChild(new PixliteOutput(lx, ipAddress,
            new PointsGrouping("5")
                .addPoints(slice.getStripById("53").points, PointsGrouping.Shift.LEFT_TWICE)
                .addPoints(slice.getStripById("54").points, PointsGrouping.REVERSE_ORDERING, PointsGrouping.Shift.RIGHT_TWICE)
                .addPoints(slice.getStripById("55").points, PointsGrouping.Shift.LEFT_TWICE)
                .addPoints(slice.getStripById("56").points, PointsGrouping.REVERSE_ORDERING, PointsGrouping.Shift.RIGHT_TWICE)
        ));

        pixlite.addChild(new PixliteOutput(lx, ipAddress,
            new PointsGrouping("4")
                .addPoints(slice.getStripById("57").points, PointsGrouping.Shift.LEFT_TWICE)
                .addPoints(slice.getStripById("58").points, PointsGrouping.REVERSE_ORDERING, PointsGrouping.Shift.RIGHT_TWICE)
                .addPoints(slice.getStripById("59").points, PointsGrouping.Shift.LEFT)
        ));

        pixlite.addChild(new PixliteOutput(lx, ipAddress,
            new PointsGrouping("3")
                .addPoints(slice.getStripById("60").points, PointsGrouping.Shift.LEFT)
                .addPoints(slice.getStripById("61").points, PointsGrouping.REVERSE_ORDERING, PointsGrouping.Shift.RIGHT)
                .addPoints(slice.getStripById("62").points, PointsGrouping.Shift.LEFT)
        ));

        pixlite.addChild(new PixliteOutput(lx, ipAddress,
            new PointsGrouping("2")
                .addPoints(slice.getStripById("63").points, PointsGrouping.Shift.LEFT)
                .addPoints(slice.getStripById("64").points, PointsGrouping.REVERSE_ORDERING, PointsGrouping.Shift.RIGHT)
                .addPoints(slice.getStripById("65").points, PointsGrouping.Shift.LEFT)
        ));

        pixlite.addChild(new PixliteOutput(lx, ipAddress,
            new PointsGrouping("1")
                .addPoints(slice.getStripById("66").points, PointsGrouping.Shift.LEFT_TWICE)
                .addPoints(slice.getStripById("67").points, PointsGrouping.REVERSE_ORDERING, PointsGrouping.Shift.RIGHT_TWICE)
        ));
    }
}
