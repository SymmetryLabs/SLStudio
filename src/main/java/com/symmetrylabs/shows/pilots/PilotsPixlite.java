package com.symmetrylabs.shows.pilots;

import heronarts.lx.LX;
import com.symmetrylabs.slstudio.output.SimplePixlite;
import com.symmetrylabs.slstudio.output.PointsGrouping;


public class PilotsPixlite extends SimplePixlite {

    public PilotsPixlite(LX lx, String ipAddress, PilotsModel.Cart cart) {
        super(lx, ipAddress);

        addPixliteOutput(new PointsGrouping("1")
            .addPoints(cart.getDatalineByChannel("1").getPoints()));

        addPixliteOutput(new PointsGrouping("2")
            .addPoints(cart.getDatalineByChannel("2").getPoints()));

        addPixliteOutput(new PointsGrouping("3")
            .addPoints(cart.getDatalineByChannel("3").getPoints()));

        addPixliteOutput(new PointsGrouping("4")
            .addPoints(cart.getDatalineByChannel("4").getPoints()));

        addPixliteOutput(new PointsGrouping("5")
            .addPoints(cart.getDatalineByChannel("5").getPoints()));

        addPixliteOutput(new PointsGrouping("6")
            .addPoints(cart.getDatalineByChannel("6").getPoints()));

        addPixliteOutput(new PointsGrouping("7")
            .addPoints(cart.getDatalineByChannel("7").getPoints()));

        addPixliteOutput(new PointsGrouping("8")
            .addPoints(cart.getDatalineByChannel("8").getPoints()));

        addPixliteOutput(new PointsGrouping("9")
            .addPoints(cart.getDatalineByChannel("9").getPoints()));

        addPixliteOutput(new PointsGrouping("10")
            .addPoints(cart.getDatalineByChannel("10").getPoints()));

        addPixliteOutput(new PointsGrouping("11")
            .addPoints(cart.getDatalineByChannel("11").getPoints()));

        addPixliteOutput(new PointsGrouping("12")
            .addPoints(cart.getDatalineByChannel("12").getPoints()));

        addPixliteOutput(new PointsGrouping("13")
            .addPoints(cart.getDatalineByChannel("13").getPoints()));

        addPixliteOutput(new PointsGrouping("14")
            .addPoints(cart.getDatalineByChannel("14").getPoints()).addPoints(cart.getDatalineByChannel("15").getPoints()));

        addPixliteOutput(new PointsGrouping("15")
            .addPoints(cart.getDatalineByChannel("15").getPoints()).addPoints(cart.getDatalineByChannel("14").getPoints()));
    }

    @Override
    public SimplePixlite addPixliteOutput(PointsGrouping pointsGrouping) {
        try {
            SimplePixliteOutput spo = new SimplePixliteOutput(pointsGrouping);
            spo.setLogConnections(false);
            addChild(spo);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }
}
