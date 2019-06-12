package com.symmetrylabs.slstudio;

import com.symmetrylabs.slstudio.ui.v2.Lwjgl3Launcher;
import com.symmetrylabs.slstudio.server.VolumeServer;


public class SymmetryLauncher {
    public static final String SLSTUDIO_NAME = "slstudio";
    public static final String VOLUME_NAME = "volume";
    public static final String VOLUME_SERVER_NAME = "server";

    public static final String DEFAULT_APP_NAME = SLSTUDIO_NAME;

    public static void main(String[] args) {
        String appName = null;
        if (args.length > 0) {
            appName = args[0].toLowerCase();
        } else if (System.getProperty("com.symmetrylabs.app") != null) {
            appName = System.getProperty("com.symmetrylabs.app").toLowerCase();
        } else {
            appName = DEFAULT_APP_NAME;
        }

        if (VOLUME_NAME.equals(appName)) {
            Lwjgl3Launcher.main(args);
            return;
        }
        if (VOLUME_SERVER_NAME.equals(appName)) {
            VolumeServer.main(args);
            return;
        }
        if (SLSTUDIO_NAME.equals(appName)) {
            SLStudio.main(args);
            return;
        }
        System.err.println("unknown app name " + appName);
    }
}
