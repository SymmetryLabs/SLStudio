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
import java.util.HashSet;
import java.util.List;
import com.symmetrylabs.slstudio.ApplicationState;
import org.lwjgl.glfw.GLFW;
import com.symmetrylabs.util.FuzzyStringFilter;

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
    private final HashSet<String> visibleGroups = new HashSet<>();
    private final FuzzyStringFilter<Object> filter;
    private boolean effectsVisible;
    private boolean warpsVisible;
    private String visibleFilterText = "";
    private OnWepSelected cb;
    private int maxHeight = 0;

    private final Object FILTER_MATCH_EFFECTS = new Object();
    private final Object FILTER_MATCH_WARPS = new Object();

    public WepUi(LX lx, OnWepSelected cb) {
        this(lx, true, cb);
    }

    public WepUi(LX lx, boolean allowPatterns, OnWepSelected cb) {
        this.lx = lx;
        this.grouping = new WEPGrouping(lx, ApplicationState.showName());
        this.cb = cb;
        this.allowPatterns = allowPatterns;

        filter = new FuzzyStringFilter<>();
        for (WEPGrouping.PatternItem i : grouping.patterns) {
            filter.addSentence(i, nameToSentence(i.label, i.group));
        }
        for (WEPGrouping.EffectItem i : grouping.effects) {
            filter.addSentence(i, nameToSentence(i.label, null));
        }
        for (WEPGrouping.WarpItem i : grouping.warps) {
            filter.addSentence(i, nameToSentence(i.label, null));
        }
        filter.addSentence(FILTER_MATCH_WARPS, "warps");
        filter.addSentence(FILTER_MATCH_EFFECTS, "effects");
        filter.setFilterText("");
        filter.run();

        resetFilter();
    }

    public void setMaxHeight(int maxHeight) {
        this.maxHeight = maxHeight;
    }

    public void resetFilter() {
        visibleFilterText = "";
        filter.setFilterText("");

        visibleGroups.clear();
        visibleGroups.addAll(grouping.groupNames);
        effectsVisible = true;
        warpsVisible = true;
        for (WEPGrouping.PatternItem pi : grouping.patterns) {
            pi.visible = true;
        }
        for (WEPGrouping.EffectItem ei : grouping.effects) {
            ei.visible = true;
        }
        for (WEPGrouping.WarpItem wi : grouping.warps) {
            wi.visible = true;
        }
    }

    public void draw(boolean setFocusOnFilter) {
        if (setFocusOnFilter) {
            UI.setKeyboardFocusHere();
        }
        visibleFilterText = UI.inputText("filter", visibleFilterText);
        if (filter.setFilterText(visibleFilterText)) {
            visibleGroups.clear();
            for (String groupName : grouping.groupNames) {
                for (WEPGrouping.PatternItem pi : grouping.groups.get(groupName)) {
                    pi.visible = filter.matches(pi);
                    if (pi.visible) {
                        visibleGroups.add(groupName);
                    }
                }
            }
            effectsVisible = false;
            for (WEPGrouping.EffectItem ei : grouping.effects) {
                ei.visible = filter.matches(ei) || filter.matches(FILTER_MATCH_EFFECTS);
                effectsVisible = effectsVisible || ei.visible;
            }
            warpsVisible = false;
            for (WEPGrouping.WarpItem wi : grouping.warps) {
                wi.visible = filter.matches(wi) || filter.matches(FILTER_MATCH_WARPS);
                warpsVisible = warpsVisible || wi.visible;
            }
        }

        boolean firstMatch = true;

        UI.beginChild("wep-tree", false, 0, 300, maxHeight);
        if (allowPatterns && !visibleGroups.isEmpty() && UI.treeNode("Patterns", UI.TREE_FLAG_DEFAULT_OPEN)) {
            for (String groupName : grouping.groupNames) {
                if (!visibleGroups.contains(groupName)) {
                    continue;
                }
                String displayName = groupName == null ? "Uncategorized" : groupName;
                /* If this returns true, the tree is expanded and we should display
                     its contents */
                if (UI.treeNode(displayName, UI.TREE_FLAG_DEFAULT_OPEN)) {
                    for (WEPGrouping.PatternItem pi : grouping.groups.get(groupName)) {
                        if (pi.visible) {
                            UI.treeNode(
                                String.format("%s/%s", groupName, pi.label),
                                UI.TREE_FLAG_LEAF | (firstMatch ? UI.TREE_FLAG_SELECTED : 0), pi.label);
                            if (UI.isItemClicked()) {
                                activate(pi);
                            }
                            if (firstMatch && UI.isKeyPressed(GLFW.GLFW_KEY_ENTER)) {
                                activate(pi);
                            }
                            firstMatch = false;
                            UI.treePop();
                        }
                    }
                    UI.treePop();
                }
            }
            UI.treePop();
        }

        if (effectsVisible && UI.treeNode("Effects", UI.TREE_FLAG_DEFAULT_OPEN)) {
            for (WEPGrouping.EffectItem ei : grouping.effects) {
                if (ei.visible) {
                    UI.treeNode(ei.label, UI.TREE_FLAG_LEAF | (firstMatch ? UI.TREE_FLAG_SELECTED : 0), ei.label);
                    if (UI.isItemClicked()) {
                        activate(ei);
                    }
                    if (firstMatch && UI.isKeyPressed(GLFW.GLFW_KEY_ENTER)) {
                        activate(ei);
                    }
                    firstMatch = false;
                    UI.treePop();
                }
            }
            UI.treePop();
        }

        if (warpsVisible && UI.treeNode("Warps", UI.TREE_FLAG_DEFAULT_OPEN)) {
            for (WEPGrouping.WarpItem wi : grouping.warps) {
                if (wi.visible) {
                    UI.treeNode(wi.label, UI.TREE_FLAG_LEAF | (firstMatch ? UI.TREE_FLAG_SELECTED : 0), wi.label);
                    if (UI.isItemClicked()) {
                        activate(wi);
                    }
                    if (firstMatch && UI.isKeyPressed(GLFW.GLFW_KEY_ENTER)) {
                        activate(wi);
                    }
                    firstMatch = false;
                    UI.treePop();
                }
            }
            UI.treePop();
        }
        UI.endChild();
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
            LXBus channel = lx.engine.getFocusedLook().getFocusedChannel();
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
            lx.engine.getFocusedLook().getFocusedChannel().addEffect(instance);
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
            lx.engine.getFocusedLook().getFocusedChannel().addWarp(instance);
            if (cb != null) {
                cb.onWepAdded();
            }
        }
    }

    private List<String> nameToSentence(String label, String group) {
        List<String> res = new ArrayList<>();
        for (int end = 0; end < label.length();) {
            int start = end;
            end++;
            while (end < label.length() && Character.isLowerCase(label.charAt(end))) end++;
            res.add(label.substring(start, end));
        }
        if (group != null) {
            res.add(group);
        }
        return res;
    }
}
