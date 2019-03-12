package com.symmetrylabs.shows.tree;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.net.InetAddress;
import heronarts.lx.parameter.StringParameter;

import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.output.LXDatagram;
import heronarts.lx.output.LXDatagramOutput;

import com.symmetrylabs.slstudio.output.TenereDatagram;
import com.symmetrylabs.shows.tree.TreeModel;


public class AssignableTenereController extends LXDatagramOutput {

	private static final int TWIGS_PER_PACKET = 3;
	private static final int POINTS_PER_PACKET = TreeModel.Twig.NUM_LEDS * TWIGS_PER_PACKET;
	private static final int OPC_PORT = 1337;

	private String ipAddress;
    private TreeModel.Branch branch;
    private final LX lx;
    private final int[][] packets;

	public AssignableTenereController(LX lx, TreeModel.Branch branch) throws SocketException {
		super(lx);
        this.lx = lx;
		this.ipAddress = branch.getConfig().ipAddress;
        this.branch = branch;

		packets = new int[][] {
		    new int[POINTS_PER_PACKET],
		    new int[POINTS_PER_PACKET],
		    new int[POINTS_PER_PACKET],
		};

        if (ipAddress.equals("0.0.0.0")) {
            enabled.setValue(false);
        }
        updateIndexesFromBranch();

        final TreeModelingTool tmt = TreeModelingTool.getInstance(lx);
        tmt.twigManipulator.index.addListener(parameter -> {
                String ip = tmt.branchManipulator.ipAddress.getString();
                if (this.ipAddress.equals(ip)) {
                    updateIndexesFromBranch();
                }
            });
        tmt.branchManipulator.ipAddress.addListener(parameter -> {
                String ip = ((StringParameter)parameter).getString();
                if (this.ipAddress.equals(ip)) {
                    this.branch = TreeModelingTool.getInstance(lx).getSelectedBranch();
                    updateIndexesFromBranch();
                }
            });
	}

    public void updateIndexesFromBranch() {
		for (int i = 0; i < packets.length; ++i) {
		    // Initialize to nothing
		    for (int j = 0; j < POINTS_PER_PACKET; j++) {
		        packets[i][j] = -1;
		    }
		}

        for (TreeModel.Twig twig : branch.getTwigs()) {
            int index = twig.index;
            int packet = index / TWIGS_PER_PACKET; // truncates to floor
            int pindex = TreeModel.Twig.NUM_LEDS * (index - (TWIGS_PER_PACKET * packet));
            for (LXPoint point : twig.points) {
                packets[packet][pindex++] = point.index;
            }
        }

        clearDatagrams();
        byte channel = 0;
        for (int[] packet : packets) {
            TenereDatagram datagram = new TenereDatagram(lx, packet, channel);
            try {
                datagram.setAddress(branch.getConfig().ipAddress).setPort(OPC_PORT);
                addDatagram(datagram);
            } catch (IOException e) {
                e.printStackTrace();
            }
            channel += TWIGS_PER_PACKET;
        }
    }

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;

		try {
			if (!InetAddress.getByName(ipAddress).isReachable(200)) {
				enabled.setValue(false);
			}

			for (LXDatagram datagram : getDatagrams()) {
				datagram.setAddress(ipAddress);
			}
		} catch (Exception e) { }
	}

	public String getIpAddress() {
		return ipAddress;
	}
}
