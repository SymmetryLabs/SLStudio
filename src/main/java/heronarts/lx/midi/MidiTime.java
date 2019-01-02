package heronarts.lx.midi;

public class MidiTime implements Cloneable {
        public enum FrameRate {
                FPS24,
                FPS25,
                FPS2997, // 29.97
                FPS30;

                public int fps() {
                        switch (this) {
                                case FPS24: return 24;
                                case FPS25: return 25;
                                /* 29.97 still counts 30 frames per second in most seconds. */
                                case FPS2997: case FPS30: return 30;
                        }
                        throw new IllegalStateException("unknown FrameRate");
                }

                public String toString() {
                        switch (this) {
                                case FPS24: return "24fps";
                                case FPS25: return "25fps";
                                case FPS2997: return "29.97fps";
                                case FPS30: return "30fps";
                        }
                        return "unknown";
                }

                /**
                 * Returns the FrameRate corresponding to the SMPTE frame rate code
                 * that is embedded in MTC messages.
                 */
                public static FrameRate fromRateCode(int rateCode) {
                        switch (rateCode) {
                                case 0: return FPS24;
                                case 1: return FPS25;
                                case 2: return FPS2997;
                                case 3: return FPS30;
                        }
                        return null;
                }
        };

        /* Intentionally package-private; we want these to be modifiable by MidiTimeClock
         * to avoid unnecessary allocations, but unmodifiable to code outside the
         * midi package, because MidiTime objects are heavily shared. */
        int hour;
        int minute;
        int second;
        int frame;
        FrameRate rate;

        public MidiTime(int hour, int minute, int second, int frame, FrameRate rate) {
                this.hour = hour;
                this.minute = minute;
                this.second = second;
                this.frame = frame;
                this.rate = rate;
        }

        public MidiTime withAddedFrames(int frames) {
                MidiTime res = clone();
                res.frame += frames;
                int fps = rate.fps();
                int realFrame = res.frame % fps;
                res.second += (res.frame - realFrame) / fps;
                res.frame = realFrame;
                if (res.second < 60) {
                        return res;
                }
                int realSecond = res.second % 60;
                res.minute += (res.second - realSecond) / 60;
                res.second = realSecond;
                if (res.minute < 60) {
                        return res;
                }
                int realMinute = res.minute % 60;
                res.hour += (res.minute - realMinute) / 60;
                res.minute = realMinute;
                return res;
        }

        @Override
        public boolean equals(Object other) {
                if (other == null || MidiTime.class.isAssignableFrom(other.getClass())) {
                        return false;
                }
                MidiTime otherMt = (MidiTime) other;
                return hour == otherMt.hour &&
                             minute == otherMt.minute &&
                             second == otherMt.second &&
                             frame == otherMt.frame &&
                             rate == otherMt.rate;
        }

        @Override
        public int hashCode() {
                int res = hour;
                res = 60 * res + minute;
                res = 60 * res + second;
                res = 30 * res + frame;
                return res;
        }

        @Override
        public String toString() {
                return String.format("%02d:%02d:%02d:%02d @ %s", hour, minute, second, frame, rate);
        }

        @Override
        public MidiTime clone() {
                return new MidiTime(hour, minute, second, frame, rate);
        }

        public static MidiTime zero(FrameRate rate) {
                return new MidiTime(0, 0, 0, 0, rate);
        }

        public int getHour() {
                return hour;
        }

        public int getMinute() {
                return minute;
        }

        public int getSecond() {
                return second;
        }

        public int getFrame() {
                return frame;
        }

        public FrameRate getRate() {
                return rate;
        }
}
