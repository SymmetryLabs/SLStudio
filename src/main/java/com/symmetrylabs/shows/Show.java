package com.symmetrylabs.shows;

import heronarts.lx.LX;
import com.symmetrylabs.slstudio.SLStudioLX;
import com.symmetrylabs.slstudio.model.SLModel;

public interface Show {
    /**
     * Constructs the root model object.  This is called early during startup and
     * should do its work without assuming that anything has been initialized yet.
     */
    SLModel buildModel();

    /**
     * This will be called after the SLStudioLX object is created but before the
     * UI is built.  Use this to set up non-UI components such as output controllers.
      */
    default void setupLx(SLStudioLX lx) {}

    /**
     * This will be called when the UI is ready if we're using the P3LX UI. Use
     * this to add panels to the UI and register additional 3-D objects to be
     * rendered in the display.
     */
    default void setupUi(SLStudioLX lx, SLStudioLX.UI ui) {}

    /**
     * This will be called when the UI is ready for all UIs.
     */
    default void setupUi(LX lx) {}
}
