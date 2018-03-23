package com.symmetrylabs.layouts.oslo;

import com.symmetrylabs.layouts.Layout;
import com.symmetrylabs.slstudio.SLStudioLX;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.output.TenereDatagram;
import heronarts.lx.LX;
import heronarts.lx.model.LXFixture;
import heronarts.lx.model.LXPoint;
import heronarts.lx.output.LXDatagramOutput;
import processing.core.PApplet;

import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.List;

import static com.symmetrylabs.util.GetIndices.getIndices;

public class OsloLayout implements Layout {
    private final PApplet applet;
    private final TreeModel.ModelMode modelMode;

    public OsloLayout(PApplet applet, TreeModel.ModelMode modelMode) {
        this.applet = applet;
        this.modelMode = modelMode;
    }

    @Override
    public SLModel buildModel() {
        return new TreeModel(applet, modelMode);
    }

    public static void addTenereOutput(LX lx, LXFixture fixture, String ip) throws SocketException, UnknownHostException {
        lx.engine.addOutput(
            new LXDatagramOutput(lx).addDatagram(
                new TenereDatagram(lx, getIndices(fixture), (byte) 0x00).setAddress(ip).setPort(1337))
                .addDatagram(
                    new TenereDatagram(lx, getIndices(fixture), (byte) 0x04).setAddress(ip).setPort(1337))
        );
    }
    public static void addTenereOutput(LX lx, int[] ch03, int[] ch47, String ip) throws SocketException, UnknownHostException {
        lx.engine.addOutput(
            new LXDatagramOutput(lx).addDatagram(
                new TenereDatagram(lx, ch03, (byte) 0x00).setAddress(ip).setPort(1337))
                .addDatagram(
                    new TenereDatagram(lx, ch47, (byte) 0x04).setAddress(ip).setPort(1337))
        );
    }

    @Override
    public void setupLx(SLStudioLX lx) {

        String[] ips = new String[]{"10.200.1.64", "10.200.1.67"};
        int ipidx = 0;
        try {
            TreeModel tree = (TreeModel) lx.model;


            for (TreeModel.Branch branch : tree.branches) {
                int pointsPerPacket = TreeModel.Branch.NUM_LEDS / 2;
                int[] channels14 = new int[pointsPerPacket];
                int[] channels58 = new int[pointsPerPacket];
                for (int i = 0; i < pointsPerPacket; ++i) {
                    // Initialize to nothing
                    channels14[i] = channels58[i] = -1;
                }


                final int[] LEAF_ORDER = {
                    0, 1, 3, 5, 2, 4, 6, 7, 8, 10, 12, 9, 11, 13, 14
                };
                for (TreeModel.LeafAssemblage assemblage : branch.assemblages) {
                    int[] buffer = (assemblage.channel < 4) ? channels14 : channels58;
                    int pi = (assemblage.channel % 4) * TreeModel.LeafAssemblage.NUM_LEDS;
                    for (int li : LEAF_ORDER) {
                        TreeModel.Leaf leaf = assemblage.leaves.get(li);
                        for (LXPoint p : leaf.points) {
                            buffer[pi++] = p.index;
                        }
                    }
                }

                addTenereOutput(lx, channels14, channels58, ips[ipidx++]);
                if (ipidx > 1){
                    break;
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

        @Override
    public void setupUi(SLStudioLX lx, SLStudioLX.UI ui) {
        ui.preview.addComponent(new UITreeGround(applet));
        UITreeStructure uiTreeStructure = new UITreeStructure((TreeModel) lx.model);
        ui.preview.addComponent(uiTreeStructure);
        UITreeLeaves uiTreeLeaves = new UITreeLeaves(lx, applet, (TreeModel) lx.model);
        ui.preview.addComponent(uiTreeLeaves);
        new UITreeControls(ui, uiTreeStructure, uiTreeLeaves).setExpanded(false).addToContainer(ui.leftPane.global);
    }
}
