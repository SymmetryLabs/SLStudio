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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
import heronarts.lx.osc.LXOscComponent;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.LXParameter;

public class LXAudioOutput extends LXAudioComponent implements LXOscComponent, LineListener {

    private SourceDataLine line;
    private AudioFormat format;
    private AudioInputStream inputStream;

    private boolean stopped = false;
    private boolean closed = false;

    public final BooleanParameter trigger = new BooleanParameter("Trigger", false)
        .setDescription("Triggers playback of the audio file from its beginning");

    public final BooleanParameter play = new BooleanParameter("Play", false)
        .setDescription("Play/Pause state of the output audio file");

    public final BooleanParameter looping = new BooleanParameter("Loop", false)
        .setDescription("Whether playback of the audio file should loop");

    public LXAudioOutput(LX lx) {
        super(lx, "Audio Output");
        this.format = STEREO;
        addParameter("play", this.play);
        addParameter("trigger", this.trigger);
        addParameter("looping", this.looping);
    }

    public String getOscAddress() {
        return "/lx/audio/output";
    }

    private OutputThread outputThread = null;

    private class OutputThread extends Thread {

        private final SourceDataLine line;

        private final byte[] buffer = new byte[STEREO_BUFFER_SIZE];

        private volatile boolean trigger = false;
        private volatile boolean flush = false;

        private OutputThread(SourceDataLine line) {
            super("LXAudioEngine Output Thread");
            this.line = line;
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

                try {
                    if (this.flush) {
                        line.flush();
                        this.flush = false;
                    }

                    if (this.trigger) {
                        if (inputStream.markSupported()) {
                            inputStream.reset();
                        } else {
                            System.err.println("Audio file does not support reset");
                        }
                        this.trigger = false;
                    }

                    boolean mono = inputStream.getFormat().getChannels() == 1;

                    // Read from the input stream
                    int len = inputStream.read(this.buffer, 0, mono ? MONO_BUFFER_SIZE : STEREO_BUFFER_SIZE);

                    // Reached the end of the file...
                    if (len <= 0) {
                        line.drain();
                        this.trigger = true;
                        if (!looping.isOn()) {
                            play.setValue(false);
                        }
                        continue;
                    }

                    // When reading mono files, double the length for stereo output
                    if (mono) {
                        for (int i = len - MONO_FRAME_SIZE; i >= 0; i -= MONO_FRAME_SIZE) {
                            this.buffer[2*i] = this.buffer[2*i+2] = this.buffer[i];
                            this.buffer[2*i+1] = this.buffer[2*i+3] = this.buffer[i+1];
                        }
                        len *= 2;
                    }

                    // Write to the output line
                    try {
                        line.write(this.buffer, 0, len);
                    } catch (Exception x) {
                        System.err.println("LXAudioOutput error: " + x.getLocalizedMessage());
                        x.printStackTrace();
                        play.setValue(false);
                    }

                    // TODO(mcslee): Need some kind of timing-fu in here so that the metering
                    // is in sync. Right now this sort of rushes ahead as the ouptut buffer is
                    // big.

                    // Put the left and right buffers
                    left.putSamples(this.buffer, 0, STEREO_BUFFER_SIZE, STEREO_FRAME_SIZE);
                    right.putSamples(this.buffer, 2, STEREO_BUFFER_SIZE, STEREO_FRAME_SIZE);
                    mix.computeMix(left, right);

                } catch (IOException iox) {
                    System.err.println(iox);
                    iox.printStackTrace();
                    break;
                }
            }

            line.flush();
        }
    }

    public LXAudioOutput setInputStream(File file) {
        try {
            return setInputStream(new FileInputStream(file));
        } catch (FileNotFoundException fnfx) {
            System.err.println(fnfx.getLocalizedMessage());
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
        AudioFormat format = inputStream.getFormat();
        if (format.getSampleRate() != SAMPLE_RATE) {
            System.err.println("Audio file must have sample rate of " + SAMPLE_RATE);
            return this;
        }
        if (format.getSampleSizeInBits() != BITS_PER_SAMPLE) {
            System.err.println("Audio file must have " + BITS_PER_SAMPLE + " bits per sample");
            return this;
        }
        if (format.isBigEndian()) {
            System.err.println("Audio file must be little endian");
            return this;
        }
        if (format.getEncoding() != AudioFormat.Encoding.PCM_SIGNED) {
            System.err.println("Audio file must be PCM signed");
            return this;
        }
        if (format.getChannels() > 2) {
            System.err.println("Audio file has more than 2 channels");
        }

        // Okay we're valid!
        this.inputStream = inputStream;
        if (this.inputStream.markSupported()) {
            this.inputStream.mark(Integer.MAX_VALUE);
        }
        if (this.outputThread != null) {
            this.outputThread.flush = true;
        }
        open();
        return this;
    }

    @Override
    public void onParameterChanged(LXParameter p) {
        super.onParameterChanged(p);
        if (p == this.play) {
            if (this.play.isOn()) {
                if (this.line == null) {
                    this.play.setValue(false);
                } else {
                    start();
                }
            } else {
                stop();
            }
        } else if (p == this.trigger) {
            if (this.trigger.isOn()) {
                if (this.line != null) {
                    this.play.setValue(true);
                    this.outputThread.trigger = true;
                }
                this.trigger.setValue(false);
            }
        }
    }

    private void open() {
        if (this.line == null) {
            try {
                this.line = (SourceDataLine) AudioSystem.getLine(STEREO_SOURCE_LINE);
                this.line.addLineListener(this);
                this.closed = false;
                this.line.open(this.format, 4*STEREO_BUFFER_SIZE);
                this.stopped = true;
                if (this.play.isOn()) {
                    this.stopped = false;
                    this.line.start();
                }
                this.outputThread = new OutputThread(this.line);
                this.outputThread.start();
            } catch (Exception x) {
                System.err.println(x.getLocalizedMessage());
                return;
            }
        }
    }

    void start() {
        if (this.line != null) {
            this.stopped = false;
            this.line.start();
            synchronized (this.outputThread) {
                this.outputThread.notify();
            }
        }
    }

    void stop() {
        if (this.line != null) {
            this.stopped = true;
            this.line.stop();
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
            if (this.line == event.getLine()) {
                this.stopped = true;
            }
        } else if (type == LineEvent.Type.CLOSE) {
            System.out.println("LXAudioOuput CLOSE");
            if (this.line == event.getLine()) {
                this.closed = true;
            }
        }
    }
}
