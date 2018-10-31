package com.symmetrylabs.slstudio.pattern.base;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.common.reflect.TypeToken;
import com.symmetrylabs.slstudio.model.SLModel;
import heronarts.lx.LXModelComponent;
import heronarts.lx.transform.LXVector;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import heronarts.lx.LXBuffer;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.LXParameter;

import com.symmetrylabs.slstudio.SLStudioLX;
import com.symmetrylabs.slstudio.render.Renderer;
import com.symmetrylabs.slstudio.render.InterpolatingRenderer;
import com.symmetrylabs.slstudio.render.Renderable;
import com.symmetrylabs.util.CaptionSource;
import com.symmetrylabs.util.Marker;
import com.symmetrylabs.util.MarkerSource;

public abstract class SLPattern<M extends SLModel> extends LXPattern implements Renderable, MarkerSource, CaptionSource {

    protected final LX lx;
    protected M model;  // overrides LXPattern's model field with a more specific type
    protected boolean isModelCompatible;  // false if lx.model is of an incompatible type

    private volatile Renderer renderer;
    private ReusableBuffer reusableBuffer = new ReusableBuffer();
    private boolean isManaged = false;

    public SLPattern(LX lx) {
        super(lx);
        this.lx = lx;
        setModel(lx.model);
        createParameters();
    }

    @Override public M getModel() {  // overrides LXPattern's getModel() to return a more specific type
        return model;
    }

    @Override public LXModelComponent setModel(LXModel model) {
        this.model = asSpecializedModel(model);
        isModelCompatible = (this.model != null);
        if (!isModelCompatible) {
            this.model = getEmptyModel();
        }
        return super.setModel(model);
    }

    /** Gets the model class, M. */
    public Class getModelClass() {
        return getEmptyModel().getClass();
    }

    /** Gets an empty instance of the model class, M. */
    private M getEmptyModel() {
        String modelClassName = new TypeToken<M>(getClass()) {}.getType().getTypeName();
        String rawModelClassName = modelClassName.replaceAll("<.*", "");
        M emptyModel;
        try {
            emptyModel = (M) Class.forName(rawModelClassName).getConstructor().newInstance();
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException |
            InstantiationException | InvocationTargetException | ClassCastException e) {
            throw new RuntimeException(
                "Could not find a public default constructor for " + modelClassName + ": " + e);
        }
        return emptyModel;
    }

    /** Casts the given model to the M type if possible, otherwise returns null. */
    private M asSpecializedModel(LXModel model) {
        try {
            if (getModelClass().isAssignableFrom(model.getClass())) {
                return (M) model;
            }
        } catch (ClassCastException e) { }
        return null;
    }

    protected void createParameters() { }

    protected Renderer createRenderer(LXModel model, int[] colors, Renderable renderable) {
        return new InterpolatingRenderer(model, colors, renderable);
        //return new SequentialRenderer(model, colors, renderable);
    }

    public synchronized void setManagedMode(boolean isManaged) {
        boolean wasManaged = this.isManaged;
        this.isManaged = isManaged;

        if (wasManaged && !isManaged) {
            onActive();
        }
        else if (!wasManaged && isManaged) {
            onInactive();
        }
    }

    @Override
    public void onActive() {
        super.onActive();
        if (lx instanceof SLStudioLX) {
            ((SLStudioLX) lx).ui.addMarkerSource(this);
            ((SLStudioLX) lx).ui.addCaptionSource(this);
        }

        synchronized (this) {
            if (!isManaged && renderer != null) {
                renderer = createRenderer(model, colors, this);
                renderer.start();
            }
        }
    }

    @Override
    public void onInactive() {
        super.onInactive();
        if (lx instanceof SLStudioLX) {
            ((SLStudioLX) lx).ui.removeMarkerSource(this);
            ((SLStudioLX) lx).ui.removeCaptionSource(this);
        }

        synchronized (this) {
            if (renderer != null) {
                renderer.stop();
                renderer = null;
            }
        }
    }

    public Collection<Marker> getMarkers() {
        return new ArrayList<Marker>();
    }

    public String getCaption() {
        return null;
    }

    @Override
    public void loop(double deltaMs) {
        // Don't run the pattern at all if it requires a different model type.
        if (!isModelCompatible) return;

        try {
            super.loop(deltaMs);
        } catch (Exception e) {
            System.err.print("\nException in " + getClass().getSimpleName() + " pattern: ");
            e.printStackTrace(System.err);
        }
    }

    @Override
    public void dispose() {
        onInactive();

        super.dispose();
    }

    @Override
    protected void run(double deltaMs) {
        Renderer renderer = this.renderer;

        if (renderer != null) {
            renderer.run(deltaMs);
        }
        else {
            render(deltaMs, getVectorList(), colors);
        }
    }

    @Override
    public void render(double deltaMs, List<LXVector> points, int[] layer) { }

    protected <T extends LXParameter> T addParam(T param) {
        addParameter(param);
        return param;
    }

    protected BooleanParameter booleanParam(String name) {
        return addParam(new BooleanParameter(name));
    }

    protected BooleanParameter booleanParam(String name, boolean value) {
        return addParam(new BooleanParameter(name, value));
    }

    protected CompoundParameter compoundParam(String name, double value, double min, double max) {
        return addParam(new CompoundParameter(name, value, min, max));
    }

    protected DiscreteParameter discreteParameter(String name, int value, int min, int max) {
        return addParam(new DiscreteParameter(name, value, min, max));
    }

    public void unconsumeKeyEvent() {
        this.keyEventConsumed = false;
    }

    public void consumeKeyEvent() {
        this.keyEventConsumed = true;
    }

    public boolean keyEventConsumed() {
        return this.keyEventConsumed;
    }

    private boolean keyEventConsumed = false;

    public void onMousePressed(MouseEvent mouseEvent, float mx, float my) {}
    public void onMouseReleased(MouseEvent mouseEvent, float mx, float my) {}
    public void onMouseClicked(MouseEvent mouseEvent, float mx, float my) {}
    public void onMouseDragged(MouseEvent mouseEvent, float mx, float my, float dx, float dy) {}
    public void onMouseMoved(MouseEvent mouseEvent, float mx, float my) {}
    public void onMouseOver(MouseEvent mouseEvent) {}
    public void onMouseOut(MouseEvent mouseEvent) {}
    public void onMouseWheel(MouseEvent mouseEvent, float mx, float my, float delta) {}
    public void onKeyPressed(KeyEvent keyEvent, char keyChar, int keyCode) {}
    public void onKeyReleased(KeyEvent keyEvent, char keyChar, int keyCode) {}
    public void onKeyTyped(KeyEvent keyEvent, char keyChar, int keyCode) {}

    public SLPattern setBuffer(int[] buffer) {
        reusableBuffer.setArray(buffer);
        return (SLPattern)setBuffer(reusableBuffer);
    }

    private class ReusableBuffer implements LXBuffer {
        private int[] layer;

        @Override
        public int[] getArray() {
            return layer;
        }

        public void setArray(int[] layer) {
            this.layer = layer;
        }
    };
}
