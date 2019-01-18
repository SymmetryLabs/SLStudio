package heronarts.lx.blend;

import com.symmetrylabs.color.Ops16;
import com.symmetrylabs.color.Ops8;
import heronarts.lx.LX;

public class NormalBlend extends LXStaticBlend {
    public NormalBlend(LX lx) { super(lx, Ops8::blend, Ops16::blend); }

    @Deprecated
    public static void lerp(int[] base, int overlay[], double alpha, int[] dest) {
        for (int i = 0; i < dest.length; i++) {
            dest[i] = Ops8.blend(base[i], overlay[i], alpha);
        }
    }
}