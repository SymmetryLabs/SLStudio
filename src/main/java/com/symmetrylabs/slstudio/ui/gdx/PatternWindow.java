package com.symmetrylabs.slstudio.ui.gdx;

import com.symmetrylabs.slstudio.ui.PatternGrouping;
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

public class PatternWindow {
    private final LX lx;
    private final PatternGrouping grouping;
    private String filterText = "";

    public PatternWindow(LX lx, String activeGroup) {
        this.lx = lx;
        this.grouping = new PatternGrouping(lx, activeGroup);
    }

    public void draw() {
        UI.begin("Components");
        filterText = UI.inputText("filter", filterText);

        if (UI.treeNode("Patterns", UI.TREE_FLAG_DEFAULT_OPEN)) {
            for (String groupName : grouping.groupNames) {
                String displayName = groupName == null ? "Uncategorized" : groupName;
                /* If this returns true, the tree is expanded and we should display
                     its contents */
                if (UI.treeNode(displayName, UI.TREE_FLAG_DEFAULT_OPEN)) {
                    for (PatternGrouping.Item pi : grouping.groups.get(groupName)) {
                        if (match(pi.label)) {
                            UI.treeNode(
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
            UI.treePop();
        }

        if (UI.treeNode("Effects", UI.TREE_FLAG_DEFAULT_OPEN)) {
            for (PatternGrouping.EffectItem ei : grouping.effects) {
                if (match(ei.label)) {
                    UI.treeNode(ei.label, UI.TREE_FLAG_LEAF, ei.label);
                    if (UI.isItemClicked()) {
                        activate(ei);
                    }
                    UI.treePop();
                }
            }
            UI.treePop();
        }

        if (UI.treeNode("Warps", UI.TREE_FLAG_DEFAULT_OPEN)) {
            for (PatternGrouping.WarpItem wi : grouping.warps) {
                if (match(wi.label)) {
                    UI.treeNode(wi.label, UI.TREE_FLAG_LEAF, wi.label);
                    if (UI.isItemClicked()) {
                        activate(wi);
                    }
                    UI.treePop();
                }
            }
            UI.treePop();
        }

        UI.end();
    }

    private boolean match(String label) {
        return filterText.length() == 0 ||
            label.toLowerCase().contains(filterText.toLowerCase());
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

    private void activate(PatternGrouping.EffectItem pi) {
        LXEffect instance = null;
        try {
            instance = pi.effect.getConstructor(LX.class).newInstance(lx);
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
            lx.engine.getFocusedChannel().addEffect(instance);
        }
    }

    private void activate(PatternGrouping.WarpItem pi) {
        LXWarp instance = null;
        try {
            instance = pi.warp.getConstructor(LX.class).newInstance(lx);
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
            lx.engine.getFocusedChannel().addWarp(instance);
        }
    }
}
