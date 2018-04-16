package com.symmetrylabs.layouts.cubes;

import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;
import java.util.HashMap;
import java.util.WeakHashMap;
import java.lang.ref.WeakReference;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;

import com.symmetrylabs.slstudio.model.SLModel;
import heronarts.lx.LX;
import heronarts.lx.output.FadecandyOutput;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.transform.LXTransform;

import com.symmetrylabs.layouts.Layout;
import com.symmetrylabs.slstudio.SLStudio;
import com.symmetrylabs.slstudio.SLStudioLX;
import com.symmetrylabs.slstudio.network.NetworkMonitor;
import com.symmetrylabs.slstudio.network.NetworkDevice;
import com.symmetrylabs.util.NetworkUtils;
import com.symmetrylabs.util.dispatch.Dispatcher;
import com.symmetrylabs.util.listenable.ListenableList;
import com.symmetrylabs.util.listenable.ListListener;
import heronarts.p3lx.ui.UI2dScrollContext;

/**
 * This file implements the mapping functions needed to lay out the cubes.
 */
public class GlowMotionLayout implements Layout {
    ListenableList<CubesController> controllers = new ListenableList<>();

    static final float globalOffsetX = 0;
    static final float globalOffsetY = 0;
    static final float globalOffsetZ = 0;

    static final float globalRotationX = -90;
    static final float globalRotationY = 0;
    static final float globalRotationZ = 0;

    static final float objOffsetX = 0;
    static final float objOffsetY = 0;
    static final float objOffsetZ = 0;

    static final float objRotationX = 0;
    static final float objRotationY = 0;
    static final float objRotationZ = 0;

    static final float CUBE_WIDTH = 24.f;
    static final float CUBE_HEIGHT = 23.25f;
    static final float TOWER_WIDTH = 23.25f;
    static final float TOWER_HEIGHT = 23.25f;
    static final float CUBE_SPACING = 0;

    static final float TOWER_VERTICAL_SPACING = 0;
    static final float TOWER_RISER = 0;
    static final float SP = CUBE_WIDTH;
    static final float JUMP = CUBE_HEIGHT;

    static final float INCHES_PER_METER = 39.3701f;

    // static final BulbConfig[] BULB_CONFIG = {
    //     // new BulbConfig("lifx-1", -50, 50, -30),
    //     // new BulbConfig("lifx-2", 0, 50, 0),
    //     // new BulbConfig("lifx-3", -65, 20, -100),
    //     // new BulbConfig("lifx-4", 0, 0, 0),
    //     // new BulbConfig("lifx-5", 0, 0, 0),
    // };



