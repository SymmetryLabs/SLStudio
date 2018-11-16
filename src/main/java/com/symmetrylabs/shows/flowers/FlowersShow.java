package com.symmetrylabs.shows.flowers;

import com.symmetrylabs.shows.Show;
import com.symmetrylabs.slstudio.SLStudioLX;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.output.SimplePixlite;
import com.symmetrylabs.slstudio.output.PointsGrouping;
import heronarts.lx.LX;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.HashSet;
import com.symmetrylabs.slstudio.output.AssignablePixlite;

public class FlowersShow implements Show {
    public static final String SHOW_NAME = "flowers";

    private final HashMap<Integer, AssignablePixlite> pixlites = new HashMap<>();
    private static final String PIXLITE_IP_FORMAT = "10.200.1.%d";

    @Override
    public SLModel buildModel() {
        return FlowersModelLoader.load();
    }

    @Override
    public void setupLx(SLStudioLX lx) {
        updatePixlites(lx, (FlowersModel) lx.model);
    }

    private void updatePixlites(SLStudioLX lx, FlowersModel model) {
        HashSet<Integer> allPixliteIds = new HashSet<>();
        for (FlowerModel fm : model.getFlowers()) {
            FlowerRecord fr = fm.getFlowerData().record;
            allPixliteIds.add(fr.pixliteId);
        }

        HashSet<Integer> toRemove = new HashSet<>(pixlites.keySet());
        toRemove.removeAll(allPixliteIds);
        HashSet<Integer> toAdd = new HashSet<>(allPixliteIds);
        toAdd.removeAll(pixlites.keySet());

        for (Integer id : toRemove) {
            lx.removeOutput(pixlites.get(id));
            pixlites.remove(id);
        }
        for (Integer id : toAdd) {
            String ip = String.format(PIXLITE_IP_FORMAT, id);
            AssignablePixlite pixlite = new AssignablePixlite(lx, ip);
        }
    }

    @Override
    public void setupUi(SLStudioLX lx, SLStudioLX.UI ui) {
        UIFlowerTool.attach(lx, ui);
    }
}
