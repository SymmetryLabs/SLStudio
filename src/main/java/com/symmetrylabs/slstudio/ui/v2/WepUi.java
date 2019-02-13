package com.symmetrylabs.slstudio.ui.v2;

import com.symmetrylabs.slstudio.ui.WEPGrouping;
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
import com.symmetrylabs.slstudio.ApplicationState;

/**
 * A window that shows a tree view of warps, effects and patterns.
 */
public class WepUi {
    public interface OnWepSelected {
        void onWepAdded();
    }

    private final LX lx;
    private final WEPGrouping grouping;
    private final boolean allowPatterns;
    private String filterText = "";
    private OnWepSelected cb;
    private int maxHeight = 0;

    public WepUi(LX lx, OnWepSelected cb) {
        this(lx, true, cb);
    }

    public WepUi(LX lx, boolean allowPatterns, OnWepSelected cb) {
        this.lx = lx;
        this.grouping = new WEPGrouping(lx, ApplicationState.showName());
        this.cb = cb;
        this.allowPatterns = allowPatterns;
    }

    public void setMaxHeight(int maxHeight) {
        this.maxHeight = maxHeight;
    }

    public void resetFilter() {
        filterText = "";
    }

    public void draw() {
        filterText = UI.inputText("filter", filterText);

        UI.beginChild("wep-tree", false, 0, 300, maxHeight);
        if (allowPatterns && UI.treeNode("Patterns", UI.TREE_FLAG_DEFAULT_OPEN)) {
            for (String groupName : grouping.groupNames) {
                String displayName = groupName == null ? "Uncategorized" : groupName;
                /* If this returns true, the tree is expanded and we should display
                     its contents */
                if (UI.treeNode(displayName, UI.TREE_FLAG_DEFAULT_OPEN)) {
                    for (WEPGrouping.PatternItem pi : grouping.groups.get(groupName)) {
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
            for (WEPGrouping.EffectItem ei : grouping.effects) {
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
            for (WEPGrouping.WarpItem wi : grouping.warps) {
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
        UI.endChild();
    }

    private boolean match(String label) {
        return filterText.length() == 0 ||
            label.toLowerCase().contains(filterText.toLowerCase());
    }

    private void activate(WEPGrouping.PatternItem pi) {
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
            if (cb != null) {
                cb.onWepAdded();
            }
        }
    }

    private void activate(WEPGrouping.EffectItem pi) {
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
            if (cb != null) {
                cb.onWepAdded();
            }
        }
    }

    private void activate(WEPGrouping.WarpItem pi) {
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
            if (cb != null) {
                cb.onWepAdded();
            }
        }
    }
}
