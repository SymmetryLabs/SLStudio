package com.symmetrylabs.slstudio.ui.v2;

import com.symmetrylabs.slstudio.cue.Cue;
import com.symmetrylabs.slstudio.cue.CueManager;
import heronarts.lx.LX;

public class CueWindow extends  CloseableWindow {

    CueManager cueManager;
    Cue selectedCue;

    LX lx;
    private final ParameterUI pui;

    public CueWindow (LX lx, CueManager cueManager) {
        super( "Cue Window" );
        this.lx = lx;
        this.cueManager = cueManager;
        this.pui = ParameterUI.getDefault(lx);

    }

    @Override
    protected void drawContents() {
        if (UI.button("add cue")){
            this.cueManager.addCue(new Cue(lx.engine.output.brightness));
        }

        int idx = 0;
        for (Cue cue : cueManager.getCues()){
            String cueLabel = cue.startAtStr.getString() + "###" + cue.uid;
            UI.CollapseResult cr = UI.collapsibleSection(cueLabel, false);
            if (cr.isOpen) {
                pui.draw(cue.startAtStr);
                pui.draw(cue.durationMs);
                pui.draw(cue.fadeTo);
            }
        }
    }

    private void setSelected(Cue cue) {
        selectedCue = cue;
        pui.draw(selectedCue.startAtStr);
        pui.draw(selectedCue.durationMs);
        pui.draw(selectedCue.fadeTo);
    }
}
