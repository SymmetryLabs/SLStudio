package heronarts.lx;

import heronarts.lx.color.LXColor;
import heronarts.lx.color.LXColor16;

import java.lang.reflect.Array;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Manages a set of color buffers of various color spaces, converting color values
 * between them automatically as needed.  Clients should call markModified()
 * after writing into any buffer; then getBuffer() will convert the data when necessary.
 * Buffers are allocated on demand; if only one is used, no memory is wasted on any others.
 */
public class PolyBuffer implements PolyBufferProvider {
    public enum Space {RGB8, RGB16, SRGB8};

    private LX lx = null;
    private Map<Space, Buffer> buffers = new EnumMap<>(Space.class);
    private Set<Space> freshSpaces = EnumSet.noneOf(Space.class);
    private static int conversionCount = 0;

    public PolyBuffer(LX lx) {
        this.lx = lx;
    }

    public PolyBuffer getPolyBuffer() {
        return this;
    }

    public Buffer getBuffer(Space space) {
        updateBuffer(space);
        return buffers.get(space);
    }

    public Object getArray(Space space) {
        return getBuffer(space).getArray();
    }

    public void markModified(Space space) {
        assert buffers.get(space) != null;
        freshSpaces = EnumSet.of(space);
    }

    public Space getFreshSpace() {
        for (Space space : freshSpaces) {
            return space;
        }
        // There should always be at least one fresh space, so we should never get here.
        return Space.RGB8;
    }

    public boolean isFresh(Space space) {
        return freshSpaces.contains(space);
    }

    protected Buffer createBuffer(Space space) {
        switch (space) {
            case RGB8:
            case SRGB8:
                return new ModelBuffer(lx);
            case RGB16:
                return new ModelLongBuffer(lx);
            default:
                return null;
        }
    }

    protected void updateBuffer(Space space) {
        if (!isFresh(space)) {
            if (buffers.get(space) == null) {
                buffers.put(space, createBuffer(space));
            }
            Object dest = buffers.get(space).getArray();
            // For the conversion source, choose the most expressive color space
            // that has fresh data in its buffer; RGB16 is preferred over SRGB8,
            // which is preferred over RGB8.
            switch (space) {
                case RGB8:
                    if (isFresh(Space.RGB16)) {
                        LXColor16.toRgb8((long[]) getArray(Space.RGB16), (int[]) dest);
                    } else if (isFresh(Space.SRGB8)) {
                        LXColor.srgb8ToRgb8((int[]) getArray(Space.SRGB8), (int[]) dest);
                    }
                    break;
                case RGB16:
                    if (isFresh(Space.SRGB8)) {
                        LXColor.srgb8ToRgb16((int[]) getArray(Space.SRGB8), (long[]) dest);
                    } else if (isFresh(Space.RGB8)) {
                        LXColor.rgb8ToRgb16((int[]) getArray(Space.RGB8), (long[]) dest);
                    }
                    break;
                case SRGB8:
                    if (isFresh(Space.RGB16)) {
                        LXColor16.toSrgb8((long[]) getArray(Space.RGB16), (int[]) dest);
                    } else if (isFresh(Space.RGB8)) {
                        LXColor.rgb8ToSrgb8((int[]) getArray(Space.RGB8), (int[]) dest);
                    }
                    break;
            }
            conversionCount++;
            freshSpaces.add(space);
        }
    }

    public static int getConversionCount() {
        return conversionCount;
    }

    public void copyFrom(PolyBufferProvider src, Space space) {
        if (src != this) {
            Object dest = getArray(space);
            System.arraycopy(src.getPolyBuffer().getArray(space), 0, dest, 0, Array.getLength(dest));
            markModified(space);
        }
    }

    // The methods below provide support for old-style use of the PolyBuffer
    // as if it were only an RGB8 buffer.

    @Deprecated
    public static PolyBuffer wrapArray(LX lx, final int[] array) {
        PolyBuffer buffer = new PolyBuffer(lx);
        buffer.setBuffer(new Buffer() {
            public Object getArray() { return array; }
        });
        return buffer;
    }

    @Deprecated
    public void setBuffer(Buffer buffer) {
        buffers.clear();
        buffers.put(Space.RGB8, buffer);
        markModified(Space.RGB8);
    }
}
