package com.symmetrylabs;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import java.util.stream.Collectors;
import java.lang.reflect.Modifier;


import com.symmetrylabs.layouts.Layout;
import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import io.github.lukehutch.fastclasspathscanner.scanner.ScanResult;

import heronarts.lx.LX;
import heronarts.lx.LXEffect;
import heronarts.lx.LXPattern;

import com.symmetrylabs.slstudio.pattern.test.SLTestPattern;

public class LXClassLoader {
    // Enable test patterns by passing -DloadTestPatterns=true as a VM option
    public static final boolean LOAD_TEST_PATTERNS = Boolean.getBoolean("loadTestPatterns");

    private static final String basePackageName = LXClassLoader.class.getPackage().getName();
    private static final ScanResult classpathScanner = new FastClasspathScanner(basePackageName).scan();

    public static List<Class<LXPattern>> findPatterns() {
        return getSubclassStream(LXPattern.class)
            .filter(it -> LOAD_TEST_PATTERNS || !SLTestPattern.class.isAssignableFrom(it))
            .collect(Collectors.toList());
    }

    public static List<Class<LXEffect>> findEffects() {
        return getSubclassStream(LXEffect.class)
            .collect(Collectors.toList());
    }

    public static List<Class<Layout>> findLayouts() {
        return getSubclassStream(Layout.class).collect(Collectors.toList());
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

    private static <T> Stream<Class<T>> getSubclassStream(Class<T> baseClass) {
        return classpathScanner.getNamesOfSubclassesOf(baseClass).stream()
            .map(LXClassLoader::classForNameOrNull)
            .filter(Objects::nonNull)
            .filter(LXClassLoader::isConstructable)
            .map(c -> (Class<T>)c);
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
