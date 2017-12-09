package com.symmetrylabs.util;

import java.lang.reflect.Field;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */ // http://stackoverflow.com/a/60766/216311
public static class ClassPathHack {
    private static final Class[] parameters = new Class[]{URL.class};

    public static void addFile(String s) throws IOException {
        File f = new File(s);
        addFile(f);
    }

    public static void addFile(File f) throws IOException {
        addURL(f.toURI().toURL());
    }

    public static void addURL(URL u) throws IOException {
        URLClassLoader sysloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        Class sysclass = URLClassLoader.class;

        // try {
        //   Method method = sysclass.getDeclaredMethod("addURL", parameters);
        //   method.setAccessible(true);
        //   method.invoke(sysloader, new Object[] {u});
        // } catch (Throwable t) {
        //   t.printStackTrace();
        //   throw new IOException("Error, could not add URL to system classloader");
        // }
    }

    // http://fahdshariff.blogspot.com/2011/08/changing-java-library-path-at-runtime.html

    /**
     * Adds the specified path to the java library path
     *
     * @param pathToAdd
     *     the path to add
     * @throws Exception
     */
    public static void addLibraryPath(String pathToAdd) throws Exception {
        final Field usrPathsField = ClassLoader.class.getDeclaredField("usr_paths");
        usrPathsField.setAccessible(true);

        //get array of paths
        final String[] paths = (String[]) usrPathsField.get(null);

        //check if the path to add is already present
        for (String path : paths) {
            if (path.equals(pathToAdd)) {
                return;
            }
        }

        //add the new path
        final String[] newPaths = Arrays.copyOf(paths, paths.length + 1);
        newPaths[newPaths.length - 1] = pathToAdd;
        usrPathsField.set(null, newPaths);
    }
}

    static public float angleBetween(PVector v1, PVector v2) {

        // We get NaN if we pass in a zero vector which can cause problems
        // Zero seems like a reasonable angle between a (0,0,0) vector and something else
        if (v1.x == 0 && v1.y == 0 && v1.z == 0) return 0.0f;
        if (v2.x == 0 && v2.y == 0 && v2.z == 0) return 0.0f;

        double dot = v1.x * v2.x + v1.y * v2.y + v1.z * v2.z;
        double v1mag = FastMath.sqrt(v1.x * v1.x + v1.y * v1.y + v1.z * v1.z);
        double v2mag = FastMath.sqrt(v2.x * v2.x + v2.y * v2.y + v2.z * v2.z);
        // This should be a number between -1 and 1, since it's "normalized"
        double amt = dot / (v1mag * v2mag);
        // But if it's not due to rounding error, then we need to fix it
        // http://code.google.com/p/processing/issues/detail?id=340
        // Otherwise if outside the range, acos() will return NaN
        // http://www.cppreference.com/wiki/c/math/acos
        if (amt <= -1) {
            return PConstants.PI;
        } else if (amt >= 1) {
            // http://code.google.com/p/processing/issues/detail?id=435
            return 0;
        }
        return (float) FastMath.acos(amt);
    }
