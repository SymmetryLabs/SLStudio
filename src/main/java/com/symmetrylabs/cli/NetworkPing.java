package com.symmetrylabs.cli;

import com.symmetrylabs.slstudio.network.NetworkMonitor;
import heronarts.lx.LX;
import heronarts.lx.data.LXVersion;
import heronarts.lx.model.GridModel;

public class NetworkPing {
    private static NetworkMonitor networkMonitor;

    private static GridModel grid = new GridModel("the matrix", 10, 10);
    public static void main(String[] args){
//        System.out.println("hello");
//
//        LX lx = new LX(RUNTIME_VERSION, grid);
//
//        networkMonitor = new NetworkMonitor(lx);
//        searchControllers();
        new cli();
    }

    private static void searchControllers() {
    }
}
