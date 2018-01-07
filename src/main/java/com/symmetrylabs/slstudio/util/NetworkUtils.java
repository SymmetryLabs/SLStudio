package com.symmetrylabs.slstudio.util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public final class NetworkUtils {

    private static Pattern macAddressPattern = null;

    private static void initMacAddressPattern() {
        if (macAddressPattern == null) {
            macAddressPattern = Pattern.compile(
                "(\\p{XDigit}{1,2}):(\\p{XDigit}{1,2}):(\\p{XDigit}{1,2}):(\\p{XDigit}{1,2}):(\\p{XDigit}{1,2}):(\\p{XDigit}{1,2})");
        }
    }

    public static String normalizeMacAddress(String macAddress) {
        initMacAddressPattern();
        Matcher m = macAddressPattern.matcher(macAddress);
        if (!m.matches()) {
            throw new IllegalArgumentException("NetworkUtils.normalizeMacAddress(String macAddress): Not a mac address: " + macAddress);
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= 6; i++) {
            if (i != 1) sb.append(":");
            sb.append(NumberUtils.normalizeHex(m.group(i)));
        }
        return sb.toString();
    }

    public static String normalizeMacAddressUpper(String macAddress) {
        initMacAddressPattern();
        Matcher m = macAddressPattern.matcher(macAddress);
        if (!m.matches()) {
            throw new IllegalArgumentException("NetworkUtils.normalizeMacAddressUpper(String macAddress): Not a mac address: " + macAddress);
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= 6; i++) {
            if (i != 1) sb.append(":");
            sb.append(NumberUtils.normalizeHexUpper(m.group(i)));
        }
        return sb.toString();
    }

    public static String macAddrToString(byte[] addr) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (byte b : addr) {
            if (i++ != 0) sb.append(":");
            sb.append(NumberUtils.normalizeHex(b));
        }
        return sb.toString();
    }

    public static InetAddress ipAddrToInetAddr(String addr) {
        try {
            return InetAddress.getByName(addr);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean isValidMacAddr(byte[] macAddr) {
        return macAddr[0] != (byte) 0xff && macAddr[1] != (byte) 0xff && macAddr[2] != (byte) 0xff
            && macAddr[3] != (byte) 0xff && macAddr[4] != (byte) 0xff && macAddr[5] != (byte) 0xff;
    }

}
