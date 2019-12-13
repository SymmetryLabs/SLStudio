package com.symmetrylabs.slstudio.ui.v2;

import com.symmetrylabs.slstudio.cue.BlackoutProcedureCue;
import com.symmetrylabs.slstudio.cue.Cue;
import com.symmetrylabs.slstudio.cue.CueManager;
import com.symmetrylabs.slstudio.cue.TriggerVezerCue;
import heronarts.lx.LX;
import heronarts.lx.parameter.BoundedParameter;

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
        if (UI.button("vezer cue")){
            this.cueManager.addCue(new TriggerVezerCue(lx, new BoundedParameter("null")));
        }
        if (UI.button("blackout cue")){
            this.cueManager.addCue(new BlackoutProcedureCue(lx, new BoundedParameter("null")));
        }
        UI.sameLine();
        if (UI.button("level cue")){
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

            if (cue instanceof TriggerVezerCue) {
                UI.pushColor(UI.COLOR_HEADER, UIConstants.RED);
                UI.pushColor(UI.COLOR_HEADER_ACTIVE, UIConstants.RED);
                UI.pushColor(UI.COLOR_HEADER_HOVERED, UIConstants.RED_HOVER);
            } else if (cue instanceof BlackoutProcedureCue) {
                UI.pushColor(UI.COLOR_HEADER, UIConstants.PURPLE);
                UI.pushColor(UI.COLOR_HEADER_ACTIVE, UIConstants.PURPLE);
                UI.pushColor(UI.COLOR_HEADER_HOVERED, UIConstants.PURPLE_HOVER);
            }
            else {
                UI.pushColor(UI.COLOR_HEADER, 0xff665566);
                UI.pushColor(UI.COLOR_HEADER_ACTIVE, 0xff887788);
                UI.pushColor(UI.COLOR_HEADER_HOVERED, 0xff887788);
            }


            UI.CollapseResult cr = UI.collapsibleSection(cueLabel, false);
            UI.popColor(3);

            if (cr.isOpen) {
                UI.textWrapped(cue.toString());
                pui.draw(cue.startAtStr);
                if (cue instanceof TriggerVezerCue){
                    pui.draw( ((TriggerVezerCue) cue).showName );
                    if (UI.button("remove" + "##" + cue.uid)){
                        this.cueManager.removeCue(cue);
                    }
                    continue;
                }
                if (cue instanceof BlackoutProcedureCue){
                    pui.draw(((BlackoutProcedureCue) cue).blackoutThreshhold);
                    pui.draw(((BlackoutProcedureCue) cue).delayBeforeCuttoff);
                    if (UI.button("trigger blackout")){
                        ((BlackoutProcedureCue) cue).execute();
                    }
                    if (UI.button("remove" + "##" + cue.uid)){
                        this.cueManager.removeCue(cue);
                    }
                    continue;
                }
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