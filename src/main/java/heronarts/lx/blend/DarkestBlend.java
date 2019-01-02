package heronarts.lx.blend;

import com.symmetrylabs.color.Ops16;
import com.symmetrylabs.color.Ops8;
import heronarts.lx.LX;

public class DarkestBlend extends LXStaticBlend {
    public DarkestBlend(LX lx) { super(lx, Ops8::darkest, Ops16::darkest); }
}