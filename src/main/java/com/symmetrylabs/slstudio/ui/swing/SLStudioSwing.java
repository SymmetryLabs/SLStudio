package com.symmetrylabs.slstudio.ui.swing;

import java.awt.EventQueue;
import javax.swing.JFrame;

public class SLStudioSwing extends JFrame {
    public SLStudioSwing() {
        setTitle("SLStudio");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    public static void main(String[] args) {
        final SLStudioSwing sl = new SLStudioSwing();
        EventQueue.invokeLater(() -> {
                sl.setVisible(true);
            });
    }
}