    static final TowerConfig[] TOWER_CONFIG = {

        // row 1 (back)
        new TowerConfig(SP*2, 0, SP*0, new String[] {"0", "0", "0", "0"}),
        new TowerConfig(SP*2, 0, SP*1, new String[] {"0", "0", "0", "0"}),
        // gap
        new TowerConfig(SP*2, 0, SP*4, new String[] {"0", "0", "0", "0"}),
        new TowerConfig(SP*2, 0, SP*5, new String[] {"0", "0", "0", "0"}),
        new TowerConfig(SP*2, 0, SP*6, new String[] {"0", "0", "0", "0"}),
        new TowerConfig(SP*2, 0, SP*7, new String[] {"0", "0", "0", "0"}),
        new TowerConfig(SP*2, 0, SP*8, new String[] {"0", "0", "0", "0"}),
        new TowerConfig(SP*2, 0, SP*9, new String[] {"0", "0", "0", "0"}),
        new TowerConfig(SP*2, 0, SP*10, new String[] {"0", "0", "0", "0"}),
        new TowerConfig(SP*2, 0, SP*11, new String[] {"0", "0", "0", "0"}),
        new TowerConfig(SP*2, 0, SP*12, new String[] {"0", "0", "0", "0"}),
        new TowerConfig(SP*2, 0, SP*13, new String[] {"0", "0", "0", "0"}),
        new TowerConfig(SP*2, 0, SP*14, new String[] {"0", "0", "0", "0"}),
        new TowerConfig(SP*2, 0, SP*15, new String[] {"0", "0", "0", "0"}),
        new TowerConfig(SP*2, 0, SP*16, new String[] {"0", "0", "0", "0"}),
        new TowerConfig(SP*2, 0, SP*17, new String[] {"0", "0", "0", "0"}),
        new TowerConfig(SP*2, 0, SP*18, new String[] {"0", "0", "0", "0"}),
        new TowerConfig(SP*2, 0, SP*19, new String[] {"0", "0", "0", "0"}),
        new TowerConfig(SP*2, 0, SP*20, new String[] {"0", "0", "0", "0"}),
        new TowerConfig(SP*2, 0, SP*21, new String[] {"0", "0", "0", "0"}),
        // gap
        new TowerConfig(SP*2, 0, SP*24, new String[] {"0", "0", "0", "0"}),
        new TowerConfig(SP*2, 0, SP*25, new String[] {"0", "0", "0", "0"}),

        // row 2
        new TowerConfig(SP*3, 0, SP*0, new String[] {"0", "0", "0", "0"}),
        new TowerConfig(SP*3, 0, SP*1, new String[] {"0", "0", "0", "0"}),
        // gap
        // new TowerConfig(SP*1, 0, SP*4, new String[] {"0", "0", "0", "0"}),
        // new TowerConfig(SP*1, 0, SP*5, new String[] {"0", "0", "0", "0"}),
        // new TowerConfig(SP*1, 0, SP*6, new String[] {"0", "0", "0", "0"}),
        // new TowerConfig(SP*1, 0, SP*7, new String[] {"0", "0", "0", "0"}),
        // new TowerConfig(SP*1, 0, SP*8, new String[] {"0", "0", "0", "0"}),
        new TowerConfig(SP*3, 0, SP*9, new String[] {"0", "0", "0", "0"}),
        new TowerConfig(SP*3, 0, SP*10, new String[] {"0", "0", "0", "0"}),
        new TowerConfig(SP*3, 0, SP*11, new String[] {"0", "0", "0", "0"}),
        new TowerConfig(SP*3, 0, SP*12, new String[] {"0", "0", "0", "0"}),
        new TowerConfig(SP*3, 0, SP*13, new String[] {"0", "0", "0", "0"}),
        new TowerConfig(SP*3, 0, SP*14, new String[] {"0", "0", "0", "0"}),
        new TowerConfig(SP*3, 0, SP*15, new String[] {"0", "0", "0", "0"}),
        new TowerConfig(SP*3, 0, SP*16, new String[] {"0", "0", "0", "0"}),
        // new TowerConfig(SP*1, 0, SP*17, new String[] {"0", "0", "0", "0"}),
        // new TowerConfig(SP*1, 0, SP*18, new String[] {"0", "0", "0", "0"}),
        // new TowerConfig(SP*1, 0, SP*19, new String[] {"0", "0", "0", "0"}),
        // new TowerConfig(SP*1, 0, SP*20, new String[] {"0", "0", "0", "0"}),
        // new TowerConfig(SP*1, 0, SP*21, new String[] {"0", "0", "0", "0"}),
        // gap
        new TowerConfig(SP*3, 0, SP*24, new String[] {"0", "0", "0", "0"}),
        new TowerConfig(SP*3, 0, SP*25, new String[] {"0", "0", "0", "0"}),

        // row 3
        new TowerConfig(SP*4, 0, SP*0, new String[] {"0", "0", "0", "0"}),
        new TowerConfig(SP*4, 0, SP*1, new String[] {"0", "0", "0", "0"}),
        new TowerConfig(SP*4, 0, SP*2, new String[] {"0", "0", "0", "0"}),
        new TowerConfig(SP*4, 0, SP*3, new String[] {"0", "0", "0", "0"}),
        new TowerConfig(SP*4, 0, SP*4, new String[] {"0", "0", "0", "0"}),
        new TowerConfig(SP*4, 0, SP*5, new String[] {"0", "0", "0", "0"}),
        new TowerConfig(SP*4, 0, SP*6, new String[] {"0", "0", "0", "0"}),
        new TowerConfig(SP*4, 0, SP*7, new String[] {"0", "0", "0", "0"}),
        // gap
        new TowerConfig(SP*4, 0, SP*18, new String[] {"0", "0", "0", "0"}),
        new TowerConfig(SP*4, 0, SP*19, new String[] {"0", "0", "0", "0"}),
        new TowerConfig(SP*4, 0, SP*20, new String[] {"0", "0", "0", "0"}),
        new TowerConfig(SP*4, 0, SP*21, new String[] {"0", "0", "0", "0"}),
        new TowerConfig(SP*4, 0, SP*22, new String[] {"0", "0", "0", "0"}),
        new TowerConfig(SP*4, 0, SP*23, new String[] {"0", "0", "0", "0"}),
        new TowerConfig(SP*4, 0, SP*24, new String[] {"0", "0", "0", "0"}),
        new TowerConfig(SP*4, 0, SP*25, new String[] {"0", "0", "0", "0"}),

        // row 4
        new TowerConfig(SP*5, 0, SP*0, new String[] {"0", "0", "0", "0"}),
        new TowerConfig(SP*5, 0, SP*1, new String[] {"0", "0", "0", "0"}),
        new TowerConfig(SP*5, 0, SP*2, new String[] {"0", "0", "0", "0"}),
        new TowerConfig(SP*5, 0, SP*3, new String[] {"0", "0", "0", "0"}),
        // gap
        new TowerConfig(SP*5, 0, SP*22, new String[] {"0", "0", "0", "0"}),
        new TowerConfig(SP*5, 0, SP*23, new String[] {"0", "0", "0", "0"}),
        new TowerConfig(SP*5, 0, SP*24, new String[] {"0", "0", "0", "0"}),
        new TowerConfig(SP*5, 0, SP*25, new String[] {"0", "0", "0", "0"}),

        // row 5
        new TowerConfig(SP*6, 0, SP*0, new String[] {"0", "0", "0", "0"}),
        new TowerConfig(SP*6, 0, SP*1, new String[] {"0", "0", "0", "0"}),
        new TowerConfig(SP*6, 0, SP*2, new String[] {"0", "0", "0", "0"}),
        // gap
        new TowerConfig(SP*6, 0, SP*23, new String[] {"0", "0", "0", "0"}),
        new TowerConfig(SP*6, 0, SP*24, new String[] {"0", "0", "0", "0"}),
        new TowerConfig(SP*6, 0, SP*25, new String[] {"0", "0", "0", "0"}),

        // FLOOR
        // row 1 (back)
        new TowerConfig(SP*0, -SP, SP*0, new String[] {"0"}),
        new TowerConfig(SP*0, -SP, SP*1, new String[] {"0"}),
        // gap
        new TowerConfig(SP*0, -SP, SP*4, new String[] {"0"}),
        new TowerConfig(SP*0, -SP, SP*5, new String[] {"0"}),
        new TowerConfig(SP*0, -SP, SP*6, new String[] {"0"}),
        new TowerConfig(SP*0, -SP, SP*7, new String[] {"0"}),
        new TowerConfig(SP*0, -SP, SP*8, new String[] {"0"}),
        new TowerConfig(SP*0, -SP, SP*9, new String[] {"0"}),
        new TowerConfig(SP*0, -SP, SP*10, new String[] {"0"}),
        new TowerConfig(SP*0, -SP, SP*11, new String[] {"0"}),
        new TowerConfig(SP*0, -SP, SP*12, new String[] {"0"}),
        new TowerConfig(SP*0, -SP, SP*13, new String[] {"0"}),
        new TowerConfig(SP*0, -SP, SP*14, new String[] {"0"}),
        new TowerConfig(SP*0, -SP, SP*15, new String[] {"0"}),
        new TowerConfig(SP*0, -SP, SP*16, new String[] {"0"}),
        new TowerConfig(SP*0, -SP, SP*17, new String[] {"0"}),
        new TowerConfig(SP*0, -SP, SP*18, new String[] {"0"}),
        new TowerConfig(SP*0, -SP, SP*19, new String[] {"0"}),
        new TowerConfig(SP*0, -SP, SP*20, new String[] {"0"}),
        new TowerConfig(SP*0, -SP, SP*21, new String[] {"0"}),
        // gap
        new TowerConfig(SP*0, -SP, SP*24, new String[] {"0"}),
        new TowerConfig(SP*0, -SP, SP*25, new String[] {"0"}),

        // row 2
        new TowerConfig(SP*1, -SP, SP*0, new String[] {"0"}),
        new TowerConfig(SP*1, -SP, SP*1, new String[] {"0"}),
        // gap
        new TowerConfig(SP*1, -SP, SP*4, new String[] {"0"}),
        new TowerConfig(SP*1, -SP, SP*5, new String[] {"0"}),
        new TowerConfig(SP*1, -SP, SP*6, new String[] {"0"}),
        new TowerConfig(SP*1, -SP, SP*7, new String[] {"0"}),
        new TowerConfig(SP*1, -SP, SP*8, new String[] {"0"}),
        new TowerConfig(SP*1, -SP, SP*9, new String[] {"0"}),
        new TowerConfig(SP*1, -SP, SP*10, new String[] {"0"}),
        new TowerConfig(SP*1, -SP, SP*11, new String[] {"0"}),
        new TowerConfig(SP*1, -SP, SP*12, new String[] {"0"}),
        new TowerConfig(SP*1, -SP, SP*13, new String[] {"0"}),
        new TowerConfig(SP*1, -SP, SP*14, new String[] {"0"}),
        new TowerConfig(SP*1, -SP, SP*15, new String[] {"0"}),
        new TowerConfig(SP*1, -SP, SP*16, new String[] {"0"}),
        new TowerConfig(SP*1, -SP, SP*17, new String[] {"0"}),
        new TowerConfig(SP*1, -SP, SP*18, new String[] {"0"}),
        new TowerConfig(SP*1, -SP, SP*19, new String[] {"0"}),
        new TowerConfig(SP*1, -SP, SP*20, new String[] {"0"}),
        new TowerConfig(SP*1, -SP, SP*21, new String[] {"0"}),
        // gap
        new TowerConfig(SP*1, -SP, SP*24, new String[] {"0"}),
        new TowerConfig(SP*1, -SP, SP*25, new String[] {"0"}),

        // row 3
        new TowerConfig(SP*2, -SP, SP*0, new String[] {"0"}),
        new TowerConfig(SP*2, -SP, SP*1, new String[] {"0"}),
        // gap
        new TowerConfig(SP*2, -SP, SP*4, new String[] {"0"}),
        new TowerConfig(SP*2, -SP, SP*5, new String[] {"0"}),
        new TowerConfig(SP*2, -SP, SP*6, new String[] {"0"}),
        new TowerConfig(SP*2, -SP, SP*7, new String[] {"0"}),
        new TowerConfig(SP*2, -SP, SP*8, new String[] {"0"}),
        new TowerConfig(SP*2, -SP, SP*9, new String[] {"0"}),
        new TowerConfig(SP*2, -SP, SP*10, new String[] {"0"}),
        new TowerConfig(SP*2, -SP, SP*11, new String[] {"0"}),
        new TowerConfig(SP*2, -SP, SP*12, new String[] {"0"}),
        new TowerConfig(SP*2, -SP, SP*13, new String[] {"0"}),
        new TowerConfig(SP*2, -SP, SP*14, new String[] {"0"}),
        new TowerConfig(SP*2, -SP, SP*15, new String[] {"0"}),
        new TowerConfig(SP*2, -SP, SP*16, new String[] {"0"}),
        new TowerConfig(SP*2, -SP, SP*17, new String[] {"0"}),
        new TowerConfig(SP*2, -SP, SP*18, new String[] {"0"}),
        new TowerConfig(SP*2, -SP, SP*19, new String[] {"0"}),
        new TowerConfig(SP*2, -SP, SP*20, new String[] {"0"}),
        new TowerConfig(SP*2, -SP, SP*21, new String[] {"0"}),
        // gap
        new TowerConfig(SP*2, -SP, SP*24, new String[] {"0"}),
        new TowerConfig(SP*2, -SP, SP*25, new String[] {"0"}),

        // row 4
        new TowerConfig(SP*3, -SP, SP*0, new String[] {"0"}),
        new TowerConfig(SP*3, -SP, SP*1, new String[] {"0"}),
        // gap
        new TowerConfig(SP*3, -SP, SP*4, new String[] {"0"}),
        new TowerConfig(SP*3, -SP, SP*5, new String[] {"0"}),
        new TowerConfig(SP*3, -SP, SP*6, new String[] {"0"}),
        new TowerConfig(SP*3, -SP, SP*7, new String[] {"0"}),
        new TowerConfig(SP*3, -SP, SP*8, new String[] {"0"}),
        new TowerConfig(SP*3, -SP, SP*9, new String[] {"0"}),
        new TowerConfig(SP*3, -SP, SP*10, new String[] {"0"}),
        new TowerConfig(SP*3, -SP, SP*11, new String[] {"0"}),
        new TowerConfig(SP*3, -SP, SP*12, new String[] {"0"}),
        new TowerConfig(SP*3, -SP, SP*13, new String[] {"0"}),
        new TowerConfig(SP*3, -SP, SP*14, new String[] {"0"}),
        new TowerConfig(SP*3, -SP, SP*15, new String[] {"0"}),
        new TowerConfig(SP*3, -SP, SP*16, new String[] {"0"}),
        new TowerConfig(SP*3, -SP, SP*17, new String[] {"0"}),
        new TowerConfig(SP*3, -SP, SP*18, new String[] {"0"}),
        new TowerConfig(SP*3, -SP, SP*19, new String[] {"0"}),
        new TowerConfig(SP*3, -SP, SP*20, new String[] {"0"}),
        new TowerConfig(SP*3, -SP, SP*21, new String[] {"0"}),
        // gap
        new TowerConfig(SP*3, -SP, SP*24, new String[] {"0"}),
        new TowerConfig(SP*3, -SP, SP*25, new String[] {"0"}),

        // row 5
        new TowerConfig(SP*4, -SP, SP*0, new String[] {"0"}),
        new TowerConfig(SP*4, -SP, SP*1, new String[] {"0"}),
        new TowerConfig(SP*4, -SP, SP*2, new String[] {"0"}),
        new TowerConfig(SP*4, -SP, SP*3, new String[] {"0"}),
        new TowerConfig(SP*4, -SP, SP*4, new String[] {"0"}),
        new TowerConfig(SP*4, -SP, SP*5, new String[] {"0"}),
        new TowerConfig(SP*4, -SP, SP*6, new String[] {"0"}),
        new TowerConfig(SP*4, -SP, SP*7, new String[] {"0"}),
        new TowerConfig(SP*4, -SP, SP*8, new String[] {"0"}),
        new TowerConfig(SP*4, -SP, SP*9, new String[] {"0"}),
        new TowerConfig(SP*4, -SP, SP*10, new String[] {"0"}),
        new TowerConfig(SP*4, -SP, SP*11, new String[] {"0"}),
        new TowerConfig(SP*4, -SP, SP*12, new String[] {"0"}),
        new TowerConfig(SP*4, -SP, SP*13, new String[] {"0"}),
        new TowerConfig(SP*4, -SP, SP*14, new String[] {"0"}),
        new TowerConfig(SP*4, -SP, SP*15, new String[] {"0"}),
        new TowerConfig(SP*4, -SP, SP*16, new String[] {"0"}),
        new TowerConfig(SP*4, -SP, SP*17, new String[] {"0"}),
        new TowerConfig(SP*4, -SP, SP*18, new String[] {"0"}),
        new TowerConfig(SP*4, -SP, SP*19, new String[] {"0"}),
        new TowerConfig(SP*4, -SP, SP*20, new String[] {"0"}),
        new TowerConfig(SP*4, -SP, SP*21, new String[] {"0"}),
        new TowerConfig(SP*4, -SP, SP*22, new String[] {"0"}),
        new TowerConfig(SP*4, -SP, SP*23, new String[] {"0"}),
        new TowerConfig(SP*4, -SP, SP*24, new String[] {"0"}),
        new TowerConfig(SP*4, -SP, SP*25, new String[] {"0"}),

        // row 6
        new TowerConfig(SP*5, -SP, SP*0, new String[] {"0"}),
        new TowerConfig(SP*5, -SP, SP*1, new String[] {"0"}),
        new TowerConfig(SP*5, -SP, SP*2, new String[] {"0"}),
        new TowerConfig(SP*5, -SP, SP*3, new String[] {"0"}),
        new TowerConfig(SP*5, -SP, SP*4, new String[] {"0"}),
        new TowerConfig(SP*5, -SP, SP*5, new String[] {"0"}),
        new TowerConfig(SP*5, -SP, SP*6, new String[] {"0"}),
        new TowerConfig(SP*5, -SP, SP*7, new String[] {"0"}),
        new TowerConfig(SP*5, -SP, SP*8, new String[] {"0"}),
        new TowerConfig(SP*5, -SP, SP*9, new String[] {"0"}),
        new TowerConfig(SP*5, -SP, SP*10, new String[] {"0"}),
        new TowerConfig(SP*5, -SP, SP*11, new String[] {"0"}),
        new TowerConfig(SP*5, -SP, SP*12, new String[] {"0"}),
        new TowerConfig(SP*5, -SP, SP*13, new String[] {"0"}),
        new TowerConfig(SP*5, -SP, SP*14, new String[] {"0"}),
        new TowerConfig(SP*5, -SP, SP*15, new String[] {"0"}),
        new TowerConfig(SP*5, -SP, SP*16, new String[] {"0"}),
        new TowerConfig(SP*5, -SP, SP*17, new String[] {"0"}),
        new TowerConfig(SP*5, -SP, SP*18, new String[] {"0"}),
        new TowerConfig(SP*5, -SP, SP*19, new String[] {"0"}),
        new TowerConfig(SP*5, -SP, SP*20, new String[] {"0"}),
        new TowerConfig(SP*5, -SP, SP*21, new String[] {"0"}),
        new TowerConfig(SP*5, -SP, SP*22, new String[] {"0"}),
        new TowerConfig(SP*5, -SP, SP*23, new String[] {"0"}),
        new TowerConfig(SP*5, -SP, SP*24, new String[] {"0"}),
        new TowerConfig(SP*5, -SP, SP*25, new String[] {"0"}),

        // row 7
        new TowerConfig(SP*6, -SP, SP*0, new String[] {"0"}),
        new TowerConfig(SP*6, -SP, SP*1, new String[] {"0"}),
        new TowerConfig(SP*6, -SP, SP*2, new String[] {"0"}),
        new TowerConfig(SP*6, -SP, SP*3, new String[] {"0"}),
        new TowerConfig(SP*6, -SP, SP*4, new String[] {"0"}),
        new TowerConfig(SP*6, -SP, SP*5, new String[] {"0"}),
        new TowerConfig(SP*6, -SP, SP*6, new String[] {"0"}),
        new TowerConfig(SP*6, -SP, SP*7, new String[] {"0"}),
        new TowerConfig(SP*6, -SP, SP*8, new String[] {"0"}),
        new TowerConfig(SP*6, -SP, SP*9, new String[] {"0"}),
        new TowerConfig(SP*6, -SP, SP*10, new String[] {"0"}),
        new TowerConfig(SP*6, -SP, SP*11, new String[] {"0"}),
        new TowerConfig(SP*6, -SP, SP*12, new String[] {"0"}),
        new TowerConfig(SP*6, -SP, SP*13, new String[] {"0"}),
        new TowerConfig(SP*6, -SP, SP*14, new String[] {"0"}),
        new TowerConfig(SP*6, -SP, SP*15, new String[] {"0"}),
        new TowerConfig(SP*6, -SP, SP*16, new String[] {"0"}),
        new TowerConfig(SP*6, -SP, SP*17, new String[] {"0"}),
        new TowerConfig(SP*6, -SP, SP*18, new String[] {"0"}),
        new TowerConfig(SP*6, -SP, SP*19, new String[] {"0"}),
        new TowerConfig(SP*6, -SP, SP*20, new String[] {"0"}),
        new TowerConfig(SP*6, -SP, SP*21, new String[] {"0"}),
        new TowerConfig(SP*6, -SP, SP*22, new String[] {"0"}),
        new TowerConfig(SP*6, -SP, SP*23, new String[] {"0"}),
        new TowerConfig(SP*6, -SP, SP*24, new String[] {"0"}),
        new TowerConfig(SP*6, -SP, SP*25, new String[] {"0"}),

        // row 8
        new TowerConfig(SP*7, -SP, SP*0, new String[] {"0"}),
        new TowerConfig(SP*7, -SP, SP*1, new String[] {"0"}),
        new TowerConfig(SP*7, -SP, SP*2, new String[] {"0"}),
        new TowerConfig(SP*7, -SP, SP*3, new String[] {"0"}),
        new TowerConfig(SP*7, -SP, SP*4, new String[] {"0"}),
        new TowerConfig(SP*7, -SP, SP*5, new String[] {"0"}),
        new TowerConfig(SP*7, -SP, SP*6, new String[] {"0"}),
        new TowerConfig(SP*7, -SP, SP*7, new String[] {"0"}),
        new TowerConfig(SP*7, -SP, SP*8, new String[] {"0"}),
        new TowerConfig(SP*7, -SP, SP*9, new String[] {"0"}),
        new TowerConfig(SP*7, -SP, SP*10, new String[] {"0"}),
        new TowerConfig(SP*7, -SP, SP*11, new String[] {"0"}),
        new TowerConfig(SP*7, -SP, SP*12, new String[] {"0"}),
        new TowerConfig(SP*7, -SP, SP*13, new String[] {"0"}),
        new TowerConfig(SP*7, -SP, SP*14, new String[] {"0"}),
        new TowerConfig(SP*7, -SP, SP*15, new String[] {"0"}),
        new TowerConfig(SP*7, -SP, SP*16, new String[] {"0"}),
        new TowerConfig(SP*7, -SP, SP*17, new String[] {"0"}),
        new TowerConfig(SP*7, -SP, SP*18, new String[] {"0"}),
        new TowerConfig(SP*7, -SP, SP*19, new String[] {"0"}),
        new TowerConfig(SP*7, -SP, SP*20, new String[] {"0"}),
        new TowerConfig(SP*7, -SP, SP*21, new String[] {"0"}),
        new TowerConfig(SP*7, -SP, SP*22, new String[] {"0"}),
        new TowerConfig(SP*7, -SP, SP*23, new String[] {"0"}),
        new TowerConfig(SP*7, -SP, SP*24, new String[] {"0"}),
        new TowerConfig(SP*7, -SP, SP*25, new String[] {"0"}),

        // row 9
        new TowerConfig(SP*8, -SP, SP*0, new String[] {"0"}),
        new TowerConfig(SP*8, -SP, SP*1, new String[] {"0"}),
        new TowerConfig(SP*8, -SP, SP*2, new String[] {"0"}),
        new TowerConfig(SP*8, -SP, SP*3, new String[] {"0"}),
        new TowerConfig(SP*8, -SP, SP*4, new String[] {"0"}),
        new TowerConfig(SP*8, -SP, SP*5, new String[] {"0"}),
        new TowerConfig(SP*8, -SP, SP*6, new String[] {"0"}),
        new TowerConfig(SP*8, -SP, SP*7, new String[] {"0"}),
        new TowerConfig(SP*8, -SP, SP*8, new String[] {"0"}),
        new TowerConfig(SP*8, -SP, SP*9, new String[] {"0"}),
        new TowerConfig(SP*8, -SP, SP*10, new String[] {"0"}),
        new TowerConfig(SP*8, -SP, SP*11, new String[] {"0"}),
        new TowerConfig(SP*8, -SP, SP*12, new String[] {"0"}),
        new TowerConfig(SP*8, -SP, SP*13, new String[] {"0"}),
        new TowerConfig(SP*8, -SP, SP*14, new String[] {"0"}),
        new TowerConfig(SP*8, -SP, SP*15, new String[] {"0"}),
        new TowerConfig(SP*8, -SP, SP*16, new String[] {"0"}),
        new TowerConfig(SP*8, -SP, SP*17, new String[] {"0"}),
        new TowerConfig(SP*8, -SP, SP*18, new String[] {"0"}),
        new TowerConfig(SP*8, -SP, SP*19, new String[] {"0"}),
        new TowerConfig(SP*8, -SP, SP*20, new String[] {"0"}),
        new TowerConfig(SP*8, -SP, SP*21, new String[] {"0"}),
        new TowerConfig(SP*8, -SP, SP*22, new String[] {"0"}),
        new TowerConfig(SP*8, -SP, SP*23, new String[] {"0"}),
        new TowerConfig(SP*8, -SP, SP*24, new String[] {"0"}),
        new TowerConfig(SP*8, -SP, SP*25, new String[] {"0"}),

        // row 10
        new TowerConfig(SP*9, -SP, SP*0, new String[] {"0"}),
        new TowerConfig(SP*9, -SP, SP*1, new String[] {"0"}),
        new TowerConfig(SP*9, -SP, SP*2, new String[] {"0"}),
        new TowerConfig(SP*9, -SP, SP*3, new String[] {"0"}),
        new TowerConfig(SP*9, -SP, SP*4, new String[] {"0"}),
        new TowerConfig(SP*9, -SP, SP*5, new String[] {"0"}),
        new TowerConfig(SP*9, -SP, SP*6, new String[] {"0"}),
        new TowerConfig(SP*9, -SP, SP*7, new String[] {"0"}),
        new TowerConfig(SP*9, -SP, SP*8, new String[] {"0"}),
        new TowerConfig(SP*9, -SP, SP*9, new String[] {"0"}),
        new TowerConfig(SP*9, -SP, SP*10, new String[] {"0"}),
        new TowerConfig(SP*9, -SP, SP*11, new String[] {"0"}),
        new TowerConfig(SP*9, -SP, SP*12, new String[] {"0"}),
        new TowerConfig(SP*9, -SP, SP*13, new String[] {"0"}),
        new TowerConfig(SP*9, -SP, SP*14, new String[] {"0"}),
        new TowerConfig(SP*9, -SP, SP*15, new String[] {"0"}),
        new TowerConfig(SP*9, -SP, SP*16, new String[] {"0"}),
        new TowerConfig(SP*9, -SP, SP*17, new String[] {"0"}),
        new TowerConfig(SP*9, -SP, SP*18, new String[] {"0"}),
        new TowerConfig(SP*9, -SP, SP*19, new String[] {"0"}),
        new TowerConfig(SP*9, -SP, SP*20, new String[] {"0"}),
        new TowerConfig(SP*9, -SP, SP*21, new String[] {"0"}),
        new TowerConfig(SP*9, -SP, SP*22, new String[] {"0"}),
        new TowerConfig(SP*9, -SP, SP*23, new String[] {"0"}),
        new TowerConfig(SP*9, -SP, SP*24, new String[] {"0"}),
        new TowerConfig(SP*9, -SP, SP*25, new String[] {"0"}),

        // row 11
        new TowerConfig(SP*10, -SP, SP*0, new String[] {"0"}),
        new TowerConfig(SP*10, -SP, SP*1, new String[] {"0"}),
        new TowerConfig(SP*10, -SP, SP*2, new String[] {"0"}),
        new TowerConfig(SP*10, -SP, SP*3, new String[] {"0"}),
        new TowerConfig(SP*10, -SP, SP*4, new String[] {"0"}),
        new TowerConfig(SP*10, -SP, SP*5, new String[] {"0"}),
        new TowerConfig(SP*10, -SP, SP*6, new String[] {"0"}),
        new TowerConfig(SP*10, -SP, SP*7, new String[] {"0"}),
        new TowerConfig(SP*10, -SP, SP*8, new String[] {"0"}),
        new TowerConfig(SP*10, -SP, SP*9, new String[] {"0"}),
        new TowerConfig(SP*10, -SP, SP*10, new String[] {"0"}),
        new TowerConfig(SP*10, -SP, SP*11, new String[] {"0"}),
        new TowerConfig(SP*10, -SP, SP*12, new String[] {"0"}),
        new TowerConfig(SP*10, -SP, SP*13, new String[] {"0"}),
        new TowerConfig(SP*10, -SP, SP*14, new String[] {"0"}),
        new TowerConfig(SP*10, -SP, SP*15, new String[] {"0"}),
        new TowerConfig(SP*10, -SP, SP*16, new String[] {"0"}),
        new TowerConfig(SP*10, -SP, SP*17, new String[] {"0"}),
        new TowerConfig(SP*10, -SP, SP*18, new String[] {"0"}),
        new TowerConfig(SP*10, -SP, SP*19, new String[] {"0"}),
        new TowerConfig(SP*10, -SP, SP*20, new String[] {"0"}),
        new TowerConfig(SP*10, -SP, SP*21, new String[] {"0"}),
        new TowerConfig(SP*10, -SP, SP*22, new String[] {"0"}),
        new TowerConfig(SP*10, -SP, SP*23, new String[] {"0"}),
        new TowerConfig(SP*10, -SP, SP*24, new String[] {"0"}),
        new TowerConfig(SP*10, -SP, SP*25, new String[] {"0"}),

        // row 12
        new TowerConfig(SP*11, -SP, SP*0, new String[] {"0"}),
        new TowerConfig(SP*11, -SP, SP*1, new String[] {"0"}),
        new TowerConfig(SP*11, -SP, SP*2, new String[] {"0"}),
        new TowerConfig(SP*11, -SP, SP*3, new String[] {"0"}),
        new TowerConfig(SP*11, -SP, SP*4, new String[] {"0"}),
        new TowerConfig(SP*11, -SP, SP*5, new String[] {"0"}),
        new TowerConfig(SP*11, -SP, SP*6, new String[] {"0"}),
        new TowerConfig(SP*11, -SP, SP*7, new String[] {"0"}),
        new TowerConfig(SP*11, -SP, SP*8, new String[] {"0"}),
        new TowerConfig(SP*11, -SP, SP*9, new String[] {"0"}),
        new TowerConfig(SP*11, -SP, SP*10, new String[] {"0"}),
        new TowerConfig(SP*11, -SP, SP*11, new String[] {"0"}),
        new TowerConfig(SP*11, -SP, SP*12, new String[] {"0"}),
        new TowerConfig(SP*11, -SP, SP*13, new String[] {"0"}),
        new TowerConfig(SP*11, -SP, SP*14, new String[] {"0"}),
        new TowerConfig(SP*11, -SP, SP*15, new String[] {"0"}),
        new TowerConfig(SP*11, -SP, SP*16, new String[] {"0"}),
        new TowerConfig(SP*11, -SP, SP*17, new String[] {"0"}),
        new TowerConfig(SP*11, -SP, SP*18, new String[] {"0"}),
        new TowerConfig(SP*11, -SP, SP*19, new String[] {"0"}),
        new TowerConfig(SP*11, -SP, SP*20, new String[] {"0"}),
        new TowerConfig(SP*11, -SP, SP*21, new String[] {"0"}),
        new TowerConfig(SP*11, -SP, SP*22, new String[] {"0"}),
        new TowerConfig(SP*11, -SP, SP*23, new String[] {"0"}),
        new TowerConfig(SP*11, -SP, SP*24, new String[] {"0"}),
        new TowerConfig(SP*11, -SP, SP*25, new String[] {"0"}),

        // row 13
        new TowerConfig(SP*12, -SP, SP*0, new String[] {"0"}),
        new TowerConfig(SP*12, -SP, SP*1, new String[] {"0"}),
        new TowerConfig(SP*12, -SP, SP*2, new String[] {"0"}),
        new TowerConfig(SP*12, -SP, SP*3, new String[] {"0"}),
        new TowerConfig(SP*12, -SP, SP*4, new String[] {"0"}),
        new TowerConfig(SP*12, -SP, SP*5, new String[] {"0"}),
        new TowerConfig(SP*12, -SP, SP*6, new String[] {"0"}),
        new TowerConfig(SP*12, -SP, SP*7, new String[] {"0"}),
        new TowerConfig(SP*12, -SP, SP*8, new String[] {"0"}),
        new TowerConfig(SP*12, -SP, SP*9, new String[] {"0"}),
        new TowerConfig(SP*12, -SP, SP*10, new String[] {"0"}),
        new TowerConfig(SP*12, -SP, SP*11, new String[] {"0"}),
        new TowerConfig(SP*12, -SP, SP*12, new String[] {"0"}),
        new TowerConfig(SP*12, -SP, SP*13, new String[] {"0"}),
        new TowerConfig(SP*12, -SP, SP*14, new String[] {"0"}),
        new TowerConfig(SP*12, -SP, SP*15, new String[] {"0"}),
        new TowerConfig(SP*12, -SP, SP*16, new String[] {"0"}),
        new TowerConfig(SP*12, -SP, SP*17, new String[] {"0"}),
        new TowerConfig(SP*12, -SP, SP*18, new String[] {"0"}),
        new TowerConfig(SP*12, -SP, SP*19, new String[] {"0"}),
        new TowerConfig(SP*12, -SP, SP*20, new String[] {"0"}),
        new TowerConfig(SP*12, -SP, SP*21, new String[] {"0"}),
        new TowerConfig(SP*12, -SP, SP*22, new String[] {"0"}),
        new TowerConfig(SP*12, -SP, SP*23, new String[] {"0"}),
        new TowerConfig(SP*12, -SP, SP*24, new String[] {"0"}),
        new TowerConfig(SP*12, -SP, SP*25, new String[] {"0"}),

        // row 14
        new TowerConfig(SP*13, -SP, SP*0, new String[] {"0"}),
        new TowerConfig(SP*13, -SP, SP*1, new String[] {"0"}),
        new TowerConfig(SP*13, -SP, SP*2, new String[] {"0"}),
        new TowerConfig(SP*13, -SP, SP*3, new String[] {"0"}),
        new TowerConfig(SP*13, -SP, SP*4, new String[] {"0"}),
        new TowerConfig(SP*13, -SP, SP*5, new String[] {"0"}),
        new TowerConfig(SP*13, -SP, SP*6, new String[] {"0"}),
        new TowerConfig(SP*13, -SP, SP*7, new String[] {"0"}),
        new TowerConfig(SP*13, -SP, SP*8, new String[] {"0"}),
        new TowerConfig(SP*13, -SP, SP*9, new String[] {"0"}),
        new TowerConfig(SP*13, -SP, SP*10, new String[] {"0"}),
        new TowerConfig(SP*13, -SP, SP*11, new String[] {"0"}),
        new TowerConfig(SP*13, -SP, SP*12, new String[] {"0"}),
        new TowerConfig(SP*13, -SP, SP*13, new String[] {"0"}),
        new TowerConfig(SP*13, -SP, SP*14, new String[] {"0"}),
        new TowerConfig(SP*13, -SP, SP*15, new String[] {"0"}),
        new TowerConfig(SP*13, -SP, SP*16, new String[] {"0"}),
        new TowerConfig(SP*13, -SP, SP*17, new String[] {"0"}),
        new TowerConfig(SP*13, -SP, SP*18, new String[] {"0"}),
        new TowerConfig(SP*13, -SP, SP*19, new String[] {"0"}),
        new TowerConfig(SP*13, -SP, SP*20, new String[] {"0"}),
        new TowerConfig(SP*13, -SP, SP*21, new String[] {"0"}),
        new TowerConfig(SP*13, -SP, SP*22, new String[] {"0"}),
        new TowerConfig(SP*13, -SP, SP*23, new String[] {"0"}),
        new TowerConfig(SP*13, -SP, SP*24, new String[] {"0"}),
        new TowerConfig(SP*13, -SP, SP*25, new String[] {"0"}),

    };

