package com.symmetrylabs.slstudio.ui;

import java.io.File;
import java.util.List;
import java.util.ArrayList;

import heronarts.lx.LX;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.studio.UICollapsibleSection;
import heronarts.p3lx.ui.component.UIItemList;
import heronarts.p3lx.ui.component.UIKnob;

import com.symmetrylabs.slstudio.workspaces.Workspaces;
import com.symmetrylabs.slstudio.workspaces.Workspace;

public class UIWorkspaces extends UICollapsibleSection {
    private final Workspaces workspaces;

    public UIWorkspaces(UI ui, LX lx, Workspaces workspaces, float x, float y, float w) {
        super(ui, x, y, w, 280);
        this.workspaces = workspaces;

        setTitle("Workspaces");

        final List<UIItemList.Item> items = new ArrayList<UIItemList.Item>();
        for (Workspace workspace : this.workspaces.getAll()) { items.add(new WorkspaceItem(lx, workspace)); }

        final UIKnob active = new UIKnob(workspaces.active);
        active.addToContainer(this);

        final UIItemList.ScrollList list = new UIItemList.ScrollList(ui, 50, 0, w-8, 200);
        list.setItems(items).setSingleClickActivate(true);
        list.addToContainer(this);
    }

    private class WorkspaceItem extends UIItemList.AbstractItem {
            final LX lx;
            final Workspace workspace;

            private WorkspaceItem(LX lx, Workspace _workspace) {
                this.lx = lx;
                this.workspace = _workspace;

                lx.addProjectListener(new LX.ProjectListener() {
                    public void projectChanged(File file, LX.ProjectListener.Change change) {
                        // if (change == lx.ProjectListener.Change.OPEN) {}
                        // if (change == lx.ProjectListener.Change.SAVE) {}
                        // if (change == lx.ProjectListener.Change.NEW) {}
                        redraw();
                    }
                });
            }

            public String getLabel() {
                return workspace.getName();
            }

            public boolean isSelected() { 
                File file = lx.getProject();
                if (file == null) return false;
                return file.getName().equals(workspace.getName());
            }

            @Override
            public boolean isActive() {
                File file = lx.getProject();
                if (file == null) return false;
                return file.getName().equals(workspace.getName());
            }

            @Override
            public int getActiveColor(UI ui) {
                    return isSelected() ? ui.theme.getPrimaryColor() : ui.theme.getSecondaryColor();
            }

            @Override
            public void onActivate() {
                workspaces.openWorkspace(workspace);
            }

            // @Override
            // public void onDeactivate() {
            //     System.out.println("onDeactivate");
            //     controller.enabled.setValue(false);
            // }
    }
}