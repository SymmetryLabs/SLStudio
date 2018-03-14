package com.symmetrylabs.slstudio.layout;

import com.symmetrylabs.slstudio.Environment;
import com.symmetrylabs.slstudio.mappings.Mappings;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.output.Hardware;

public abstract class Layout {

        private SLModel model;
        private boolean isMappable = true;
        private Hardware hardware;

        public Layout() {
        }

        public abstract Environment getEnvironment();

        public SLModel getModel() {
                if (model == null) {
                        model = createModel();
                }
                return model;
        }

        protected abstract SLModel createModel();

        public boolean isMappable() {
                return isMappable;
        }

        protected void setIsMappable(boolean isMappable) {
                this.isMappable = isMappable;
        }

        protected abstract Hardware createHardware();

        public Hardware getHardware() {
                if (hardware == null) {
                        hardware = createHardware();
                        hardware.setEnvironment(getEnvironment());
                }
                return hardware;
        }

        public Mappings getMappings() {
                return getHardware().getMappings();
        }

}
