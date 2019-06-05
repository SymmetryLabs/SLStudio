package com.symmetrylabs.shows.pilots;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import com.symmetrylabs.shows.cubes.CubesModel;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.model.LXAbstractFixture;
import heronarts.lx.transform.LXTransform;
import heronarts.lx.transform.LXVector;
import com.symmetrylabs.slstudio.model.Strip;
import com.symmetrylabs.slstudio.model.StripsModel;
import com.symmetrylabs.slstudio.model.SLModel;

import static com.symmetrylabs.util.MathConstants.*;

public class PilotsModel extends StripsModel<Strip> {

    public static final float STRIP_LENGTH = 29;
    public static final float CART_X = STRIP_LENGTH*4;
    public static final float CART_Y = STRIP_LENGTH*4;
    public static final float CART_Z = STRIP_LENGTH*2;



    public static final int N_PIXELS = 29;
    public static final float CART_SPACING = 2;
    public static final float PIXEL_PITCH = STRIP_LENGTH / (N_PIXELS + 1f);

    public List<Cart> carts = new ArrayList<>();
    public Map<String, Cart> cartMap = new HashMap<>();

    public PilotsModel(List<Strip> strips, List<Cart> carts) {
        super(PilotsShow.SHOW_NAME, strips);
        this.carts = carts;
        for (Cart cart : carts) {
            cartMap.put(cart.modelId, cart);
        }
    }

    private static class Fixture extends LXAbstractFixture {
        private Fixture(List<Cart> carts) {
            for (Cart cart : carts) {
                for (LXPoint p : cart.points) {
                    this.points.add(p);
                }
            }
        }
    }

    public Cart getCartById(String id) {
        return cartMap.get(id);
    }

    /**
     * Each cart is a grid structure.
     * They are built in sub-components that model the physical wiring topology
     *     in order to make that mapping easier.
     */
    public static class Cart extends LXModel {
        private Map<String, Dataline> datalineMap = new HashMap<>();
        public final List<Dataline> datalines = new ArrayList<>();
        public final List<Strip> strips = new ArrayList<>();

        Cart(String id, LXVector position) {
            super(id, new Fixture(position));

            Fixture fixture = (Cart.Fixture) this.fixtures.get(0);
            datalines.addAll(fixture.datalines);
            strips.addAll(fixture.strips);

            for (Dataline dataline : datalines) {
                datalineMap.put(dataline.channel, dataline);
            }
        }

        public Dataline getDatalineByChannel(String index) {
            return datalineMap.get(index);
        }

        private static class Fixture extends LXAbstractFixture {
            public final List<Dataline> datalines = new ArrayList<>();
            public final List<Strip> strips = new ArrayList<>();

            private Fixture(LXVector position) {
                LXTransform t = new LXTransform();
                t.translate(position.x, position.y, position.z);

                addDataline(new VerticalDataline("1",
                    new LXVector(STRIP_LENGTH*0, 0, 0), t));

                addDataline(new VerticalDataline("2",
                    new LXVector(STRIP_LENGTH*1, 0, 0), t));

                addDataline(new VerticalDataline("3",
                    new LXVector(STRIP_LENGTH*2, 0, 0), t));

                addDataline(new VerticalDataline("4",
                    new LXVector(STRIP_LENGTH*3, 0, 0), t));

                addDataline(new VerticalDataline("5",
                    new LXVector(STRIP_LENGTH*4, 0, 0), t));

                addDataline(new HorizontalDataline("6", HorizontalDataline.Type.A,
                    new LXVector(0, STRIP_LENGTH*0, 0), t));

                addDataline(new HorizontalDataline("7", HorizontalDataline.Type.B,
                    new LXVector(0, STRIP_LENGTH*0, 0), t));

                addDataline(new HorizontalDataline("8", HorizontalDataline.Type.A,
                    new LXVector(0, STRIP_LENGTH*1, 0), t));

                addDataline(new HorizontalDataline("9", HorizontalDataline.Type.B,
                    new LXVector(0, STRIP_LENGTH*1, 0), t));

                addDataline(new HorizontalDataline("10", HorizontalDataline.Type.A,
                    new LXVector(0, STRIP_LENGTH*2, 0), t));

                addDataline(new HorizontalDataline("11", HorizontalDataline.Type.B,
                    new LXVector(0, STRIP_LENGTH*2, 0), t));

                addDataline(new HorizontalDataline("12", HorizontalDataline.Type.A,
                    new LXVector(0, STRIP_LENGTH*3, 0), t));

                addDataline(new HorizontalDataline("13", HorizontalDataline.Type.B,
                    new LXVector(0, STRIP_LENGTH*3, 0), t));

                addDataline(new HorizontalDataline("14", HorizontalDataline.Type.A,
                    new LXVector(0, STRIP_LENGTH*4, 0), t));

                addDataline(new HorizontalDataline("15", HorizontalDataline.Type.B,
                    new LXVector(0, STRIP_LENGTH*4, 0), t));
            }

