package com.symmetrylabs.slstudio.effect;

import heronarts.lx.LX;
import heronarts.lx.LXEffect;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.DiscreteParameter;

import com.symmetrylabs.slstudio.model.nissan.NissanModel;
import com.symmetrylabs.slstudio.model.nissan.NissanCar;

public class MaskNissanCar extends LXEffect {

    public final DiscreteParameter selectedCar = new DiscreteParameter("car", 0, 3);

    public MaskNissanCar(LX lx) {
        super(lx);
        addParameter(selectedCar);
    }

    @Override
    public void run(double deltaMs, double amount) {
        int i = 0;
        for (NissanCar car : ((NissanModel)model).getCars()) {
            if (i++ != selectedCar.getValuei()) {
                for (LXPoint p : car.points) {
                    colors[p.index] = LXColor.BLACK;
                }
            }
        }
    }
}
