/**
 * Copyright 2017- Mark C. Slee, Heron Arts LLC
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 * @author Mark C. Slee <mark@heronarts.com>
 */

package heronarts.lx.audio;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.TargetDataLine;

public class LXAudioInput extends LXAudioBuffer implements LineListener {

    private static final int SAMPLE_RATE = 44100;
    private static final int SAMPLE_BUFFER_SIZE = 256;
    private static final int BYTES_PER_SAMPLE = 2;
    private static final int NUM_CHANNELS = 2;
    private static final int FRAME_SIZE = BYTES_PER_SAMPLE * NUM_CHANNELS;
    private static final int INPUT_DATA_SIZE = SAMPLE_BUFFER_SIZE*BYTES_PER_SAMPLE*NUM_CHANNELS;

    private final static float INV_16_BIT = 1 / 32768.0f;

    private TargetDataLine line;
    private final AudioFormat format;

    private final byte[] rawBytes = new byte[INPUT_DATA_SIZE];

    public final LXAudioBuffer left = new LXAudioBuffer(SAMPLE_BUFFER_SIZE);
    public final LXAudioBuffer right = new LXAudioBuffer(SAMPLE_BUFFER_SIZE);
    public final LXAudioBuffer mix = this;

    private boolean closed = true;
    private boolean stopped = false;

    private InputThread inputThread = null;

    private class InputThread extends Thread {

        private InputThread() {
            super("LXAudioEngine Input Thread");
        }

        @Override
        public void run() {
            while (!closed) {
                while (stopped) {
                    if (closed) {
                        return;
                    }
                    try {
                        synchronized (this) {
                            wait();
                        }
                    } catch (InterruptedException ix) {}
                }

                // Read from the audio line
                line.read(rawBytes, 0, rawBytes.length);

                // Put the left and right buffers
                putBuffer(left, 0);
                putBuffer(right, 2);

                // Compute the mix buffer
                synchronized (this) {
                    float sumSquares = 0;
                    for (int i = 0; i < samples.length; ++i) {
                        samples[i] = (left.samples[i] + right.samples[i]) / 2.f;
                        sumSquares += samples[i] * samples[i];
                    }
                    rms = (float) Math.sqrt(sumSquares / samples.length);
                }
            }
        }

        private void putBuffer(LXAudioBuffer buffer, int offset) {
            synchronized (buffer) {
                int frameIndex = 0;
                float sumSquares = 0;
                for (int i = 0; i < INPUT_DATA_SIZE; i += FRAME_SIZE) {
                    buffer.samples[frameIndex] = ((rawBytes[offset + i+1] << 8) | (rawBytes[offset + i] & 0xff)) * INV_16_BIT;
                    sumSquares += buffer.samples[frameIndex] * buffer.samples[frameIndex];
                    ++frameIndex;
                }
                buffer.rms = (float) Math.sqrt(sumSquares / buffer.samples.length);
            }
        }
    };

    LXAudioInput() {
        super(SAMPLE_BUFFER_SIZE);
        this.format = new AudioFormat(SAMPLE_RATE, 8*BYTES_PER_SAMPLE, NUM_CHANNELS, true, false);
    }

    public AudioFormat getFormat() {
        return this.format;
    }

    void open() {
        if (this.line == null) {
            DataLine.Info info = new DataLine.Info(TargetDataLine.class,  this.format);
            if (!AudioSystem.isLineSupported(info)) {
                System.err.println("AudioSystem does not support stereo 16-bit input");
                return;
            }
            try {
                this.line = (TargetDataLine) AudioSystem.getLine(info);
                this.line.addLineListener(this);
                this.line.open(this.format, INPUT_DATA_SIZE*2);
                this.line.start();
                this.stopped = false;
                this.closed = false;
                this.inputThread = new InputThread();
                this.inputThread.start();
            } catch (Exception x) {
                System.err.println(x.getLocalizedMessage());
                return;
            }
        }
    }

    void start() {
        if (this.line == null) {
            throw new IllegalStateException("Cannot start() LXAudioInput before open()");
        }
        this.stopped = false;
        this.line.start();
        synchronized (this.inputThread) {
            this.inputThread.notify();
        }
    }

    void stop() {
        if (this.line == null) {
            throw new IllegalStateException("Cannot stop() LXAudioInput before open()");
        }
        this.stopped = true;
        this.line.stop();
    }

    void close() {
        if (this.line != null) {
            this.line.flush();
            stop();
            this.closed = true;
            this.line.close();
            this.line = null;
            synchronized (this.inputThread) {
                this.inputThread.notify();
            }
            try {
                this.inputThread.join();
            } catch (InterruptedException ix) {
                ix.printStackTrace();
            }
            this.inputThread = null;
        }
    }

    @Override
    public void update(LineEvent event) {
        LineEvent.Type type = event.getType();
        if (type == LineEvent.Type.OPEN) {
        } else if (type == LineEvent.Type.START){
        } else if (type == LineEvent.Type.STOP) {
            this.stopped = true;
        } else if (type == LineEvent.Type.CLOSE) {
            this.closed = true;
        }
    }

    public int sampleRate() {
        return SAMPLE_RATE;
    }

}
