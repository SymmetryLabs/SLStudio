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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import heronarts.lx.LX;

public class LXAudioOutput extends LXAudioComponent implements LineListener {

    private static final int SAMPLE_RATE = 44100;
    private static final int SAMPLE_BUFFER_SIZE = 512;
    private static final int BYTES_PER_SAMPLE = 2;
    private static final int NUM_CHANNELS = 2;
    private static final int FRAME_SIZE = BYTES_PER_SAMPLE * NUM_CHANNELS;
    private static final int OUTPUT_DATA_SIZE = SAMPLE_BUFFER_SIZE * FRAME_SIZE;

    private SourceDataLine line;
    private final AudioFormat format;
    private AudioInputStream inputStream;

    // private boolean stopped = false;
    // private boolean closed = false;

    public LXAudioOutput(LX lx) {
        super(lx, "Audio Output");
        this.format = new AudioFormat(SAMPLE_RATE, 8*BYTES_PER_SAMPLE, NUM_CHANNELS, true, false);
    }

    private OutputThread outputThread = null;

    private class OutputThread extends Thread {

        private final byte[] buffer = new byte[OUTPUT_DATA_SIZE];

        private OutputThread() {
            super("LXAudioEngine Output Thread");
            System.out.println(this.buffer.length);
        }

        @Override
        public void run() {
            while (true) {
                try {
                    // Read from the input stream
                    int len = inputStream.read(this.buffer, 0, this.buffer.length);
                    if (len < 0) {
                        break;
                    }

                    // Write to the output line
                    line.write(this.buffer, 0, len);

                    // TODO(mcslee): Need some kind of timing-fu in here so that the metering
                    // is in sync. Right now this sort of rushes ahead as the ouptut buffer is
                    // big.

                    // Put the left and right buffers
                    left.putSamples(this.buffer, 0, OUTPUT_DATA_SIZE, FRAME_SIZE);
                    right.putSamples(this.buffer, 2, OUTPUT_DATA_SIZE, FRAME_SIZE);
                    mix.computeMix(left, right);

                } catch (IOException iox) {
                    System.err.println(iox);
                    break;
                }
            }
            System.out.println("LXAudioOutput draining the line");
            line.drain();
            System.out.println("LXAudioOutput thread finished");
        }
    }

    public LXAudioOutput setInputStream(File file) {
        try {
            return setAudioInputStream(AudioSystem.getAudioInputStream(file));
        } catch (UnsupportedAudioFileException uafx) {
            uafx.printStackTrace(System.err);
            System.err.println(uafx.getLocalizedMessage());
        } catch (IOException iox) {
            System.err.println(iox.getLocalizedMessage());
        }
        return this;
    }

    public LXAudioOutput setInputStream(InputStream inputStream) {
        if (!inputStream.markSupported()) {
            inputStream = new BufferedInputStream(inputStream);
        }
        try {
            return setAudioInputStream(AudioSystem.getAudioInputStream(inputStream));
        } catch (UnsupportedAudioFileException uafx) {
            uafx.printStackTrace(System.err);
            System.err.println(uafx.getLocalizedMessage());
        } catch (IOException iox) {
            System.err.println(iox.getLocalizedMessage());
        }
        return this;
    }

    public LXAudioOutput setAudioInputStream(AudioInputStream inputStream) {
        // TOOD(mcslee): handle case where already open
        this.inputStream = inputStream;
        System.out.println("Try opening");
        open();
        return this;
    }

    private void open() {
        if (this.line == null) {
            try {
                this.line = (SourceDataLine) AudioSystem.getLine(STEREO_LINE);
                this.line.addLineListener(this);
                this.line.open(this.format);
                this.line.start();
                // this.stopped = false;
                // this.closed = false;
                this.outputThread = new OutputThread();
                this.outputThread.start();
            } catch (Exception x) {
                System.err.println(x.getLocalizedMessage());
                return;
            }
        }
    }

    @Override
    public void update(LineEvent event) {
        LineEvent.Type type = event.getType();
        if (type == LineEvent.Type.OPEN) {
            System.out.println("LXAudioOuput OPEN");
        } else if (type == LineEvent.Type.START){
            System.out.println("LXAudioOuput START");
        } else if (type == LineEvent.Type.STOP) {
            System.out.println("LXAudioOuput STOP");
            // this.stopped = true;
        } else if (type == LineEvent.Type.CLOSE) {
            System.out.println("LXAudioOuput CLOSE");
            // this.closed = true;
        }
    }

}
