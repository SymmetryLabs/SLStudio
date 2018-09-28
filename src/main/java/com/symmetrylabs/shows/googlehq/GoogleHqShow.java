package com.symmetrylabs.shows.googlehq;

import com.symmetrylabs.shows.HasWorkspace;
import com.symmetrylabs.shows.Show;
import com.symmetrylabs.slstudio.SLStudioLX;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.workspaces.Workspace;
import com.symmetrylabs.util.Marker;
import com.symmetrylabs.util.MarkerSource;
import java.util.Collection;

public class GoogleHqShow implements Show, HasWorkspace, MarkerSource {
    public static final String SHOW_NAME = "googlehq";
    private Workspace workspace;
    private GoogleHqModel model;

    @Override
    public SLModel buildModel() {
        model = GoogleHqModel.load();
        return model;
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

    @Override
    public Collection<Marker> getMarkers() {
        return model.getMarkers();
    }
}
