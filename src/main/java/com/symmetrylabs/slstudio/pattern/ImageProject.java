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

    private static final float PI = (float) Math.PI;
    private static final float HALF_PI = PI / 2;
    private static final float TWO_PI = PI * 2;

    public enum Projection {
        PLANAR,
        MERCATOR,
        S4,
        S8,
    };

    private final StringParameter file = new StringParameter("file", null);
    private final BooleanParameter chooseFile = new BooleanParameter("pick", false).setMode(BooleanParameter.Mode.MOMENTARY);
    private final CompoundParameter thetaParam = new CompoundParameter("theta", 0, -180, 180);
    private final CompoundParameter phiParam = new CompoundParameter("phi", 0, -180, 180);
    private final EnumParameter<Projection> projectionParam = new EnumParameter<>("proj", Projection.PLANAR);
    private final BooleanParameter flipI = new BooleanParameter("flipi", false);
    private final BooleanParameter flipJ = new BooleanParameter("flipj", false);

    private final float pointu[];
    private final float pointv[];
    boolean cacheok = false;

    private BufferedImage image = null;

    public ImageProject(LX lx) {
        super(lx);
        addParameter(file);
        addParameter(chooseFile);
        addParameter(thetaParam);
        addParameter(phiParam);
        addParameter(projectionParam);
        addParameter(flipI);
        addParameter(flipJ);

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
        float theta = PI / 180 * thetaParam.getValuef();
        float phi = PI / 180 * phiParam.getValuef();
        Projection proj = projectionParam.getEnum();

        switch (proj) {
        case PLANAR: {
            LXMatrix xf = LXMatrix.identity().rotateX(phi).rotateY(theta);
            LXVector u = xf.apply(1, 0, 0);
            LXVector v = xf.apply(0, 1, 0);
            float umin = Float.POSITIVE_INFINITY;
            float umax = Float.NEGATIVE_INFINITY;
            float vmin = Float.POSITIVE_INFINITY;
            float vmax = Float.NEGATIVE_INFINITY;

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
                pointu[vec.index] = (u.dot(vec) - umin) / (umax - umin);
                pointv[vec.index] = (v.dot(vec) - vmin) / (vmax - vmin);
            }
            break;
        }
        case MERCATOR: {
            LXVector center = new LXVector(model.cx, model.cy, model.cz);
            for (LXVector vec : getVectors()) {
                LXVector x = center.copy().mult(-1).add(vec);
                x.normalize();
                pointu[vec.index] = (float) ((Math.PI + Math.atan2(x.x, -x.z) + theta) / (2 * Math.PI));
                pointv[vec.index] = (float) ((Math.PI - Math.acos(x.y) + phi) / Math.PI);
            }
            break;
        }
        case S4: {
            LXVector center = new LXVector(model.cx, model.cy, model.cz);
            for (LXVector vec : getVectors()) {
                LXVector x = center.copy().mult(-1).add(vec);
                x.normalize();
                double uu = wrap((float) Math.atan2(x.x, -x.z) + theta, -PI, PI) / PI;
                if (uu < 0) {
                    uu = -uu;
                }
                double vv = wrap((float) Math.acos(x.y) + phi - HALF_PI, -HALF_PI, HALF_PI) / HALF_PI;
                if (vv < 0) {
                    vv = -vv;
                }
                pointu[vec.index] = (float) uu;
                pointv[vec.index] = (float) vv;
            }
            break;
        }
        case S8: {
            LXVector center = new LXVector(model.cx, model.cy, model.cz);
            for (LXVector vec : getVectors()) {
                LXVector x = center.copy().mult(-1).add(vec);
                x.normalize();
                double uu = wrap((float) Math.atan2(x.x, -x.z) + theta + PI, 0, TWO_PI) / HALF_PI;
                double vv = wrap((float) Math.acos(x.y) + phi, 0, PI) / HALF_PI;
                uu = ((int) uu) % 2 == 0 ? uu - Math.floor(uu) : Math.ceil(uu) - uu;
                vv = ((int) vv) % 2 == 0 ? vv - Math.floor(vv) : Math.ceil(vv) - vv;
                pointu[vec.index] = (float) uu;
                pointv[vec.index] = (float) vv;
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
            int wlo = flipI.getValueb() ? image.getWidth() - 1 : 0;
            int whi = flipI.getValueb() ? 0 : image.getWidth() - 1;
            int hlo = flipJ.getValueb() ? 0 : image.getHeight() - 1;
            int hhi = flipJ.getValueb() ? image.getHeight() - 1 : 0;
            for (LXVector vec : getVectors()) {
                int i = Math.round(mapPeriodic(pointu[vec.index], 0, 1, wlo, whi));
                int j = Math.round(mapPeriodic(pointv[vec.index], 0, 1, hlo, hhi));
                ccs[vec.index] = image.getRGB(i, j);
            }
        }
        markModified(PolyBuffer.Space.SRGB8);
    }
}
