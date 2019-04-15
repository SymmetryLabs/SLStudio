package com.symmetrylabs.shows.cubes;

import java.util.Optional;
import com.google.common.base.Preconditions;


public class CubeRecord {
    public final CubesModel.Cube.Type type;
    public final String cubeId;
    public final String firstControllerId;
    public final String firstControllerMac;
    public final Optional<String> secondControllerId;
    public final Optional<String> secondControllerMac;

    public CubeRecord(CubesModel.Cube.Type type, String cubeId, String firstControllerId, String firstControllerMac) {
        this(type, cubeId, firstControllerId, firstControllerMac, Optional.empty(), Optional.empty());
    }

    public CubeRecord(
            CubesModel.Cube.Type type, String cubeId,
            String firstControllerId, String firstControllerMac,
            String secondControllerId, String secondControllerMac) {
        this(type, cubeId, firstControllerId, firstControllerMac, Optional.of(secondControllerId), Optional.of(secondControllerMac));
    }

    protected CubeRecord(
            CubesModel.Cube.Type type, String cubeId,
            String firstControllerId, String firstControllerMac,
            Optional<String> secondControllerId, Optional<String> secondControllerMac) {
        Preconditions.checkArgument(
            secondControllerId.isPresent() == secondControllerMac.isPresent(),
            "only one of second controller ID and MAC given; must specify both or neither");
        this.type = type;
        this.cubeId = cubeId;
        this.firstControllerId = firstControllerId;
        this.firstControllerMac = firstControllerMac;
        this.secondControllerId = secondControllerId;
        this.secondControllerMac = secondControllerMac;
    }

    public boolean isDoubleController() {
        return secondControllerId.isPresent();
    }

    @Override
    public String toString() {
        if (isDoubleController()) {
            return String.format(
                "%s: %s [A: %s@%s] [B: %s@%s]",
                cubeId, type, firstControllerId, firstControllerMac,
                secondControllerId.get(), secondControllerMac.get());
        }
        return String.format("%s: %s [A: %s@%s]", cubeId, type, firstControllerId, firstControllerMac);
    }
}