    static final StripConfig[] STRIP_CONFIG = {
        // controller-id x y z x-rot y-rot z-rot num-leds pitch-in-inches
        //new StripConfig("206", 0, 0, 0, 0, 0, 0, 10, 0.25),
    };

    static class StripConfig {
        String id;
        int numPoints;
        float spacing;
        float x;
        float y;
        float z;
        float xRot;
        float yRot;
        float zRot;

        StripConfig(String id, float x, float y, float z, float xRot, float yRot, float zRot, int numPoints, float spacing) {
            this.id = id;
            this.numPoints = numPoints;
            this.spacing = spacing;
            this.x = x;
            this.y = y;
            this.z = z;
            this.xRot = xRot;
            this.yRot = yRot;
            this.zRot = zRot;
        }
    }

    static class TowerConfig {

        final CubesModel.Cube.Type type;
        final float x;
        final float y;
        final float z;
        final float xRot;
        final float yRot;
        final float zRot;
        final String[] ids;
        final float[] yValues;

        TowerConfig(float x, float y, float z, String[] ids) {
            this(CubesModel.Cube.Type.LARGE, x, y, z, ids);
        }

        TowerConfig(float x, float y, float z, float yRot, String[] ids) {
            this(x, y, z, 0, yRot, 0, ids);
        }

