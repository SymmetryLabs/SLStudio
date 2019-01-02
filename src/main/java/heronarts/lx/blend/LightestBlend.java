package heronarts.lx.blend;

import com.symmetrylabs.color.Ops16;
import com.symmetrylabs.color.Ops8;
import heronarts.lx.LX;

public class LightestBlend extends LXStaticBlend {
    public LightestBlend(LX lx) { super(lx, Ops8::lightest, Ops16::lightest); }
}