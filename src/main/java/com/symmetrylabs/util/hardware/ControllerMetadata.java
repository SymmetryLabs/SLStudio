package com.symmetrylabs.util.hardware;

import com.google.gson.annotations.Expose;
import com.symmetrylabs.slstudio.network.NetworkDevice;
import com.symmetrylabs.slstudio.output.DiscoverableController;
import com.symmetrylabs.slstudio.output.SLController;
import com.symmetrylabs.util.NetworkUtil.MACAddress;
import org.jetbrains.annotations.NotNull;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;

public class ControllerMetadata implements Comparable<ControllerMetadata>{
    @Expose
    NetworkDevice networkDevice;

    @Expose
    String allocatedToShow; // where is this controller currently?

    @Expose
    Inet4Address ipAddr;

    MACAddress macAddr;
    @Expose
    String macAddrString;

    @Expose
    String humanID;

    @Expose
    String statusNotes;

    @Expose
    int switchPort = -1;

    public ControllerMetadata(String[] chunkArr) {
        if (chunkArr.length > 4|| chunkArr.length < 3) {
            throw new IllegalStateException("Chunk malformed, incorrect number data elts.");
        }
        try {
            ipAddr = (Inet4Address) InetAddress.getByName(chunkArr[0]);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        macAddr= MACAddress.valueOf(chunkArr[1]);
        macAddrString = macAddr.toString();
        humanID = chunkArr[2];
        statusNotes = chunkArr[3] == null ? "null" : chunkArr[3];
    }

    public ControllerMetadata(DiscoverableController slc) {
            ipAddr = (Inet4Address) slc.networkDevice.ipAddress;

        macAddrString = MACAddress.valueOf(slc.networkDevice.deviceId).toString();
        humanID = slc.humanID;
        statusNotes = slc.notes;
        switchPort = slc.switchPortNumber;
    }

    public ControllerMetadata() {
    }

    public ControllerMetadata(String hID, NetworkDevice networkDevice) {
        this.networkDevice = networkDevice;
        this.humanID = hID;
    }

    public String getHumanID() {
        return humanID;
    }

    public String getHostAddress() { return ipAddr.getHostAddress(); }

    public String getMacAddr() {
        return macAddrString;
    }

    @Override
    public int compareTo(@NotNull ControllerMetadata o) {
        return humanID.compareTo(o.humanID);
    }
}
