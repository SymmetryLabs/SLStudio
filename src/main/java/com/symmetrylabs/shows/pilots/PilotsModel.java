package com.symmetrylabs.shows.pilots;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.transform.LXVector;
import com.symmetrylabs.slstudio.model.Strip;
import com.symmetrylabs.slstudio.model.StripsModel;
import com.symmetrylabs.slstudio.model.SLModel;


public class PilotsModel extends StripsModel<Strip> {

    public static final float STRIP_LENGTH = 29;
    public static final int N_PIXELS = 29;
    public static final float CART_SPACING = 2;
    public static final float PIXEL_PITCH = STRIP_LENGTH / (N_PIXELS + 1f);

    public List<Cart> carts = new ArrayList<>();
    public Map<String, Cart> cartMap = new HashMap<>();

    public PilotsModel(List<Strip> strips, List<Cart> carts) {
        super(strips);
        this.carts = carts;
        for (Cart cart : carts) {
            cartMap.put(cart.id, cart);
        }
    }

    public Cart getCartById(String id) {
        return cartMap.get(id);
    }

    /**
     * Each cart is a grid structure that looks as if it were made up of 4
     * cubes along X and Y and 2 cubes along Z.
     */
    public static class Cart {
        public final String id;
        public final List<Strip> strips = new ArrayList<>();

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

        public List<Strip> createStrips() {
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

        public Strip getStrip(int i) {
            return strips.get(i);
        }
    }
}