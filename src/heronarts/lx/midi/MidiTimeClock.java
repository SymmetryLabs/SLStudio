package heronarts.lx.midi;

import javax.sound.midi.ShortMessage;
import javax.sound.midi.SysexMessage;

public class MidiTimeClock {
        private static final int QF_DATA_MASK         = 0x0F;
        private static final int QF_TYPE_MASK         = 0xF0;
        private static final int QF_TYPE_FRAME_L      = 0x00;
        private static final int QF_TYPE_FRAME_H      = 0x10;
        private static final int QF_TYPE_SEC_L        = 0x20;
        private static final int QF_TYPE_SEC_H        = 0x30;
        private static final int QF_TYPE_MIN_L        = 0x40;
        private static final int QF_TYPE_MIN_H        = 0x50;
        private static final int QF_TYPE_HOUR_L       = 0x60;
        private static final int QF_TYPE_HOUR_H_RATE  = 0x70;

        /**
         * MTC time code messages take 2 frames to send, so if we just listen to the
         * messages we'll only update every other frame and we'll be two frames behind.
         * This keeps track of how many frames we need to add to the last confirmed time
         * to get the real current frame.
         */
        private int offsetFrame;

        private MidiTime confirmedTime;
        private MidiTime timeInFlight;

        public MidiTimeClock() {
                confirmedTime = MidiTime.zero(MidiTime.FrameRate.FPS30);
                timeInFlight = MidiTime.zero(MidiTime.FrameRate.FPS30);
                offsetFrame = 0;
        }

        /** Pushes a MIDI message into the clock.
         * @param m An MTC ShortMessage. It is assumed that the message is an MTC message (meaning its status is ShortMessage.MIDI_TIME_CODE).
         * @return true if this message updated the clock time.
         */
        public boolean pushMessage(ShortMessage m) {
                int type = m.getData1() & QF_TYPE_MASK;
                int data = m.getData1() & QF_DATA_MASK;
                MidiTime t;
                boolean updated = false;

                switch (type) {
                        case QF_TYPE_FRAME_L:
                                timeInFlight.frame = data;
                                /* the frame we just finished receiving was the one they
                                 * started sending two frames ago. */
                                offsetFrame = 2;
                                /* offsetFrame changes here; it was 1 on the previous go-through. */
                                updated = true;
                                break;

                        case QF_TYPE_FRAME_H:
                                timeInFlight.frame |= data << 4;
                                offsetFrame = 2;
                                break;

                        case QF_TYPE_SEC_L:
                                timeInFlight.second = data;
                                offsetFrame = 2;
                                break;

                        case QF_TYPE_SEC_H:
                                timeInFlight.second |= data << 4;
                                offsetFrame = 2;
                                break;

                        case QF_TYPE_MIN_L:
                                timeInFlight.minute = data;
                                /* one more frame elapses here, so now we're three ahead of
                                 * the last one we received. */
                                offsetFrame = 3;
                                updated = true;
                                break;

                        case QF_TYPE_MIN_H:
                                timeInFlight.minute |= data << 4;
                                offsetFrame = 3;
                                break;

                        case QF_TYPE_HOUR_L:
                                timeInFlight.hour = data;
                                offsetFrame = 3;
                                break;

                        case QF_TYPE_HOUR_H_RATE:
                                timeInFlight.hour |= (data & 0x01) << 4;
                                timeInFlight.rate = MidiTime.FrameRate.fromRateCode((data >> 1) & 0x03);
                                /* swap confirmedTime and timeInFlight; this lets us avoid
                                 * making an allocation on each message receipt by reusing
                                 * these two MidiTime objects. */
                                t = confirmedTime;
                                confirmedTime = timeInFlight;
                                timeInFlight = t;
                                /* we're finished receiving the frame we started receiving
                                 * in this sequence, so for one quarter-frame we're only
                                 * one frame ahead of the one we're done receiving (because
                                 * one frame elapsed between when we started receiving and
                                 * when we finished). */
                                offsetFrame = 1;
                                updated = true;
                                break;
                }

                System.out.println(String.format("%d %d %s", type, data, confirmedTime.toString()));
                return updated;
        }

        /** Pushes a MIDI message into the clock.
         * @return true if this message updated the clock time.
         */
        public boolean pushMessage(SysexMessage m) {
                return false;
        }

        public MidiTime getTime() {
                /* offsetFrame is basically never zero, so a branch to avoid the
                 * method call here would be pointless. */
                return confirmedTime.withAddedFrames(offsetFrame);
        }
}
