package heronarts.lx.blend;

import com.symmetrylabs.color.Ops16;
import com.symmetrylabs.color.Ops8;
import heronarts.lx.LX;

public class ScreenBlend extends LXStaticBlend {
    public ScreenBlend(LX lx) { super(lx, Ops8::screen, Ops16::screen); }

    @Deprecated
    public static void screen(int[] base, int overlay[], double alpha, int[] dest) {
        for (int i = 0; i < dest.length; i++) {
            dest[i] = Ops8.screen(base[i], overlay[i], alpha);
        }
    }
}