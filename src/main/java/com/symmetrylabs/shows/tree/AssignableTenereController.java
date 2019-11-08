package com.symmetrylabs.shows.tree;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.net.InetAddress;
import java.util.HashMap;

import com.symmetrylabs.slstudio.output.SLController;
import com.symmetrylabs.util.hardware.SLControllerInventory;
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
    private com.symmetrylabs.shows.treeV2.TreeModel.Branch branch_v2 = null;

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

	static HashMap<String, AssignableTenereController> allImportedIps = new HashMap<>();
	// TODO: undo copied code from above
    public AssignableTenereController(LX lx, com.symmetrylabs.shows.treeV2.TreeModel.Branch branch, SLControllerInventory slControllerInventory) throws SocketException {
        super(lx);
        this.lx = lx;
        String ipIn = slControllerInventory.getHostAddressByControllerID(branch.controllerId.getString());
        this.ipAddress = ipNotAlreadyAlocated(ipIn, this) ? ipIn : "0.0.0.0"; // only assign ip if it's hasn't been taken in
        this.branch_v2 = branch;

        packets = new int[][] {
            new int[POINTS_PER_PACKET],
            new int[POINTS_PER_PACKET],
            new int[POINTS_PER_PACKET],
        };

        if (ipAddress.equals("0.0.0.0")) {
            enabled.setValue(false);
        }
        updateIndexesFromBranch2();
    }

    private boolean ipNotAlreadyAlocated(String ipIn, AssignableTenereController branch) {
        if (allImportedIps.containsKey(ipIn)){
            System.out.println("Oops.. " + ipIn + " was already allocated to controller: " + allImportedIps.get(ipIn).branch_v2.controllerId);
            return false;
        }
        else {
            allImportedIps.put(ipIn, this);
            return true;
        }
    }

    public void updateIndexesFromBranch2() {
        for (int i = 0; i < packets.length; ++i) {
            // Initialize to nothing
            for (int j = 0; j < POINTS_PER_PACKET; j++) {
                packets[i][j] = -1;
            }
        }

        for (com.symmetrylabs.shows.treeV2.TreeModel.Twig twig : branch_v2.twigs) {
            int index = twig.index;
            int packet = index / TWIGS_PER_PACKET; // truncates to floor
            int pindex = com.symmetrylabs.shows.treeV2.TreeModel.Twig.NUM_LEDS * (index - (TWIGS_PER_PACKET * packet));
            for (int indexInTwig = 0; indexInTwig < twig.points.length; indexInTwig++) {
                boolean skip = false;
                packets[packet][pindex++] = twig.points[indexInTwig].index;
            }
        }

        clearDatagrams();
        byte channel = 0;
        for (int[] packet : packets) {
            TenereDatagram datagram = new TenereDatagram(lx, packet, channel);
            try {
                datagram.setAddress(this.ipAddress).setPort(OPC_PORT);
                addDatagram(datagram);
            } catch (IOException e) {
                e.printStackTrace();
            }
            channel += TWIGS_PER_PACKET;
        }
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
            for (int indexInTwig = 0; indexInTwig < twig.points.length; indexInTwig++) {
                boolean skip = false;
                int[] disabled = twig.getConfig().disabledPixels;
                if (disabled != null) {
                    for (int d = 0; d < disabled.length; d++) {
                        if (disabled[d] == indexInTwig) {
                            skip = true;
                        }
                    }
                }
                packets[packet][pindex++] = skip ? -1 : twig.points[indexInTwig].index;
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
			for (LXDatagram datagram : getDatagrams()) {
				datagram.setAddress(ipAddress);
			}
		} catch (Exception e) { }
	}

	public String getIpAddress() {
		return ipAddress;
	}
}
