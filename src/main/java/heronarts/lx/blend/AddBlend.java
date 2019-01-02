package heronarts.lx.blend;

import com.symmetrylabs.color.Ops16;
import com.symmetrylabs.color.Ops8;
import heronarts.lx.LX;

public class AddBlend extends LXStaticBlend {
    public AddBlend(LX lx) { super(lx, Ops8::add, Ops16::add); }
}