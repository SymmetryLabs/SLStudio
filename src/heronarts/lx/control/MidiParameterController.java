/**
 * ##library.name##
 * ##library.sentence##
 * ##library.url##
 *
 * Copyright ##copyright## ##author##
 * All Rights Reserved
 * 
 * @author      ##author##
 * @modified    ##date##
 * @version     ##library.prettyVersion## (##library.version##)
 */

package heronarts.lx.control;

import java.util.Map;
import java.util.HashMap;

import rwmidi.Controller;
import rwmidi.Note;
import rwmidi.RWMidi;
import rwmidi.MidiInputDevice;

public class MidiParameterController {
    
    private final MidiInputDevice device;
    private final Map<Integer, LXParameter> ccMappings = new HashMap<Integer, LXParameter>();
    private final Map<Integer, LXParameter> noteMappings = new HashMap<Integer, LXParameter>();
    
    public MidiParameterController(MidiInputDevice device) {
        this.device = device;
        this.device.createInput(this);
    }
    
    public MidiParameterController addCCMapping(int cc, LXParameter parameter) {
        ccMappings.put(cc, parameter);
        return this;
    }
    
    public MidiParameterController addNoteMapping(int noteNum, LXParameter parameter) {
        noteMappings.put(noteNum, parameter);
        return this;
    }
    
    public void controllerChangeReceived(Controller controller) {
        if (ccMappings.containsKey(controller.getCC())) {
            LXParameter parameter = ccMappings.get(controller.getCC());
            parameter.setValue(controller.getValue() / 127.);
        }
    }
    
    public void noteOnReceived(Note note) {
        if (noteMappings.containsKey(note.getPitch())) {
            LXParameter parameter = noteMappings.get(note.getPitch());
            parameter.setValue(note.getVelocity() / 127.);
        }
    }
    
    public void noteOffReceived(Note note) {
        if (noteMappings.containsKey(note.getPitch())) {
            LXParameter LXParameter = noteMappings.get(note.getPitch());
            LXParameter.setValue(0);
        }
    }
}
