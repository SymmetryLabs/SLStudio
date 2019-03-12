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
                new BranchConfig(false, "10.200.1.255", 5, 21.960005f, 21.0f, -5.7599936f, -21.6f, 0.0f, -7.2f, BRANCH_TYPE_A)
            })
        });
        return new TreeModel(config);
    }

    public void setupLx(SLStudioLX lx) {
        try {
            TreeModel tree = (TreeModel) (lx.model);
            List<TenereDatagram> datagrams = new ArrayList<>();

            for (TreeModel.Branch branch : tree.branches) {
                int twigsPerPacket = 3;
                int pointsPerPacket = TreeModel.Twig.NUM_LEDS * twigsPerPacket;

                int[][] packets = {
                    new int[pointsPerPacket],
                    new int[pointsPerPacket],
                    new int[pointsPerPacket],
                };

                for (int i = 0; i < packets.length; ++i) {
                    // Initialize to nothing
                    for (int j = 0; j < pointsPerPacket; j++) {
                        packets[i][j] = -1;
                    }
                }

                int twigIndex = 0;
                List<TreeModel.Twig> twigs = branch.getTwigs();

                for (int i = 0; i < packets.length; i++) {
                    int pi = 0;
                    for (int j = 0; j < twigsPerPacket; j++) {
                        if (twigIndex < twigs.size()) {
                            for (LXPoint point : twigs.get(twigIndex).points) {
                                packets[i][pi++] = point.index;
                            }
                        }
                        twigIndex++;
                    }
                }

                String ip = "10.200.1.255";
                int OPC_PORT = 1337;
                byte channel = 0;
                for (int[] packet : packets) {
                    TenereDatagram datagram = new TenereDatagram(lx, packet, channel);
                    datagram.setAddress(ip).setPort(OPC_PORT);
                    datagrams.add(datagram);
                    channel += twigsPerPacket;
                }

                break; // for testing, just do one branch for now
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