        TowerConfig(CubesModel.Cube.Type type, float x, float y, float z, String[] ids) {
            this(type, x, y, z, 0, 0, 0, ids);
        }

        TowerConfig(CubesModel.Cube.Type type, float x, float y, float z, float yRot, String[] ids) {
            this(type, x, y, z, 0, yRot, 0, ids);
        }

        TowerConfig(float x, float y, float z, float xRot, float yRot, float zRot, String[] ids) {
            this(CubesModel.Cube.Type.LARGE, x, y, z, xRot, yRot, zRot, ids);
        }

        TowerConfig(CubesModel.Cube.Type type, float x, float y, float z, float xRot, float yRot, float zRot, String[] ids) {
            this.type = type;
            this.x = x;
            this.y = y;
            this.z = z;
            this.xRot = xRot;
            this.yRot = yRot;
            this.zRot = zRot;
            this.ids = ids;

            this.yValues = new float[ids.length];
            for (int i = 0; i < ids.length; i++) {
                yValues[i] = y + i * (CUBE_HEIGHT + CUBE_SPACING);
            }
        }

    }

    static Map<String, String> macToPhysid = new HashMap<>();
    static Map<String, String> physidToMac = new HashMap<>();

    public SLModel buildModel() {

        byte[] bytes = SLStudio.applet.loadBytes("physid_to_mac.json");
        if (bytes != null) {
            try {
                JsonObject json = new Gson().fromJson(new String(bytes), JsonObject.class);
                for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                    macToPhysid.put(entry.getValue().getAsString(), entry.getKey());
                    physidToMac.put(entry.getKey(), entry.getValue().getAsString());
                }
            }  catch (JsonSyntaxException e) {
                e.printStackTrace();
            }
        }

