package com.symmetrylabs.layouts.tree.config;

private class TreeConfigLoader extends LXComponent {

    private static final String KEY = "treeConfiguration";

    private final Tree tree;

    private TreeConfigLoader(LX lx) {
        super(lx);
        this.tree = (TreeModel)lx.model;
    }

    @Override
    public void load(LX lx, JsonObject obj) {
        if (obj.has(KEY)) {
            TreeConfig config = new TreeConfig();

            // add to config...

            tree.reconfigure(config);
        }
    }
}