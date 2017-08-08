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
