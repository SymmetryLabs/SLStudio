/**
 * Copyright 2016- Mark C. Slee, Heron Arts LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @author Mark C. Slee <mark@heronarts.com>
 */

package heronarts.lx;

import java.io.InputStream;

import ddf.minim.AudioInput;
import ddf.minim.Minim;
import heronarts.lx.audio.FrequencyGate;
import heronarts.lx.audio.GraphicEQ;

// TODO(mcslee): make this an LXComponent that the engine runs
public class LXAudio {

    private final LX lx;

    /**
     * Callback for Minim implementation
     */
    private Object minimCallback = this;

    /**
     * Minim instance to provide audio input.
     */
    private Minim minim;

    /**
     * Audio input object
     */
    private AudioInput audioInput;

    private FrequencyGate beatDetect = null;

    /**
     * Default sample rate for audio input
     */
    public final static int DEFAULT_SAMPLE_RATE = 44100;

    LXAudio(LX lx) {
        this.lx = lx;
    }

    /**
     * Returns the underlying Minim audio object, creating if necessary
     *
     * @return minim Audio driver
     */
    public final Minim getMinim() {
        if (this.minim == null) {
            this.minim = new Minim(this.minimCallback);
        }
        return this.minim;
    }

    /**
     * Retrieves the audio input object at default sample rate of 44.1kHz
     *
     * @return Audio input object
     */
    public final AudioInput getInput() {
        return getInput(DEFAULT_SAMPLE_RATE);
    }

    /**
     * Retrieves an audio input at given sample rate
     *
     * @param sampleRate Sample rate for input
     * @return AudioInput at desired sample rate
     */
    public final AudioInput getInput(int sampleRate) {
        if (this.audioInput == null) {
            this.audioInput = this.getMinim().getLineIn(Minim.STEREO, 1024, sampleRate);
        }
        return this.audioInput;
    }

    /**
     * Gets the global beat detection object, creating if necessary
     *
     * @return Beat detection object
     */
    public final FrequencyGate beatDetect() {
        if (this.beatDetect == null) {
            GraphicEQ eq = new GraphicEQ(getInput(), 4);
            eq.attack.setValue(10);
            eq.release.setValue(250);
            eq.range.setValue(14);
            eq.gain.setValue(16);

            this.beatDetect = new FrequencyGate("BEAT", eq).setBands(1, 4);
            this.beatDetect.floor.setValue(0.9);
            this.beatDetect.threshold.setValue(0.75);
            this.beatDetect.release.setValue(480);

            this.lx.engine.addModulator(eq).start();
            this.lx.engine.addModulator(this.beatDetect).start();
        }
        return this.beatDetect;
    }

    public LXAudio setMinimCallback(Object callback) {
        this.minimCallback = callback;
        return this;
    }

    public String sketchPath(String fileName) {
        // For Minim compatibility
        return fileName;
    }

    public InputStream createInput(String fileName) {
        // Audio input not yet supported in LX
        return null;
    }

    void dispose() {
        if (this.audioInput != null) {
            this.audioInput.close();
            this.audioInput = null;
        }
        if (this.minim != null) {
            this.minim.stop();
            this.minim = null;
        }
    }

}
