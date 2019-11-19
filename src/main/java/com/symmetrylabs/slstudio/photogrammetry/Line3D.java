package com.symmetrylabs.slstudio.photogrammetry;

import com.symmetrylabs.slstudio.ApplicationState;
import heronarts.lx.transform.LXVector;
import processing.core.PVector;

public class Line3D {
    PVector start;
    PVector end;

    Line3D(PVector start, PVector end){ this.start = start; this.end = end; }

    public PVector getDirectionVector(){
        return PVector.sub(end, start);
    }

    public PVector getLerpPoint(float amt) {
        PVector dir = this.getDirectionVector();
        return new PVector(start.x + dir.x * amt, start.y + dir.y * amt, start.z + dir.z * amt);
//        return PVector.lerp(end, start, amt);
    }

    public float distFromPoint(PVector x0) {
        float result = 0;
        PVector x2 = end;
        PVector x1 = start;

        // logic to get result
        float numerator = PVector.sub(x0, x1).cross(PVector.sub(x0, x2)).mag();
        float denominator = PVector.sub(x2, x1).mag();
        result = numerator/denominator;

//        ApplicationState.setWarning("numerator", numerator + "");
//        ApplicationState.setWarning("denominator", denominator + "");
        ApplicationState.setWarning("result", result + "");
        return result;
    }

    public float lineLength(){
        float dist = PVector.dist(start, end);
        ApplicationState.setWarning("dist", dist + "");
        return dist;
    }
}
