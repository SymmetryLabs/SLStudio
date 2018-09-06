package com.symmetrylabs.slstudio.ui;

import java.io.File;
import java.util.List;
import java.util.ArrayList;

import heronarts.lx.LX;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.studio.UICollapsibleSection;
import heronarts.p3lx.ui.component.UIItemList;

import com.symmetrylabs.slstudio.workspaces.Workspace;
import com.symmetrylabs.slstudio.workspaces.WorkspaceProject;

public class UIWorkspace extends UICollapsibleSection {
    private final Workspace workspace;

    public UIWorkspace(UI ui, LX lx, Workspace workspaces, float x, float y, float w) {
        super(ui, x, y, w, 400);
        this.workspace = workspaces;
        setTitle("WORKSPACE");

        final List<UIItemList.Item> items = new ArrayList<UIItemList.Item>();
        for (WorkspaceProject workspace : this.workspace.getAll()) {
            items.add(new WorkspaceItem(lx, workspace));
        }

        final UIItemList.ScrollList list = new UIItemList.ScrollList(ui, 0, 0, w - 8, 375);
        list.setItems(items).setSingleClickActivate(true);
        list.addToContainer(this);
    }

    private class WorkspaceItem extends UIItemList.AbstractItem {
            final LX lx;
            final WorkspaceProject project;

            private WorkspaceItem(LX lx, WorkspaceProject project) {
                this.lx = lx;
                this.project = project;
                lx.addProjectListener((file, change) -> redraw());
            }

            public String getLabel() {
                return project.getLabel();
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
                return project.matches(currentProject);
            }

            @Override
            public int getActiveColor(UI ui) {
                    return isSelected() ? ui.theme.getPrimaryColor() : ui.theme.getSecondaryColor();
            }

            @Override
            public void onActivate() {
                UIWorkspace.this.workspace.openProject(project);
            }
    }
}
