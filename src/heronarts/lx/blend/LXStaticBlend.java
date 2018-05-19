package heronarts.lx.blend;

import com.symmetrylabs.color.Ops16;
import com.symmetrylabs.color.Ops8;
import heronarts.lx.LX;
import heronarts.lx.PolyBuffer;

import static heronarts.lx.PolyBuffer.Space.RGB16;
import static heronarts.lx.PolyBuffer.Space.RGB8;

public class LXStaticBlend extends LXBlend {
    protected final Ops8.BlendFunc blend8;
    protected final Ops16.BlendFunc blend16;

    public LXStaticBlend(LX lx, Ops8.BlendFunc blend8, Ops16.BlendFunc blend16) {
        super(lx);
        this.blend8 = blend8;
        this.blend16 = blend16;
    }

    public void blend(PolyBuffer base, PolyBuffer overlay,
                                        double alpha, PolyBuffer dest, PolyBuffer.Space space) {
        if (space == RGB8) {
            blend8((int[]) base.getArray(RGB8), (int[]) overlay.getArray(RGB8),
                    alpha, (int[]) dest.getArray(RGB8));
            dest.markModified(RGB8);
        } else {
            blend16((long[]) base.getArray(RGB16), (long[]) overlay.getArray(RGB16),
                    alpha, (long[]) dest.getArray(RGB16));
            dest.markModified(RGB16);
        }
    }

    protected void blend8(int[] base, int[] overlay, double alpha, int[] dest) {
        for (int i = 0; i < dest.length; i++) {
            dest[i] = blend8.apply(base[i], overlay[i], alpha);
        }
    }

    protected void blend16(long[] base, long[] overlay, double alpha, long[] dest) {
        for (int i = 0; i < dest.length; i++) {
            dest[i] = blend16.apply(base[i], overlay[i], alpha);
        }
    }
}
