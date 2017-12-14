package com.symmetrylabs.slstudio.pixlites;

import com.symmetrylabs.slstudio.mappings.Sun10BackBottomPixliteConfig;
import com.symmetrylabs.slstudio.mappings.Sun10BackTopPixliteConfig;
import com.symmetrylabs.slstudio.mappings.Sun10FrontBottomPixliteConfig;
import com.symmetrylabs.slstudio.mappings.Sun10FrontTopPixliteConfig;
import com.symmetrylabs.slstudio.mappings.Sun1BackPixliteConfig;
import com.symmetrylabs.slstudio.mappings.Sun1FrontPixliteConfig;
import com.symmetrylabs.slstudio.mappings.Sun2BackPixliteConfig;
import com.symmetrylabs.slstudio.mappings.Sun2FrontPixliteConfig;
import com.symmetrylabs.slstudio.mappings.Sun3BackTopPixliteConfig;
import com.symmetrylabs.slstudio.mappings.Sun3FrontTopPixliteConfig;
import com.symmetrylabs.slstudio.mappings.Sun4BackPixliteConfig;
import com.symmetrylabs.slstudio.mappings.Sun4FrontPixliteConfig;
import com.symmetrylabs.slstudio.mappings.Sun5BackTopPixliteConfig;
import com.symmetrylabs.slstudio.mappings.Sun5FrontTopPixliteConfig;
import com.symmetrylabs.slstudio.mappings.Sun6BackBottomPixliteConfig;
import com.symmetrylabs.slstudio.mappings.Sun6BackTopPixliteConfig;
import com.symmetrylabs.slstudio.mappings.Sun6FrontBottomPixliteConfig;
import com.symmetrylabs.slstudio.mappings.Sun6FrontTopPixliteConfig;
import com.symmetrylabs.slstudio.mappings.Sun7BackBottomPixliteConfig;
import com.symmetrylabs.slstudio.mappings.Sun7BackTopPixliteConfig;
import com.symmetrylabs.slstudio.mappings.Sun7FrontBottomPixliteConfig;
import com.symmetrylabs.slstudio.mappings.Sun7FrontTopPixliteConfig;
import com.symmetrylabs.slstudio.mappings.Sun8BackBottomPixliteConfig;
import com.symmetrylabs.slstudio.mappings.Sun8BackTopPixliteConfig;
import com.symmetrylabs.slstudio.mappings.Sun8FrontBottomPixliteConfig;
import com.symmetrylabs.slstudio.mappings.Sun8FrontTopPixliteConfig;
import com.symmetrylabs.slstudio.mappings.Sun9BackBottomPixliteConfig;
import com.symmetrylabs.slstudio.mappings.Sun9BackTopPixliteConfig;
import com.symmetrylabs.slstudio.mappings.Sun9FrontBottomPixliteConfig;
import com.symmetrylabs.slstudio.mappings.Sun9FrontTopPixliteConfig;
import com.symmetrylabs.slstudio.model.Slice;
import heronarts.lx.LX;
import heronarts.lx.output.LXDatagramOutput;
import heronarts.lx.output.LXOutputGroup;


public class Pixlite extends LXOutputGroup {
    private final int MAX_NUM_POINTS_PER_UNIVERSE = 170;

    public Slice slice;
    public final String ipAddress;
    private LXDatagramOutput datagramOutput;

