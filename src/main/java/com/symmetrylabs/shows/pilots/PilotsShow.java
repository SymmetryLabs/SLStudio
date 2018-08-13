package com.symmetrylabs.shows.pilots;

import com.symmetrylabs.shows.Show;
import com.symmetrylabs.slstudio.SLStudioLX;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.model.Strip;
import com.symmetrylabs.slstudio.model.StripsModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.transform.LXVector;

import java.util.ArrayList;
import java.util.List;


/**
 * This file implements the mapping functions needed to lay out the cubes.
 */
public class PilotsShow implements Show {
    static final String SHOW_NAME = "pilots";

    private static final float STRIP_LENGTH = 29;
    private static final float CART_SPACING = 2;
    private static final int N_PIXELS = 29;
    private static final float PIXEL_PITCH = STRIP_LENGTH / (N_PIXELS + 1f);

    /**
     * Each cart is a grid structure that looks as if it were made up of 4
     * cubes along X and Y and 2 cubes along Z.
     */
    private static class Cart {
        private String id;
        private LXVector baseLoc;

        Cart(String id, LXVector baseLoc) {
            this.id = id;
            this.baseLoc = baseLoc;
        }

        private LXVector start(int x, int y, int z) {
            return new LXVector(x * STRIP_LENGTH, y * STRIP_LENGTH, z * STRIP_LENGTH).add(baseLoc);
        }

        private void addXStrip(List<Strip> strips, int x, int y, int z) {
            LXVector start = start(x, y, z);
            List<LXPoint> points = new ArrayList<>();
            for (int i = 1; i <= N_PIXELS; i++) {
                points.add(new LXPoint(start.x + PIXEL_PITCH * i, start.y, start.z));
            }
            Strip.Metrics metrics = new Strip.Metrics(N_PIXELS, PIXEL_PITCH);
            strips.add(new Strip(String.format("%s-%d%d%dX", id, x, y, z), metrics, points));
        }

        private void addYStrip(List<Strip> strips, int x, int y, int z) {
            LXVector start = start(x, y, z);
            List<LXPoint> points = new ArrayList<>();
            for (int i = 1; i <= N_PIXELS; i++) {
                points.add(new LXPoint(start.x, start.y + PIXEL_PITCH * i, start.z));
            }
            Strip.Metrics metrics = new Strip.Metrics(N_PIXELS, PIXEL_PITCH);
            strips.add(new Strip(String.format("%s-%d%d%dY", id, x, y, z), metrics, points));
        }

        private void addZStrip(List<Strip> strips, int x, int y, int z) {
            LXVector start = start(x, y, z);
            List<LXPoint> points = new ArrayList<>();
            for (int i = 1; i <= N_PIXELS; i++) {
                points.add(new LXPoint(start.x, start.y, start.z + PIXEL_PITCH * i));
            }
            Strip.Metrics metrics = new Strip.Metrics(N_PIXELS, PIXEL_PITCH);
            strips.add(new Strip(String.format("%s-%d%d%dZ", id, x, y, z), metrics, points));
        }

        public List<Strip> getStrips() {
            List<Strip> strips = new ArrayList<>();
            for (int x = 0; x < 5; x++) {
                for (int y = 0; y < 5; y++) {
                    for (int z = 0; z < 3; z++) {
                        if (x != 4) addXStrip(strips, x, y, z);
                        if (y != 4) addYStrip(strips, x, y, z);
                        if (z != 2) addZStrip(strips, x, y, z);
                    }
                }
            }
            return strips;
        }
    }

    private Cart[] carts = new Cart[] {
        /* no extra cart spacing here because they're not actually adjacent;
         * FSL is in front of BSL. */
        new Cart("FSL",  new LXVector(
            24 * STRIP_LENGTH + 4 * CART_SPACING, 0, 0)),

        new Cart("BSL",  new LXVector(
            20 * STRIP_LENGTH + 4 * CART_SPACING, 0, 3 * STRIP_LENGTH)),

        new Cart("BSCL", new LXVector(
            16 * STRIP_LENGTH + 3 * CART_SPACING, 0, 3 * STRIP_LENGTH)),

        new Cart("BSC",  new LXVector(
            12 * STRIP_LENGTH + 2 * CART_SPACING, 0, 3 * STRIP_LENGTH)),

        new Cart("BSCR", new LXVector(
            8 * STRIP_LENGTH + CART_SPACING, 0, 3 * STRIP_LENGTH)),

        new Cart("BSR",  new LXVector(
            4 * STRIP_LENGTH, 0, 3 * STRIP_LENGTH)),

        new Cart("FSR",  new LXVector(
            0, 0, 0)),
    };

    @Override
    public SLModel buildModel() {
        List<Strip> strips = new ArrayList<>();
        for (Cart cart : carts) {
            strips.addAll(cart.getStrips());
        }
        return new StripsModel<>(strips);
    }

    @Override
    public void setupLx(SLStudioLX lx) {
    }

    @Override
    public void setupUi(SLStudioLX lx, SLStudioLX.UI ui) {
    }
}
