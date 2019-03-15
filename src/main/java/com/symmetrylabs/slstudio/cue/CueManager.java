package com.symmetrylabs.slstudio.cue;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.symmetrylabs.slstudio.SLStudioLX;
import com.symmetrylabs.util.CaptionSource;
import com.symmetrylabs.util.FileUtils;
import heronarts.lx.LX;
import heronarts.lx.LXComponent;
import heronarts.lx.LXLoopTask;
import heronarts.lx.LXSerializable;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.StringParameter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Duration;
import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import static com.symmetrylabs.util.MathUtils.*;


public class CueManager implements LXLoopTask, CaptionSource, SLStudioLX.SaveHook {
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

    protected LX lx;
    private final List<CueData> cues = new ArrayList<>();
    private final List<CueListListener> listListeners = new ArrayList<>();

    private CueData current;
    private double t;
    private double currentCueValueAtStart;
    private PeriodFormatter periodFormatter;

    public CueManager(LX lx) {
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
        ArrayList<Cue> list = new ArrayList<>();
        for (CueData cd : cues) {
            list.add(cd.cue);
        }
        return list;
    }

    public void addCue(Cue cue) {
        cues.add(new CueData(cue));
        for (CueListListener cll : listListeners) {
            cll.cueListChanged();
        }
    }

    public void removeCue(Cue cue) {
        Iterator<CueData> cds = cues.iterator();
        boolean changed = false;
        while (cds.hasNext()) {
            CueData cd = cds.next();
            if (cd.cue == cue) {
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

    private static File getCueFile() {
        return FileUtils.getShowFile("cues.json");
    }

    private static final String KEY_CUES = "cues";

    public void save(JsonObject obj) {
        JsonArray cueArr = new JsonArray();
        for (CueData cd : cues) {
            JsonObject cueObj = new JsonObject();
            cd.cue.save(cueObj);
            cueArr.add(cueObj);
        }
        obj.add(KEY_CUES, cueArr);
    }

    public void load(JsonObject obj) {
        cues.clear();
        if (obj.has(KEY_CUES)) {
            JsonArray cueArr = obj.getAsJsonArray(KEY_CUES);
            for (JsonElement cueElem : cueArr) {
                JsonObject cueObj = cueElem.getAsJsonObject();
                Cue c = new Cue(lx, lx.engine.output.brightness);
                c.load(cueObj);
                addCue(c);
            }
        }
    }

    public void loadFromCueFile() {
        File inf = getCueFile();
        if (inf.exists()) {
            try (FileReader fr = new FileReader(inf)) {
                JsonObject obj = new Gson().fromJson(fr, JsonObject.class);
                load(obj);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onSave() {
        File outf = getCueFile();
        JsonObject obj = new JsonObject();
        this.save(obj);
        try {
            JsonWriter writer = new JsonWriter(new FileWriter(outf));
            new GsonBuilder().create().toJson(obj, writer);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
