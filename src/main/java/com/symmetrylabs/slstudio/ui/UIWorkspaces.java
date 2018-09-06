package com.symmetrylabs.slstudio.ui;

import java.io.File;
import java.util.List;
import java.util.ArrayList;

import heronarts.lx.LX;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.studio.UICollapsibleSection;
import heronarts.p3lx.ui.component.UIItemList;
import heronarts.p3lx.ui.component.UIKnob;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;

import com.symmetrylabs.slstudio.workspaces.Workspaces;
import com.symmetrylabs.slstudio.workspaces.Workspace;

public class UIWorkspaces extends UICollapsibleSection {
    private final Workspaces workspaces;

    public UIWorkspaces(UI ui, LX lx, Workspaces workspaces, float x, float y, float w) {
        super(ui, x, y, w, 400);
        this.workspaces = workspaces;
        setTitle("WORKSPACE");

        final List<UIItemList.Item> items = new ArrayList<UIItemList.Item>();
        for (Workspace workspace : this.workspaces.getAll()) {
            items.add(new WorkspaceItem(lx, workspace));
        }

        final UIItemList.ScrollList list = new UIItemList.ScrollList(ui, 0, 0, w - 8, 375);
        list.setItems(items).setSingleClickActivate(true);
        list.addToContainer(this);
    }

    private class WorkspaceItem extends UIItemList.AbstractItem {
            final LX lx;
            final Workspace workspace;

            private WorkspaceItem(LX lx, Workspace _workspace) {
                this.lx = lx;
                this.workspace = _workspace;
                lx.addProjectListener((file, change) -> redraw());
            }

            public String getLabel() {
                return workspace.getLabel();
            }

            public boolean isSelected() { 
                return isActive();
            }

            @Override
            public boolean isActive() {
                File currentProject = lx.getProject();
                if (currentProject == null || !currentProject.exists()) {
                    return false;
                }
                return workspace.matches(currentProject);
            }

            @Override
            public int getActiveColor(UI ui) {
                    return isSelected() ? ui.theme.getPrimaryColor() : ui.theme.getSecondaryColor();
            }

            @Override
            public void onActivate() {
                workspaces.openWorkspace(workspace);
            }
    }
}
