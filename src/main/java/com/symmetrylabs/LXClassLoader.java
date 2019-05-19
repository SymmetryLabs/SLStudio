package com.symmetrylabs;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import java.util.stream.Collectors;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import org.lwjgl.system.CallbackI.D;


import com.symmetrylabs.shows.Show;
import heronarts.lx.warp.LXWarp;
import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import io.github.lukehutch.fastclasspathscanner.scanner.ScanResult;

import heronarts.lx.LX;
import heronarts.lx.LXEffect;
import heronarts.lx.LXPattern;
import heronarts.lx.model.LXModel;

import com.symmetrylabs.slstudio.component.HiddenComponent;
import com.symmetrylabs.slstudio.effect.SLEffect;
import com.symmetrylabs.slstudio.pattern.test.SLTestPattern;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import com.symmetrylabs.slstudio.warp.SLWarp;
import com.symmetrylabs.slstudio.component.RequiresProcessing;
import com.symmetrylabs.slstudio.SLStudio;

public class LXClassLoader {
    // Enable test patterns by passing -DloadTestPatterns=true as a VM option
    public static final boolean LOAD_TEST_PATTERNS = Boolean.getBoolean("loadTestPatterns");

    private static final String basePackageName = LXClassLoader.class.getPackage().getName();
    private static final ScanResult classpathScanner =
        new FastClasspathScanner(basePackageName, "heronarts.lx").scan();

    public static List<Class<LXPattern>> findPatterns() {
        return findPatterns(null);
    }

    public static List<Class<LXPattern>> findPatterns(Class<? extends LXModel> modelClass) {
        return getSubclassStream(LXPattern.class, modelClass)
            .filter(it -> LOAD_TEST_PATTERNS || !SLTestPattern.class.isAssignableFrom(it))
            .collect(Collectors.toList());
    }

    public static List<Class<LXEffect>> findEffects() {
        return findEffects(null);
    }

    public static List<Class<LXEffect>> findEffects(Class<? extends LXModel> modelClass) {
        return getSubclassStream(LXEffect.class, modelClass).collect(Collectors.toList());
    }

    public static List<Class<LXWarp>> findWarps() {
        return findWarps(null);
    }

    public static List<Class<LXWarp>> findWarps(Class<? extends LXModel> modelClass) {
        return getSubclassStream(LXWarp.class, modelClass).collect(Collectors.toList());
    }

    public static List<Class<Show>> findShows() {
        return getSubclassStream(Show.class, null).collect(Collectors.toList());
    }

    public static String guessExistingPatternClassName(String className) {
        return guessExistingClassName(className, LXPattern.class);
    }

    public static String guessExistingEffectClassName(String className) {
        return guessExistingClassName(className, LXEffect.class);
    }

    private static <T> String guessExistingClassName(String className, Class<T> parent) {
        if (classExists(className))
            return className;

        final String simpleName = className.replaceAll(".*[\\.\\$]", "");
        final List<String> names = classpathScanner.getNamesOfSubclassesOf(parent)
                .stream()
                .filter(it -> it.endsWith("." + simpleName) || it.endsWith("$" + simpleName))
                .collect(Collectors.toList());

        if (names.size() == 1)
            return names.get(0);

        if (names.isEmpty())
            throw new IllegalArgumentException("No class found with name " + simpleName + " (from " + className + ")");

        throw new IllegalArgumentException("Multiple classes found with name " + simpleName + " (from " + className + "): " + names);
    }

    private static <T> Stream<Class<T>> getSubclassStream(Class<T> baseClass, Class<? extends LXModel> modelClass) {
        return classpathScanner.getNamesOfSubclassesOf(baseClass).stream()
            .map(LXClassLoader::classForNameOrNull)
            .filter(Objects::nonNull)
            .filter(LXClassLoader::isConstructable)
            .filter(LXClassLoader::isVisible)
            .filter(cls -> supportsModelClass(modelClass, cls))
            .map(c -> (Class<T>)c);
    }

