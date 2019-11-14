package com.symmetrylabs.controllers.symmeTreeController.infrastructure;

import com.google.gson.annotations.Expose;
import com.symmetrylabs.slstudio.output.DiscoverableController;
import com.symmetrylabs.util.hardware.powerMon.ControllerWithPowerFeedback;
import com.symmetrylabs.util.persistance.ClassWriterLoader;

import java.io.IOException;
import java.util.Collection;
import java.util.TreeMap;

// persistent port power mask mapped to controllers.  Should be indexed on a per-show basis.
public class AllPortsPowerEnableMask {
    private static String PERSISTENT_PORT_MASK = "persistent-port-mask.json";

    @Expose
    private final TreeMap<String, Byte> portMaskByControllerHumanID = new TreeMap<>();

    public void loadControllerSetMaskStateTo_RAM(Collection<DiscoverableController> controllerSet) {
        controllerSet.stream().filter(powerController -> powerController instanceof ControllerWithPowerFeedback)
            .map(powerController -> (ControllerWithPowerFeedback) powerController)
            .forEach(this::saveStateByController);
    }

    private void saveStateByController(ControllerWithPowerFeedback controller){
        Byte portMask;
        portMask = filterIntToByte(controller.getLastSample().powerOnStateMask);
        portMaskByControllerHumanID.put(controller.getHumanId(), portMask);
    }

    private void applyStateToController(ControllerWithPowerFeedback controller){
        Byte portMask;
        portMask = portMaskByControllerHumanID.get(controller.getHumanId());
        controller.setPortPowerMask(portMask);
    }

    private Byte filterIntToByte(int powerOnStateMask) {
        assert (powerOnStateMask >= 0 && powerOnStateMask <= 0xff);
        return (byte) powerOnStateMask;
    }

    // Apply VolumeApp port masks in memory to controllers
    public void RAM_ApplyMaskAllControllers(Collection<DiscoverableController> controllerSet) {
        controllerSet.stream().filter(powerController -> powerController instanceof ControllerWithPowerFeedback)
            .map(powerController -> (ControllerWithPowerFeedback) powerController)
            .forEach(this::applyStateToController);
    }

    public void saveToDisk () throws IOException {
        new ClassWriterLoader<>(PERSISTENT_PORT_MASK, AllPortsPowerEnableMask.class).writeObj(this);
    }

    public static AllPortsPowerEnableMask loadFromDisk (){
        return new ClassWriterLoader<>(PERSISTENT_PORT_MASK, AllPortsPowerEnableMask.class).loadObj();
    }
}

