package com.symmetrylabs.slstudio.ui;

import com.symmetrylabs.util.CaptionSource;

import java.util.HashSet;
import java.util.Set;

import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI3dContext;

public class UICaptionText extends UITextOverlay {
    Set<CaptionSource> sources = new HashSet<>();

    public UICaptionText(UI ui, UI3dContext parent, int anchorX, int anchorY, int alignX, int alignY) {
        super(ui, parent, anchorX, anchorY, alignX, alignY);
        redrawEveryFrame = true;
    }

    public synchronized void addSource(CaptionSource source) {
        sources.add(source);
    }

    public synchronized void removeSource(CaptionSource source) {
        sources.remove(source);
    }

    public String getText() {
        String result = "";
        for (CaptionSource source : sources) {
            String caption = source.getCaption();
            if (caption != null && !caption.isEmpty()) {
                result += source.getClass().getSimpleName() + " - " + caption + "\n";
            }
        }
        return result.trim();
    }
}
