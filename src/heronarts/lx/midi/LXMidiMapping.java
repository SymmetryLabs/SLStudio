/**
 * Copyright 2013- Mark C. Slee, Heron Arts LLC
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

package heronarts.lx.midi;

import com.google.gson.JsonObject;

import heronarts.lx.LX;
import heronarts.lx.LXSerializable;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.LXNormalizedParameter;
import heronarts.lx.parameter.LXParameter;

public abstract class LXMidiMapping implements LXSerializable {

    public enum Type {
        NOTE,
        CONTROL_CHANGE
    };

    public final int channel;

    public final Type type;

    public final LXParameter parameter;

    protected LXMidiMapping(LX lx, int channel, Type type, LXParameter parameter) {
        if (parameter.getComponent() == null) {
            throw new IllegalStateException("Cannot map parameter with no component: " + parameter);
        }
        this.channel = channel;
        this.type = type;
        this.parameter = parameter;
    }

    protected LXMidiMapping(LX lx, JsonObject object, Type type) {
        this(
            lx,
            object.get(KEY_CHANNEL).getAsInt(),
            type,
            lx.getComponent(object.get(KEY_COMPONENT_ID).getAsInt()).getParameter(object.get(KEY_PARAMETER_PATH).getAsString())
        );
    }

    static boolean isValidMessageType(LXShortMessage message) {
        return (message instanceof MidiNote) || (message instanceof MidiControlChange);
    }

    static LXMidiMapping create(LX lx, LXShortMessage message, LXParameter parameter) {
        if (message instanceof MidiNote) {
            return new Note(lx, (MidiNote) message, parameter);
        } else if (message instanceof MidiControlChange) {
            return new ControlChange(lx, (MidiControlChange) message, parameter);
        }
        throw new IllegalArgumentException("Not a valid message type for a MIDI mapping: " + message);
    }

    static LXMidiMapping create(LX lx, JsonObject object) {
        Type type = Type.valueOf(object.get(KEY_TYPE).getAsString());
        switch (type) {
        case NOTE: return new Note(lx, object);
        case CONTROL_CHANGE: return new ControlChange(lx, object);
        }
        throw new IllegalArgumentException("Not a valid MidiMapping type: " + object);
    }

    abstract boolean matches(LXShortMessage message);
    abstract void apply(LXShortMessage message);

    public abstract String getDescription();

    protected void setValue(boolean value) {
        if (this.parameter instanceof BooleanParameter) {
            ((BooleanParameter) this.parameter).setValue(value);
        } else if (this.parameter instanceof LXNormalizedParameter) {
            ((LXNormalizedParameter) this.parameter).setNormalized(value ? 1 : 0);
        } else {
            this.parameter.setValue(value ? 1 : 0);
        }
    }

    protected void setNormalized(double normalized) {
        if (this.parameter instanceof LXNormalizedParameter) {
            ((LXNormalizedParameter) this.parameter).setNormalized(normalized);
        } else {
            this.parameter.setValue(normalized);
        }
    }

    protected void toggleValue() {
        if (this.parameter instanceof BooleanParameter) {
            ((BooleanParameter) this.parameter).toggle();
        } else if (this.parameter instanceof LXNormalizedParameter) {
            LXNormalizedParameter normalized = (LXNormalizedParameter) this.parameter;
            normalized.setNormalized(normalized.getNormalized() == 0 ? 1 : 0);
        } else {
            this.parameter.setValue(this.parameter.getValue() == 0 ? 1 : 0);
        }
    }

    private static final String KEY_CHANNEL = "channel";
    private static final String KEY_TYPE = "type";
    private static final String KEY_COMPONENT_ID = "componentId";
    private static final String KEY_PARAMETER_PATH = "parameterPath";

    @Override
    public void save(LX lx, JsonObject object) {
        object.addProperty(KEY_CHANNEL, this.channel);
        object.addProperty(KEY_TYPE, this.type.toString());
        object.addProperty(KEY_COMPONENT_ID, this.parameter.getComponent().getId());
        object.addProperty(KEY_PARAMETER_PATH, this.parameter.getPath());
    }

    @Override
    public void load(LX lx, JsonObject object) {
        throw new UnsupportedOperationException("Use LXMidiMapping.create() to load from JsonObject");
    }

    public static class Note extends LXMidiMapping {

        public final int pitch;

        private Note(LX lx, MidiNote note, LXParameter parameter) {
            super(lx, note.getChannel(), Type.NOTE, parameter);
            this.pitch = note.getPitch();
        }

        private Note(LX lx, JsonObject object) {
            super(lx, object, Type.NOTE);
            this.pitch = object.get(KEY_PITCH).getAsInt();
        }

        @Override
        boolean matches(LXShortMessage message) {
            if (!(message instanceof MidiNote)) {
                return false;
            }
            MidiNote note = (MidiNote) message;
            return
                (note.getChannel() == this.channel) &&
                (note.getPitch() == this.pitch);
        }

        @Override
        void apply(LXShortMessage message) {
            MidiNote note = (MidiNote) message;
            if ((note instanceof MidiNoteOff) || note.getVelocity() == 0) {
                if (this.parameter instanceof BooleanParameter) {
                    BooleanParameter booleanParameter = (BooleanParameter) this.parameter;
                    if (booleanParameter.getMode() == BooleanParameter.Mode.MOMENTARY) {
                        booleanParameter.setValue(false);
                    }
                }
            } else {
                if (this.parameter instanceof DiscreteParameter) {
                    ((DiscreteParameter) this.parameter).increment();
                } else if (this.parameter instanceof BooleanParameter) {
                    BooleanParameter booleanParameter = (BooleanParameter) this.parameter;
                    if (booleanParameter.getMode() == BooleanParameter.Mode.MOMENTARY) {
                        booleanParameter.setValue(true);
                    } else {
                        booleanParameter.toggle();
                    }
                } else {
                    toggleValue();
                }
            }
        }

        @Override
        public String getDescription() {
            return MidiNote.getPitchString(this.pitch);
        }

        private static final String KEY_PITCH = "pitch";

        @Override
        public void save(LX lx, JsonObject object) {
            super.save(lx, object);
            object.addProperty(KEY_PITCH, this.pitch);
        }
    }

    public static class ControlChange extends LXMidiMapping {

        public final int cc;

        private ControlChange(LX lx, MidiControlChange controlChange, LXParameter parameter) {
            super(lx, controlChange.getChannel(), Type.CONTROL_CHANGE, parameter);
            this.cc = controlChange.getCC();
        }

        private ControlChange(LX lx, JsonObject object) {
            super(lx, object, Type.CONTROL_CHANGE);
            this.cc = object.get(KEY_CC).getAsInt();
        }

        @Override
        boolean matches(LXShortMessage message) {
            if (!(message instanceof MidiControlChange)) {
                return false;
            }
            MidiControlChange controlChange = (MidiControlChange) message;
            return
                (controlChange.getChannel() == this.channel) &&
                (controlChange.getCC() == this.cc);
        }

        @Override
        void apply(LXShortMessage message) {
            MidiControlChange controlChange = (MidiControlChange) message;
            setNormalized(controlChange.getValue() / 127.);
        }

        @Override
        public String getDescription() {
            return "CC" + this.cc;
        }

        private static final String KEY_CC = "cc";

        @Override
        public void save(LX lx, JsonObject object) {
            super.save(lx, object);
            object.addProperty(KEY_CC, this.cc);
        }
    }

}
