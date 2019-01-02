package heronarts.lx.blend;

import com.symmetrylabs.color.Ops16;
import com.symmetrylabs.color.Ops8;
import heronarts.lx.LX;

public class MultiplyBlend extends LXStaticBlend {
    public MultiplyBlend(LX lx) { super(lx, Ops8::multiply, Ops16::multiply); }

    @Deprecated
    public static void multiply(int[] base, int overlay, double alpha, int[] dest) {
        for (int i = 0; i < dest.length; i++) {
            dest[i] = Ops8.multiply(base[i], overlay, alpha);
        }
    }
}