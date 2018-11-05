package com.symmetrylabs.slstudio.ui.gdx;

import com.symmetrylabs.slstudio.ui.PatternGrouping;
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
    private final PatternGrouping grouping;
    private String filterText = "";

    public PatternWindow(LX lx, String activeGroup) {
        this.lx = lx;
        this.grouping = new PatternGrouping(lx, activeGroup);
    }

    public void draw() {
        UI.begin("Patterns");
        filterText = UI.inputText("filter", filterText);

        for (String groupName : grouping.groupNames) {
            String displayName = groupName == null ? "Uncategorized" : groupName;
            /* If this returns true, the tree is expanded and we should display
                 its contents */
            if (UI.treeNode(displayName, UI.TREE_FLAG_DEFAULT_OPEN)) {
                for (PatternGrouping.Item pi : grouping.groups.get(groupName)) {
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

    private void activate(PatternGrouping.Item pi) {
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
}
