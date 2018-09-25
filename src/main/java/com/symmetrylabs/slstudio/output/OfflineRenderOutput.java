package com.symmetrylabs.slstudio.output;

import heronarts.lx.LX;
import heronarts.lx.PolyBuffer;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.output.LXOutput;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.StringParameter;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.zip.GZIPOutputStream;

public class OfflineRenderOutput extends LXOutput {
    private File output = null;
    private Writer writer = null;
    private LXModel model;
    private long lastFrameNanos;

    public final BooleanParameter enabled = new BooleanParameter("enabled", false);
    public final StringParameter outputFile = new StringParameter("output", "");

    public OfflineRenderOutput(LX lx) {
        super(lx);
        this.model = lx.model;
        outputFile.addListener(p -> {
                dispose();
                output = new File(outputFile.getString());
            });
        enabled.addListener(p -> {
                if (!enabled.getValueb()) {
                    dispose();
                    writer = null;
                }
            });
    }

    public void dispose() {
        if (writer != null) {
            try {
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        writer = null;
        output = null;
    }

    @Override
    protected void onSend(PolyBuffer colors) {
        if (!enabled.getValueb() || output == null) {
            return;
        }

        if (writer == null && output != null) {
            try {
                OutputStream out = new GZIPOutputStream(new FileOutputStream(output));
                writer = new BufferedWriter(new OutputStreamWriter(out));
                writer.write(String.format("SLOutput v=1 pn=%d\n", model.size));
                for (LXPoint p : model.points) {
                    writer.write(String.format("%f %f %f\n", p.x, p.y, p.z));
                }
                lastFrameNanos = System.nanoTime();
            } catch (IOException e) {
                System.err.println("could not write model:");
                e.printStackTrace();
                dispose();
                return;
            }
        }

        long timeSinceLast = System.nanoTime() - lastFrameNanos;
        lastFrameNanos += timeSinceLast;
        int[] data = (int[]) colors.getArray(PolyBuffer.Space.RGB8);

        try {
            writer.write(String.format("t=%d", timeSinceLast));
            writer.write(" c=");
            for (int i = 0; i < data.length; i++) {
                writer.write(String.format(i == 0 ? "%x" : ",%x", data[i]));
            }
            writer.write("\n");
        } catch (IOException e) {
            System.err.println("failed to write color data:");
            e.printStackTrace();
            dispose();
        }
    }
}
