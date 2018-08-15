package com.symmetrylabs.shows.obj;

import java.util.*;
import java.lang.ref.WeakReference;

import heronarts.lx.LX;
import heronarts.lx.transform.LXTransform;

import com.symmetrylabs.shows.Show;
import com.symmetrylabs.slstudio.objimporter.ObjImporter;
import com.symmetrylabs.slstudio.model.SLModel;
import static com.symmetrylabs.util.MathConstants.*;

public class ObjShow implements Show {

    static final float offsetX = 0;
    static final float offsetY = 0;
    static final float offsetZ = 0;

    static final float rotationX = 0;
    static final float rotationY = 0;
    static final float rotationZ = 0;

    public SLModel buildModel() {
        LXTransform transform = new LXTransform();
        transform.translate(offsetX, offsetY, offsetZ);
        transform.rotateX(rotationX * PI / 180.);
        transform.rotateY(rotationY * PI / 180.);
        transform.rotateZ(rotationZ * PI / 180.);

        SLModel[] objModels = new ObjImporter("data", transform).getModels();
        return new SLModel(objModels);
    }

    private static Map<LX, WeakReference<ObjShow>> instanceByLX = new WeakHashMap<>();

    public static ObjShow getInstance(LX lx) {
        WeakReference<ObjShow> weakRef = instanceByLX.get(lx);
        return weakRef == null ? null : weakRef.get();
    }
}
