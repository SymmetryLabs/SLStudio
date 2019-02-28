package com.symmetrylabs.slstudio.pattern;

import static com.symmetrylabs.util.MathUtils.*;

import com.symmetrylabs.slstudio.ApplicationState;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import com.symmetrylabs.slstudio.ui.v2.FileDialog;
import heronarts.lx.LX;
import heronarts.lx.PolyBuffer;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.EnumParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.StringParameter;
import heronarts.lx.transform.LXMatrix;
import heronarts.lx.transform.LXVector;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import javax.imageio.ImageIO;


public class ImageProject extends SLPattern<SLModel> {
    private static final String TAG = "ImageProject";

    public enum Projection {
        PLANAR,
        MERCATOR,
    };

    private final StringParameter file = new StringParameter("file", null);
    private final BooleanParameter chooseFile = new BooleanParameter("pick", false).setMode(BooleanParameter.Mode.MOMENTARY);
    private final CompoundParameter thetaParam = new CompoundParameter("theta", 0, -180, 180);
    private final CompoundParameter phiParam = new CompoundParameter("phi", 0, -90, 90);
    private final EnumParameter<Projection> projectionParam = new EnumParameter<>("proj", Projection.PLANAR);

    private final float pointu[];
    private final float pointv[];
    float umin, umax, vmin, vmax;
    boolean cacheok = false;

    private BufferedImage image = null;

    public ImageProject(LX lx) {
        super(lx);
        addParameter(file);
        addParameter(chooseFile);
        addParameter(thetaParam);
        addParameter(phiParam);
        addParameter(projectionParam);

        pointu = new float[model.size];
        pointv = new float[model.size];
    }

    @Override
    public void onActive() {
        super.onActive();
        if (image == null && file.getString() != null) {
            loadFile();
        }
    }

    @Override
    public void onParameterChanged(LXParameter p) {
        if (p == chooseFile && chooseFile.getValueb()) {
            FileDialog.open(lx, "Choose image:", load -> {
                    Path loadPath = load.toPath().toAbsolutePath();
                    Path repoRoot = Paths.get("").toAbsolutePath();
                    Path rel = repoRoot.relativize(loadPath);
                    file.setValue(rel.toString());
                    loadFile();
                });
        } else {
            cacheok = false;
        }
    }

    @Override
    public void onVectorsChanged() {
        super.onVectorsChanged();
        project();
    }

    private void project() {
        umin = Float.POSITIVE_INFINITY;
        umax = Float.NEGATIVE_INFINITY;
        vmin = Float.POSITIVE_INFINITY;
        vmax = Float.NEGATIVE_INFINITY;
        float theta = (float) Math.PI / 180 * thetaParam.getValuef();
        float phi = (float) Math.PI / 180 * phiParam.getValuef();
        Projection proj = projectionParam.getEnum();

        switch (proj) {
        case PLANAR: {
            LXMatrix xf = LXMatrix.identity().rotateX(phi).rotateY(theta);
            LXVector u = xf.apply(1, 0, 0);
            LXVector v = xf.apply(0, 1, 0);

            for (LXVector vec : getVectors()) {
                float uval = u.dot(vec);
                float vval = v.dot(vec);
                if (uval < umin) {
                    umin = uval;
                }
                if (uval > umax) {
                    umax = uval;
                }
                if (vval < vmin) {
                    vmin = vval;
                }
                if (vval > vmax) {
                    vmax = vval;
                }
            }
            for (LXVector vec : getVectors()) {
                pointu[vec.index] = u.dot(vec);
                pointv[vec.index] = v.dot(vec);
            }
            break;
        }
        case MERCATOR: {
            umin = (float) -Math.PI;
            umax = (float) Math.PI;
            vmin = 0;
            vmax = (float) Math.PI;
            LXVector center = new LXVector(model.cx, model.cy, model.cz);
            for (LXVector vec : getVectors()) {
                LXVector x = center.copy().mult(-1).add(vec);
                x.normalize();
                pointu[vec.index] = (float) Math.atan2(x.x, -x.z) + theta;
                pointv[vec.index] = (float) Math.acos(x.y) + phi;
            }
            break;
        }
        }
        cacheok = true;
    }

    private void loadFile() {
        ApplicationState.setWarning(TAG, null);
        String path = file.getString();
        if (path == null) {
            return;
        }
        try {
            image = ImageIO.read(new File(path));
            project();
        } catch (IOException e) {
            ApplicationState.setWarning(TAG, String.format("could not read file %s: %s", path, e.getMessage()));
        }
    }

    @Override
    public void run(double elapsedMs, PolyBuffer.Space preferredSpace) {
        int[] ccs = (int[]) getArray(PolyBuffer.Space.SRGB8);
        if (image == null) {
            Arrays.fill(ccs, 0);
        } else {
            if (!cacheok) {
                project();
            }
            for (LXVector vec : getVectors()) {
                int i = Math.round(mapPeriodic(pointu[vec.index], umin, umax, 0, image.getWidth() - 1));
                int j = Math.round(mapPeriodic(pointv[vec.index], vmin, vmax, image.getHeight() - 1, 0));
                ccs[vec.index] = image.getRGB(i, j);
            }
        }
        markModified(PolyBuffer.Space.SRGB8);
    }
}
