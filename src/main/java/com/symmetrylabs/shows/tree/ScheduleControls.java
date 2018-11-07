package com.symmetrylabs.shows.tree;

import java.util.Calendar;

import heronarts.lx.LX;
import heronarts.lx.LXRunnableComponent;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.modulator.LinearEnvelope;

import com.symmetrylabs.slstudio.SLStudio;


public class ScheduleControls extends LXRunnableComponent {

    private static ScheduleControls instance = null;

    private final Calendar calendar = Calendar.getInstance();

    public final DiscreteParameter startHour = new DiscreteParameter("startHour", 0, 23);
    public final DiscreteParameter startMinute = new DiscreteParameter("startMinute", 1, 59);

    public final DiscreteParameter endHour = new DiscreteParameter("endHour", 0, 23);
    public final DiscreteParameter endMinute = new DiscreteParameter("endMinute", 1, 59);

    boolean isOn = false;

    private ScheduleControls(LX lx) {
        super(lx);
        addParameter(startHour.setUnits(LXParameter.Units.INTEGER));
        addParameter(startMinute.setUnits(LXParameter.Units.INTEGER));
        addParameter(endHour.setUnits(LXParameter.Units.INTEGER));
        addParameter(endMinute.setUnits(LXParameter.Units.INTEGER));
        checkEndRange();

        startHour.addListener(parameter -> {
            checkEndRange();
        });
        startMinute.addListener(parameter -> {
            checkEndRange();
        });
        endHour.addListener(parameter -> {
            checkStartRange();
        });
        endMinute.addListener(parameter -> {
            checkStartRange();
        });
    }

    private void checkStartRange() {
        int numStartMinutes = startHour.getValuei() * 60 + startMinute.getValuei();
        int numEndMinutes = endHour.getValuei() * 60 + endMinute.getValuei();

        if (numEndMinutes < numStartMinutes) {
            startHour.setValue((numStartMinutes-2) / 60);
            startMinute.setValue((numStartMinutes-2) % 60);
        }
    }
    private void checkEndRange() {
        int numStartMinutes = startHour.getValuei() * 60 + startMinute.getValuei();
        int numEndMinutes = endHour.getValuei() * 60 + endMinute.getValuei();

        if (numEndMinutes < numStartMinutes) {
            endHour.setValue((numStartMinutes+2) / 60);
            endMinute.setValue((numStartMinutes+2) % 60);
        }
    }

    protected void run(double deltaMs) {
        calendar.getTime();
        int hours = calendar.get(Calendar.HOUR_OF_DAY);
        int minutes = calendar.get(Calendar.MINUTE);

        //System.out.println("Hours: " + hours + ", minutes: " + minutes);

        if (hours >= startHour.getValuei() && minutes >= startMinute.getValuei()
            && hours < endHour.getValuei() && minutes < endMinute.getValuei()) {
            if (!isOn) turnOn();
        } else {
            if (isOn) turnOff();
        }
    }

    private void turnOn() {
        SLStudio.applet.lx_OG.engine.output.enabled.setValue(true);
        SLStudio.applet.lx_OG.engine.output.brightness.setValue(1);
    }

    private void turnOff() {
        SLStudio.applet.lx_OG.engine.output.enabled.setValue(true);
        SLStudio.applet.lx_OG.engine.output.brightness.setValue(1);
    }

    public static ScheduleControls getInstance(LX lx) {
        if (instance == null)
            instance = new ScheduleControls(lx);

        return instance;
    }

}
