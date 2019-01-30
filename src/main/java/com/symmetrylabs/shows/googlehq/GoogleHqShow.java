package com.symmetrylabs.shows.googlehq;

import com.symmetrylabs.shows.HasWorkspace;
import com.symmetrylabs.shows.Show;
import com.symmetrylabs.slstudio.SLStudioLX;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.output.PointsGrouping;
import com.symmetrylabs.slstudio.output.SimplePixlite;
import com.symmetrylabs.slstudio.workspaces.Workspace;
import com.symmetrylabs.util.Marker;
import com.symmetrylabs.util.MarkerSource;

import java.util.Collection;
import java.util.Arrays;
import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;

public class GoogleHqShow implements Show, MarkerSource {
    public static final String SHOW_NAME = "googlehq";
    private Workspace workspace;
    private GoogleHqModel model;

    @Override
    public SLModel buildModel() {
        model = GoogleHqModel.load();
        return model;
    }

    @Override
    public void setupLx(LX lx) {
        lx.addOutput(
            new SimplePixlite(lx, "10.200.1.16")
            .addPixliteOutput(
                new PointsGrouping("1")
                .addPoints(Arrays.copyOfRange(model.points, 348, 548), PointsGrouping.REVERSE_ORDERING))
            .addPixliteOutput(
                new PointsGrouping("2")
                .addPoints(Arrays.copyOfRange(model.points, 0, 349), PointsGrouping.REVERSE_ORDERING)
                .addPoints(Arrays.copyOfRange(model.points, 549, 799), PointsGrouping.REVERSE_ORDERING))
            );
    }

    @Override
    public void setupUi(SLStudioLX lx, SLStudioLX.UI ui) {
    }

    @Override
    public Collection<Marker> getMarkers() {
        return model.getMarkers();
    }
}
