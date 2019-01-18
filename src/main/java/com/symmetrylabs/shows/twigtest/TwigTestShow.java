package com.symmetrylabs.shows.twigtest;

import com.symmetrylabs.shows.Show;
import com.symmetrylabs.shows.tree.TreeModel;
import com.symmetrylabs.shows.tree.config.BranchConfig;
import com.symmetrylabs.shows.tree.config.LimbConfig;
import com.symmetrylabs.shows.tree.config.TreeConfig;
import com.symmetrylabs.shows.tree.config.TwigConfig;
import com.symmetrylabs.slstudio.SLStudioLX;
import com.symmetrylabs.slstudio.model.SLModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import heronarts.lx.model.LXPoint;
import heronarts.lx.output.LXDatagramOutput;
import heronarts.lx.output.OPCDatagram;

import static com.symmetrylabs.util.DistanceConstants.FEET;

public class TwigTestShow implements Show {
    public static final String SHOW_NAME = "twigtest";

    // female
    final TwigConfig[] BRANCH_TYPE_A = new TwigConfig[]{
        new TwigConfig( 0.0f,  37.4f, 0.0f, -32.4f, 0.0f, 0.0f, 1), // 8
        new TwigConfig(-16.3f, 36.5f, 0.0f,   6.8f, 0.0f, 0.0f, 2), // 7
        new TwigConfig(-29.8f, 25.9f, 0.0f,  28.8f, 0.0f, 0.0f, 3), // 6
        new TwigConfig(-37.4f, 10.5f, 0.0f,  28.8f, 0.0f, 0.0f, 4), // 5
        new TwigConfig( -5.7f,  9.5f, 0.0f,  14.4f, 0.0f, 0.0f, 5), // 4
        new TwigConfig( 14.4f, 19.2f, 0.0f,  18.0f, 0.0f, 0.0f, 6), // 3
        new TwigConfig( 24.5f, 16.3f, 0.0f, -36.0f, 0.0f, 0.0f, 7), // 2
        new TwigConfig( 16.3f,  0.0f, 0.0f, -61.2f, 0.0f, 0.0f, 8), // 1
    };

    @Override public SLModel buildModel() {
        TreeConfig config = new TreeConfig(new LimbConfig[] {
            // bottom
            new LimbConfig(false, 50, 13*FEET, -210.0f, -90, 0, new BranchConfig[] {
                new BranchConfig(false, "10.200.1.151", 5, 21.960005f, 21.0f, -5.7599936f, -21.6f, 0.0f, -7.2f, BRANCH_TYPE_A)
            })
        });
        return new TreeModel(config);
    }

    public void setupLx(SLStudioLX lx) {
        try {
            TreeModel tree = (TreeModel) (lx.model);
            List<TenereDatagram> datagrams = new ArrayList<>();

            for (TreeModel.Branch branch : tree.branches) {
                int pointsPerPacket = TreeModel.Twig.NUM_LEDS * 2;

                int[][] channels = {
                    new int[pointsPerPacket],
                    new int[pointsPerPacket],
                    new int[pointsPerPacket],
                    new int[pointsPerPacket]
                };

                for (int i = 0; i < channels.length; ++i) {
                    // Initialize to nothing
                    for (int j = 0; j < pointsPerPacket; j++) {
                        channels[i][j] = -1;
                    }
                }

                for (int i = 0; i < channels.length; i++) {
                    TreeModel.Twig[] twigs = new TreeModel.Twig[] {
                        branch.getTwigs().get(i * 2),
                        branch.getTwigs().get(i * 2 + 1)
                    };

                    int pi = 0;
                    for (TreeModel.Twig twig : twigs) {
                        for (LXPoint point : twig.points) {
                            channels[i][pi++] = point.index;
                        }
                    }
                }

                String ip = "10.200.1.151";
                int OPC_PORT = 1337;
                TenereDatagram datagram1 = (TenereDatagram) new TenereDatagram(lx, channels[0], (byte) 0x00).setAddress(ip).setPort(OPC_PORT);
                TenereDatagram datagram2 = (TenereDatagram) new TenereDatagram(lx, channels[1], (byte) 0x02).setAddress(ip).setPort(OPC_PORT);
                TenereDatagram datagram3 = (TenereDatagram) new TenereDatagram(lx, channels[2], (byte) 0x04).setAddress(ip).setPort(OPC_PORT);
                TenereDatagram datagram4 = (TenereDatagram) new TenereDatagram(lx, channels[3], (byte) 0x06).setAddress(ip).setPort(OPC_PORT);
                datagrams.add(datagram1);
                datagrams.add(datagram2);
                datagrams.add(datagram3);
                datagrams.add(datagram4);

                break;
            }

            // Create an LXDatagramOutput to own these packets
            LXDatagramOutput datagramOutput = new LXDatagramOutput(lx);
            for (OPCDatagram datagram : datagrams) {
                datagramOutput.addDatagram(datagram);
            }
            // Add to the output
            lx.engine.output.addChild(datagramOutput);
        } catch (IOException e) { }
    }

    public void setupUi(SLStudioLX lx, SLStudioLX.UI ui) {}
}
