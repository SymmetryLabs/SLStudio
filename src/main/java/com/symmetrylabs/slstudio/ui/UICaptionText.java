package com.symmetrylabs.slstudio.ui;

import com.symmetrylabs.util.CaptionSource;
import java.util.WeakHashMap;

import java.util.HashSet;
import java.util.Set;

import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI3dContext;

public class UICaptionText extends UITextOverlay {
    WeakHashMap<CaptionSource, Object> sources = new WeakHashMap<>();
    static final String SPACES = "                                                   ";

    public UICaptionText(UI ui, UI3dContext parent, int anchorX, int anchorY, int alignX, int alignY) {
        super(ui, parent, anchorX, anchorY, alignX, alignY);
        redrawEveryFrame = true;
    }

    public synchronized void addSource(CaptionSource source) {
        sources.put(source, new Object());
    }

    public synchronized void removeSource(CaptionSource source) {
        sources.remove(source);
    }

    public synchronized String getText() {
        String result = "";
        for (CaptionSource source : sources.keySet()) {
            String caption = source.getCaption();
            if (caption != null && !caption.isEmpty()) {
                String prefix = source.getClass().getSimpleName() + " - ";
                result = result.trim() + "\n" + prefix +
                      caption.replace("\n", "\n" + SPACES.substring(0, prefix.length()));
            }
        }
        return result.trim();
    }
}
