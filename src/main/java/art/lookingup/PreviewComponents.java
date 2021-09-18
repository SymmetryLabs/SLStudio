package art.lookingup;

import heronarts.lx.color.LXColor;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI3dComponent;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PVector;

import static processing.core.PConstants.TRIANGLE_STRIP;

public class PreviewComponents {

    public static class Axes extends UI3dComponent {
        private UICylinder xAxis;
        private UICylinder yAxis;
        private UICylinder zAxis;
        private Floor floor;
        public boolean showAxes;
        public boolean showCtrlPoints;
        public boolean showFloor;
        public boolean showCables;
        static public int selectedRun;
        static public int selectedBezier;
        static public int selectedCtrlPt;
        static public float ptSize;

        public Axes() {
            xAxis = new UICylinder(1f, 10.0f, 4, LXColor.rgb(127,0,0));
            yAxis = new UICylinder(1f, 10.0f, 4, LXColor.rgb(0, 127, 0));
            zAxis = new UICylinder(1f, 10.0f, 4, LXColor.rgb(0, 0, 127));
            floor = new Floor();
            showAxes = true;
            showFloor = true;
            showCtrlPoints = false;
            showCables = true;
            selectedRun = 0;
            selectedBezier = 0;
            selectedCtrlPt = 0;
            ptSize = 5f;
        }

        public void nextSelectedRun() {
            selectedRun = (selectedRun + 1) % KaledoscopeModel.allButterflyRuns.size();
        }

        public void nextSelectedBezier() {
            selectedBezier = (selectedBezier + 1) % KaledoscopeModel.BEZIERS_PER_RUN;
        }

        public void nextSelectedCtrlPt() {
            selectedCtrlPt = (selectedCtrlPt + 1) % 4;
        }

        static public Bezier.Point getCurSelPt() {
            KaledoscopeModel.Run selRun = KaledoscopeModel.allRuns.get(selectedRun);
            if (selectedBezier != -1) {
                if (selRun != null && selRun.beziers != null && selRun.beziers.size() > 0) {
                    Bezier selBezier = selRun.beziers.get(selectedBezier);
                    if (selectedCtrlPt != -1) {
                        Bezier.Point ctrlPt = getBezierPointByIndex(selBezier, selectedCtrlPt);
                        return ctrlPt;
                    }
                }
            }
            return null;
        }

        static public Bezier getCurBezier() {
            return getCurSelRun().beziers.get(selectedBezier);
        }

        static public KaledoscopeModel.Run getCurSelRun() {
            return KaledoscopeModel.allRuns.get(selectedRun);
        }

        static public Bezier.Point getBezierPointByIndex(Bezier bezier, int index) {
            switch (index) {
                case 0:
                    return bezier.start;
                case 1:
                    return bezier.c1;
                case 2:
                    return bezier.c2;
                case 3:
                    return bezier.end;
                default:
                    return null;
            }
        }

