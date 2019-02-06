package com.symmetrylabs.slstudio.ui;

import com.symmetrylabs.slstudio.output.DoubleBufferedOfflineRenderOutput;
import com.symmetrylabs.slstudio.output.ModelCsvWriter;
import heronarts.lx.LX;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.StringParameter;
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

import javax.swing.*;

public class UIOfflineRender extends UICollapsibleSection {
    private static final int HEIGHT = 110;

    private final StringParameter renderTarget = new StringParameter("target", "scene.lxo");
    private final BooleanParameter writeModel = new BooleanParameter("writeModel", false).setMode(BooleanParameter.Mode.MOMENTARY);

    private final BooleanParameter useContinuous = new BooleanParameter("useContinuous", false);

//    private OfflineRenderOutput output;
    private DoubleBufferedOfflineRenderOutput output;

    public UIOfflineRender(UI ui, LX lx, float x, float y, float w) {
        super(ui, x, y, w, HEIGHT);


        useContinuous.setValue(true);
        if (useContinuous.getValueb()){
           output = new DoubleBufferedOfflineRenderOutput(lx);
           lx.addOutput(output);
           setTitle("CONTINUOUS OFFLINE RENDER");
        }


        final int pad = 6;
        final int height = 14;
        int cy = pad;

        if (useContinuous.getValueb()){
            new UILabel(6, cy, 46, height)
                .setLabel("Directory")
                .setTextAlignment(PConstants.LEFT, PConstants.CENTER)
                .addToContainer(this);
        }else{
            new UILabel(6, cy, 46, height)
                .setLabel("File")
                .setTextAlignment(PConstants.LEFT, PConstants.CENTER)
                .addToContainer(this);
        }
        if (useContinuous.getValueb()){
            new ContinuousUIFileBox(52, cy, w - 64, height)
                .setParameter(output.pOutputDir)
                .setTextAlignment(PConstants.LEFT, PConstants.CENTER)
                .addToContainer(this);
        } else{
            new UIFileBox(52, cy, w - 64, height)
                .setParameter(output.pOutputFile)
                .setTextAlignment(PConstants.LEFT, PConstants.CENTER)
                .addToContainer(this);
        }
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

        new UIButton(w - 18 - 52 - 80 - 52, cy, 80, 18)
            .setParameter(output.externalSync)
            .setLabel("EXT")
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


    private static class ContinuousUIFileBox extends UITextBox {
        public ContinuousUIFileBox(float x, float y, float w, float h) {
            super(x, y, w, h);
            setCanEdit(false);
        }

//        public void setValue(File f) {
//            setValue(f.getAbsoluteFile().getPath());
//        }

        @Override
        public void onMousePressed(MouseEvent event, float mx, float my) {
            setValue("");

            EventQueue.invokeLater(() -> {
                JFileChooser f = new JFileChooser();
                f.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                f.showSaveDialog(null);

//                System.out.println(f.getCurrentDirectory());
//                System.out.println(f.getSelectedFile());

//                String selectedDir = f.getCurrentDirectory().getPath();
                String selectedDir = f.getSelectedFile().getPath();
                System.out.println("Set value to: " + selectedDir);
                setValue(selectedDir);

//                if (fname == null) {
//                    return;
//                }
//                setValue(new File(f.getCurrentDirectory(), ""));
            });
        }
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
