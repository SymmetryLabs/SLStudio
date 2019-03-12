package com.symmetrylabs.shows.tree;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.net.InetAddress;

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

	public AssignableTenereController(LX lx, TreeModel.Branch branch) throws SocketException {
		super(lx);
		this.ipAddress = branch.getConfig().ipAddress;

		int[][] packets = {
		    new int[POINTS_PER_PACKET],
		    new int[POINTS_PER_PACKET],
		    new int[POINTS_PER_PACKET],
		};

		for (int i = 0; i < packets.length; ++i) {
		    // Initialize to nothing
		    for (int j = 0; j < POINTS_PER_PACKET; j++) {
		        packets[i][j] = -1;
		    }
		}

		int twigIndex = 1;
		for (int i = 0; i < packets.length; i++) {
		    int pi = 0;
		    for (int j = 0; j < TWIGS_PER_PACKET; j++) {
		        if (twigIndex < branch.getTwigs().size()+1) {
		        	TreeModel.Twig twig = branch.getTwigByWiringIndex(twigIndex);
                    if (twig != null) {
                        for (LXPoint point : twig.points) {
                            packets[i][pi++] = point.index;
                        }
                    } else {
                        for (int k = 0; k < TreeModel.Twig.NUM_LEDS; k++) {
                            packets[i][pi++] = -1;
                        }
                    }
		        }
		        twigIndex++;
		    }
		}

		try {
			byte channel = 0;
			for (int[] packet : packets) {
		    	TenereDatagram datagram = new TenereDatagram(lx, packet, channel);
		    	datagram.setAddress(branch.getConfig().ipAddress).setPort(OPC_PORT);
		    	addDatagram(datagram);
		    	channel += TWIGS_PER_PACKET;
			}

			if (ipAddress.equals("0.0.0.0")) {
				enabled.setValue(false);
			}
		} catch (Exception e) { }
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
