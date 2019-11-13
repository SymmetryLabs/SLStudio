//package com.symmetrylabs.util.hardware;
//
//import com.google.gson.annotations.Expose;
//import com.symmetrylabs.slstudio.output.SLController;
//import com.symmetrylabs.util.NetworkUtil.MACAddress;
//
//import java.net.Inet4Address;
//import java.net.InetAddress;
//import java.net.UnknownHostException;
//
//public class ControllerMetadata{
//    @Expose
//    Inet4Address ipAddr;
//
//    MACAddress macAddress;
//    @Expose
//    String macAddrString;
//
//    @Expose
//    String humanID;
//
//    @Expose
//    String statusNotes;
//
//    @Expose
//    int switchPort = -1;
//
//    public ControllerMetadata(String[] chunkArr) {
//        if (chunkArr.length > 4|| chunkArr.length < 3) {
//            throw new IllegalStateException("Chunk malformed, incorrect number data elts.");
//        }
//        try {
//            ipAddr = (Inet4Address) InetAddress.getByName(chunkArr[0]);
//        } catch (UnknownHostException e) {
//            e.printStackTrace();
//        }
//        macAddress = MACAddress.valueOf(chunkArr[1]);
//        macAddrString = macAddress.toString();
//        humanID = chunkArr[2];
//        statusNotes = chunkArr[3] == null ? "null" : chunkArr[3];
//    }
//
//    public ControllerMetadata(SLController slc) {
//            ipAddr = (Inet4Address) slc.networkDevice.ipAddress;
//
//        macAddrString = MACAddress.valueOf(slc.networkDevice.deviceId).toString();
//        humanID = slc.humanID;
//        statusNotes = slc.notes;
//        switchPort = slc.switchPortNumber;
//    }
//
//    public String getHumanID() {
//        return humanID;
//    }
//
//    public String getHostAddress() { return ipAddr.getHostAddress(); }
//}
