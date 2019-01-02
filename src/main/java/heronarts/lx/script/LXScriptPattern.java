/**
 * Copyright 2017- Mark C. Slee, Heron Arts LLC
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 * @author Mark C. Slee <mark@heronarts.com>
 */

package heronarts.lx.script;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import javax.script.Bindings;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;

import heronarts.lx.LX;
import heronarts.lx.LXComponent;
import heronarts.lx.LXPattern;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.StringParameter;

public class LXScriptPattern extends LXPattern {

    public final StringParameter scriptPath =
        new StringParameter("Path")
        .setDescription("Path to the script file");

    private ScriptContext context;
    private Bindings bindings;

    protected final List<LXParameter> jsParameters = new ArrayList<LXParameter>();

    private boolean jsInitializing = false;
    private boolean jsInitialized = false;

    private final String IMPORT_JS =
        // Color
        "var LXColor = Java.type(\"heronarts.lx.color.LXColor\");" +

        // Parameters
        "var BooleanParameter = Java.type(\"heronarts.lx.parameter.BooleanParameter\");" +
        "var BoundedParameter = Java.type(\"heronarts.lx.parameter.BoundedParameter\");" +
        "var CompoundParameter = Java.type(\"heronarts.lx.parameter.CompoundParameter\");" +
        "var DiscreteParameter = Java.type(\"heronarts.lx.parameter.DiscreteParameter\");" +
        "var StringParameter = Java.type(\"heronarts.lx.parameter.StringParameter\");" +

        // Modulators
        "var Accelerator = Java.type(\"heronarts.lx.modulator.Accelerator\");" +
        "var Click = Java.type(\"heronarts.lx.modulator.Click\");" +
        "var LinearEnvelope = Java.type(\"heronarts.lx.modulator.LinearEnvelope\");" +
        "var QuadraticEnvelope = Java.type(\"heronarts.lx.modulator.QuadraticEnvelope\");" +
        "var SawLFO = Java.type(\"heronarts.lx.modulator.SawLFO\");" +
        "var SinLFO = Java.type(\"heronarts.lx.modulator.SinLFO\");" +
        "var SquareLFO = Java.type(\"heronarts.lx.modulator.SquareLFO\");" +
        "var TriangleLFO = Java.type(\"heronarts.lx.modulator.TriangleLFO\");" +
        "var VariableLFO = Java.type(\"heronarts.lx.modulator.VariableLFO\");" +

        // Pattern methods
        "var addModulator = function(modulator) { self.addModulator(modulator); };" +
        "var startModulator = function(modulator) { self.startModulator(modulator) };" +
        "var addParameter = function(path, parameter) { parameter ? self.addParameter(path, parameter) : self.addParameter(path); };";

    public LXScriptPattern(LX lx) {
        super(lx);
        addParameter(this.scriptPath);
    }

    @Override
    public LXComponent addParameter(String path, LXParameter parameter) {
        if (this.jsInitializing) {
            this.jsParameters.add(parameter);
        }
        return super.addParameter(path, parameter);
    }

    @Override
    public void onParameterChanged(LXParameter p) {
        if (p == this.scriptPath) {
            initialize();
        }
    }

    protected File getFile() {
        return new File(lx.engine.script.getScriptPath() + "/" + this.scriptPath.getString());
    }

    protected void initialize() {
        for (LXParameter parameter : this.jsParameters) {
            removeParameter(parameter);
        }
        this.jsParameters.clear();
        this.jsInitialized = false;
        this.jsInitializing = true;
        try {
            FileReader reader = new FileReader(getFile());
            ScriptEngine engine = lx.engine.script.getEngine();

            this.context = new SimpleScriptContext();
            this.context.setBindings(engine.createBindings(), ScriptContext.ENGINE_SCOPE);
            this.bindings = this.context.getBindings(ScriptContext.ENGINE_SCOPE);

            this.bindings.put("self", this);
            this.bindings.put("lx", this.lx);
            this.bindings.put("palette", this.palette);
            this.bindings.put("model", this.model);
            this.bindings.put("colors", this.colors);

            engine.setContext(this.context);
            engine.eval(IMPORT_JS);
            engine.eval(reader);
            ((Invocable) engine).invokeFunction("init");
            this.jsInitialized = true;
        } catch (FileNotFoundException fnfx) {
            System.err.println("[SCRIPT] " + getLabel() + " (" + this.scriptPath.getString() + ") File not found: " + fnfx.getLocalizedMessage());
        } catch (NoSuchMethodException nsmx) {
            System.err.println("[SCRIPT] " + getLabel() + " (" + this.scriptPath.getString() + ") has no init() function: " + nsmx.getLocalizedMessage());
        } catch (ScriptException sx) {
            System.err.println("[SCRIPT] " + getLabel() + " (" + this.scriptPath.getString() + ") failed to initialize: " + sx.getLocalizedMessage());
        } catch (Exception x) {
            System.err.println("[SCRIPT] " + getLabel() + " (" + this.scriptPath.getString() + ") error: " + x.getLocalizedMessage());
        }
        this.jsInitializing = false;
    }

    @Override
    public void run(double deltaMs) {
        if (this.jsInitialized) {
            ScriptEngine engine = lx.engine.script.getEngine();
            engine.setContext(this.context);
            try {
                ((Invocable) engine).invokeFunction("run", deltaMs);
            } catch (NoSuchMethodException nsmx) {
                System.err.println("[SCRIPT] " + getLabel() + " (" + this.scriptPath.getString() + ") has no run() function: " + nsmx.getLocalizedMessage());
                this.jsInitialized = false;
            } catch (ScriptException sx) {
                System.err.println("[SCRIPT] " + getLabel() + " (" + this.scriptPath.getString() + ") error in run: " + sx.getLocalizedMessage());
                this.jsInitialized = false;
            } catch (Exception x) {
                System.err.println("[SCRIPT] " + getLabel() + " (" + this.scriptPath.getString() + ") error: " + x.getLocalizedMessage());
                this.jsInitialized = false;
            }
        }
    }

}
