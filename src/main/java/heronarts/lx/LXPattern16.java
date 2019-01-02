package heronarts.lx;

import static heronarts.lx.PolyBuffer.Space.RGB16;

/**
 * For convenience, patterns that are written only for the RGB16 color space
 * can extend this class and implement just the abstract run() method.
 */
public abstract class LXPattern16 extends LXPattern {
    protected LXPattern16(LX lx) {
        super(lx);
        setPreferredSpace(RGB16);
    }

    @Override protected final void run(double deltaMs, PolyBuffer.Space preferredSpace) {
        run(deltaMs, (long[]) getArray(RGB16));
        markModified(RGB16);
    }

    /**
     * Implements the pattern.  Subclasses should override this method to
     * write colors into the 16-bit color array.
     *
     * @param deltaMs Number of milliseconds elapsed since last invocation
     * @param colors16 The 16-bit color buffer.
     */
    protected abstract void run(double deltaMs, long[] colors16);
}
