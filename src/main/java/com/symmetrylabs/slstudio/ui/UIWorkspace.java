package com.symmetrylabs.slstudio.ui;

import java.io.File;
import java.util.List;
import java.util.ArrayList;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.StringParameter;
import heronarts.lx.data.Project;
import heronarts.p3lx.ui.component.UIButton;
import heronarts.p3lx.ui.component.UITextBox;
import heronarts.p3lx.ui.component.UILabel;

import heronarts.lx.LX;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.studio.UICollapsibleSection;
import heronarts.p3lx.ui.component.UIItemList;

import com.symmetrylabs.slstudio.ApplicationState;
import com.symmetrylabs.slstudio.workspaces.Workspace;
import com.symmetrylabs.slstudio.workspaces.WorkspaceProject;
import java.util.Timer;
import java.util.TimerTask;

public class UIWorkspace extends UICollapsibleSection {
    private final Workspace workspace;

    private final BooleanParameter autoSwitchEnabled = new BooleanParameter("autoSwitch", false);
    private final StringParameter autoSwitchPeriodParam =
        new StringParameter("autoSwitchPeriod", "5m0s");
    private long switchPeriodMs = -1;

    private class SwitchTimerTask extends TimerTask {
        @Override
        public void run() {
            workspace.advance();
        }
    };
    private TimerTask switchTask;
    private final Timer timer;

    public UIWorkspace(UI ui, LX lx, Workspace workspaces, float x, float y, float w) {
        super(ui, x, y, w, 440);
        this.workspace = workspaces;
        setTitle("WORKSPACE");

        new UILabel(0, 0, 100, 10).setLabel("Auto-switch project").addToContainer(this);
        new UIButton(0, 16, 20, 20).setLabel("").setParameter(autoSwitchEnabled).addToContainer(this);
        new UITextBox(25, 16, 50, 20).setParameter(autoSwitchPeriodParam).addToContainer(this);

        timer = new Timer(/* daemon: */ true);
        autoSwitchEnabled.addListener(p -> {
                if (autoSwitchEnabled.getValueb() && switchPeriodMs > 0) {
                    if (switchTask != null) {
                        switchTask.cancel();
                    }
                    switchTask = new SwitchTimerTask();
                    timer.scheduleAtFixedRate(switchTask, switchPeriodMs, switchPeriodMs);
                } else {
                    if (switchTask != null) {
                        switchTask.cancel();
                        switchTask = null;
                    }
                }
            });
        autoSwitchPeriodParam.addListener(p -> {
                autoSwitchEnabled.setValue(false);
                long ms = parseSwitchPeriod(autoSwitchPeriodParam.getString());
                if (ms > 0) {
                    ApplicationState.setWarning("WorkspaceTimer", null);
                    switchPeriodMs = ms;
                } else {
                    ApplicationState.setWarning("WorkspaceTimer", "bad period format: should be XmYs");
                }
            });

        final List<UIItemList.Item> items = new ArrayList<UIItemList.Item>();
        for (WorkspaceProject workspace : this.workspace.getAll()) {
            items.add(new WorkspaceItem(lx, workspace));
        }

        final UIItemList.ScrollList list = new UIItemList.ScrollList(ui, 0, 40, w - 8, 375);
        list.setItems(items).setSingleClickActivate(true);
        list.addToContainer(this);
    }

    private long parseSwitchPeriod(String s) {
        int split1 = s.indexOf('m');
        int split2 = s.indexOf('s');
        if (split1 < 0 || split2 < 0) {
            return -1;
        }
        String minStr = s.substring(0, split1);
        String secStr = s.substring(split1 + 1, split2);
        try {
            return 1000 * (Integer.parseInt(secStr) + 60 * Integer.parseInt(minStr));
        } catch (NumberFormatException e) {
            return -1;
        }
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
                Project currentProject = lx.getProject();
                if (currentProject == null) {
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
