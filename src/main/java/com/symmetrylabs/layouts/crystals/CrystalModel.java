package com.symmetrylabs.layouts.crystals;

import com.symmetrylabs.slstudio.model.SLModel;
import heronarts.lx.model.LXAbstractFixture;
import heronarts.lx.model.LXPoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CrystalModel extends SLModel {

    protected final List<Panel> panels = new ArrayList<>();
    protected final List<SubPanel> subpanels = new ArrayList<>();

    protected final Map<String, Panel> panelTable = new HashMap<>();
    public CrystalModel(List<Panel> panels) {

        super(new Fixture(panels));
        System.out.println("crystalmodel constructor hit");
        for (Panel panel : panels) {
            this.panels.add(panel);
            for (SubPanel subpanel : subpanels) {
                System.out.println("subpanels added");
                this.subpanels.add(subpanel);
            }
        }

//        this(new ArrayList<>());

    }

    private static class Fixture extends LXAbstractFixture {
        private Fixture(List<Panel> panels) {
            for (Panel panel : panels) {
                for (LXPoint point : panel.points){
                    System.out.println(point);
                    this.points.add(point);
//                }
//                panels.add(panel);
//                for (SubPanel subpanel : subpanels) {
//                    subpanels.add(subpanel);
//                    for (LXPoint point : subpanel.) {
//                        points.add(point);

                }
            }
        }
    }
}
