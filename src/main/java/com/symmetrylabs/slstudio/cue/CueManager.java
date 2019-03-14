package com.symmetrylabs.slstudio.cue;

import com.google.common.base.Preconditions;
import com.symmetrylabs.util.CaptionSource;
import heronarts.lx.LX;
import heronarts.lx.LXComponent;
import heronarts.lx.LXLoopTask;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.StringParameter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Duration;
import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
import java.util.Iterator;

import static com.symmetrylabs.util.MathUtils.*;


public class CueManager extends LXComponent implements LXLoopTask, CaptionSource {
    private class CueData {
        Cue cue;
        DateTime lastStartedAt;

        CueData(Cue cue) {
            this.cue = cue;
            this.lastStartedAt = null;
        }
    }

    public interface CueListListener {
        void cueListChanged();
    }

    private final List<CueData> cues = new ArrayList<>();
    private final List<CueListListener> listListeners = new ArrayList<>();

    private CueData current;
    private double t;
    private double currentCueValueAtStart;
    private PeriodFormatter periodFormatter;

    public CueManager(LX lx) {
        super(lx);
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
        ArrayList<Cue> list = new ArrayList<>();
        for (CueData cd : cues) {
            list.add(cd.cue);
        }
        return list;
    }

    public void addCue(Cue cue) {
        cues.add(new CueData(cue));
        addSubcomponent(cue);
        for (CueListListener cll : listListeners) {
            cll.cueListChanged();
        }
    }

    public void removeCue(Cue cue) {
        Iterator<CueData> cds = cues.iterator();
        boolean changed = false;
        System.out.println("remove " + cue);
        while (cds.hasNext()) {
            CueData cd = cds.next();
            if (cd.cue == cue) {
                cd.cue.dispose();
                cds.remove();
                changed = true;
            }
        }
        if (changed) {
            for (CueListListener cll : listListeners) {
                cll.cueListChanged();
            }
        }
    }

    public void addCueListListener(CueListListener cll) {
        listListeners.add(cll);
    }

    public void removeCueListListener(CueListListener cll) {
        listListeners.remove(cll);
    }

    @Override
    public void loop(double deltaMs) {
        DateTime now = DateTime.now();
        current = null;
        double elapsedMs = 0;

        for (CueData cd : cues) {
            if (cd.lastStartedAt != null) {
                Duration elapsed = new Duration(cd.lastStartedAt, now);
                double ms = elapsed.getMillis();
                if (ms <= cd.cue.durationMs.getValue()) {
                    current = cd;
                    elapsedMs = ms;
                    break;
                } else if (ms < 60_000) {
                    /* we prevent cues from triggering twice in the same minute
                       (since cues are given with accuracy only to the minute in
                       the UI). */
                    continue;
                }
            }
            if (cd.cue.getStartTime().getMinuteOfDay() == now.getMinuteOfDay()) {
                cd.lastStartedAt = now;
                currentCueValueAtStart = cd.cue.cuedParameter.getValue();
                current = cd;
                elapsedMs = 0;
                break;
            }
        }
        if (current == null) {
            return;
        }

        Cue cc = current.cue;
        t = constrain(elapsedMs / cc.durationMs.getValue(), 0, 1);
        double newFader = currentCueValueAtStart + (cc.fadeTo.getValue() - currentCueValueAtStart) * t;
        cc.cuedParameter.setValue(newFader);
    }

    @Override
    public String getCaption() {
        /* single assignment, so that cue cannot become null between the if and the format */
        CueData c = current;
        if (c == null) {
            Cue next = null;
            Duration timeUntilNext = null;
            DateTime now = DateTime.now();
            for (CueData cd : cues) {
                DateTime nextTrigger = cd.cue.getStartTime().withDate(now.toLocalDate());
                if (nextTrigger.isBefore(now)) {
                    nextTrigger = nextTrigger.plus(Days.ONE);
                }
                Duration timeUntilTrigger = new Duration(now, nextTrigger);
                if (timeUntilNext == null || timeUntilTrigger.isShorterThan(timeUntilNext)) {
                    next = cd.cue;
                    timeUntilNext = timeUntilTrigger;
                }
            }
            if (next != null) {
                return String.format("no cue active, %s until %s", periodFormatter.print(timeUntilNext.toPeriod()), next);
            }
            // no cues defined, so don't print a caption
            return null;
        }
        return String.format("%.0f%% done with %s", 100 * t, c.cue);
    }

    public static CueManager attach(LX lx) {
        CueManager mgr = new CueManager(lx);
        lx.engine.addLoopTask(mgr);
        return mgr;
    }
}
