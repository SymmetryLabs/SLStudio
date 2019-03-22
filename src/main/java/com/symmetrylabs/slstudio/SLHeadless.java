package com.symmetrylabs.slstudio;

import java.io.File;

import heronarts.lx.LX;
import heronarts.lx.LXChannel;
import heronarts.lx.LXPattern;
import heronarts.lx.LXEffect;
import heronarts.lx.LXSerializable;
import heronarts.lx.LXMappingEngine;
import heronarts.lx.data.Project;
import heronarts.lx.effect.BlurEffect;
import heronarts.lx.effect.DesaturationEffect;
import heronarts.lx.effect.FlashEffect;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.GridModel;
import heronarts.lx.pattern.SolidColorPattern;
import heronarts.lx.pattern.IteratorTestPattern;
import heronarts.lx.output.OPCOutput;

import com.symmetrylabs.LXClassLoader;
import com.symmetrylabs.util.Utils;
import com.symmetrylabs.slstudio.palettes.PaletteLibrary;

public class SLHeadless {

    public static final String COPYRIGHT = "Symmetry Labs";

    private static final String DEFAULT_PROJECT_FILE = "default.lxp";
    private static final String PROJECT_FILE_NAME = ".lxproject";

    private final LX lx;

    public SLHeadless() {
        Utils.setSketchPath(System.getProperty("user.dir"));

        LXModel model = new GridModel(10, 10);

        lx = new LX(model);

        try {
            File projectFile = Utils.saveFile(PROJECT_FILE_NAME);
            if (projectFile.exists()) {
                String[] lines = Utils.loadStrings(PROJECT_FILE_NAME);
                if (lines != null && lines.length > 0) {
                    File file = Utils.saveFile(lines[0]);
                    if (file.exists()) {
                        lx.openProject(Project.createLegacyProject(file, 0));
                    }
                }
            } else {
                File defaultProject = Utils.saveFile(DEFAULT_PROJECT_FILE);
                if (defaultProject.exists()) {
                    lx.openProject(Project.createLegacyProject(defaultProject, 0));
                }
            }
        } catch (Exception x) {
            // ignored
        }

        lx.addProjectListener((project, change) -> {
            if (project != null) {
                Utils.saveStrings(
                    PROJECT_FILE_NAME,
                    new String[]{
                        // Relative path of the project file WRT the default save file location for the sketch
                        Utils.saveFile(PROJECT_FILE_NAME).toPath().getParent().relativize(project.getRoot()).toString()
                    }
                );
            }
        });

        lx.addOutput(new OPCOutput(lx, "localhost", 11122));

        registerPatternsAndEffects(lx);

        LXChannel channel = lx.engine.addChannel(new LXPattern[] { new IteratorTestPattern(lx) });
        channel.enabled.setValue(true);
        channel.fader.setValue(1f);

        lx.engine.isChannelMultithreaded.setValue(true);
        lx.engine.isNetworkMultithreaded.setValue(true);
        lx.engine.audio.enabled.setValue(true);
        lx.engine.output.enabled.setValue(true);

        lx.engine.setThreaded(true);
        lx.engine.onDraw();
    }

    public static void main(String[] args) {
        new SLHeadless();
    }

    private static void registerPatternsAndEffects(LX lx) {
        // Add all effects
        LXClassLoader.findEffects().stream().forEach(c -> lx.registerEffect(c));

        // Add all patterns
        LXClassLoader.findPatterns().stream().forEach(c -> lx.registerPattern(c));

        lx.registerPattern(SolidColorPattern.class);
        lx.registerPattern(IteratorTestPattern.class);

        lx.registerEffect(FlashEffect.class);
        lx.registerEffect(BlurEffect.class);
        lx.registerEffect(DesaturationEffect.class);
    }
}
