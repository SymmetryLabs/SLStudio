package heronarts.lx;

import com.symmetrylabs.color.Spaces;

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

    /** Returns the most expressive color space whose buffer contains fresh data. */
    public Space getBestFreshSpace() {
        if (isFresh(Space.RGB16)) return Space.RGB16;
        if (isFresh(Space.SRGB8)) return Space.SRGB8;
        if (isFresh(Space.RGB8)) return Space.RGB8;

        // There should always be at least one fresh space, so we should never get here.
        throw new IllegalStateException("Execution should never reach this point");
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
            Object dest = getArray(space);
            Space srcSpace = getBestFreshSpace();
            Object src = getArray(srcSpace);

            if (srcSpace == Space.RGB16 && space == Space.SRGB8) {
                Spaces.rgb16ToSrgb8((long[]) src, (int[]) dest);
            } else if (srcSpace == Space.RGB16 && space == Space.RGB8) {
                Spaces.rgb16ToRgb8((long[]) src, (int[]) dest);
            } else if (srcSpace == Space.SRGB8 && space == Space.RGB16) {
                Spaces.srgb8ToRgb16((int[]) src, (long[]) dest);
            } else if (srcSpace == Space.SRGB8 && space == Space.RGB8) {
                Spaces.srgb8ToRgb8((int[]) src, (int[]) dest);
            } else if (srcSpace == Space.RGB8 && space == Space.RGB16) {
                Spaces.rgb8ToRgb16((int[]) src, (long[]) dest);
            } else if (srcSpace == Space.RGB8 && space == Space.SRGB8) {
                Spaces.rgb8ToSrgb8((int[]) src, (int[]) dest);
            } else {
                // There should always be at least one fresh space, so we should never get here.
                throw new IllegalStateException("Execution should never reach this point");
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
            Object srcArray = src.getPolyBuffer().getArray(space);
            Object destArray = getArray(space);
            System.arraycopy(srcArray, 0, destArray, 0, Array.getLength(destArray));
            markModified(space);
        }
    }

    // The methods below provide support for old-style use of the PolyBuffer
    // as if it were only an SRGB8 buffer.

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
        buffers.put(Space.SRGB8, buffer);
        markModified(Space.SRGB8);
    }
}
