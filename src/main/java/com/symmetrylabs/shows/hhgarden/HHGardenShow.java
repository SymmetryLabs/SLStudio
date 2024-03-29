package com.symmetrylabs.shows.hhgarden;

import com.symmetrylabs.shows.HasWorkspace;
import com.symmetrylabs.shows.Show;
import com.symmetrylabs.slstudio.SLStudioLX;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.network.NetworkMonitor;
import com.symmetrylabs.slstudio.output.ArtNetDmxDatagram;
import com.symmetrylabs.slstudio.output.AssignablePixlite.Dataline;
import com.symmetrylabs.slstudio.output.AssignablePixlite;
import com.symmetrylabs.slstudio.output.PointsGrouping;
import com.symmetrylabs.slstudio.output.SimplePixlite;
import com.symmetrylabs.slstudio.workspaces.Workspace;
import heronarts.lx.LX;
import heronarts.lx.LXChannel;
import heronarts.lx.LXEngine;
import heronarts.lx.LXLook;
import heronarts.lx.model.LXPoint;
import heronarts.p3lx.ui.UI3dContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class HHGardenShow implements Show, HasWorkspace, UIFlowerTool.Listener, LXLook.Listener {
    public static final String SHOW_NAME = "hhgarden";
    private static final boolean DEBUG_UNMAPPED = false;

    private final HashMap<Integer, AssignablePixlite> pixlites = new HashMap<>();
    static final String PIXLITE_IP_FORMAT = "10.200.1.%d";
    private LX lx;
    private Workspace workspace;

    @Override
    public SLModel buildModel() {
        return FlowersModelLoader.load(SHOW_NAME);
    }

    @Override
    public void setupLx(LX lx) {
        this.lx = lx;
        NetworkMonitor.getInstance(lx).start();

        updatePixlites(lx, (FlowersModel) lx.model);
        lx.engine.getFocusedLook().addListener(this);
        for (LXChannel c : lx.engine.getAllSubChannels()) {
            c.autoDisable.setValue(true);
        }
    }

    private void updatePixlites(LX lx, FlowersModel model) {
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
                // 9 flowers with 10 points per flower plus 30 for the "send extra pixels to
                // reduce flashing" hack
                int[] indexes = new int[9 * 10 + 30];
                Arrays.fill(indexes, -1);
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
                if (DEBUG_UNMAPPED) {
                    for (ArtNetDmxDatagram andg : dataline.getArtNetDmxDatagrams()) {
                        andg.setUnmappedPointColor(0xFF0000, true);
                    }
                }
            }
        }
    }

    @Override
    public void setupUi(SLStudioLX lx, SLStudioLX.UI ui) {
        UIFlowerTool.attach(lx, ui).addListener(this);
        ui.preview.setInteractionMode(UI3dContext.InteractionMode.ZOOM_Z_UP);
        workspace = new Workspace(lx, ui, "shows/hhgarden");
    }

    @Override
    public void onFlowerSelected(FlowerData d) {}

    @Override
    public void onPixliteSelected(int pixliteId) {}

    @Override
    public void onUpdate() {
        updatePixlites(lx, (FlowersModel) lx.model);
    }

    @Override
    public Workspace getWorkspace() {
        return workspace;
    }

    @Override
    public void channelAdded(LXLook look, LXChannel lxChannel) {
        lxChannel.autoDisable.setValue(true);
    }

    @Override
    public void channelRemoved(LXLook look, LXChannel lxChannel) {
    }

    @Override
    public void channelMoved(LXLook look, LXChannel lxChannel) {
    }
}
