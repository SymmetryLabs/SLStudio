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
    protected final TreeConfig config;
    protected final String pkg;
    protected final boolean writeTypes;

    public TreeCompiler(TreeConfig config, String pkg, boolean writeTypes) {
        this.config = config;
        this.pkg = pkg;
        this.writeTypes = writeTypes;
    }

    public void emit(Writer w) throws IOException {
        w.write("package ");
        w.write(pkg);
        w.write(";\n\nimport com.symmetrylabs.shows.tree.config.*;\n\n");
        w.write("public class CompiledTreeData {\n");
        if (writeTypes) {
            for (String branchTypeName : TreeConfig.getBranchTypes()) {
                TwigConfig[] branchType = TreeConfig.getBranchType(branchTypeName);
                emit(w, branchTypeName, branchType);
                w.write("\n");
            }
            for (String limbTypeName : TreeConfig.getLimbTypes()) {
                BranchConfig[] limbType = TreeConfig.getLimbType(limbTypeName);
                emit(w, limbTypeName, limbType);
                w.write("\n");
            }
        }
        w.write("\tpublic static final TreeConfig CONFIG = new TreeConfig(new LimbConfig[] {\n");
        for (LimbConfig lc : config.getLimbs()) {
            w.write(
                String.format(
                    "\t\tnew LimbConfig(%s, %ff, %ff, %ff, %ff, %ff, new BranchConfig[] {\n",
                    lc.locked, lc.length, lc.height, lc.azimuth, lc.elevation, lc.tilt));
            for (BranchConfig bc : lc.getBranches()) {
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

    protected void emit(Writer w, String name, TwigConfig[] btype) throws IOException {
        w.write("\tpublic static final TwigConfig[] BRANCH_TYPE_");
        w.write(ident(name));
        w.write(" = {\n");
        for (int i = 0; i < btype.length; i++) {
            TwigConfig tc = btype[i];
            w.write("\t\t");
            emit(w, tc);
            w.write(",\n");
        }
        w.write("\t};\n");
    }

    protected void emit(Writer w, TwigConfig tc) throws IOException {
        w.write(
            String.format(
                "new TwigConfig(%ff, %ff, %ff, %ff, %ff, %ff, %d)",
                tc.x, tc.y, tc.z, tc.azimuth, tc.elevation, tc.tilt, tc.index));
    }

    protected void emit(Writer w, String name, BranchConfig[] ltype) throws IOException {
        w.write("\tpublic static final BranchConfig[] LIMB_TYPE_");
        w.write(ident(name));
        w.write(" = {\n");
        for (int i = 0; i < ltype.length; i++) {
            w.write("\t\t");
            emit(w, ltype[i]);
            w.write(",\n");
        }
        w.write("\t};\n");
    }

    protected void emit(Writer w, BranchConfig bc) throws IOException {
        w.write(
            String.format(
                "new BranchConfig(%s, \"%s\", %d, %ff, %ff, %ff, %ff, %ff, %ff, new TwigConfig[] {\n",
                bc.locked, bc.ipAddress, bc.channel, bc.x, bc.y, bc.z, bc.azimuth, bc.elevation, bc.tilt));
        for (TwigConfig tc : bc.getTwigs()) {
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
