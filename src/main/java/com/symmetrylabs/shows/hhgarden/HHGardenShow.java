package com.symmetrylabs.shows.hhgarden;

import com.symmetrylabs.shows.Show;
import com.symmetrylabs.slstudio.SLStudioLX;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.output.SimplePixlite;
import com.symmetrylabs.slstudio.output.PointsGrouping;
import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import com.symmetrylabs.slstudio.output.AssignablePixlite;
import com.symmetrylabs.slstudio.output.AssignablePixlite.Dataline;

public class HHGardenShow implements Show, UIFlowerTool.Listener {
    public static final String SHOW_NAME = "hhgarden";

    private final HashMap<Integer, AssignablePixlite> pixlites = new HashMap<>();
    static final String PIXLITE_IP_FORMAT = "10.200.1.%d";
    private SLStudioLX lx;

    @Override
    public SLModel buildModel() {
        return FlowersModelLoader.load();
    }

    @Override
    public void setupLx(SLStudioLX lx) {
        this.lx = lx;
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
            if (id == FlowerRecord.UNKNOWN_PIXLITE_ID) {
                continue;
            }
            String ip = String.format(PIXLITE_IP_FORMAT, id);
            System.out.println(String.format("adding new pixlite %s", ip));
            // 4 datalines, 90 (9 flowers, 10 LEDs/flower) points per dataline
            AssignablePixlite pixlite = new AssignablePixlite(lx, ip, 4, 90);
            pixlites.put(id, pixlite);
            lx.addOutput(pixlite);
        }

        for (Integer pixliteId : pixlites.keySet()) {
            AssignablePixlite pixlite = pixlites.get(pixliteId);
            Map<Integer, List<FlowerModel>> harnesses = model.getPixliteHarnesses(pixliteId);
            if (harnesses == null) {
                continue;
            }
            for (Integer harness : harnesses.keySet()) {
                Dataline dataline = pixlite.get(harness);
                int[] indexes = new int[9 * 10]; // 9 flowers with 10 points per flower
                int nextIndex = 0;
                for (FlowerModel fm : harnesses.get(harness)) {
                    if (fm == null) {
                        for (int i = 0; i < 10; i++) {
                            indexes[nextIndex++] = -1;
                        }
                    } else {
                        for (LXPoint p : fm.points) {
                            indexes[nextIndex++] = p.index;
                        }
                    }
                }
                dataline.setIndices(indexes);
            }
        }
    }

    @Override
    public void setupUi(SLStudioLX lx, SLStudioLX.UI ui) {
        UIFlowerTool.attach(lx, ui).addListener(this);
    }

    @Override
    public void onFlowerSelected(FlowerData d) {}

    @Override
    public void onPixliteSelected(int pixliteId) {}

    @Override
    public void onUpdate() {
        updatePixlites(lx, (FlowersModel) lx.model);
    }
}