            private void addDataline(Dataline dataline) {
                datalines.add(dataline);
                strips.addAll(dataline.strips);
                for (Strip strip : strips) {
                    this.points.addAll(strip.getPoints());
                }
            }
        }

        public static class VerticalDataline extends Dataline {
            public VerticalDataline(String channel, LXVector position, LXTransform t) {
                super(channel);

                // We just walk down the physical wiring and create strips (in the correct orientation)
                t.push();
                t.translate(position.x, position.y, position.z);

                // back vertical
                t.push();
                t.rotateZ(HALF_PI);
                addStrip(t);
                t.translate(STRIP_LENGTH, 0, 0);
                addStrip(t);
                t.translate(STRIP_LENGTH, 0, 0);
                addStrip(t);
                t.translate(STRIP_LENGTH, 0, 0);
                addStrip(t);
                t.pop();

                // middle vertical
                t.push();
                t.translate(0, STRIP_LENGTH*4, -STRIP_LENGTH);
                t.rotateZ(-HALF_PI);
                addStrip( t);
                t.translate(STRIP_LENGTH, 0, 0);
                addStrip(t);
                t.translate(STRIP_LENGTH, 0, 0);
                addStrip(t);
                t.translate(STRIP_LENGTH, 0, 0);
                addStrip( t);
                t.pop();

                // front vertical
                t.push();
                t.translate(0, 0, -STRIP_LENGTH*2);
                t.rotateZ(HALF_PI);
                addStrip(t);
                t.translate(STRIP_LENGTH, 0, 0);
                addStrip(t);
                t.translate(STRIP_LENGTH, 0, 0);
                addStrip(t);
                t.translate(STRIP_LENGTH, 0, 0);
                addStrip(t);
                t.pop();

                t.pop();
            }
        }

        public static class HorizontalDataline extends Dataline {
            public enum Type {
                A, B
            }

            public HorizontalDataline(String channel, Type type, LXVector position, LXTransform t) {
                super(channel);

                // We just walk down the physical wiring and create strips
                t.push();
                t.translate(position.x, position.y, position.z);

                // B is the same just mirrored on x & z axes
                if (type == Type.B) {
                    t.translate(STRIP_LENGTH*4, 0, -STRIP_LENGTH*2); // corrects for rotation
                    t.rotateZ(PI);
                    t.rotateX(PI);
                }

                t.translate(STRIP_LENGTH*2, 0, -STRIP_LENGTH);
                t.rotateY(-HALF_PI);
                addStrip(t);
                t.translate(STRIP_LENGTH, 0, 0);
                t.rotateY(-HALF_PI);
                addStrip(t);
                t.translate(STRIP_LENGTH, 0, 0);
                addStrip(t);
                t.translate(STRIP_LENGTH, 0, 0);
                t.rotateY(-HALF_PI);
                addStrip(t);
                t.translate(STRIP_LENGTH, 0, 0);
                addStrip(t);
                t.translate(STRIP_LENGTH, 0, 0);
                t.rotateY(-HALF_PI);
                addStrip(t);
                t.translate(STRIP_LENGTH, 0, 0);
                addStrip(t);
                t.rotateY(-HALF_PI);
                addStrip(t);
                t.translate(STRIP_LENGTH, 0, 0);
                addStrip(t);
                t.rotateY(-HALF_PI);
                t.translate(STRIP_LENGTH, 0, 0);
                t.rotateY(PI);
                addStrip(t);
                t.translate(STRIP_LENGTH, 0, 0);
                addStrip(t);

                t.pop();
            }
        }

        public abstract static class Dataline {
            public final String channel;
            public final List<Strip> strips = new ArrayList<>();
            private int numStrips = 0;

            public Dataline(String channel) {
                this.channel = channel;
            }

            public void addStrip(LXTransform t) {
                Strip.Metrics metrics = new Strip.Metrics(N_PIXELS, PIXEL_PITCH);
                strips.add(new Strip("channel-" + channel + "/strip-" + numStrips++, metrics, t));
            }

            public List<LXPoint> getPoints() {
                List<LXPoint> points = new ArrayList<>();
                for (Strip strip : strips) {
                    points.addAll(Arrays.asList(strip.points));
                }
                return points;
            }
        }
    }
}
