package heronarts.lx.blend;

import com.symmetrylabs.color.Ops16;
import com.symmetrylabs.color.Ops8;
import heronarts.lx.LX;

public class DissolveBlend extends LXStaticBlend {
    public DissolveBlend(LX lx) { super(lx, Ops8::dissolve, Ops16::dissolve); }
}