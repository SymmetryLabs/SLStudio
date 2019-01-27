package heronarts.lx.blend;

import com.symmetrylabs.color.Ops16;
import com.symmetrylabs.color.Ops8;
import heronarts.lx.LX;

public class SubtractBlend extends LXStaticBlend {
    public SubtractBlend(LX lx) { super(lx, Ops8::subtract, Ops16::subtract); }
}