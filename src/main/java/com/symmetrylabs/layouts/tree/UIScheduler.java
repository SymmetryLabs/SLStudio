package com.symmetrylabs.layouts.tree.ui;

import java.util.List;
import java.util.ArrayList;

import heronarts.lx.LX;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.StringParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.output.LXOutput;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI2dContainer;
import heronarts.p3lx.ui.component.UILabel;
import heronarts.p3lx.ui.component.UIButton;
import heronarts.p3lx.ui.studio.UICollapsibleSection;
import com.symmetrylabs.layouts.tree.ScheduleControls;
import heronarts.p3lx.ui.component.UIItemList;

import com.symmetrylabs.slstudio.SLStudio;
import com.symmetrylabs.layouts.tree.*;
import com.symmetrylabs.layouts.kalpa.KalpaLayout;
import com.symmetrylabs.layouts.tree.config.*;
import static com.symmetrylabs.util.MathUtils.*;

import heronarts.p3lx.ui.component.UIIntegerBox;


public class UIScheduler extends UICollapsibleSection {

    public UIScheduler(LX lx, UI ui, float x, float y, float w) {
        super(ui, x, y, w, 230);
        setTitle("SCHEDULER");
        setPadding(5);
        setTitleX(20);
        addTopLevelComponent(new UIButton(5, 5, 12, 12)
            .setParameter(ScheduleControls.getInstance(lx).running).setBorderRounding(4));

        new UILabel(0, 0, 40, 20)
            .setLabel("Start")
            .setPadding(5, 5)
            .setBackgroundColor(ui.theme.getDarkBackgroundColor())
            .addToContainer(this);

        new UIIntegerBox(50, 0, 40, 20)
            .setParameter(ScheduleControls.getInstance(lx).startHour).addToContainer(this);

        new UILabel(92, 0, 5, 20)
            .setLabel(":")
            .setPadding(5, 0)
            //.setBackgroundColor(ui.theme.getDarkBackgroundColor())
            .addToContainer(this);

        new UIIntegerBox(97, 0, 40, 20)
            .setParameter(ScheduleControls.getInstance(lx).startMinute).addToContainer(this);


        new UILabel(0, 25, 40, 20)
            .setLabel("End")
            .setPadding(5, 5)
            .setBackgroundColor(ui.theme.getDarkBackgroundColor())
            .addToContainer(this);

        new UIIntegerBox(50, 25, 40, 20)
            .setParameter(ScheduleControls.getInstance(lx).endHour).addToContainer(this);

        new UILabel(92, 25, 5, 20)
            .setLabel(":")
            .setPadding(5, 0)
            //.setBackgroundColor(ui.theme.getDarkBackgroundColor())
            .addToContainer(this);

        new UIIntegerBox(97, 25, 40, 20)
            .setParameter(ScheduleControls.getInstance(lx).endMinute).addToContainer(this);
    }
}
