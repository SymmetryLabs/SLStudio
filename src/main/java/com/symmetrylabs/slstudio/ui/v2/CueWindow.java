package com.symmetrylabs.slstudio.ui.v2;

import com.symmetrylabs.slstudio.cue.Cue;
import com.symmetrylabs.slstudio.cue.CueManager;
import heronarts.lx.LX;

public class CueWindow extends CloseableWindow {

    CueManager cueManager;
    Cue selectedCue;

    LX lx;
    private final ParameterUI pui;

    public CueWindow (LX lx, CueManager cueManager) {
        super( "Cue Window" );
        this.lx = lx;
        this.cueManager = cueManager;
        this.pui = ParameterUI.getDefault(lx);
        this.cueManager.loadFromCueFile();
    }

    @Override
    protected void drawContents() {
        if (UI.button("add cue")){
            this.cueManager.addCue(new Cue(lx.engine.output.brightness));
        }
        UI.sameLine();
        if (UI.button("save cues")){
            this.cueManager.onSave();
        }
        UI.separator();

        int idx = 0;
        for (Cue cue : cueManager.getCues()){
            String cueLabel = cue.startAtStr.getString() + "###" + cue.uid;
            UI.CollapseResult cr = UI.collapsibleSection(cueLabel, false);
            if (cr.isOpen) {
                UI.textWrapped(cue.toString());
                pui.draw(cue.startAtStr);
                pui.draw(cue.durationSec);
                pui.draw(cue.fadeTo);
                if (UI.button("remove" + "##" + cue.uid)){
                    this.cueManager.removeCue(cue);
                }
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
