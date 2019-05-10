package heronarts.lx.output;

import org.junit.Test;
import org.junit.Assert;
import heronarts.lx.LX;
import heronarts.lx.PolyBuffer;
import heronarts.lx.Buffer;
import heronarts.lx.PolyBuffer.Space;
import heronarts.lx.color.LXColor;
import java.util.Random;
import com.symmetrylabs.slstudio.ApplicationState;
import com.symmetrylabs.color.Ops8;

public class LXOutputTest {
    @Test
    public void testGammaCorrectionMapsPerceptualToLinear() {
        ApplicationState.setProvider(new ApplicationState.DummyProvider());

        /* 1000x100 grid -> 100000 colors per buffer */
        LX lx = new LX(1000, 100);

        PolyBuffer pbuf = new PolyBuffer(lx);

        /* we do all of our pattern generation in perceptual space */
        int[] perceptualBuf = (int[]) pbuf.getArray(PolyBuffer.Space.RGB8);

        /* fixed seed for deterministic tests */
        Random r = new Random(0);
        for (int i = 0; i < perceptualBuf.length; i++) {
            perceptualBuf[i] = LXColor.hsb(r.nextDouble() * 360, r.nextDouble() * 100, r.nextDouble() * 100);
        }

        TestLXOutput out = new TestLXOutput(lx);
        out.gammaCorrection.setValue(2);
        out.send(pbuf);
        int[] linearBuf = (int[]) out.getBuffer().getArray(PolyBuffer.Space.RGB8);

        Assert.assertEquals(linearBuf.length, perceptualBuf.length);
        for (int i = 0; i < linearBuf.length; i++) {
            /* our linear output should always be less than our perceptual output, since the whole
             * point of gamma correction is to give you more dynamic range at the low end */
            double linearLevel = Ops8.level(linearBuf[i]);
            double perceptualLevel = Ops8.level(perceptualBuf[i]);
            Assert.assertTrue(linearLevel <= perceptualLevel);
        }
    }

    private static class TestLXOutput extends LXOutput {
        TestLXOutput(LX lx) {
            super(lx, "TestOutput");
        }

        PolyBuffer getBuffer() {
            return buffer;
        }
    }
}
