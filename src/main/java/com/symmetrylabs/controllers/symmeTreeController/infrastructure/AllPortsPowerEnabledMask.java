package com.symmetrylabs.controllers.symmeTreeController.infrastructure;

import com.google.gson.annotations.Expose;
import com.symmetrylabs.shows.base.SLShow;
import com.symmetrylabs.slstudio.output.AbstractSLControllerBase;
import com.symmetrylabs.util.hardware.powerMon.ControllerWithPowerFeedback;
import com.symmetrylabs.util.persistance.ClassWriterLoader;

import java.io.IOException;
import java.util.Collection;
import java.util.TreeMap;

// persistent port power mask mapped to controllers.  Should be indexed on a per-show basis.
public class AllPortsPowerEnabledMask {
    private static String PERSISTENT_PORT_MASK = "persistent-port-mask.json";

    @Expose
    private final TreeMap<String, Byte> portMaskByControllerHumanID = new TreeMap<>();


    // default constructor
//    public AllPortsPowerEnabledMask() {}

    // builds the map in memory from the current state of all the Outputs in the "Sculpture" (i.e. the whole Oslo tree) as read off the network.
//    public void loadCurrentSculptureOutputStateTo_RAM(){
//        Collection<AbstractSLControllerBase> controllerSet = show.getSortedControllers();
//
//        loadControllerSetMaskStateTo_RAM(controllerSet);
//    }

    public void loadControllerSetMaskStateTo_RAM(Collection<AbstractSLControllerBase> controllerSet) {
        controllerSet.stream().filter(powerController -> powerController instanceof ControllerWithPowerFeedback)
            .map(powerController -> (ControllerWithPowerFeedback) powerController)
            .forEach(this::saveStateByController);
//        for (AbstractSLControllerBase controller : controllerSet){
//            if (!(controller instanceof  ControllerWithPowerFeedback)){
//                continue;
//            }
//            ControllerWithPowerFeedback pController = (ControllerWithPowerFeedback) controller;
//            saveStateByController(pController);
//        }
    }

    private void saveStateByController(ControllerWithPowerFeedback controller){
        Byte portMask;
        portMask = filterIntToByte(controller.getLastSample().powerOnStateMask);
        portMaskByControllerHumanID.put(controller.getHumanId(), portMask);
    }

    private Byte filterIntToByte(int powerOnStateMask) {
        assert (powerOnStateMask >= 0 && powerOnStateMask <= 0xff);
        return (byte) powerOnStateMask;
    }

    // Apply VolumeApp port masks in memory to controllers
    public void RAM_ApplyMaskAllControllers () {
    }

    public void saveToDisk () throws IOException {
        new ClassWriterLoader<>(PERSISTENT_PORT_MASK, AllPortsPowerEnabledMask.class).writeObj(this);
    }

    public static AllPortsPowerEnabledMask loadFromDisk (){
        return new ClassWriterLoader<>(PERSISTENT_PORT_MASK, AllPortsPowerEnabledMask.class).loadObj();
    }
}

