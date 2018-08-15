package com.symmetrylabs.shows;

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
     * This will be called when the UI is ready.  Use this to add panels to the UI
     * and register additional 3-D objects to be rendered in the display.
     */
    default void setupUi(SLStudioLX lx, SLStudioLX.UI ui) {}

    /**
     * Returns the string identifier for this show. This should match the string
     * the show is associated with in ShowRegistry, and should also match the
     * GROUP_NAME of all patterns written for this show.
     */
    String getShowName();
}
