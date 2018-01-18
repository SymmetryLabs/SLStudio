package com.symmetrylabs.slstudio;

import java.util.WeakHashMap;
import java.io.File;

import heronarts.lx.LX;
import heronarts.lx.LXChannel;
import heronarts.lx.LXPattern;
import heronarts.lx.LXEffect;
import heronarts.lx.LXSerializable;
import heronarts.lx.LXMappingEngine;
import heronarts.lx.effect.BlurEffect;
import heronarts.lx.effect.DesaturationEffect;
import heronarts.lx.effect.FlashEffect;
import heronarts.lx.model.LXModel;
import heronarts.lx.pattern.SolidColorPattern;
import heronarts.lx.pattern.IteratorTestPattern;

import com.symmetrylabs.LXClassLoader;
import com.symmetrylabs.util.Utils;
import com.symmetrylabs.slstudio.palettes.PaletteLibrary;

public class SLHeadlessLX extends LX {

    public static final String COPYRIGHT = "Symmetry Labs";

    private static final String DEFAULT_PROJECT_FILE = "default.lxp";
    private static final String PROJECT_FILE_NAME = ".lxproject";

    public SLHeadlessLX(LXModel model) {
        this(model, true);
    }

    public SLHeadlessLX(LXModel model, boolean startEngineThread) {
        super(model);

        /*
        try {
            File projectFile = Utils.saveFile(PROJECT_FILE_NAME);
            if (projectFile.exists()) {
                String[] lines = Utils.loadStrings(PROJECT_FILE_NAME);
                if (lines != null && lines.length > 0) {
                    File file = Utils.saveFile(lines[0]);
                    if (file.exists()) {
                        openProject(file);
                    }
                }
            } else {
                File defaultProject = Utils.saveFile(DEFAULT_PROJECT_FILE);
                if (defaultProject.exists()) {
                    openProject(defaultProject);
                }
            }
        } catch (Exception x) {
            // ignored
        }
        */

        initialize(this);

        this.engine.setThreaded(startEngineThread);
    }

    @Override
    protected void setProject(File file, ProjectListener.Change change) {
        super.setProject(file, change);

        if (file != null) {
            /*
            Utils.saveStrings(
                PROJECT_FILE_NAME,
                new String[]{
                    // Relative path of the project file WRT the default save file location for the sketch
                    Utils.saveFile(PROJECT_FILE_NAME).toPath().getParent().relativize(file.toPath()).toString()
                }
            );
            */
        }
    }

    private static void initialize(LX lx) {
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

    @Override
    protected LXEffect instantiateEffect(final String className) {
        return super.instantiateEffect(LXClassLoader.guessExistingEffectClassName(className));
    }

    @Override
    protected LXPattern instantiatePattern(final String className) {
        return super.instantiatePattern(LXClassLoader.guessExistingPatternClassName(className));
    }
}
