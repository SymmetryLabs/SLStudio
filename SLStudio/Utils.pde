import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

// See: https://stackoverflow.com/questions/228477/how-do-i-programmatically-determine-operating-system-in-java
public static final class OsUtils {

  private static String osName = null;

  public static String getOsName() {
    if (osName == null) {
      osName = System.getProperty("os.name");
    }
    return osName;
  }

  public static boolean isWindows() {
    return getOsName().startsWith("Windows");
  }

  public static boolean isUnix() {
    return false;
  }

  public static boolean isMacOsX() {
    return getOsName().equals("Mac OS X");
  }

}

// See: http://stackoverflow.com/a/5607373/216311
public class NotYetImplementedException extends RuntimeException {
  private static final long serialVersionUID = 1L;
}

public static final class NetworkUtils {

  private static Pattern macAddressPattern = null;

  private static void initMacAddressPattern() {
    if (macAddressPattern == null) {
      macAddressPattern = Pattern.compile("(\\p{XDigit}{1,2}):(\\p{XDigit}{1,2}):(\\p{XDigit}{1,2}):(\\p{XDigit}{1,2}):(\\p{XDigit}{1,2}):(\\p{XDigit}{1,2})");
    }
  }

  public static String normalizeMacAddress(String macAddress) {
    initMacAddressPattern();
    Matcher m = macAddressPattern.matcher(macAddress);
    if (!m.matches()) {
      throw new IllegalArgumentException("NetworkUtils.normalizeMacAddress(String macAddress): Not a mac address: " + macAddress);
    }
    StringBuilder sb = new StringBuilder();
    for (int i = 1; i <= 6; i++) {
      if (i != 1) sb.append(":");
      sb.append(NumberUtils.normalizeHex(m.group(i)));
    }
    return sb.toString();
  }

  public static String normalizeMacAddressUpper(String macAddress) {
    initMacAddressPattern();
    Matcher m = macAddressPattern.matcher(macAddress);
    if (!m.matches()) {
      throw new IllegalArgumentException("NetworkUtils.normalizeMacAddressUpper(String macAddress): Not a mac address: " + macAddress);
    }
    StringBuilder sb = new StringBuilder();
    for (int i = 1; i <= 6; i++) {
      if (i != 1) sb.append(":");
      sb.append(NumberUtils.normalizeHexUpper(m.group(i)));
    }
    return sb.toString();
  }

  public static String macAddrToString(byte[] addr) {
    StringBuilder sb = new StringBuilder();
    int i = 0;
    for (byte b : addr) {
      if (i++ != 0) sb.append(":");
      sb.append(NumberUtils.normalizeHex(b));
    }
    return sb.toString();
  }

  public static InetAddress ipAddrToInetAddr(String addr) {
    try {
      return InetAddress.getByName(addr);
    } catch (UnknownHostException e) {
      e.printStackTrace();
    }
    return null;
  }

  public static boolean isValidMacAddr(byte[] macAddr) {
    return macAddr[0] != (byte)0xff && macAddr[1] != (byte)0xff && macAddr[2] != (byte)0xff
      && macAddr[3] != (byte)0xff && macAddr[4] != (byte)0xff && macAddr[5] != (byte)0xff;
  }

}

public static final class NumberUtils {

  public static String normalizeHex(String hex) {
    int value = Integer.parseInt(hex, 16);
    return Integer.toString(value, 16);
  }

  public static String normalizeHex(byte hex) {
    return Integer.toString(hex & 0xFF, 16);
  }

  public static String normalizeHexUpper(String hex) {
    int value = Integer.parseInt(hex, 16);
    return String.format("%02X", value);
  }

  public static String normalizeHexUpper(byte hex) {
    return String.format("%02X", hex & 0xFF);
  }

  public static byte hexStringToByte(String hex) {
    return (byte)Integer.parseInt(hex, 16);
  }

  public static int byteToInt(byte b) {
    return (b + 256) % 256;
  }

}

public static final class MathUtils {

  public static byte byteSubtract(int a, int b) {
    byte res = (byte)(a - b);
    return (byte)(res & (byte)((b&0xFF) <= (a&0xFF) ? -1 : 0));
  }
  
  public static byte byteMultiply(byte b, double s) {
    int res = (int)((b&0xFF) * s);
    byte hi = (byte)(res >> 8);
    byte lo = (byte)(res);
    return (byte)(lo | (byte)(hi==0 ? 0 : -1));
  }

