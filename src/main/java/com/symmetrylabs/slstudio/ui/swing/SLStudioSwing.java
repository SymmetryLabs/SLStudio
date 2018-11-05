package com.symmetrylabs.slstudio.ui.swing;

import com.jogamp.newt.awt.NewtCanvasAWT;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.util.FPSAnimator;
import com.symmetrylabs.LXClassLoader;
import com.symmetrylabs.shows.Show;
import com.symmetrylabs.shows.ShowRegistry;
import heronarts.lx.LX;
import heronarts.lx.model.LXModel;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;
import javax.swing.JTree;
import javax.swing.border.EmptyBorder;

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

        final GLWindow glEmbed = GLWindow.create(
            new GLCapabilities(GLProfile.get(GLProfile.GL4)));
        glEmbed.addGLEventListener(new GLEventListener() {
            @Override
            public void init(GLAutoDrawable d) {
                renderer = new ModelRenderer(lx, model, (GL4) d.getGL());
            }

            @Override
            public void dispose(GLAutoDrawable d) {
            }

            @Override
            public void display(GLAutoDrawable d) {
                GL gl = d.getGL();
                gl.glClearColor(0.05f, 0.05f, 0.05f, 1.f);
                gl.glClear(GL.GL_COLOR_BUFFER_BIT);
                lx.engine.onDraw();
                if (renderer != null) {
                    renderer.draw();
                }
            }

            @Override
            public void reshape(GLAutoDrawable d, int x, int y, int w, int h) {
                d.getGL().glViewport(0, 0, w, h);
                renderer.setAspect(w, h);
            }
        });

        NewtCanvasAWT canvas = new NewtCanvasAWT(glEmbed);
        add(canvas, BorderLayout.CENTER);

        loadLxComponents();
        JTree patternTree = new JTree(new PatternListDataModel(lx, showName));
        patternTree.setPreferredSize(new Dimension(200, 0));
        patternTree.setBorder(new EmptyBorder(5, 5, 5, 5));
        add(patternTree, BorderLayout.WEST);

        FPSAnimator animator = new FPSAnimator(glEmbed, 60);
        animator.start();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                animator.stop();
            }
        });

        lx.engine.start();
    }

    public static void main(String[] args) {
        final SLStudioSwing sl = new SLStudioSwing();
        EventQueue.invokeLater(() -> {
                sl.setVisible(true);
            });
    }

    private void loadLxComponents() {
        LXClassLoader.findWarps().stream().forEach(lx::registerWarp);
        LXClassLoader.findEffects().stream().forEach(lx::registerEffect);
        LXClassLoader.findPatterns().stream().forEach(lx::registerPattern);
        lx.registerPattern(heronarts.p3lx.pattern.SolidColorPattern.class);
    }
}
