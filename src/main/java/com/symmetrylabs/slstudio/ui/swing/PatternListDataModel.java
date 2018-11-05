package com.symmetrylabs.slstudio.ui.swing;

import com.symmetrylabs.slstudio.ui.PatternGrouping;
import heronarts.lx.LX;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

public class PatternListDataModel implements TreeModel {
    private static class TreeRoot {
        public String toString() {
            return "Patterns";
        }
    }

    private static final Object UNCATEGORIZED = "Uncategorized";

    private final TreeRoot root = new TreeRoot();
    private final LX lx;
    private final PatternGrouping grouping;

    public PatternListDataModel(LX lx, String activeGroup) {
        this.lx = lx;
        this.grouping = new PatternGrouping(lx, activeGroup);
    }

    @Override
    public void addTreeModelListener(TreeModelListener l) {
        /* list is immutable, so no reason to keep track of these. */
    }

    @Override
    public Object getChild(Object parent, int index) {
        if (parent == root) {
            String res = grouping.groupNames.get(index);
            return res == null ? UNCATEGORIZED : res;
        } else {
            if (parent == UNCATEGORIZED) {
                parent = null;
            }
            return grouping.groups.get(parent).get(index);
        }
    }

    @Override
    public int getChildCount(Object parent) {
        if (parent == root) {
            return grouping.groupNames.size();
        } else {
            if (parent == UNCATEGORIZED) {
                parent = null;
            }
            return grouping.groups.get(parent).size();
        }
    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
        if (parent == root) {
            return grouping.groupNames.indexOf(child);
        } else {
            if (parent == UNCATEGORIZED) {
                parent = null;
            }
            return grouping.groups.get(parent).indexOf(child);
        }
    }

    @Override
    public Object getRoot() {
        return root;
    }

    @Override
    public boolean isLeaf(Object node) {
        return node instanceof PatternGrouping.Item;
    }

    @Override
    public void removeTreeModelListener(TreeModelListener l) {
    }

    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {
    }
}