        // Any global transforms
        LXTransform globalTransform = new LXTransform();
        globalTransform.translate(globalOffsetX, globalOffsetY, globalOffsetZ);
        globalTransform.rotateY(globalRotationY * Math.PI / 180.);
        globalTransform.rotateX(globalRotationX * Math.PI / 180.);
        globalTransform.rotateZ(globalRotationZ * Math.PI / 180.);

        /* Cubes ----------------------------------------------------------*/
        List<CubesModel.Tower> towers = new ArrayList<>();
        List<CubesModel.Cube> allCubes = new ArrayList<>();

        int stripId = 0;
        for (TowerConfig config : TOWER_CONFIG) {
            List<CubesModel.Cube> cubes = new ArrayList<>();
            float x = config.x;
            float z = config.z;
            float xRot = config.xRot;
            float yRot = config.yRot;
            float zRot = config.zRot;
            CubesModel.Cube.Type type = config.type;

            for (int i = 0; i < config.ids.length; i++) {
                float y = config.yValues[i];
                CubesModel.Cube cube = new CubesModel.Cube(config.ids[i], x, y, z, xRot, yRot, zRot, globalTransform, type);
                cubes.add(cube);
                allCubes.add(cube);
            }
            towers.add(new CubesModel.Tower("", cubes));
        }
        /*-----------------------------------------------------------------*/

