package com.symmetrylabs.shows.flash;

import com.symmetrylabs.slstudio.model.Strip;
import heronarts.lx.model.LXPoint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Per-strip output blackout for the flash show.
 *
 * Strips flagged here are forced to black in the ArtNet data sent by
 * {@link FlashShow.FlashPixlite} only; pattern engine colors and the 3D preview
 * are untouched. State is session-only (not written to the mapping file).
 */
public final class FlashBlackout {

    private static final int[] EMPTY = new int[0];

    /** Global strip indices that are blacked out. Mutated only from the UI thread. */
    private static final Set<Integer> blackedStrips = new TreeSet<>();

    /** Live strip list, set at output-build time. */
    private static List<Strip> strips = Collections.emptyList();

    /** Flattened point indices of all blacked strips; read by the output thread. */
    private static volatile int[] blackedPointIndices = EMPTY;

    private FlashBlackout() {}

    /** Called when the model is (re)built so point indices can be resolved. */
    public static synchronized void setStrips(List<Strip> liveStrips) {
        strips = (liveStrips != null) ? liveStrips : Collections.<Strip>emptyList();
        recompute();
    }

    public static synchronized boolean isBlackedOut(int stripIndex) {
        return blackedStrips.contains(stripIndex);
    }

    public static synchronized void setBlackedOut(int stripIndex, boolean blackedOut) {
        if (stripIndex < 0) return;
        boolean changed = blackedOut
            ? blackedStrips.add(stripIndex)
            : blackedStrips.remove(Integer.valueOf(stripIndex));
        if (changed) recompute();
    }

    /**
     * Point indices to force black, or an empty array when nothing is blacked out.
     * Safe to read from the output thread.
     */
    public static int[] getBlackedPointIndices() {
        return blackedPointIndices;
    }

    private static void recompute() {
        if (blackedStrips.isEmpty() || strips.isEmpty()) {
            blackedPointIndices = EMPTY;
            return;
        }
        List<Integer> indices = new ArrayList<>();
        for (Integer si : blackedStrips) {
            if (si == null || si < 0 || si >= strips.size()) continue;
            for (LXPoint p : strips.get(si).getPoints()) {
                indices.add(p.index);
            }
        }
        int[] out = new int[indices.size()];
        for (int i = 0; i < out.length; i++) out[i] = indices.get(i);
        blackedPointIndices = out;
    }
}
