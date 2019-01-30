package com.symmetrylabs.slstudio.mappings;

import com.google.gson.annotations.Expose;
import com.symmetrylabs.util.ClassUtils;
import heronarts.lx.model.LXPoint;

public class MappingItem {

    @Expose private OutputMappingItemRef output;
    private OutputMappingItem outputObj;

    public LXPoint[] points;

    public OutputMappingItemRef getOutput() {
        return output;
    }

    public void assignOutputObj(OutputMappingItem outputObj) {
        if (this.outputObj == outputObj) return;

        if (this.outputObj != null) {
            this.outputObj.mappingItemWasRemoved(this);
        }
        this.outputObj = outputObj;
        if (this.outputObj != null) {
            this.outputObj.mappingItemWasAdded(this);
        }
    }

    public void clearOutput() {
        OutputMappingItem oldOutputObj = outputObj;
        output = null;
        outputObj = null;
        if (oldOutputObj != null) {
            oldOutputObj.mappingItemWasRemoved(this);
        }
    }

    public <OutputMappingRefType extends OutputMappingItemRef> OutputMappingRefType getOutputAs(Class<OutputMappingRefType> type) {
        return ClassUtils.tryCast(output, type);
    }

    public <OutputMappingRefType extends OutputMappingItemRef> OutputMappingRefType getOrCreateOutputAs(Class<OutputMappingRefType> type) {
        if (output == null) {
            output = ClassUtils.tryCreateObject(type);
        }
        return getOutputAs(type);
    }

    public <OutputMappingType extends OutputMappingItem> OutputMappingType getOutputObjAs(Class<OutputMappingType> type) {
        if (outputObj == null) {
            outputObj = ClassUtils.tryCreateObject(type);
        }
        return ClassUtils.tryCast(outputObj, type);
    }

}
