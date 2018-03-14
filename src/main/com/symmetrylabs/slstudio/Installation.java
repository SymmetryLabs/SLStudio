package com.symmetrylabs.slstudio;

import com.symmetrylabs.slstudio.layout.Layout;
import com.symmetrylabs.slstudio.mappings.Mappings;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.output.Hardware;
import heronarts.lx.output.LXOutput;

public class Installation {

        private static Layout currentLayout;

        public static Layout getLayout() {
                return currentLayout;
        }

        public static void setCurrentLayout(Layout layout) {
                currentLayout = layout;
        }

        public static SLModel getModel() {
                return getLayout().getModel();
        }

        public static Hardware getHardware() {
                return getLayout().getHardware();
        }

        public static Mappings getMappings() {
                return getHardware().getMappings();
        }

        public static LXOutput[] getOutputs() {
                return getHardware().getOutputs();
        }

        private Installation() {
        }
}
