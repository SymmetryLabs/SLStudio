package com.symmetrylabs.slstudio.ui;

import heronarts.lx.LX;
import heronarts.lx.LXBus;
import heronarts.lx.LXChannel;
import heronarts.lx.LXPattern;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class PatternGrouping {
    private final LX lx;
    public final HashMap<String, List<Item>> groups = new HashMap<>();
    public final List<String> groupNames;

    public PatternGrouping(LX lx, String activeGroup) {
        this.lx = lx;

        List<Class<? extends LXPattern>> patterns = lx.getRegisteredPatterns();
        for (Class<? extends LXPattern> p : patterns) {
            String group = LXPattern.getGroupName(p);
            groups.putIfAbsent(group, new ArrayList<>());
            groups.get(group).add(new Item(p));
        }

        for (List<Item> ps : groups.values()) {
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
    }

    public static class Item implements Comparable<Item> {
        public final Class<? extends LXPattern> pattern;
        public final String label;

        Item(Class<? extends LXPattern> pattern) {
            this.pattern = pattern;
            String simple = pattern.getSimpleName();
            if (simple.endsWith("Pattern")) {
                simple = simple.substring(0, simple.length() - "Pattern".length());
            }
            this.label = simple;
        }

        @Override
        public int compareTo(Item o) {
            return label.compareToIgnoreCase(o.label);
        }

        @Override
        public String toString() {
            return label;
        }
    }
}
