package heronarts.lx.modulator;

import heronarts.lx.parameter.LXParameter;


/**
 * Shelf objects are modulators that have a bunch of mappable parameters in rows and columns.
 */
public abstract class Shelf extends LXModulator {
    public Shelf(String label) {
        super(label);
    }

    public abstract int rows();
    public abstract int cols();
    public abstract LXParameter getParameter(int row, int col);

    @Override
    protected double computeValue(double deltaMs) {
        /* Not relevant */
        return 0;
    }

    public static Shelf instantiateShelf(String className) {
        try {
            Class<? extends Shelf> cls = Class.forName(className).asSubclass(Shelf.class);
            return cls.getConstructor().newInstance();
        } catch (Exception e) {
            System.err.println(String.format("Exception when instantiating shelf type %s:", className));
            e.printStackTrace();
        }
        return null;
    }
}
