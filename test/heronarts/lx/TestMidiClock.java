package heronarts.lx;

import heronarts.lx.midi.LXMidiInput;
import heronarts.lx.midi.MidiTime;
import heronarts.lx.model.GridModel;
import heronarts.lx.model.LXModel;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Not really a unit test: this just prints out MTC as it receives it.
 */
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
                                mi.addTimeListener(new LXMidiInput.MidiTimeListener() {
                                        @Override
                                        public void onBeatClockUpdate(int beatCount, double periodEstimate) {
                                                System.out.println(String.format(
                                                                "%16s: beat: %06d period: %.4f", mi.getName(), beatCount, periodEstimate));
                                        }

                                        @Override
                                        public void onMTCUpdate(MidiTime time) {
                                                System.out.println(String.format("%16s: mtc:  %s", mi.getName(), time));
                                        }
                                });
                        }
                });
                long time = System.nanoTime();
                while (System.nanoTime() - time < 5_000_000) {
                        lx.engine.run();
                }
        }
}
