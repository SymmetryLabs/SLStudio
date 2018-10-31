package com.symmetrylabs.slstudio.ui;

import glm_.vec2.Vec2;
import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static com.symmetrylabs.slstudio.ui.ImGuiManager.UI;

public class PatternWindow {
    private final LX lx;
    private final HashMap<String, List<PatternItem>> groups = new HashMap<>();
    private final List<String> groupNames;
    private final char[] filterText = new char[128];

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
        UI.begin("Patterns", null, 0);
        UI.inputText("filter", filterText, 0);
        String filter = new String(filterText).trim();

        for (String groupName : groupNames) {
            String displayName = groupName == null ? "Uncategorized" : groupName;
            /* If this returns true, the tree is expanded and we should display
                 its contents */
            if (UI.treeNode(displayName)) {
                for (PatternItem pi : groups.get(groupName)) {
                    if (filter.length() == 0 || pi.label.matches(filter)) {
                        boolean selected = UI.treeNodeEx(
                            String.format("%s/%s", groupName, pi.label),
                            imgui.TreeNodeFlag.Leaf.getI(),
                            pi.label);
                        if (UI.isItemClicked(0)) {
                            System.out.println(pi.label);
                        }
                        UI.treePop();
                    }
                }
                UI.treePop();
            }
        }
        UI.end();
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
