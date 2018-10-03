package com.symmetrylabs.slstudio.ui;

import com.symmetrylabs.slstudio.output.OfflineRenderOutput;
import heronarts.lx.LX;
import heronarts.lx.parameter.StringParameter;
import heronarts.p3lx.ui.UI2dContainer;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.component.UIButton;
import heronarts.p3lx.ui.component.UILabel;
import heronarts.p3lx.ui.component.UITextBox;
import heronarts.p3lx.ui.studio.UICollapsibleSection;
import java.awt.EventQueue;
import java.awt.FileDialog;
import java.awt.Frame;
import java.io.File;
import processing.core.PConstants;
import processing.event.MouseEvent;

public class UIOfflineRender extends UICollapsibleSection {
    private static final int HEIGHT = 44;

    private final StringParameter renderTarget = new StringParameter("target", "scene.lxo");
    private OfflineRenderOutput output;

    public UIOfflineRender(UI ui, LX lx, float x, float y, float w) {
        super(ui, x, y, w, HEIGHT);

        output = new OfflineRenderOutput(lx);
        lx.addOutput(output);

        setTitle("OFFLINE RENDER");
        setTitleX(20);
        addTopLevelComponent(
            new UIButton(4, 4, 12, 12).setParameter(output.enabled).setBorderRounding(4));

        UI2dContainer border = (UI2dContainer) new UI2dContainer(0, 0, getContentWidth(), getContentHeight())
            .setBackgroundColor(ui.theme.getDarkBackgroundColor())
            .setBorderRounding(4)
            .addToContainer(this);

        new UILabel(6, 6, 46, 14)
            .setLabel("File")
            .setTextAlignment(PConstants.LEFT, PConstants.CENTER)
            .addToContainer(border);

        new UIFileBox(52, 6, w - 64, 14)
            .setParameter(output.outputFile)
            .setTextAlignment(PConstants.LEFT, PConstants.CENTER)
            .addToContainer(border);
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
