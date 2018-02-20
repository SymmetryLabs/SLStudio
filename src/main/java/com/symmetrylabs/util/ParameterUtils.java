package com.symmetrylabs.util;

import heronarts.lx.parameter.DiscreteParameter;

public class ParameterUtils {
    public static boolean setDiscreteParameter(DiscreteParameter parameter, String name) {
        String[] options = parameter.getOptions();
        for (int i = 0; i < options.length; i++) {
            if (options[i].equals(name)) {
                parameter.setValue(i);
                return true;
            }
        }
        return false;
    }
}
