package com.symmetrylabs.slstudio.effect;

import java.util.ArrayList;
import java.util.List;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;

import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.BooleanParameter;
import com.symmetrylabs.shows.cubes.CubesModel;


public class CubeMask extends ModelSpecificEffect<CubesModel> {
    @Override protected CubesModel createEmptyModel() {
        return new CubesModel();
    }

    private final List<Mask> masks = new ArrayList<>();

    final DiscreteParameter selectedMask;
    final BooleanParameter maskEnabled = new BooleanParameter("mask", false);
    final BooleanParameter flipMask = new BooleanParameter("flip", false);
    final BooleanParameter highlight = new BooleanParameter("highlight", false);
    final BooleanParameter reset = new BooleanParameter("reset").setMode(BooleanParameter.Mode.MOMENTARY);

    public CubeMask(LX lx) {
        super(lx);

        for (CubesModel.Cube cube : model.getCubes()) {
            Mask mask = new Mask(cube);
            masks.add(mask);
        }

        this.selectedMask = new DiscreteParameter("select", 0, 0, masks.size());

        selectedMask.addListener(parameter -> {
            maskEnabled.setValue(getSelectedMask().isEnabled());
        });

        maskEnabled.addListener(parameter -> {
            getSelectedMask().setEnabled(maskEnabled.isOn());
        });

        reset.addListener(parameter -> {
            for (Mask mask : masks) {
                mask.setEnabled(false);
            }
        });

        addParameter(selectedMask);
        addParameter(maskEnabled);
        addParameter(flipMask);
        addParameter(highlight);
        addParameter(reset);
    }

    private Mask getSelectedMask() {
        return masks.get(selectedMask.getValuei());
    }

    @Override
    public void run(double deltaMs, double amount) {
        for (Mask mask : masks) {
            if (mask.isEnabled() && !flipMask.isOn()) setCube(mask.getCube(), LXColor.BLACK);
            if (!mask.isEnabled() && flipMask.isOn()) setCube(mask.getCube(), LXColor.BLACK);
        }

        if (highlight.isOn()) {
            highlightMasks();
            highlightSelectedMask();
        }
    }

    private void highlightMasks() {
        for (Mask mask : masks) {
            if (mask.isEnabled()) {
                setCube(mask.getCube(), LXColor.RED);
            }
        }
    }

    private void highlightSelectedMask() {
        setCube(getSelectedMask().getCube(), LXColor.GREEN);
    }

    private void setCube(CubesModel.Cube cube, int color) {
        for (LXPoint p : cube.points) {
            colors[p.index] = color;
        }
    }

    private class Mask {
        private final CubesModel.Cube cube;
        private final BooleanParameter parameter;

        private Mask(CubesModel.Cube cube) {
            this.cube = cube;
            this.parameter = new BooleanParameter("mask/" + masks.size());
        }

        CubesModel.Cube getCube() {
            return cube;
        }

        BooleanParameter getParameter() {
            return parameter;
        }

        boolean isEnabled() {
            return parameter.isOn();
        }

        void setEnabled(boolean enabled) {
            parameter.setValue(enabled);
        }
    }

    @Override
    public void save(LX lx, JsonObject obj) {
        super.save(lx, obj);

        JsonObject json = new JsonObject();
        for (Mask mask : masks) {
            json.addProperty(mask.getParameter().getLabel(), mask.isEnabled());
        }
        obj.add("masks", json);
    }

    @Override
    public void load(LX lx, JsonObject obj) {
        super.load(lx, obj);

        if (obj.has("masks")) {
            JsonObject json = obj.getAsJsonObject("masks");

            for (Mask mask : masks) {
                String path = mask.getParameter().getLabel();
                if (json.has(path)) {
                    JsonElement value = json.get(path);
                    mask.setEnabled(value.getAsBoolean());
                }
            }
        }
    }
}

