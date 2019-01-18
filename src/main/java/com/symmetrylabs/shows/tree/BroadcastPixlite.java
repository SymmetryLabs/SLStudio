package com.symmetrylabs.shows.tree;

import heronarts.lx.LX;
import com.symmetrylabs.slstudio.output.PointsGrouping;
import com.symmetrylabs.slstudio.output.SimplePixlite;


public class BroadcastPixlite extends SimplePixlite {

    private static BroadcastPixlite instance = null;

    private final int NUM_POINTS_PER_DATALINE = 1020;

    public BroadcastPixlite(LX lx) {
        super(lx, "10.200.1.255"); // TODO remove hardcoded ip address

        PointsGrouping points = new PointsGrouping();
        for (int i = 0; i < NUM_POINTS_PER_DATALINE; i++) {
            points.addPoint(lx.model.points[i]);
        }

        addPixliteOutput("1", points);
        addPixliteOutput("2", points);
        addPixliteOutput("3", points);
        addPixliteOutput("4", points);
        addPixliteOutput("5", points);
        addPixliteOutput("6", points);
        addPixliteOutput("7", points);
        addPixliteOutput("8", points);
        addPixliteOutput("9", points);
        addPixliteOutput("10", points);
        addPixliteOutput("11", points);
        addPixliteOutput("12", points);
        addPixliteOutput("13", points);
        addPixliteOutput("14", points);
        addPixliteOutput("15", points);
        addPixliteOutput("16", points);
    }

    public static BroadcastPixlite getInstance(LX lx) {
        if (instance == null)
            instance = new BroadcastPixlite(lx);

        return instance;
    }

    public static BroadcastPixlite getInstance() {
        if (instance != null) {
            return instance;
        }
        return null;
    }
}