  public static void interpolateArray(float[] in, float[] out) {
    if (out.length == in.length) {
      System.arraycopy(in, 0, out, 0, out.length);
      return;
    }

    float outPerIn = 1.0f * (out.length-1) / (in.length-1);
    for (int outIndex = 0; outIndex < out.length; outIndex++) {
      int inIndex = (int)(outIndex / outPerIn);
      // Test if we're the nearest index to the exact index in the `in` array
      // to keep those crisp and un-aliased
      if ((int)(outIndex % outPerIn) == 0) { //  || inIndex+1 >= in.length
        out[outIndex] = in[inIndex];
      } else {
        // Use spline fitting. (Double up the value if we're at the edge of the `out` array)
        if (inIndex >= 1 && inIndex < in.length-2) {
          out[outIndex] = Utils.curvePoint(in[inIndex-1], in[inIndex], in[inIndex+1],
            in[inIndex+2], (outIndex/outPerIn) % 1);
        } else if (inIndex == 0) {
          out[outIndex] = Utils.curvePoint(in[inIndex], in[inIndex], in[inIndex+1],
            in[inIndex+2], (outIndex/outPerIn) % 1);
        } else {
          out[outIndex] = Utils.curvePoint(in[inIndex-1], in[inIndex], in[inIndex+1],
            in[inIndex+1], (outIndex/outPerIn) % 1);
        }
      }
    }
  }

}

public static final class ColorUtils {

  public static int setAlpha(int rgb, int alpha) {
    return (rgb & (~LXColor.ALPHA_MASK)) | ((alpha << LXColor.ALPHA_SHIFT) & LXColor.ALPHA_MASK);
  }

  public static int setAlpha(int rgb, float alpha) {
    return setAlpha(rgb, (int) (alpha * 0xff));
  }

  public static int setAlpha(int rgb, double alpha) {
    return setAlpha(rgb, (int) (alpha * 0xff));
  }

  public static int scaleAlpha(int argb, double s) {
    return setAlpha(argb, MathUtils.byteMultiply(LXColor.alpha(argb), s));
  }

  public static int subtractAlpha(int argb, int amount) {
    return setAlpha(argb, MathUtils.byteSubtract(LXColor.alpha(argb), amount));
  }

  public static void blend(int[] dst, int[] src) {
    for (int i = 0; i < src.length; i++) {
      dst[i] = ColorUtils.blend(dst[i], src[i]);
    }
  }

  public static int blend(int dst, int src) {
    float dstA = (dst>>24&0xFF) / 255.0;
    float srcA = (src>>24&0xFF) / 255.0;
    float outA = srcA + dstA * (1 - srcA);
    if (outA == 0) {
      return 0;
    }
    int outR = FastMath.round(((src>>16&0xFF) * srcA + (dst>>16&0xFF) * dstA * (1 - srcA)) / outA)&0xFF;
    int outG = FastMath.round(((src>>8&0xFF) * srcA + (dst>>8&0xFF) * dstA * (1 - srcA)) / outA)&0xFF;
    int outB = FastMath.round(((src&0xFF) * srcA + (dst&0xFF) * dstA * (1 - srcA)) / outA)&0xFF;
    return (((int)(outA*0xFF))&0xFF)<<24 | outR<<16 | outG<<8 | outB;
  }

  public static int max(int dst, int src) {
    int outA = FastMath.max(src>>24&0xFF, dst>>24&0xFF);
    int outR = FastMath.max(src>>16&0xFF, dst>>16&0xFF);
    int outG = FastMath.max(src>>8&0xFF, dst>>8&0xFF);
    int outB = FastMath.max(src&0xFF, dst&0xFF);
    return outA<<24 | outR<<16 | outG<<8 | outB;
  }

  public static int maxAlpha(int dst, int src) {
    return (src>>24&0xFF) > (dst>>24&0xFF) ? src : dst;
  }

}

public static final class ReflectionUtils {

  public static void replaceAllFields(Object start, List oldObjects, List newObjects) {
    Set duplicateCheck = new HashSet();
    LinkedList traversal = new LinkedList();
    duplicateCheck.add(start);
    traversal.add(start);
    ListIterator iter = traversal.listIterator(0);
    while (iter.hasNext()) {
      Object obj = iter.next();
      Class objClass = obj.getClass();
      // println("objClass.getName(): "+objClass.getName());
      while (objClass != null) {
        for (Field field : objClass.getDeclaredFields()) {
          // println("field: "+field);
          field.setAccessible(true);
          try {
            Class fieldType = field.getType();
            Object fieldObj = field.get(obj);
            if (fieldObj == null) continue;
            if (fieldType.isAssignableFrom(LXPattern.class)) {
              if (fieldObj != null) {
                int index = oldObjects.indexOf(fieldObj);
                if (index != -1) {
                  Object newObj = newObjects.get(index);
                  setField(field, obj, newObj);
                }
              }
            } else if (fieldType.getPackage() == null || fieldType.getPackage().getName().startsWith("heronarts")) {
              if (!duplicateCheck.contains(fieldObj)) {
                duplicateCheck.add(fieldObj);
                iter.add(fieldObj);
                iter.previous();
              }
            } else if (List.class.isAssignableFrom(fieldType)) {
              List list = (List)fieldObj;
              for (int i = 0; i < list.size(); i++) {
                Object o = list.get(i);
                if (o == null) continue;
                if (o instanceof LXPattern) {
                  int index = oldObjects.indexOf(fieldObj);
                  if (index != -1) {
                    Object newObj = newObjects.get(index);
                    list.set(i, newObj);
                  }
                } else if (o.getClass().getPackage() == null || o.getClass().getPackage().getName().startsWith("heronarts")) {
                  if (!duplicateCheck.contains(o)) {
                    duplicateCheck.add(o);
                    iter.add(o);
                    iter.previous();
                  }
                }
              }
            } else if (fieldType.isArray()) {
              Object[] array = (Object[])fieldObj;
              for (int i = 0; i < array.length; i++) {
                Object o = array[i];
                if (o == null) continue;
                if (o instanceof LXPattern) {
                  int index = oldObjects.indexOf(fieldObj);
                  if (index != -1) {
                    Object newObj = newObjects.get(index);
                    array[i] = newObj;
                  }
                } else if (o.getClass().getPackage() == null || o.getClass().getPackage().getName().startsWith("heronarts")) {
                  if (!duplicateCheck.contains(o)) {
                    duplicateCheck.add(o);
                    iter.add(o);
                    iter.previous();
                  }
                }
              }
            }
          } catch (IllegalAccessException e) {
            println("e: "+e);
            continue;
          }
        }
        objClass = objClass.getSuperclass();
      }
    }
  }

