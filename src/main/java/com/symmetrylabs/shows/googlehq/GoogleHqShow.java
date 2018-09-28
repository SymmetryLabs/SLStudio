package com.symmetrylabs.shows.googlehq;

import com.symmetrylabs.shows.HasWorkspace;
import com.symmetrylabs.shows.Show;
import com.symmetrylabs.slstudio.SLStudioLX;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.workspaces.Workspace;


public class GoogleHqShow implements Show, HasWorkspace {
    public static final String SHOW_NAME = "googlehq";
    private Workspace workspace;

    @Override
    public SLModel buildModel() {
        return new GoogleHqModel();
    }

    @Override
    public void setupLx(SLStudioLX lx) {
    }

    @Override
    public void setupUi(SLStudioLX lx, SLStudioLX.UI ui) {
        workspace = new Workspace(lx, ui, "shows/pilots");
    }

    @Override
    public Workspace getWorkspace() {
        return workspace;
    }
}
