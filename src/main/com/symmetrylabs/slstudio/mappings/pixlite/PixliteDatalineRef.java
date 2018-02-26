package com.symmetrylabs.slstudio.mappings.pixlite;

import com.google.gson.annotations.Expose;
import com.symmetrylabs.slstudio.mappings.OutputMappingItemRef;

public class PixliteDatalineRef extends OutputMappingItemRef {

        @Expose public int datalineIndex;
        @Expose public int datalineOrderIndex;

}
