package com.symmetrylabs.slstudio.ui;

import com.symmetrylabs.slstudio.output.ModelCsvWriter;
import com.symmetrylabs.slstudio.output.OfflineRenderOutput;
import heronarts.lx.LX;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.StringParameter;
import heronarts.p3lx.ui.UI2dContainer;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.component.UIButton;
import heronarts.p3lx.ui.component.UIDoubleBox;
import heronarts.p3lx.ui.component.UIIntegerBox;
import heronarts.p3lx.ui.component.UILabel;
import heronarts.p3lx.ui.component.UITextBox;
import heronarts.p3lx.ui.studio.UICollapsibleSection;
import java.awt.EventQueue;
import java.awt.FileDialog;
import java.awt.Frame;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import processing.core.PConstants;
import processing.event.MouseEvent;

public class UIOfflineRender extends UICollapsibleSection {
    private static final int HEIGHT = 110;

    private final StringParameter renderTarget = new StringParameter("target", "scene.lxo");
    private final BooleanParameter writeModel = new BooleanParameter("writeModel", false).setMode(BooleanParameter.Mode.MOMENTARY);

    private OfflineRenderOutput output;

    public UIOfflineRender(UI ui, LX lx, float x, float y, float w) {
        super(ui, x, y, w, HEIGHT);

        output = new OfflineRenderOutput(lx);
        lx.addOutput(output);

        setTitle("OFFLINE RENDER");

        final int pad = 6;
        final int height = 14;
        int cy = pad;

        new UILabel(6, cy, 46, height)
            .setLabel("File")
            .setTextAlignment(PConstants.LEFT, PConstants.CENTER)
            .addToContainer(this);
        new UIFileBox(52, cy, w - 64, height)
            .setParameter(output.pOutputFile)
            .setTextAlignment(PConstants.LEFT, PConstants.CENTER)
            .addToContainer(this);
        cy += height + pad;

        new UILabel(6, cy, 46, height)
            .setLabel("Frames")
            .setTextAlignment(PConstants.LEFT, PConstants.CENTER)
            .addToContainer(this);
        new UIIntegerBox(52, cy, w - 64, height)
            .setParameter(output.pFramesToCapture)
            .setTextAlignment(PConstants.RIGHT, PConstants.CENTER)
            .addToContainer(this);
        cy += height + pad;

        new UILabel(6, cy, 46, height)
            .setLabel("FPS")
            .setTextAlignment(PConstants.LEFT, PConstants.CENTER)
            .addToContainer(this);
        new UIDoubleBox(52, cy, w - 64, height)
            .setParameter(output.pFrameRate)
            .setTextAlignment(PConstants.RIGHT, PConstants.CENTER)
            .addToContainer(this);
        cy += height + pad;

        new UITextBox(6, cy, 46, 18)
            .setParameter(output.pStatus)
            .setCanEdit(false)
            .setTextAlignment(PConstants.CENTER, PConstants.CENTER)
            .addToContainer(this);

        new UIButton(w - 18 - 52 - 80, cy, 80, 18)
            .setParameter(writeModel)
            .setLabel("WriteModel")
            .addToContainer(this);

        new UIButton(w - 12 - 52, cy, 52, 18)
            .setParameter(output.pStart)
            .setLabel("Start")
            .addToContainer(this);

        writeModel.addListener((p) -> {
                if (!writeModel.getValueb()) {
                    return;
                }
                EventQueue.invokeLater(() -> {
                        FileDialog dialog = new FileDialog((Frame) null, "Save model data as:", FileDialog.SAVE);
                        dialog.setVisible(true);
                        String fname = dialog.getFile();
                        if (fname == null) {
                            return;
                        }
                        try {
                            FileWriter fw = new FileWriter(new File(dialog.getDirectory(), fname));
                            ModelCsvWriter.write(lx.model, fw);
                            fw.close();
                        } catch (IOException e) {
                            System.err.println("Couldn't write model CSV:");
                            e.printStackTrace();
                        }
                    });
            });
    }

    private static class UIFileBox extends UITextBox {
        public UIFileBox(float x, float y, float w, float h) {
            super(x, y, w, h);
            setCanEdit(false);
        }

        public void setValue(File f) {
            setValue(f.getAbsoluteFile().getPath());
        }

        @Override
        public void onMousePressed(MouseEvent event, float mx, float my) {
            setValue("");
            EventQueue.invokeLater(() -> {
                    FileDialog dialog = new FileDialog(
                        (Frame) null, "Save output data as:", FileDialog.SAVE);
                    dialog.setVisible(true);
                    String fname = dialog.getFile();
                    if (fname == null) {
                        return;
                    }
                    setValue(new File(dialog.getDirectory(), fname));
                });
        }
    }
}
