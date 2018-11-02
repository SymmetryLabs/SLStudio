package com.symmetrylabs.slstudio.ui.swing;

import com.jogamp.newt.awt.NewtCanvasAWT;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL4ES3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.symmetrylabs.LXClassLoader;
import com.symmetrylabs.shows.Show;
import com.symmetrylabs.shows.ShowRegistry;
import heronarts.lx.LX;
import heronarts.lx.model.LXModel;
import java.awt.EventQueue;
import java.awt.BorderLayout;
import javax.swing.JFrame;

public class SLStudioSwing extends JFrame {
    private String showName;
    private Show show;
    private ModelRenderer renderer;
    private LX lx;

    public SLStudioSwing() {
        showName = "pilots";
        show = ShowRegistry.getShow(showName);

        LXModel model = show.buildModel();
        lx = new LX(model);

        setTitle("SLStudio");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        final GLWindow glEmbed = GLWindow.create(new GLCapabilities(GLProfile.getGL4ES3()));
        glEmbed.addGLEventListener(new GLEventListener() {
            @Override
            public void init(GLAutoDrawable d) {
                renderer = new ModelRenderer(lx, model, (GL4ES3) d.getGL());
            }

            @Override
            public void dispose(GLAutoDrawable d) {
            }

            @Override
            public void display(GLAutoDrawable d) {
                GL gl = d.getGL();
                gl.glClearColor(0.1f, 0.1f, 0.4f, 1.f);
                gl.glClear(GL.GL_COLOR_BUFFER_BIT);
                if (renderer != null) {
                    renderer.draw();
                }
            }

            @Override
            public void reshape(GLAutoDrawable d, int x, int y, int w, int h) {
                d.getGL().glViewport(0, 0, w, h);
            }
        });

        NewtCanvasAWT canvas = new NewtCanvasAWT(glEmbed);
        add(canvas, BorderLayout.CENTER);
    }

    public static void main(String[] args) {
        final SLStudioSwing sl = new SLStudioSwing();
        EventQueue.invokeLater(() -> {
                sl.setVisible(true);
            });
    }
}
