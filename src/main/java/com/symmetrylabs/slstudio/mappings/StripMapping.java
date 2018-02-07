package com.symmetrylabs.slstudio.mappings;

import com.google.gson.annotations.Expose;

public class StripMapping extends MappingItem {
        @Expose public int numPoints;
        @Expose public float rotation;
        @Expose public boolean reversed;
}