        CubesModel.Cube[] allCubesArr = new CubesModel.Cube[allCubes.size()];
        for (int i = 0; i < allCubesArr.length; i++) {
            allCubesArr[i] = allCubes.get(i);
        }



        return new CubesModel(towers, allCubesArr);
    }

    /*
    public static LXModel importObjModel() {
        return new LXModel(new ObjImporter("data", globalTransform).getModels().toArray(new LXModel[0]));
    }
    */

    private static Map<LX, WeakReference<GlowMotionLayout>> instanceByLX = new WeakHashMap<>();

    public static GlowMotionLayout getInstance(LX lx) {
        WeakReference<GlowMotionLayout> weakRef = instanceByLX.get(lx);
        return weakRef == null ? null : weakRef.get();
    }

    public static void addFadecandyOutput(LX lx) throws Exception {
        lx.engine.addOutput(new FadecandyOutput(lx, "localhost", 7890, lx.model));
//    lx.engine.addOutput(new FadecandyOutput(lx, "192.168.0.113", 1234, lx.model));
    }
    public void setupLx(SLStudioLX lx) {
        instanceByLX.put(lx, new WeakReference<>(this));

        final NetworkMonitor networkMonitor = NetworkMonitor.getInstance(lx).start();
        final Dispatcher dispatcher = Dispatcher.getInstance(lx);


        CubesController local_debug = new CubesController(lx, "localhost", "localdebug");
        controllers.add(local_debug);
        lx.addOutput(local_debug);


        TowerConfig config = new TowerConfig(SP*3.0f, (JUMP*2)+3          , -SP*5.5f, new String[] {"188"});
        List<CubesModel.Cube> cubes = new ArrayList<>();
        float x = config.x;
        float z = config.z;
        float xRot = config.xRot;
        float yRot = config.yRot;
        float zRot = config.zRot;
        CubesModel.Cube.Type type = config.type;

        LXTransform globalTransform = new LXTransform();
        globalTransform.translate(globalOffsetX, globalOffsetY, globalOffsetZ);
        globalTransform.rotateY(globalRotationY * Math.PI / 180.);
        globalTransform.rotateX(globalRotationX * Math.PI / 180.);
        globalTransform.rotateZ(globalRotationZ * Math.PI / 180.);
        for (int i = 0; i < config.ids.length; i++) {
            float y = config.yValues[i];
            CubesModel.Cube cube = new CubesModel.Cube(config.ids[i], x, y, z, xRot, yRot, zRot, globalTransform, type);
        }

        networkMonitor.networkDevices.addListener(new ListListener<NetworkDevice>() {
            public void itemAdded(int index, NetworkDevice device) {
                String macAddr = NetworkUtils.macAddrToString(device.macAddress);
                String physid = macToPhysid.get(macAddr);
                if (physid == null) {
                    physid = macAddr;
                    System.err.println("WARNING: MAC address not in physid_to_mac.json: " + macAddr);
                }
                final CubesController controller = new CubesController(lx, device, physid);
                controllers.add(index, controller);
                dispatcher.dispatchNetwork(() -> lx.addOutput(controller));
                //controller.enabled.setValue(false);
            }

            public void itemRemoved(int index, NetworkDevice device) {
                final CubesController controller = controllers.remove(index);
                dispatcher.dispatchNetwork(() -> {
                    //lx.removeOutput(controller);
                });
            }
        });

//    lx.addOutput(new CubesController(lx, "10.200.1.255"));
        //lx.addOutput(new LIFXOutput());

        lx.engine.output.enabled.addListener(param -> {
            boolean isEnabled = ((BooleanParameter) param).isOn();
            for (CubesController controller : controllers) {
                controller.enabled.setValue(isEnabled);
            }
        });
    }

    public List<CubesController> getSortedControllers() {
        List<CubesController> sorted = new ArrayList<CubesController>(controllers);
        sorted.sort(new Comparator<CubesController>() {
            public int compare(CubesController o1, CubesController o2) {
                try {
                    return Integer.parseInt(o1.id) - Integer.parseInt(o2.id);
                } catch (NumberFormatException e) {
                    return o1.id.compareTo(o2.id);
                }
            }
        });
        return sorted;
    }

    public void addControllerListListener(ListListener<CubesController> listener) {
        controllers.addListener(listener);
    }

    public void setupUi(SLStudioLX lx, SLStudioLX.UI ui) {
        UI2dScrollContext utility = ui.rightPane.utility;
        //new UIOutputs(lx, ui, this, 0, 0, utility.getContentWidth()).addToContainer(utility);
        new UIMappingPanel(lx, ui, 0, 0, utility.getContentWidth()).addToContainer(utility);
    }
}
