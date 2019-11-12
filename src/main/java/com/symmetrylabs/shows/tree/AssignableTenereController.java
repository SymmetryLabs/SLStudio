package com.symmetrylabs.shows.tree;

import java.io.IOException;
import java.net.SocketException;
import java.util.HashMap;

import com.symmetrylabs.color.PerceptualColorScale;
import com.symmetrylabs.slstudio.network.NetworkDevice;
import com.symmetrylabs.slstudio.output.AbstractSLControllerBase;
import com.symmetrylabs.slstudio.output.symmetree.TenereDatagramSet;
import com.symmetrylabs.util.hardware.SLControllerInventory;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.StringParameter;

import heronarts.lx.LX;
import heronarts.lx.output.LXDatagram;

import com.symmetrylabs.slstudio.output.TenereDatagram;


public class AssignableTenereController extends AbstractSLControllerBase {

	private static final int TWIGS_PER_PACKET = 3;
	private static final int POINTS_PER_PACKET = TreeModel.Twig.NUM_LEDS * TWIGS_PER_PACKET;
	private static final int OPC_PORT = 1337;
    private com.symmetrylabs.shows.treeV2.TreeModel.Branch branch_v2 = null;

    private String ipAddress;
    private TreeModel.Branch branch;
    private final LX lx;
    private final int[][] pixelColors;

	public AssignableTenereController(LX lx, TreeModel.Branch branch) throws SocketException {
		super(lx, branch.getConfig().ipAddress);
        this.lx = lx;
		this.ipAddress = branch.getConfig().ipAddress;
        this.branch = branch;

		pixelColors = new int[][] {
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

	private static HashMap<String, AssignableTenereController> allImportedIps = new HashMap<>();
	// TODO: undo copied code from above
    public AssignableTenereController(LX lx, com.symmetrylabs.shows.treeV2.TreeModel.Branch branch, SLControllerInventory slControllerInventory) throws SocketException {
        super(lx, slControllerInventory.getHostAddressByControllerID(branch.controllerId.getString()));
        this.lx = lx;
        String ipIn = slControllerInventory.getHostAddressByControllerID(branch.controllerId.getString());
        this.ipAddress = ipNotAlreadyAllocated(ipIn, this) ? ipIn : "0.0.0.0"; // only assign ip if it's hasn't been taken in
        this.branch_v2 = branch;

        pixelColors = new int[][] {
            new int[POINTS_PER_PACKET],
            new int[POINTS_PER_PACKET],
            new int[POINTS_PER_PACKET],
        };

        if (ipAddress.equals("0.0.0.0")) {
            enabled.setValue(false);
        }
        updateIndexesFromBranch2();
    }

    public AssignableTenereController(LX lx, NetworkDevice device, SLControllerInventory controllerInventory) throws SocketException {
        super(lx, device, controllerInventory, new PerceptualColorScale(new double[] { 2.0, 2.1, 2.8 }, 1.0) );
        this.lx = lx;
        this.ipAddress = device.ipAddress.getHostAddress();
        this.branch = branch;

        pixelColors = new int[][] {
            new int[POINTS_PER_PACKET],
            new int[POINTS_PER_PACKET],
            new int[POINTS_PER_PACKET],
        };

        if (ipAddress.equals("0.0.0.0")) {
            enabled.setValue(false);
        }
    }

    private boolean ipNotAlreadyAllocated(String ipIn, AssignableTenereController branch) {
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
        for (int i = 0; i < pixelColors.length; ++i) {
            // Initialize to nothing
            for (int j = 0; j < POINTS_PER_PACKET; j++) {
                pixelColors[i][j] = -1;
            }
        }

        for (com.symmetrylabs.shows.treeV2.TreeModel.Twig twig : branch_v2.twigs) {
            int index = twig.index;
            int packet = index / TWIGS_PER_PACKET; // truncates to floor
            int pindex = com.symmetrylabs.shows.treeV2.TreeModel.Twig.NUM_LEDS * (index - (TWIGS_PER_PACKET * packet));
            for (int indexInTwig = 0; indexInTwig < twig.points.length; indexInTwig++) {
                boolean skip = false;
                pixelColors[packet][pindex++] = twig.points[indexInTwig].index;
            }
        }

        clearDatagrams();
        byte channel = 0;
        for (int[] packet : pixelColors) {
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
		for (int i = 0; i < pixelColors.length; ++i) {
		    // Initialize to nothing
		    for (int j = 0; j < POINTS_PER_PACKET; j++) {
		        pixelColors[i][j] = -1;
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
                pixelColors[packet][pindex++] = skip ? -1 : twig.points[indexInTwig].index;
            }
        }

        clearDatagrams();
        byte channel = 0;
        for (int[] packet : pixelColors) {
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
		} catch (Exception e) { System.out.println("could not allocate host");}
	}

	public String getIpAddress() {
		return ipAddress;
	}

    @Override
    protected void initPacketData(int numPixels, boolean use16) {
    }

    @Override
    protected void setPixel8(int pixelIndex, int c) {
        // to which packet does this pixel belong
        int[] pixelColorPacket = pixelColors[pixelIndex/POINTS_PER_PACKET];

        pixelColorPacket[pixelIndex%POINTS_PER_PACKET] = c;
    }

    @Override
    protected void setNumPixels() {
        this.numPixels = TWIGS_PER_PACKET*150*3;
    }

    @Override
    protected void fillDatagramsAndAddToOutput() {
        clearDatagrams();
        addDatagrams(new TenereDatagramSet(lx, pixelColors, networkDevice).getDatagrams());
    }
}
