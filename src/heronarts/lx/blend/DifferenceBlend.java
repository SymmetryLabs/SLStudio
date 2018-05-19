package heronarts.lx.blend;

import com.symmetrylabs.color.Ops16;
import com.symmetrylabs.color.Ops8;
import heronarts.lx.LX;

public class DifferenceBlend extends LXStaticBlend {
    public DifferenceBlend(LX lx) { super(lx, Ops8::difference, Ops16::difference); }
}