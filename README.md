# SLStudio

## Running

From the root of the project, execute

    ./gradlew run

### Java 9
Processing won't run on Java 9 on macOS. You'll see an error like `java.lang.NoClassDefFoundError: com/apple/eawt/QuitHandler`
See https://github.com/processing/processing/wiki/Supported-Platforms for more info

To fix this, install Java 8, and modify your `~/.gradle/gradle.properties` or the project specific `./gradle.properties`
to include the location of your Java 8 home, e.g. 

    org.gradle.java.home=/Library/Java/JavaVirtualMachines/jdk1.8.0_152.jdk/Contents/Home
    
You can determine the location to use by running `/usr/libexec/java_home -V` which will list available JVMs


## Directories

    cache    # Holds various runtime caches for SLStudio
    data     # Holds various data files needed for the project
    gradle   # Holds the gradle wrapper
    libs     # Holds old JAR-based libraries for the project
    projects # Holds LXStudio project files (*.lxp)
    src      # Holds the Java and Kotlin source files for the project
    tools    # Holds any extra scripts and such
    