  public static void swapObjects(Object original, Object replacement) {
    // println("original.getClass().getName(): "+original.getClass().getName());
    Class<?> originalClass = original.getClass();
    Class<?> replacementClass = replacement.getClass();
    while (originalClass != null && replacementClass != null) {
      for (Field oldField : originalClass.getDeclaredFields()) {
        // println("oldField: "+oldField);
        setField(oldField, replacementClass, original, replacement);
      }
      originalClass = originalClass.getSuperclass();
      replacementClass = replacementClass.getSuperclass();
    }
  }

  public static void setField(Field originalField, Class replacementClass, Object original, Object replacement) {
    try {
      Field newField = replacementClass.getDeclaredField(originalField.getName());

      if (!newField.getType().isAssignableFrom(originalField.getType())) return;

      newField.setAccessible(true);
      originalField.setAccessible(true);

      // ignore final modifier
      Field modifiersField = Field.class.getDeclaredField("modifiers");
      modifiersField.setAccessible(true);
      modifiersField.setInt(newField, newField.getModifiers() & ~Modifier.FINAL);

      newField.set(replacement, originalField.get(original));
    } catch (NoSuchFieldException e) {
      println("setField: "+e);
      return;
    } catch (IllegalAccessException e) {
      println("setField: "+e);
      return;
    }
  }

  public static void setField(Field field, Object owner, Object newValue) {
    try {
      field.setAccessible(true);

      // ignore final modifier
      Field modifiersField = Field.class.getDeclaredField("modifiers");
      modifiersField.setAccessible(true);
      modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

      field.set(owner, newValue);
    } catch (NoSuchFieldException e) {
      return;
    } catch (IllegalAccessException e) {
      println("e: "+e);
      return;
    }
  }
}

// See: http://stackoverflow.com/a/692580/216311
/**Writes to nowhere*/
public static class NullOutputStream extends OutputStream {
  @Override
  public void write(int b) throws IOException {
  }
}

public static final class PrintUtils {

  private static final PrintStream defaultOut = System.out;
  private static final PrintStream nullOut = new PrintStream(new NullOutputStream());

  public static void disablePrintln() {
    System.setOut(nullOut);
  }

  public static void enablePrintln() {
    System.setOut(defaultOut);
  }

}

// http://stackoverflow.com/a/60766/216311
public static class ClassPathHack {
  private static final Class[] parameters = new Class[] {URL.class};

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
  * @param pathToAdd the path to add
  * @throws Exception
  */
  public static void addLibraryPath(String pathToAdd) throws Exception {
    final Field usrPathsField = ClassLoader.class.getDeclaredField("usr_paths");
    usrPathsField.setAccessible(true);
 
    //get array of paths
    final String[] paths = (String[])usrPathsField.get(null);
 
    //check if the path to add is already present
    for(String path : paths) {
      if(path.equals(pathToAdd)) {
        return;
      }
    }
 
    //add the new path
    final String[] newPaths = Arrays.copyOf(paths, paths.length + 1);
    newPaths[newPaths.length-1] = pathToAdd;
    usrPathsField.set(null, newPaths);
  }
}

static public float angleBetween(PVector v1, PVector v2) {

  // We get NaN if we pass in a zero vector which can cause problems
  // Zero seems like a reasonable angle between a (0,0,0) vector and something else
  if (v1.x == 0 && v1.y == 0 && v1.z == 0 ) return 0.0f;
  if (v2.x == 0 && v2.y == 0 && v2.z == 0 ) return 0.0f;

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

static class AudioUtils {

  static final double LOG_2 = Math.log(2);

  static double freqToOctave(double freq) {
    return freqToOctave(freq, 1);
  }

  static double freqToOctave(double freq, double freqRef) {
    return Math.log(Math.max(1, freq / freqRef)) / LOG_2;
  }

}
