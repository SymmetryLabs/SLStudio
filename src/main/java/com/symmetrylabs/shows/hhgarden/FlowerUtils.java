package com.symmetrylabs.shows.hhgarden;

import heronarts.lx.model.LXModel;

public class FlowerUtils {
    private static FlowersModel cachedFlowersModel = null;
    private static FlowerModel.FlowerPoint[] cachedFlowerPoints = null;

    /** Returns the FlowerPoints for a model, or null if it's not a FlowersModel. */
    public static FlowerModel.FlowerPoint[] getFlowerPoints(LXModel model) {
        if (model instanceof FlowersModel) {
            FlowersModel flowerModel = (FlowersModel) model;
            if (cachedFlowerPoints == null || cachedFlowersModel != flowerModel) {
                cachedFlowerPoints = new FlowerModel.FlowerPoint[model.size];
                for (int i = 0; i < model.size; i++) {
                    cachedFlowerPoints[i] = (FlowerModel.FlowerPoint) model.points[i];
                }
            }
            return cachedFlowerPoints;
        }
        return null;
    }
}
