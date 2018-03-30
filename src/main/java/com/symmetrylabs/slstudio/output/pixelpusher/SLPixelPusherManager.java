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
    //private final Observer observer = new Observer();

    public SLPixelPusherManager(LX lx) {
        this.lx = lx;
        //registry.addObserver(observer);
        registry.startPushing();
    }

    public void addDataline(PointsGrouping pointsGrouping) {
        lx.addOutput(new PixelPusherDataline(lx, pointsGrouping));
    }

    // // Pixel Pushers 'datalines' are referred to as 'strips'
    // public List<Strip> getStrip(String id) {
    //   return registry.getStrips(id);
    // }

    // private static class Observer implements Observer {
    //   public void update(Observable registry, Object updatedDevice) {
    //     if (updatedDevice != null) {
    //       println("Device change: " + updatedDevice);
    //     }
    //   };
    // }

    public class PixelPusherDataline extends LXOutput {
        private final PointsGrouping pointsGrouping;

        public PixelPusherDataline(LX lx, PointsGrouping pointsGrouping) {
            super(lx);
            this.pointsGrouping = pointsGrouping;
        }

        @Override
        protected void onSend(int[] colors) {
            Strip strip = registry.getStrips(Integer.parseInt(pointsGrouping.id)).get(0);

            if (strip != null) {
                int i = 0;
                for (LXPoint point : pointsGrouping.getPoints()) {
                    strip.setPixel(colors[point.index], i++);
                }
            } else {
                System.out.println("PixelPusher Strip (" + pointsGrouping.id + ") is not currently on the network");
            }
        }
    }
}