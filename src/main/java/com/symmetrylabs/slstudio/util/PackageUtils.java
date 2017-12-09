package com.symmetrylabs.slstudio.util;

import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import org.reflections.Reflections;

import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class PackageUtils {

    private static Set<Class<? extends LXPattern>> findPatternClasses(Reflections r) {
        Set<Class<? extends LXPattern>> keep = new HashSet<>();
        try {
            for (Class<? extends LXPattern> c : r.getSubTypesOf(LXPattern.class)) {
                int mod = c.getModifiers();
                if (Modifier.isAbstract(mod) || Modifier.isInterface(mod))
                    continue;

                try {
                    c.getConstructor(LX.class);
                } catch (NoSuchMethodException e) {
                    continue;
                }

                keep.add(c);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return keep;
    }

    public static Set<Class<? extends LXPattern>> getPatternClassesInPackage(String pkg) {
        return findPatternClasses(new Reflections(pkg));
    }

    public static Set<Class<? extends LXPattern>> getPatternClasses() {
        return findPatternClasses(new Reflections());
    }
}
