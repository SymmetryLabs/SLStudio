package com.symmetrylabs.shows.tree;

import java.io.Writer;
import java.io.IOException;
import com.symmetrylabs.shows.tree.config.*;
import java.util.List;


/**
 * Generates Java code that hardcodes a tree config, for deployment on hosts where we can't load files from disk.
 *
 * This is really not my favorite thing and I'm sorry.
 *
 * @author definitely not Haldean
 */
public class TreeCompiler {
    protected final TreeModel model;
    protected final String pkg;

    public TreeCompiler(TreeModel model, String pkg, boolean writeTypes) {
        this.model = model;
        this.pkg = pkg;
    }

    public void emit(Writer w) throws IOException {
        w.write("package ");
        w.write(pkg);
        w.write(";\n\nimport com.symmetrylabs.shows.tree.config.*;\n\n");
        w.write("public class CompiledTreeData {\n");
        w.write("\tpublic static final TreeConfig CONFIG = new TreeConfig(new LimbConfig[] {\n");
        for (TreeModel.Limb lc : model.limbs) {
            w.write(
                String.format(
                    "\t\tnew LimbConfig(%s, %ff, %ff, %ff, %ff, %ff, new BranchConfig[] {\n",
                    lc.getConfig().locked, lc.length, lc.height, lc.azimuth, lc.elevation, lc.tilt));
            for (TreeModel.Branch bc : lc.getBranches()) {
                w.write("\t\t\t");
                emit(w, bc);
                w.write(",\n");
            }
            w.write("\t\t}),\n");
        }
        w.write("\t});\n");
        w.write("}\n");
        w.flush();
    }

    protected void emit(Writer w, TreeModel.Twig tc) throws IOException {
        w.write(
            String.format(
                "new TwigConfig(%ff, %ff, %ff, %ff, %ff, %ff, %d)",
                tc.x, tc.y, tc.z, tc.azimuth, tc.elevation, tc.tilt, tc.index));
    }

    protected void emit(Writer w, TreeModel.Branch bc) throws IOException {
        w.write(
            String.format(
                "new BranchConfig(%s, \"%s\", %d, %ff, %ff, %ff, %ff, %ff, %ff, new TwigConfig[] {\n",
                bc.getConfig().locked, bc.getConfig().ipAddress, bc.getConfig().channel,
                bc.x, bc.y, bc.z, bc.azimuth, bc.elevation, bc.tilt));
        for (TreeModel.Twig tc : bc.getTwigs()) {
            w.write("\t\t\t\t");
            emit(w, tc);
            w.write(",\n");
        }
        w.write("\t\t\t})");
    }

    protected String ident(String name) {
        return name.replace(" ", "_").replace("/", "_").toUpperCase();
    }
}
