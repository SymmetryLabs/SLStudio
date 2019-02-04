package com.symmetrylabs.slstudio.ui.v2;

import com.symmetrylabs.slstudio.ApplicationState;
import heronarts.lx.LX;
import java.awt.EventQueue;
import java.io.File;

public class FileDialog {
    public static boolean PREFER_NATIVE_DIALOGS = true;

    public enum Type {
        SAVE, OPEN;
    }

    private enum Impl {
        AWT, UNIX_FILE_PICKER_WINDOW, NONE;
    }

    public interface FilePickedCallback {
        void onFilePicked(File f);
    }

    private final String title;
    private final Type type;
    private final Impl impl;
    private final FilePickedCallback fpc;
    private final LX lx;

    public FileDialog(LX lx, String title, Type type, FilePickedCallback fpc) {
        this.lx = lx;
        this.title = title;
        this.type = type;
        this.fpc = fpc;

        Impl sysImpl = Impl.NONE;
        if (PREFER_NATIVE_DIALOGS) {
            String headlessProp = System.getProperty("java.awt.headless");
            if (headlessProp == null || headlessProp.equals("")) {
                sysImpl = Impl.AWT;
            }
        }
        if (sysImpl == Impl.NONE && !System.getProperty("os.name").startsWith("Windows")) {
            sysImpl = Impl.UNIX_FILE_PICKER_WINDOW;
        }
        impl = sysImpl;
    }

    public void show() {
        switch (impl) {
        case AWT:
            EventQueue.invokeLater(this::showImplAwt);
            return;
        case UNIX_FILE_PICKER_WINDOW:
            WindowManager.addTransient(new UnixFilePickerWindow(lx, title, type, fpc));
            return;
        case NONE:
            ApplicationState.setWarning("FileDialog", "this platform does not support file dialogs");
            return;
        }
    }

    public static void open(LX lx, String title, FilePickedCallback fpc) {
        new FileDialog(lx, title, Type.OPEN, fpc).show();
    }

    public static void save(LX lx, String title, FilePickedCallback fpc) {
        new FileDialog(lx, title, Type.SAVE, fpc).show();
    }

    private void showImplAwt() {
        java.awt.FileDialog fd = new java.awt.FileDialog(
            (java.awt.Frame) null, title,
            type == Type.SAVE ? java.awt.FileDialog.SAVE : java.awt.FileDialog.LOAD);
        fd.show();
        String f = fd.getFile();
        if (f == null) {
            return;
        }
        final File res = new File(fd.getDirectory(), f);
        lx.engine.addTask(() -> fpc.onFilePicked(res));
    }
}
