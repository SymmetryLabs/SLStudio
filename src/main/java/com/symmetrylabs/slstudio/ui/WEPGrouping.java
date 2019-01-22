package com.symmetrylabs.slstudio.ui;

import heronarts.lx.LX;
import heronarts.lx.LXBus;
import heronarts.lx.LXChannel;
import heronarts.lx.LXEffect;
import heronarts.lx.LXPattern;
import heronarts.lx.warp.LXWarp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Splits warps, effects and patterns into ordered groups by their declared group name.
 *
 * Patterns can optionally declare membership in a group by adding a GROUP_NAME field to
 * their class. This class groups patterns based on that field, and also determines the
 * appropriate sort order both for the groups and the patterns. It is intended for use
 * by UI widgets that want to display the pattern groups.
 *
 * At sort time, if an active group is specified, that group is sorted above all other
 * groups. For the remaining groups, the "null group" (patterns that don't declare a
 * group) are sorted first, followed by the remaining groups in alphabetical order.
 */
public class WEPGrouping {
    private final LX lx;
    public final HashMap<String, List<PatternItem>> groups = new HashMap<>();
    public final List<String> groupNames;
    public final List<WarpItem> warps;
    public final List<EffectItem> effects;

    /**
     * Create a pattern grouping and sort all registered patterns into groups.
     *
     * After this constructor is called, all member fields are populated with the
     * sort result.
     */
    public WEPGrouping(LX lx, String activeGroup) {
        this.lx = lx;

        List<Class<? extends LXPattern>> patterns = lx.getRegisteredPatterns();
        for (Class<? extends LXPattern> p : patterns) {
            String group = LXPattern.getGroupName(p);
            groups.putIfAbsent(group, new ArrayList<>());
            groups.get(group).add(new PatternItem(p));
        }

        for (List<PatternItem> ps : groups.values()) {
            Collections.sort(ps);
        }
        groupNames = new ArrayList<>(groups.keySet());

        Collections.sort(groupNames, (a, b) -> {
                int aSortKey, bSortKey;
                if (activeGroup != null) {
                    aSortKey = a == null ? 0 : a.equals(activeGroup) ? -1 : 1;
                    bSortKey = b == null ? 0 : b.equals(activeGroup) ? -1 : 1;
                } else {
                    aSortKey = a == null ? 0 : 1;
                    bSortKey = b == null ? 0 : 1;
                }
                int sortKeyCompare = Integer.compare(aSortKey, bSortKey);
                if (sortKeyCompare != 0)
                    return sortKeyCompare;
                return a.compareToIgnoreCase(b);
            });

        List<Class<? extends LXEffect>> effectClasses = lx.getRegisteredEffects();
        effects = new ArrayList<>();
        for (Class<? extends LXEffect> effectClass : effectClasses) {
            effects.add(new EffectItem(effectClass));
        }
        Collections.sort(effects);

        List<Class<? extends LXWarp>> warpClasses = lx.getRegisteredWarps();
        warps = new ArrayList<>();
        for (Class<? extends LXWarp> warpClass : warpClasses) {
            warps.add(new WarpItem(warpClass));
        }
        Collections.sort(warps);
    }

    /** Represents a pattern class registered with LX. */
    public static class PatternItem implements Comparable<PatternItem> {
        public final Class<? extends LXPattern> pattern;
        public final String label;

        PatternItem(Class<? extends LXPattern> pattern) {
            this.pattern = pattern;
            String simple = pattern.getSimpleName();
            if (simple.endsWith("Pattern")) {
                simple = simple.substring(0, simple.length() - "Pattern".length());
            }
            this.label = simple;
        }

        @Override
        public int compareTo(PatternItem o) {
            return label.compareToIgnoreCase(o.label);
        }

        @Override
        public String toString() {
            return label;
        }
    }

    /** Represents a warp class registered with LX. */
    public static class WarpItem implements Comparable<WarpItem> {
        public final Class<? extends LXWarp> warp;
        public final String label;

        WarpItem(Class<? extends LXWarp> warp) {
            this.warp = warp;
            String simple = warp.getSimpleName();
            if (simple.endsWith("Warp")) {
                simple = simple.substring(0, simple.length() - "Warp".length());
            }
            this.label = simple;
        }

        @Override
        public int compareTo(WarpItem o) {
            return label.compareToIgnoreCase(o.label);
        }

        @Override
        public String toString() {
            return label;
        }
    }

    /** Represents an effect class registered with LX. */
    public static class EffectItem implements Comparable<EffectItem> {
        public final Class<? extends LXEffect> effect;
        public final String label;

        EffectItem(Class<? extends LXEffect> effect) {
            this.effect = effect;
            String simple = effect.getSimpleName();
            if (simple.endsWith("Effect")) {
                simple = simple.substring(0, simple.length() - "Effect".length());
            }
            this.label = simple;
        }

        @Override
        public int compareTo(EffectItem o) {
            return label.compareToIgnoreCase(o.label);
        }

        @Override
        public String toString() {
            return label;
        }
    }
}