    public Pixlite(LX lx, String ipAddress, Slice slice) {
        super(lx);
        this.ipAddress = ipAddress;
        this.slice = slice;
        try {
            this.datagramOutput = new LXDatagramOutput(lx);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        addChild(this.datagramOutput);

        if (slice == null) {
            IllegalArgumentException e = new IllegalArgumentException("slice is null for " + ipAddress);
            e.printStackTrace();
            throw new IllegalArgumentException("slice is null for " + ipAddress);
        }
        if (slice.id == null) throw new IllegalArgumentException("slice.id is null for " + ipAddress);

        // com.symmetrylabs.slstudio.model.Sun 1
        if (slice.id.equals("sun1_top_front")) {
            new Sun1FrontPixliteConfig(lx, slice, this);
        }
        if (slice.id.equals("sun1_top_back")) {
            new Sun1BackPixliteConfig(lx, slice, this);
        }

        // com.symmetrylabs.slstudio.model.Sun 2
        if (slice.id.equals("sun2_top_front")) {
            new Sun2FrontPixliteConfig(lx, slice, this);
        }
        if (slice.id.equals("sun2_top_back")) {
            new Sun2BackPixliteConfig(lx, slice, this);
        }

        // com.symmetrylabs.slstudio.model.Sun 3
        if (slice.id.equals("sun3_top_front")) {
            new Sun3FrontTopPixliteConfig(lx, slice, this);
        }
        if (slice.id.equals("sun3_top_back")) {
            new Sun3BackTopPixliteConfig(lx, slice, this);
        }

        // com.symmetrylabs.slstudio.model.Sun 4
        if (slice.id.equals("sun4_top_front")) {
            new Sun4FrontPixliteConfig(lx, slice, this);
        }
        if (slice.id.equals("sun4_top_back")) {
            new Sun4BackPixliteConfig(lx, slice, this);
        }

        // com.symmetrylabs.slstudio.model.Sun 5
        if (slice.id.equals("sun5_top_front")) {
            new Sun5FrontTopPixliteConfig(lx, slice, this);
        }
        if (slice.id.equals("sun5_top_back")) {
            new Sun5BackTopPixliteConfig(lx, slice, this);
        }

        // com.symmetrylabs.slstudio.model.Sun 6
        if (slice.id.equals("sun6_top_front")) {
            new Sun6FrontTopPixliteConfig(lx, slice, this);
        }
        if (slice.id.equals("sun6_bottom_front")) {
            new Sun6FrontBottomPixliteConfig(lx, slice, this);
        }
        if (slice.id.equals("sun6_top_back")) {
            new Sun6BackTopPixliteConfig(lx, slice, this);
        }
        if (slice.id.equals("sun6_bottom_back")) {
            new Sun6BackBottomPixliteConfig(lx, slice, this);
        }

        // com.symmetrylabs.slstudio.model.Sun 7
        if (slice.id.equals("sun7_top_front")) {
            new Sun7FrontTopPixliteConfig(lx, slice, this);
        }
        if (slice.id.equals("sun7_bottom_front")) {
            new Sun7FrontBottomPixliteConfig(lx, slice, this);
        }
        if (slice.id.equals("sun7_top_back")) {
            new Sun7BackTopPixliteConfig(lx, slice, this);
        }
        if (slice.id.equals("sun7_bottom_back")) {
            new Sun7BackBottomPixliteConfig(lx, slice, this);
        }

        // com.symmetrylabs.slstudio.model.Sun 8
        if (slice.id.equals("sun8_top_front")) {
            new Sun8FrontTopPixliteConfig(lx, slice, this);
        }
        if (slice.id.equals("sun8_bottom_front")) {
            new Sun8FrontBottomPixliteConfig(lx, slice, this);
        }
        if (slice.id.equals("sun8_top_back")) {
            new Sun8BackTopPixliteConfig(lx, slice, this);
        }
        if (slice.id.equals("sun8_bottom_back")) {
            new Sun8BackBottomPixliteConfig(lx, slice, this);
        }

        // com.symmetrylabs.slstudio.model.Sun 9
        if (slice.id.equals("sun9_top_front")) {
            new Sun9FrontTopPixliteConfig(lx, slice, this);
        }
        if (slice.id.equals("sun9_bottom_front")) {
            new Sun9FrontBottomPixliteConfig(lx, slice, this);
        }
        if (slice.id.equals("sun9_top_back")) {
            new Sun9BackTopPixliteConfig(lx, slice, this);
        }
        if (slice.id.equals("sun9_bottom_back")) {
            new Sun9BackBottomPixliteConfig(lx, slice, this);
        }

        // com.symmetrylabs.slstudio.model.Sun 10
        if (slice.id.equals("sun10_top_front")) {
            new Sun10FrontTopPixliteConfig(lx, slice, this);
        }
        if (slice.id.equals("sun10_bottom_front")) {
            new Sun10FrontBottomPixliteConfig(lx, slice, this);
        }
        if (slice.id.equals("sun10_top_back")) {
            new Sun10BackTopPixliteConfig(lx, slice, this);
        }
        if (slice.id.equals("sun10_bottom_back")) {
            new Sun10BackBottomPixliteConfig(lx, slice, this);
        }

        // com.symmetrylabs.slstudio.model.Sun 11
        if(slice.id.equals("sun11_top_front")) {
            new com.symmetrylabs.slstudio.mappings.Sun11FrontTopPixliteConfig(lx, slice, this);
        }
        if(slice.id.equals("sun11_bottom_front")) {
            new com.symmetrylabs.slstudio.mappings.Sun11FrontBottomPixliteConfig(lx, slice, this);
        }
        if(slice.id.equals("sun11_top_back")) {
            new com.symmetrylabs.slstudio.mappings.Sun11BackTopPixliteConfig(lx, slice, this);
        }
        if(slice.id.equals("sun11_bottom_back")) {
            new com.symmetrylabs.slstudio.mappings.Sun11BackBottomPixliteConfig(lx, slice, this);
        }
    }

    public void addPointsGroup(PointsGrouping pointsGrouping) {
        int outputIndex = Integer.parseInt(pointsGrouping.id);
        int firstUniverseOnOutput = outputIndex * 10;

        // the points for one pixlite output have to be spread across multiple universes
        int numPoints = pointsGrouping.size();
        int numUniverses = (numPoints / MAX_NUM_POINTS_PER_UNIVERSE) + 1;
        int counter = 0;

        for (int i = 0; i < numUniverses; i++) {
            int universe = firstUniverseOnOutput + i;
            int numIndices = ((i + 1) * MAX_NUM_POINTS_PER_UNIVERSE) > numPoints
                ? (numPoints % MAX_NUM_POINTS_PER_UNIVERSE)
                : MAX_NUM_POINTS_PER_UNIVERSE;
            int[] indices = new int[numIndices];
            for (int i1 = 0; i1 < numIndices; i1++) {
                indices[i1] = pointsGrouping.getPoint(counter++).index;
            }
            datagramOutput.addDatagram(new ArtNetDatagram(ipAddress, indices, universe - 1));
        }
    }
}