        public void onDraw(UI ui, PGraphics pg) {
            if (showAxes) {

                yAxis.onDraw(ui, pg);
                pg.pushMatrix();
                pg.rotateZ(-PConstants.HALF_PI);
                xAxis.onDraw(ui, pg);
                pg.popMatrix();
                pg.pushMatrix();
                pg.rotateX(PConstants.HALF_PI);
                zAxis.onDraw(ui, pg);
                pg.popMatrix();

                if (showCtrlPoints) {
                    KaledoscopeModel.Run selRun = null;
                    Bezier selBezier = null;
                    Bezier.Point ctrlPt = null;
                    selRun = KaledoscopeModel.allRuns.get(selectedRun);
                    selBezier = selRun.beziers.get(selectedBezier);
                    ctrlPt = getBezierPointByIndex(selBezier, selectedCtrlPt);

                    // show cubes at each bezier control point
                    for (KaledoscopeModel.Run run : KaledoscopeModel.allRuns) {
                        if (run.beziers != null) {
                            if (run == selRun) {
                                for (Bezier bezier : run.beziers) {
                                    pg.stroke(0);
                                    pg.pushMatrix();
                                    pg.translate(bezier.start.x, KaledoscopeModel.butterflyYHeight, bezier.start.y);
                                    int bright = 255;
                                    pg.stroke(0);
                                    if (selRun == run &&
                                        selBezier == bezier &&
                                        ctrlPt == bezier.start)
                                        pg.stroke(255);
                                    pg.fill(LXColor.rgb(0, 0, bright));
                                    pg.box(ptSize);
                                    pg.popMatrix();

                                    pg.pushMatrix();
                                    pg.translate(bezier.end.x, KaledoscopeModel.butterflyYHeight, bezier.end.y);
                                    pg.stroke(0);
                                    if (selRun == run &&
                                        selBezier == bezier &&
                                        ctrlPt == bezier.end)
                                        pg.stroke(255);
                                    pg.fill(LXColor.rgb(0, bright, bright));
                                    pg.box(ptSize);
                                    pg.popMatrix();

                                    pg.pushMatrix();
                                    pg.translate(bezier.c1.x, KaledoscopeModel.butterflyYHeight, bezier.c1.y);
                                    pg.stroke(0);
                                    if (selRun == run &&
                                        selBezier == bezier &&
                                        ctrlPt == bezier.c1)
                                        pg.stroke(255);
                                    pg.fill(LXColor.rgb(bright, 0, 0));
                                    pg.box(ptSize);
                                    pg.popMatrix();

                                    pg.pushMatrix();
                                    pg.translate(bezier.c2.x, KaledoscopeModel.butterflyYHeight, bezier.c2.y);
                                    pg.stroke(0);
                                    if (selRun == run &&
                                        selBezier == bezier &&
                                        ctrlPt == bezier.c2)
                                        pg.stroke(255);
                                    pg.fill(LXColor.rgb(0, bright, 0));
                                    pg.box(ptSize);
                                    pg.popMatrix();
                                    pg.stroke(0);
                                }
                            }
                        }
                    }
                }
            }
            if (showFloor)
                floor.onDraw(ui, pg);
        }
    }

    /**
     * Utility class for drawing cylinders. Assumes the cylinder is oriented with the
     * y-axis vertical. Use transforms to position accordingly.
     */
    public static class UICylinder extends UI3dComponent {

        private final PVector[] base;
        private final PVector[] top;
        private final int detail;
        public final float len;
        private int fill;

        public UICylinder(float radius, float len, int detail, int fill) {
            this(radius, radius, 0, len, detail, fill);
        }

        public UICylinder(float baseRadius, float topRadius, float len, int detail, int fill) {
            this(baseRadius, topRadius, 0, len, detail, fill);
        }

        public UICylinder(float baseRadius, float topRadius, float yMin, float yMax, int detail, int fill) {
            this.base = new PVector[detail];
            this.top = new PVector[detail];
            this.detail = detail;
            this.len = yMax - yMin;
            this.fill = fill;
            for (int i = 0; i < detail; ++i) {
                float angle = i * PConstants.TWO_PI / detail;
                this.base[i] = new PVector(baseRadius * (float)Math.cos(angle), yMin, baseRadius * (float)Math.sin(angle));
                this.top[i] = new PVector(topRadius * (float)Math.cos(angle), yMax, topRadius * (float)Math.sin(angle));
            }
        }

        public void onDraw(UI ui, PGraphics pg) {
            pg.fill(fill);
            pg.noStroke();
            pg.beginShape(TRIANGLE_STRIP);
            for (int i = 0; i <= this.detail; ++i) {
                int ii = i % this.detail;
                pg.vertex(this.base[ii].x, this.base[ii].y, this.base[ii].z);
                pg.vertex(this.top[ii].x, this.top[ii].y, this.top[ii].z);
            }
            pg.endShape(PConstants.CLOSE);
        }
    }

    static public class Floor extends UI3dComponent {
        public Floor() {

        }

        public void onDraw(UI ui, PGraphics pg) {
            float x1 = 120.0f;
            float y1 = 0f;
            float z1 = -1170.0f;
            pg.noStroke();
            pg.fill(40, 40, 40);
            pg.beginShape();
            pg.vertex(x1, y1, 0f);
            pg.vertex(-x1, y1, 0f);
            pg.vertex(-x1, y1, -z1);
            pg.vertex(x1, y1, -z1);
            pg.endShape(PConstants.CLOSE);
        }
    }
}
