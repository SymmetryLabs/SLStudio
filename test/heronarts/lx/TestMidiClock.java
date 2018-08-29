package heronarts.lx;

import heronarts.lx.midi.LXMidiInput;
import heronarts.lx.midi.MidiTimeClock;
import heronarts.lx.model.GridModel;
import heronarts.lx.model.LXModel;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestMidiClock {
        private static LX lx;

        @BeforeClass
        public static void setupLx() {
                LXModel model = new GridModel(10, 10);
                lx = new LX(model);
                lx.engine.start();
        }

        @Test
        public void testReceive() {
                lx.engine.midi.whenReady(() -> {
                        for (LXMidiInput mi : lx.engine.midi.getInputs()) {
                                mi.channelEnabled.setValue(true);
                        }
                });
                while (true) {
                        lx.engine.run();
                }
        }
}
