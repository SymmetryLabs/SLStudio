package com.symmetrylabs.slstudio.ui;

import heronarts.lx.LX;
import heronarts.lx.LXBus;
import heronarts.lx.LXChannel;
import heronarts.lx.LXPattern;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class PatternWindow {
    private final LX lx;
    private final HashMap<String, List<PatternItem>> groups = new HashMap<>();
    private final List<String> groupNames;
    private String filterText = "";

    public PatternWindow(LX lx, String activeGroup) {
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
    }

    public void draw() {
        UI.begin("Patterns");
        filterText = UI.inputText("filter", filterText);

        for (String groupName : groupNames) {
            String displayName = groupName == null ? "Uncategorized" : groupName;
            /* If this returns true, the tree is expanded and we should display
                 its contents */
            if (UI.treeNode(displayName, UI.TREE_FLAG_DEFAULT_OPEN)) {
                for (PatternItem pi : groups.get(groupName)) {
                    if (filterText.length() == 0 || pi.label.toLowerCase().contains(filterText.toLowerCase())) {
                        boolean selected = UI.treeNode(
                            String.format("%s/%s", groupName, pi.label),
                            UI.TREE_FLAG_LEAF, pi.label);
                        if (UI.isItemClicked()) {
                            activate(pi);
                        }
                        UI.treePop();
                    }
                }
                UI.treePop();
            }
        }
        UI.end();
    }

    private void activate(PatternItem pi) {
        LXPattern instance = null;
        try {
            instance = pi.pattern.getConstructor(LX.class).newInstance(lx);
        } catch (NoSuchMethodException nsmx) {
            nsmx.printStackTrace();
        } catch (java.lang.reflect.InvocationTargetException itx) {
            itx.printStackTrace();
        } catch (IllegalAccessException ix) {
            ix.printStackTrace();
        } catch (InstantiationException ix) {
            ix.printStackTrace();
        }

        if (instance != null) {
            LXBus channel = lx.engine.getFocusedChannel();
            if (channel instanceof LXChannel) {
                ((LXChannel) channel).addPattern(instance);
            } else {
                lx.engine.addChannel(new LXPattern[] { instance });
            }
        }
    }

    private static class PatternItem implements Comparable<PatternItem> {
        final Class<? extends LXPattern> pattern;
        final String label;

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
    }
}
