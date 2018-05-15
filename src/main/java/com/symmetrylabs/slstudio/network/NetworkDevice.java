package com.symmetrylabs.slstudio.network;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.symmetrylabs.util.listenable.ListenableInt;

public class NetworkDevice {
    public final InetAddress ipAddress;
    public final String productId;
    public final String versionId;
    public final String deviceId;
    public final Set<String> featureIds = new HashSet<String>();
    public final ListenableInt versionNumber = new ListenableInt(-1);

    protected final static Pattern PRODUCT_VERSION = Pattern.compile("^(\\w+)/([\\w+]+)");
    protected final static Pattern FEATURE_LIST = Pattern.compile("\\(([\\w,]+)\\)");
    protected final static Pattern DEVICE_ID = Pattern.compile("\\[(\\w+)\\]");

    public NetworkDevice(InetAddress ipAddress, String productId, String versionId, String deviceId, String[] featureIds) {
        this.ipAddress = ipAddress;
        this.productId = productId;
        this.versionId = versionId;
        this.deviceId = deviceId;
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

    public static NetworkDevice fromMacAddress(InetAddress ipAddress, byte[] mac) {
        String deviceId = String.format(
            "%02x%02x%02x%02x%02x%02x", mac[0], mac[1], mac[2], mac[3], mac[4], mac[5]);
        return new NetworkDevice(
            ipAddress, null, null, deviceId, new String[0]);
    }

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
        return new NetworkDevice(ipAddress, productId, versionId, deviceId, featureIds);
    }
}
