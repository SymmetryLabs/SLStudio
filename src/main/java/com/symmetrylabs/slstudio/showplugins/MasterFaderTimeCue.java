package com.symmetrylabs.slstudio.showplugins;

import heronarts.lx.LXLoopTask;
import heronarts.lx.LX;
import com.symmetrylabs.slstudio.SLStudioLX;
import com.symmetrylabs.util.CaptionSource;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import java.util.List;
import java.util.ArrayList;
import org.joda.time.Duration;
import com.google.common.base.Preconditions;
import static com.symmetrylabs.util.MathUtils.*;
import java.util.Collections;
import org.joda.time.Days;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;


public class MasterFaderTimeCue implements LXLoopTask, CaptionSource {
    private final LX lx;

    public static class Cue {
        public DateTime startAt;
        public double durationSeconds;
        public double fadeTo;

        private DateTime lastStartedAt;
        private double faderWhenStarted;

        public Cue(DateTime startAt, double durationSeconds, double fadeTo) {
            this.startAt = startAt;
            this.durationSeconds = durationSeconds;
            this.fadeTo = fadeTo;
        }

        @Override
        public String toString() {
            return String.format("%.3f second fade to %.0f%% starting at %02d:%02d",
                                 durationSeconds, 100 * fadeTo, startAt.getHourOfDay(), startAt.getMinuteOfHour());
        }
    }

    private final List<Cue> cues = new ArrayList<>();

    /* these are only used for displaying current cue information in the UI */
    private Cue current;
    private double t;
    private PeriodFormatter periodFormatter;

    public MasterFaderTimeCue(LX lx, SLStudioLX.UI ui) {
        this.lx = lx;
        periodFormatter = new PeriodFormatterBuilder()
            .minimumPrintedDigits(2)
            .appendHours()
            .appendSuffix("h")
            .appendMinutes()
            .appendSuffix("m")
            .printZeroAlways()
            .appendSeconds()
            .appendSuffix("s")
            .toFormatter();
    }

    public List<Cue> getCues() {
        return Collections.unmodifiableList(cues);
    }

    public void addCue(Cue cue) {
        cues.add(cue);
    }

    public void removeCue(Cue cue) {
        cues.remove(cue);
    }

    @Override
    public void loop(double deltaMs) {
        DateTime now = DateTime.now();
        current = null;
        double elapsedSec = 0;

        for (Cue cue : cues) {
            if (cue.lastStartedAt != null) {
                Duration elapsed = new Duration(cue.lastStartedAt, now);
                double seconds = elapsed.getMillis() / 1000.0;
                if (seconds <= cue.durationSeconds) {
                    current = cue;
                    elapsedSec = seconds;
                    break;
                } else if (seconds < 60) {
                    /* we prevent cues from triggering twice in the same minute
                       (since cues are given with accuracy only to the minute in
                       the UI). */
                    continue;
                }
            }
            if (cue.startAt.getMinuteOfDay() == now.getMinuteOfDay()) {
                cue.lastStartedAt = now;
                cue.faderWhenStarted = lx.engine.output.brightness.getValue();
                current = cue;
                elapsedSec = 0;
                break;
            }
        }
        if (current == null) {
            return;
        }

        t = constrain(elapsedSec / current.durationSeconds, 0, 1);
        double newFader = current.faderWhenStarted + (current.fadeTo - current.faderWhenStarted) * t;
        lx.engine.output.brightness.setValue(newFader);
    }

    @Override
    public String getCaption() {
        /* single assignment, so that cue cannot become null between the if and the format */
        Cue c = current;
        if (c == null) {
            Cue next = null;
            Duration timeUntilNext = null;
            DateTime now = DateTime.now();
            for (Cue cue : cues) {
                DateTime nextTrigger = cue.startAt.withDate(now.toLocalDate());
                if (nextTrigger.isBefore(now)) {
                    nextTrigger = nextTrigger.plus(Days.ONE);
                }
                Duration timeUntilTrigger = new Duration(now, nextTrigger);
                if (timeUntilNext == null || timeUntilTrigger.isShorterThan(timeUntilNext)) {
                    next = cue;
                    timeUntilNext = timeUntilTrigger;
                }
            }
            if (next != null) {
                return String.format("no cue active, %s until %s", periodFormatter.print(timeUntilNext.toPeriod()), next);
            }
            return "no cues active";
        }
        return String.format("%.0f%% done with %s", 100 * t, c);
    }

    public static void attach(LX lx, SLStudioLX.UI ui) {
        MasterFaderTimeCue mft = new MasterFaderTimeCue(lx, ui);
        lx.engine.addLoopTask(mft);
        ui.captionText.addSource(mft);
    }
}
