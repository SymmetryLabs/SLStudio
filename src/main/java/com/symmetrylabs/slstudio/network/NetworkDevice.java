package com.symmetrylabs.slstudio.network;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.annotations.Expose;
import com.symmetrylabs.util.listenable.ListenableInt;

public class NetworkDevice {
    @Expose
    public final InetAddress ipAddress;
    @Expose
    public final String productId;
    @Expose
    public final String versionId;
    @Expose
    public final String deviceId;
    @Expose
    public final Set<String> featureIds = new HashSet<String>();

    @Deprecated // Remove this field after all controllers are updated to Aura.
    public final ListenableInt version = new ListenableInt(-1);

    protected final static Pattern PRODUCT_VERSION = Pattern.compile("^(\\w+)/([\\w+]+)");
    protected final static Pattern FEATURE_LIST = Pattern.compile("\\(([\\w,]+)\\)");
    protected final static Pattern DEVICE_ID = Pattern.compile("\\[(\\w+)\\]");
    protected final static Pattern DEVICE_ID_LONG_MAC = Pattern.compile("\\[(\\w+::\\w+::\\w+::\\w+::\\w+::\\w+)\\]");

    public NetworkDevice(InetAddress ipAddress, String productId, String versionId, String deviceId, String[] featureIds) {
        this.ipAddress = ipAddress;
        this.productId = productId == null ? "" : productId;
        this.versionId = versionId == null ? "" : versionId;
        this.deviceId = deviceId == null ? "" : deviceId;
        this.featureIds.addAll(Arrays.asList(featureIds));
    }

    public boolean equals(Object object) {
        if (object instanceof NetworkDevice) {
            NetworkDevice other = (NetworkDevice) object;
            return ipAddress.equals(other.ipAddress) &&
                productId.equals(other.productId) &&
                versionId.equals(other.versionId) &&
                deviceId.equals(other.deviceId) &&
                featureIds.equals(other.featureIds);
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("NetworkDevice {");
        sb.append("ip=");
        sb.append(ipAddress);
        sb.append(", pid=");
        sb.append(productId);
        sb.append(", did=");
        sb.append(deviceId);
        sb.append(", vid=");
        sb.append(versionId);
        sb.append(", features=[ ");
        for (String fid : featureIds) {
            sb.append(fid);
            sb.append(" ");
        }
        sb.append("]}");
        return sb.toString();
    }

    @Deprecated // Remove this after all controllers are updated to Aura.
    public static NetworkDevice fromMacAddress(InetAddress ipAddress, byte[] mac) {
        String deviceId = String.format(
            "%02x%02x%02x%02x%02x%02x", mac[0], mac[1], mac[2], mac[3], mac[4], mac[5]);
        return new NetworkDevice(
            ipAddress, "", "", deviceId, new String[0]);
    }

    /** Parses a SYMMETRY_LABS_IDENTIFY response like "aura/r1 (rgb16) [d88034ab34f5]". */
    public static NetworkDevice fromIdentifier(InetAddress ipAddress, byte[] identifier) {
        String idStr = "";
        try {
            idStr = new String(identifier, "UTF-8");
        } catch (UnsupportedEncodingException e) { }

        String productId = "";
        String versionId = "";
        String deviceId = "";
        String[] featureIds = new String[0];
        Matcher matcher = PRODUCT_VERSION.matcher(idStr);
        if (matcher.find()) {
            productId = matcher.group(1);
            versionId = matcher.group(2);
        }
        matcher = FEATURE_LIST.matcher(idStr);
        if (matcher.find()) {
            featureIds = matcher.group(1).split(",");
        }
        matcher = DEVICE_ID.matcher(idStr);
        if (matcher.find()) {
            deviceId = matcher.group(1);
        }
        else {
            matcher = DEVICE_ID_LONG_MAC.matcher(idStr);
            if (matcher.find()) {
                deviceId = matcher.group(1);
            }
        }
        return new NetworkDevice(ipAddress, productId, versionId, deviceId, featureIds);
    }
}