    private static boolean isVisible(Class component) {
        while (component != null) {
            if (component.isAnnotationPresent(HiddenComponent.class)) {
                return false;
            }
            if (SLStudio.applet == null && component.isAnnotationPresent(RequiresProcessing.class)) {
                return false;
            }
            component = component.getSuperclass();
        }
        return true;
    }

    private static <T> boolean supportsModelClass(Class<? extends LXModel> modelClass, Class<T> componentClass) {
        if (modelClass == null) {
            return true;
        }

        Type supportedModelType = null;
        if (SLPattern.class.isAssignableFrom(componentClass)) {
            supportedModelType = findModelParameterOfBaseType(componentClass, SLPattern.class);
        } else if (SLEffect.class.isAssignableFrom(componentClass)) {
            supportedModelType = findModelParameterOfBaseType(componentClass, SLEffect.class);
        } else if (SLWarp.class.isAssignableFrom(componentClass)) {
            supportedModelType = findModelParameterOfBaseType(componentClass, SLWarp.class);
        }

        if (supportedModelType == null) {
            return true;
        }
        if (!(supportedModelType instanceof Class)) {
            System.err.println(
                String.format(
                    "warning: pattern %s has unindexable model type parameter %s of type %s",
                    componentClass.getTypeName(),
                    supportedModelType.getTypeName(),
                    supportedModelType.getClass().getTypeName()));
            return true;
        }
        return ((Class) supportedModelType).isAssignableFrom(modelClass);
    }

    private static Type findModelParameterOfBaseType(Class sub, Class base) {
        Type st = sub.getGenericSuperclass();
        Type lastTypeVariable = null;
        String prefix = base.getTypeName().replaceFirst("<.*", "");
        while (!st.getTypeName().replaceFirst("<.*", "").equals(prefix)) {
            if (st instanceof Class) {
                st = ((Class) st).getGenericSuperclass();
            } else if (st instanceof ParameterizedType) {
                ParameterizedType pt = (ParameterizedType) st;
                st = ((Class) pt.getRawType()).getGenericSuperclass();
                lastTypeVariable = pt.getActualTypeArguments()[0];
            }
        }

        /* If this uses the bare type of the supertype, the type is unparameterized and it is assumed
             to support all types of models. */
        if (!(st instanceof ParameterizedType)) {
            return null;
        }

        Type supportedModelType = ((ParameterizedType) st).getActualTypeArguments()[0];
        if (supportedModelType instanceof ParameterizedType) {
            supportedModelType = ((ParameterizedType) supportedModelType).getRawType();
        }
        /* This is a best guess; we don't build the complete type tree, but we keep track of the last type
             variable instatiation as we walk up the tree. We assume that generic pattern base classes will
             use that type variable to store what kind of model it supports. If the instantiation of that
             type variable was not a subclass of LXModel, we guessed wrong and we assume this pattern
             supports all types.

             We could instead keep all of the information we need around to walk back up the tree and figure
             out what type we're instantiating the SLPattern type with, but that's much more complicated and
             this works for our (relatively-simple) class hierarchy for now. */
        if (supportedModelType instanceof TypeVariable) {
            if (lastTypeVariable instanceof ParameterizedType) {
                lastTypeVariable = ((ParameterizedType) lastTypeVariable).getRawType();
            }
            if (lastTypeVariable instanceof Class) {
                if (LXModel.class.isAssignableFrom((Class) lastTypeVariable)) {
                    supportedModelType = lastTypeVariable;
                }
            }
        }
        return supportedModelType;
    }

    private static Class<?> classForNameOrNull(String name) {
        try {
            return Class.forName(name);
        }
        catch (ClassNotFoundException e) {
            return null;
        }
    }

    private static boolean classExists(String name) {
        return classForNameOrNull(name) != null;
    }

    private static boolean isConstructable(Class c) {
        int mod = c.getModifiers();
        if (Modifier.isAbstract(mod) || Modifier.isInterface(mod))
            return false;

        try {
            c.getConstructor(LX.class);
        }
        catch (NoSuchMethodException e) {
            return false;
        }

        return true;
    }
}
