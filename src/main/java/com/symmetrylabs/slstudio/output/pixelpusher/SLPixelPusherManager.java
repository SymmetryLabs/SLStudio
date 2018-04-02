package com.symmetrylabs.slstudio.output.pixelpusher;

import java.util.*;

import com.heroicrobot.dropbit.registry.DeviceRegistry;
import com.heroicrobot.dropbit.devices.pixelpusher.Pixel;
import com.heroicrobot.dropbit.devices.pixelpusher.Strip;

import heronarts.lx.LX;
import heronarts.lx.output.LXOutput;
import heronarts.lx.model.LXPoint;

import com.symmetrylabs.slstudio.output.PointsGrouping;

public class SLPixelPusherManager {
    private final LX lx;
    private final DeviceRegistry registry = new DeviceRegistry();

    public SLPixelPusherManager(LX lx) {
        this.lx = lx;
        registry.startPushing();
    }

    public void addDataline(String id, PointsGrouping[] pointsGroupings) {
        lx.addOutput(new PixelPusherDataline(lx, id, pointsGroupings));
    }

    public class PixelPusherDataline extends LXOutput {
        private final String groupId;
        private final PointsGrouping[] pointsGroupings;

        public PixelPusherDataline(LX lx, String groupId, PointsGrouping[] pointsGroupings) {
            super(lx);
            this.groupId = groupId;
            this.pointsGroupings = pointsGroupings;
        }

        @Override
        protected void onSend(int[] colors) {
            List<Strip> strips = registry.getStrips(Integer.parseInt(groupId));

            // TODO: figure out better way to query individual strips (right now we get back an array of strips per controller)
            for (int i = 0; i < pointsGroupings.length; i++) {
                if (i >= strips.size()) {
                    System.out.println("Number of pointsgroupings doesn't match number of strips on the network");
                } else {
                    Strip strip = strips.get(i);
                    int pi = 0;
                    for (LXPoint point : pointsGroupings[i].getPoints()) {
                        strip.setPixel(colors[point.index], pi++);
                    }
                }
            }
        }
    }
}