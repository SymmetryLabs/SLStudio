package com.symmetrylabs.slstudio.output;

import heronarts.lx.LX;
import heronarts.lx.PolyBuffer;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.output.LXOutput;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.StringParameter;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;
import java.io.DataOutputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public class OfflineRenderOutput extends LXOutput {
    public static final String HEADER = "SLOutput";
    public static final int VERSION = 3;

    private File output = null;
    private DataOutputStream writer = null;
    private LXModel model;
    private long startFrameNanos;

    public final BooleanParameter enabled = new BooleanParameter("enabled", false);
    public final StringParameter outputFile = new StringParameter("output", "");

    public OfflineRenderOutput(LX lx) {
        super(lx);
        this.model = lx.model;
        outputFile.addListener(p -> {
                dispose();
                if (!outputFile.getString().equals("")) {
                    output = new File(outputFile.getString());
                }
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
            synchronized (writer) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
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
                writer = new DataOutputStream(out);
                writer.writeUTF(HEADER);
                writer.writeInt(VERSION);
                writer.writeInt(model.size);
                for (LXPoint p : model.points) {
                    writer.writeFloat(p.x);
                    writer.writeFloat(p.y);
                    writer.writeFloat(p.z);
                }
                startFrameNanos = System.nanoTime();
            } catch (IOException e) {
                System.err.println("could not write model:");
                e.printStackTrace();
                dispose();
                return;
            }
        }

        synchronized (writer) {
            long frameTime = System.nanoTime() - startFrameNanos;
            int[] data = (int[]) colors.getArray(PolyBuffer.Space.RGB8);

            try {
                writer.writeLong(frameTime);
                ByteBuffer bb = ByteBuffer.allocate(4 * data.length);
                IntBuffer ib = bb.asIntBuffer();
                ib.put(data);
                byte[] arr = bb.array();
                writer.writeInt(arr.length);
                writer.write(arr, 0, arr.length);
            } catch (IOException e) {
                System.err.println("failed to write color data:");
                e.printStackTrace();
                dispose();
            }
        }
    }
}
