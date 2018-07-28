package com.symmetrylabs.util;

import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class NetworkUtils {
    public static String macAddrToString(byte[] addr) {
        String[] hexBytes = new String[addr.length];
        for (int i = 0; i < addr.length; i++) {
            hexBytes[i] = String.format("%02x", addr[i]);
        }
        return String.join(":", hexBytes);
    }

    public static InetAddress toInetAddress(String host) {
        try {
            return InetAddress.getByName(host);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean isValidMacAddr(byte[] macAddr) {
        for (byte b : macAddr) {
            if (b == (byte) 0xff) return false;
        }
        return true;
    }

    private static final long BCAST_ADDR_CACHE_UPDATE_NS =
        System.getProperty("os.name").contains("Windows") ? 5_000_000_000L : 0L;
    private static final Object bcastAddrCacheLock = new Object();

    private static List<InetAddress> bcastAddrCache = new ArrayList<InetAddress>();
    private static boolean addrCacheIsUpdating = false;
    private static long lastBcastAddrCacheUpdate = 0;

    private static List<InetAddress> loadBroadcastAddresses() {
        List<InetAddress> addrs = new ArrayList<InetAddress>();
        try {
            for (NetworkInterface iface : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                for (InterfaceAddress addr : iface.getInterfaceAddresses()) {
                    if (addr.getBroadcast() != null) {
                        addrs.add(addr.getBroadcast());
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return addrs;
    }

    /**
     * Returns the list of broadcast addresses for all attached network
     * interfaces.
     *
     * On Windows, enumerating the list of available network interfaces can
     * take an ungodly 200ms; on Mac OS it happens so quickly a sampling
     * profiler doesn't even catch it. On non-Windows OSes, this directly
     * calls the underlying OS methods to get network interfaces and
     * addresses. On Windows, it uses a time-based cache to
     * avoid spending so much time polling something that rarely changes;
     * a side-effect of this is that it may take Windows machines up to 5
     * seconds to find a new network interface to broadcast on.
     *
     * The cache is updated on a separate thread to avoid blocking the
     * caller for hundreds of milliseconds, which means that (again, only
     * on Windows), for the first 200ms or so after this function is
     * called the first time, it will return an empty list of addresses.
     */
    public static List<InetAddress> getBroadcastAddresses() {
        if (BCAST_ADDR_CACHE_UPDATE_NS == 0)
            return loadBroadcastAddresses();

        synchronized (bcastAddrCacheLock) {
            boolean needsUpdating =
                bcastAddrCache.isEmpty() ||
                    System.nanoTime() - lastBcastAddrCacheUpdate > BCAST_ADDR_CACHE_UPDATE_NS;

            if (needsUpdating && !addrCacheIsUpdating) {
                addrCacheIsUpdating = true;
                new Thread(() -> {
                    /* This happens outside the lock so that getBroadcastAddresses() consumers
                       aren't blocked while it happens. */
                    List<InetAddress> addrs = loadBroadcastAddresses();
                    synchronized (bcastAddrCacheLock) {
                        bcastAddrCache = addrs;
                        lastBcastAddrCacheUpdate = System.nanoTime();
                        addrCacheIsUpdating = false;
                    }
                }, "BroadcastAddressLoader").start();
            }

            return bcastAddrCache;
        }
    }

    public static List<InetAddress> getInetAddresses() {
        List<InetAddress> addresses = new ArrayList<InetAddress>();
        try {
            for (NetworkInterface iface : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                for (InterfaceAddress addr : iface.getInterfaceAddresses()) {
                    if (addr.getAddress() != null) {
                        addresses.add(addr.getAddress());
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return addresses;
    }
}
