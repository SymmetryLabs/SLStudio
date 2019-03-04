package com.symmetrylabs.slstudio.ui.v2;

import com.symmetrylabs.slstudio.ApplicationState;
import java.io.IOException;
import org.apache.commons.io.IOUtils;
import java.nio.ByteBuffer;

public class FontLoader {
    public static long DEFAULT_FONT;
    public static long DEFAULT_FONT_L;
    public static long DEFAULT_FONT_XL;

    public long load(String name, String resourcePath, float size) {
        try {
            byte[] data = IOUtils.toByteArray(getClass().getClassLoader().getResourceAsStream(resourcePath));
            ByteBuffer buf = ByteBuffer.allocateDirect(data.length);
            buf.put(data);
            buf.flip();
            return UI.addFont(name, buf, size);
        } catch (IOException e) {
            ApplicationState.setWarning("FontLoader/" + resourcePath, "couldn't load font");
            e.printStackTrace();
            return 0;
        }
    }

    public static void loadAll() {
        FontLoader fl = new FontLoader();
        DEFAULT_FONT = fl.load("Inter Medium", "fonts/Inter-Medium.ttf", 16);
        DEFAULT_FONT_L = fl.load("Big Inter Medium", "fonts/Inter-Medium.ttf", 20);
        DEFAULT_FONT_XL = fl.load("Big Inter Medium", "fonts/Inter-Medium.ttf", 30);
        fl.load("Scout Regular", "fonts/scout-regular-webfont.ttf", 16);
        fl.load("B612 Regular", "fonts/B612-Regular.ttf", 14);
        fl.load("B612 Bold", "fonts/B612-Bold.ttf", 14);
        fl.load("Bell Gothic Regular", "fonts/bell-gothic.ttf", 16);
        fl.load("Bell Gothic Bold", "fonts/bell-gothic-bold.ttf", 16);
        fl.load("Benton Sans Medium", "fonts/BentonSans-Medium.ttf", 14);
        fl.load("Benton Sans Bold", "fonts/BentonSans-Bold.ttf", 14);
        fl.load("Antenna Regular", "fonts/antenna-regular.ttf", 16);
        fl.load("Inter Regular", "fonts/Inter-Regular.ttf", 16);
        fl.load("Inter SemiBold", "fonts/Inter-SemiBold.ttf", 16);
        fl.load("Inter Bold", "fonts/Inter-Bold.ttf", 16);
    }
}
