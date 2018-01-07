package com.symmetrylabs.slstudio.util;

import java.util.function.Supplier;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Implementation of triple buffering, largely based on
 * http://remis-thoughts.blogspot.com/2012/01/triple-buffering-as-concurrency_30.html
 */
public class TripleBuffer<T> {

    private static final int SNAP_BUFFER_MASK = 0b00000011;
    private static final int NEXT_SNAP_BUFFER_MASK = 0b00001100;
    private static final int WRITE_BUFFER_MASK = 0b00110000;
    private static final int NEWLY_WRITTEN_MASK = 0b01000000;

    private final T[] buffers;
    private final AtomicInteger flags;

    @SuppressWarnings("unchecked")
    public TripleBuffer(Supplier<T> supplier) {
        buffers = (T[])new Object[3];
        buffers[0] = supplier.get();
        buffers[1] = supplier.get();
        buffers[2] = supplier.get();

        flags = new AtomicInteger(buildFlags(false, 0, 2, 1));
    }

    public T getWriteBuffer() {
        return buffers[(flags.get() & WRITE_BUFFER_MASK) >> 4];
    }

    public T flipWriteBuffer() {
        int flagsNow, newFlags;

        do {
            flagsNow = flags.get();
            newFlags = NEWLY_WRITTEN_MASK
                    | (flagsNow & SNAP_BUFFER_MASK)
                    | ((flagsNow & NEXT_SNAP_BUFFER_MASK) << 2)
                    | ((flagsNow & WRITE_BUFFER_MASK) >> 2);
        }
        while (!flags.compareAndSet(flagsNow, newFlags));

        return getWriteBuffer();
    }

    public T getSnapshotBuffer() {
        return buffers[flags.get() & SNAP_BUFFER_MASK];
    }

    public boolean hasSnapshotChanged() {
        return (flags.get() & NEWLY_WRITTEN_MASK) != 0;
    }

    public T takeSnapshot() {
        int flagsNow, newFlags;

        do {
            flagsNow = flags.get();

            if ((flagsNow & NEWLY_WRITTEN_MASK) == 0)
                break;

            newFlags = (flagsNow & WRITE_BUFFER_MASK)
                    | ((flagsNow & SNAP_BUFFER_MASK) << 2)
                    | ((flagsNow & NEXT_SNAP_BUFFER_MASK) >> 2);
        }
        while (!flags.compareAndSet(flagsNow, newFlags));

        return getSnapshotBuffer();
    }

    private int buildFlags(boolean isNewlyWritten,
            int writeBufferIndex, int snapBufferIndex, int nextSnapBufferIndex) {

        int newFlags = nextSnapBufferIndex;

        newFlags |= snapBufferIndex << 2;
        newFlags |= writeBufferIndex << 4;

        if (isNewlyWritten) {
            newFlags |= NEWLY_WRITTEN_MASK;
        }

        return newFlags;
    }
}
