package com.symmetrylabs.slstudio.cue;

import com.symmetrylabs.slstudio.SLStudio;
import com.symmetrylabs.slstudio.SLStudioLX;
import heronarts.lx.LX;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.component.UIItemList;
import heronarts.p3lx.ui.studio.UICollapsibleSection;
import java.util.ArrayList;
import java.util.List;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.p3lx.ui.component.UIButton;
import heronarts.p3lx.ui.component.UIKnob;
import heronarts.p3lx.ui.component.UILabel;
import heronarts.p3lx.ui.component.UITextBox;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.lx.parameter.LXParameter;
import processing.core.PConstants;


public class UICuePanel extends UICollapsibleSection implements CueManager.CueListListener, LXParameterListener {
    private static final float HEIGHT = 380;
    private final CueManager mgr;
    private final LX lx;
    private final UIItemList.ScrollList cueList;
    private Cue selectedCue = null;

    private UITextBox timeInput;
    private UIKnob duration;
    private UIKnob fadeTo;

    public UICuePanel(LX lx, UI ui, float px, float py, float w) {
        super(ui, px, py, w, HEIGHT);
        this.lx = lx;
        mgr = SLStudio.applet.cueManager;
        mgr.addCueListListener(this);

        setTitle("CUES");

        float y = 0;
        final BooleanParameter addP = new BooleanParameter("add").setMode(BooleanParameter.Mode.MOMENTARY);
        addP.addListener(p -> { if (addP.getValueb()) addCue(); });
        new UIButton(0, 0, getContentWidth(), 20)
            .setLabel("Add new cue")
            .setParameter(addP)
            .addToContainer(this);
        y += 22;

        float listHeight = HEIGHT - 180;
        cueList = new UIItemList.ScrollList(ui, 0, y, getContentWidth(), listHeight);
        cueList.setSingleClickActivate(true);
        cueList.addToContainer(this);
        y += listHeight + 10;

        new UILabel(0, y, getContentWidth(), 20).setLabel("CUE INFO").addToContainer(this);
        y += 25;

        new UILabel(0, y, getContentWidth() / 3, 20)
            .setLabel("Run at:")
            .setTextAlignment(PConstants.LEFT, PConstants.CENTER)
            .addToContainer(this);
        timeInput = new UITextBox(getContentWidth() / 3, y, 2 * getContentWidth() / 3, 20);
        timeInput.addToContainer(this);
        y += 25;

        duration = new UIKnob(getContentWidth() - 2 * (UIKnob.WIDTH + 5), y);
        duration.addToContainer(this);
        fadeTo = new UIKnob(getContentWidth() - UIKnob.WIDTH - 5, y);
        fadeTo.addToContainer(this);
        y += UIKnob.HEIGHT + 5;

        final BooleanParameter removeP = new BooleanParameter("remove").setMode(BooleanParameter.Mode.MOMENTARY);
        removeP.addListener(p -> {
                if (removeP.getValueb() && selectedCue != null) {
                    mgr.removeCue(selectedCue);
                }
            });
        new UIButton(getContentWidth() / 3, y, 2 * getContentWidth() / 3, 20)
            .setLabel("Remove")
            .setParameter(removeP)
            .addToContainer(this);

        cueListChanged();
    }

    @Override
    public void cueListChanged() {
        System.out.println("cue list changed");
        cueList.setFocusIndex(-1);
        setSelected(null);
        List<CueItem> items = new ArrayList<>();
        for (Cue c : mgr.getCues()) {
            items.add(new CueItem(c));
        }
        cueList.setItems(items);
        redraw();
    }

    private void addCue() {
        /* this will end up calling cueListChanged, which will trigger the redraw */
        mgr.addCue(new Cue(lx, lx.engine.output.brightness));
    }

    private void setSelected(Cue cue) {
        if (selectedCue != null) {
            selectedCue.startAtStr.removeListener(UICuePanel.this);
            selectedCue.durationMs.removeListener(UICuePanel.this);
            selectedCue.fadeTo.removeListener(UICuePanel.this);
        }

        selectedCue = cue;
        if (selectedCue == null) {
            timeInput.setParameter(null);
            duration.setParameter(null);
            fadeTo.setParameter(null);
        } else {
            timeInput.setParameter(selectedCue.startAtStr);
            duration.setParameter(selectedCue.durationMs);
            fadeTo.setParameter(selectedCue.fadeTo);

            cue.startAtStr.addListener(UICuePanel.this);
            cue.durationMs.addListener(UICuePanel.this);
            cue.fadeTo.addListener(UICuePanel.this);
        }
    }

    @Override
    public void onParameterChanged(LXParameter parameter) {
        redraw();
    }

    private class CueItem extends UIItemList.AbstractItem {
        final Cue cue;

        CueItem(Cue cue) {
            this.cue = cue;
        }

        @Override
        public void onActivate() {
            setSelected(cue);
        }

        public String getLabel() {
            return String.format("%s to %.0f%% @ %02d:%02d",
                                 cue.cuedParameter.getLabel(),
                                 100 * cue.fadeTo.getValue(),
                                 cue.getStartTime().getHourOfDay(),
                                 cue.getStartTime().getMinuteOfHour());
        }
    }
}
