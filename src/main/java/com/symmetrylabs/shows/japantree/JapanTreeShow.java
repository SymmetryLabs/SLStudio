package com.symmetrylabs.shows.japantree;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.net.SocketException;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.RotationOrder;
import org.apache.commons.math3.geometry.euclidean.threed.RotationConvention;
import java.io.IOException;

import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;

import com.symmetrylabs.shows.Show;
import com.symmetrylabs.slstudio.SLStudio;
import com.symmetrylabs.slstudio.SLStudioLX;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.shows.tree.*;
import com.symmetrylabs.shows.tree.config.*;
import com.symmetrylabs.shows.tree.ui.*;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI3dComponent;

import org.smurn.jply.PlyReaderFile;
import org.smurn.jply.ElementReader;
import org.smurn.jply.Element;

import static com.symmetrylabs.util.DistanceConstants.*;
import static com.symmetrylabs.util.MathConstants.*;
import static com.symmetrylabs.util.MathUtils.*;

import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI3dComponent;
import processing.core.PGraphics;


public class JapanTreeShow extends TreeShow {
    public static final String SHOW_NAME = "japantree";

    public SLModel buildModel() {
        return new TreeModel(CompiledTreeData.CONFIG);
    }
}
