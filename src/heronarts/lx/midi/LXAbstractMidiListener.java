/**
 * Copyright 2013- Mark C. Slee, Heron Arts LLC
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

package heronarts.lx.midi;

public abstract class LXAbstractMidiListener implements LXMidiListener {

    @Override
    public void noteOnReceived(LXMidiNoteOn note) {
    }

    @Override
    public void noteOffReceived(LXMidiNote note) {
    }

    @Override
    public void controlChangeReceived(LXMidiControlChange cc) {
    }

    @Override
    public void programChangeReceived(LXMidiProgramChange pc) {
    }

    @Override
    public void pitchBendReceived(LXMidiPitchBend pitchBend) {
    }

    @Override
    public void aftertouchReceived(LXMidiAftertouch aftertouch) {
    }

}
