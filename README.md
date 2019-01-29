# SLStudio

## Running UIv1

From the root of the project, execute

    ./gradlew run

### Java 9
Processing won't run on Java 9 on macOS. You'll see an error like `java.lang.NoClassDefFoundError: com/apple/eawt/QuitHandler`
See https://github.com/processing/processing/wiki/Supported-Platforms for more info

To fix this, install Java 8, and modify your `~/.gradle/gradle.properties` or the project specific `./gradle.properties`
to include the location of your Java 8 home, e.g. 

    org.gradle.java.home=/Library/Java/JavaVirtualMachines/jdk1.8.0_152.jdk/Contents/Home
    
You can determine the location to use by running `/usr/libexec/java_home -V` which will list available JVMs

It may also be helpful to set your default Java if executing from the shell:

    export JAVA_HOME=`/usr/libexec/java_home -v 1.8`

You may need to set the default JDK home for intelliJ products: https://stackoverflow.com/questions/31215452/intellij-idea-importing-gradle-project-getting-java-home-not-defined-yet

### Creating the IntelliJ Project

- Click 'import new project'
- Select the build.gradle file in the SLStudio folder
- Go to run/edit configurations and click on gradle or gradle/run
- Click the '+' icon to add a new configuration, name it 'run'
- Select the project home is the outer SLStudio folder
- For tasks, type “run” - should autocomplete - you want to call the “run” task
- Apply changes, compile and run 

## Running UIv2

If you're on a Mac, you probably need to set your `JAVA_HOME` variable.
You can do that by adding this to your `.bashrc` or by just running it
in whichever terminal you run gradle in:

    export JAVA_HOME=$(/usr/libexec/java_home)

If you forget to run this, gradle will prompt you to.

Once you've set `JAVA_HOME`, you can run UIv2 using:

    ./gradlew runUIv2

If you edit `slimgui`, the native code library used for drawing the GUI
in UIv2, you will need to rebuild the prebuilt slimgui shared library in
the libs folder. You can do that by running:

    ./gradlew genPrebuiltSlimgui

If you have a C++ development environment on your machine, you can just
always safely run:

    ./gradlew buildAndRunUIv2

Which will rebuild the prebuilts if needed, and then run UIv2.


## Directories

    cache    # Holds various runtime caches for SLStudio
    data     # Holds various data files needed for the project
    gradle   # Holds the gradle wrapper
    libs     # Holds old JAR-based libraries for the project
    projects # Holds LXStudio project files (*.lxp)
    src      # Holds the Java and Kotlin source files for the project
    tools    # Holds any extra scripts and such
    
