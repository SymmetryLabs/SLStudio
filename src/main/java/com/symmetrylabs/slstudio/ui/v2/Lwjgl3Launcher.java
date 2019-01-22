package com.symmetrylabs.slstudio.ui.v2;

import java.nio.file.Paths;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import org.lwjgl.system.Configuration;

/** Launches the desktop (LWJGL3) application. */
public class Lwjgl3Launcher {
    public static final String PREBUILT_DIR =
        Paths.get(System.getProperty("user.dir"), "libs").toString();

    static {
        /* we want to use our shipped prebuilts for everything, and LWJGL will skip
             extracting the stuff from its lwjgl-natives JAR if it finds the equivalent
             dylib in its extract path already. We extract these into a known location so that
           we can link against the same copy of these dynamic libraries when we
           build slimgui. */
        Configuration.SHARED_LIBRARY_EXTRACT_PATH.set(PREBUILT_DIR);
    }

    public static void main(String[] args) {
        createApplication();
    }

    private static Lwjgl3Application createApplication() {
        return new Lwjgl3Application(new SLStudioGDX(), getDefaultConfiguration());
    }

    private static Lwjgl3ApplicationConfiguration getDefaultConfiguration() {
        Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
        configuration.useOpenGL3(true, 4, 1);
        configuration.setTitle("SLStudio");
        configuration.setWindowedMode(640, 480);
        configuration.setWindowIcon("application.png");
        /* 8 bits per color, 0 bits for depth/stencil, 3 samples per pixel */
        configuration.setBackBufferConfig(8, 8, 8, 8, 0, 0, 3);
        configuration.enableGLDebugOutput(true, System.out);
        return configuration;
    }
}
