package com.symmetrylabs.slstudio.ui.v2;

import heronarts.lx.LX;
import heronarts.lx.LXLook;
import heronarts.lx.data.Project;
import heronarts.p3lx.ui.studio.project.UIProjectManager;


public class MasterWindow extends CloseableWindow {
    private final LX lx;
    private WepUI wepUi;
    private ParameterUI pui;
    Project project;
    String projecttext;
    public MasterWindow(LX lx) {
        super("Master", UI.WINDOW_ALWAYS_AUTO_RESIZE | UI.WINDOW_NO_RESIZE | UI.WINDOW_NO_TITLE_BAR);
        this.lx = lx;
        this.wepUi = new WepUI(lx, false, () -> UI.closePopup());
        this.pui = ParameterUI.getDefault(lx).allowMapping(true);

        lx.addProjectListener(new LX.ProjectListener() {
            public void projectChanged(Project project, LX.ProjectListener.Change change) {
                UIProjectManager.project = project;
            }
        });
    }

    @Override
    protected void windowSetup() {
        UI.setNextWindowPosition(UI.width - 20, 40, 1.0f, 0.0f);
    }



    @Override
    protected void drawContents() {
        this.project = lx.getProject();
        /* drawn without ParameterUI so that we can get custom labels */
        if (project != null) {
            UI.textWrapped(project.getName());
        }
        else
        {
            UI.text("New Project");
        }
        boolean live = UI.checkbox("LIVE", lx.engine.output.enabled.getValueb());
        if (live != lx.engine.output.enabled.getValueb()) {
            lx.engine.addTask(() -> lx.engine.output.enabled.setValue(live));
        }
        UI.sameLine();
        float level = UI.sliderFloat("##master-level", lx.engine.output.brightness.getValuef(), 0, 1);
        if (level != lx.engine.output.brightness.getValuef()) {
            lx.engine.addTask(() -> lx.engine.output.brightness.setValue(level));
        }

        LXLook look = lx.engine.getFocusedLook();
        UI.separator();
        pui.draw(look.crossfader);
        pui.draw(look.crossfaderBlendMode);
        pui.draw(look.cueA, true);
        UI.sameLine();
        pui.draw(look.cueB, true);
        pui.draw(lx.engine.speed);
        pui.draw(lx.engine.framesPerSecond);
    }
}
