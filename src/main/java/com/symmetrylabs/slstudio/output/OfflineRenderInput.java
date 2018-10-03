package com.symmetrylabs.slstudio.output;

import com.symmetrylabs.slstudio.SLStudio;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import java.util.ArrayList;
import java.io.DataInput;
import java.util.Optional;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import heronarts.lx.LX;
import heronarts.lx.PolyBuffer;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.StringParameter;

import java.awt.EventQueue;
import java.awt.FileDialog;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;

public class OfflineRenderInput extends SLPattern<SLModel> {
    private File input = null;
    private LXModel model;
    private long lastFrameNanos;

    private static class Frame {
        double t;
        int[] colors;
    }
    private List<Frame> frames;

    public final StringParameter inputFile = new StringParameter("output", "");
    public final BooleanParameter chooseInput = new BooleanParameter("pick", false).setMode(BooleanParameter.Mode.MOMENTARY);
    public final BooleanParameter sync = new BooleanParameter("sync", false).setMode(BooleanParameter.Mode.MOMENTARY);

    private double time;
    private double timeMax;
    private int frameIndex; /* stored in a field so it can be included in the caption */

    public OfflineRenderInput(LX lx) {
        super(lx);
        this.model = lx.model;
        addParameter(inputFile);
        addParameter(chooseInput);
        addParameter(sync);
    }

    @Override
    public void onParameterChanged(LXParameter p) {
        if (p == inputFile) {
            String inputStr = inputFile.getString();
            if (inputStr == null || inputStr.equals("")) {
                input = null;
                frames.clear();
                return;
            }
            input = new File(inputFile.getString());
            lx.engine.addTask(() -> { loadInput(); });
        } else if (p == chooseInput && chooseInput.getValueb()) {
            inputFile.setValue("");
            EventQueue.invokeLater(() -> {
                    FileDialog dialog = new FileDialog(
                        (java.awt.Frame) null, "Read input data from:", FileDialog.LOAD);
                    dialog.setVisible(true);
                    String fname = dialog.getFile();
                    if (fname == null) {
                        return;
                    }
                    inputFile.setValue(new File(dialog.getDirectory(), fname).getAbsoluteFile().getPath());
                });
        } else if (p == sync && sync.getValueb()) {
            time = 0;
        }
    }

    private Frame loadFrame(DataInputStream stream) throws IOException {
        Frame f = new Frame();
        long ns = stream.readLong();
        f.t = 1e-6 * (double) ns;
        int n = stream.readInt();
        ByteBuffer bb = ByteBuffer.allocate(n);
        stream.readFully(bb.array());
        f.colors = new int[n / 4];
        bb.asIntBuffer().get(f.colors);
        return f;
    }

    protected void loadInput() {
        long startTime = System.nanoTime();
        frames = new ArrayList<>();
        try {
            InputStream in = new GZIPInputStream(new FileInputStream(input));
            DataInputStream reader = new DataInputStream(in);
            String header = reader.readUTF();
            if (!header.equals(OfflineRenderOutput.HEADER)) {
                throw new RuntimeException(String.format("bad header string '%s'", header));
            }
            int version = reader.readInt();
            if (version != OfflineRenderOutput.VERSION) {
                throw new RuntimeException(
                    String.format(
                        "version mismatch: file is version %d, code supports version %d",
                        version, OfflineRenderOutput.VERSION));
            }
            int modelSize = reader.readInt();
            /* discard model points */
            for (int i = 0; i < modelSize; i++) {
                reader.readFloat();
                reader.readFloat();
                reader.readFloat();
            }

            while (reader.available() > 0) {
                frames.add(loadFrame(reader));
            }
            timeMax = frames.isEmpty() ? 0 : frames.get(frames.size() - 1).t;
            SLStudio.setWarning("OfflineRenderInput", null);
        } catch (IOException | RuntimeException e) {
            SLStudio.setWarning(
                "OfflineRenderInput",
                String.format("couldn't load input file: %s", e.getMessage()));
            e.printStackTrace();
        }
        long elapsedNs = System.nanoTime() - startTime;
        System.out.println(
            String.format(
                "loaded %d frames in %.0fms",
                frames == null ? 0 : frames.size(),
                elapsedNs * 1e-6));
    }

    @Override
    public String getCaption() {
        return String.format(
            "%s / %05d frames / %05d f / %06.0f t / %06.0f tmax / % 3.0f source fps",
            input == null ? "no file" : input.getAbsolutePath(),
            frames == null ? 0 : frames.size(),
            frameIndex,
            time,
            timeMax,
            frames == null ? 0 : 1000 / (timeMax / frames.size()));
    }

    @Override
    public void onInactive() {
        super.onInactive();
        SLStudio.setWarning("OfflineRenderInput", null);
    }

    @Override
    public void run(double elapsedMs, PolyBuffer.Space preferredSpace) {
        int[] colors = (int[]) getArray(PolyBuffer.Space.RGB8);
        if (frames == null || frames.size() == 0) {
            Arrays.fill(colors, 0xFF000000);
            markModified(PolyBuffer.Space.RGB8);
            return;
        }

        time += elapsedMs;
        frameIndex = 0;
        for (frameIndex = 1; frameIndex < frames.size(); frameIndex++) {
            if (frames.get(frameIndex).t > time) {
                frameIndex--;
                break;
            }
        }
        if (frameIndex == frames.size()) {
            Arrays.fill(colors, 0xFF000000);
            markModified(PolyBuffer.Space.RGB8);
            return;
        }

        Frame f = frames.get(frameIndex);
        if (colors.length != f.colors.length) {
            SLStudio.setWarning("OfflineRenderInput", "input file has wrong number of points");
            Arrays.fill(colors, 0xFF000000);
            markModified(PolyBuffer.Space.RGB8);
            return;
        }

        for (int j = 0; j < colors.length; j++) {
            colors[j] = f.colors[j];
        }
        markModified(PolyBuffer.Space.RGB8);
    }
}
