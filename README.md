# SLStudio

## Cloning this repo

This repo uses Git-LFS.  In order to clone it:

  - `git -version` must be 2.15 or higher.

  - Git-LFS must be installed.

To install Git-LFS on a Mac, run these two commands:

    brew install git-lfs
    git lfs install

For other platforms, see [these installation instructions](https://help.github.com/articles/installing-git-large-file-storage/).

## Running from the command line

From the root of the project, execute:

    ./gradlew run

## Setting up the IntelliJ Project

1. In the IntelliJ startup dialog, click *Import new project*; or if a project is already open, click *File* > *New* > *Project from Existing Sources...*
2. Select the `build.gradle` file in the SLStudio folder and click *Open*
3. In the "Import Project from Gradle" dialog, click *OK*
4. Click *Run* > *Edit configurations...*
5. In the "Run/Debug Configurations" window, click the *+* and choose *Gradle*
6. For "Name", replace "Untittled" with `SLStudio`
7. For "Gradle project" choose *SLStudio*
8. For "Tasks" enter `run`
9. Click *OK*
10. Click *Run* > *Run 'SLStudio'*

Everything should now build and run; your IntelliJ project is ready to use.

## Java 9

Processing won't run on Java 9 on macOS. You'll see an error like `java.lang.NoClassDefFoundError: com/apple/eawt/QuitHandler`
See https://github.com/processing/processing/wiki/Supported-Platforms for more info

To fix this, install Java 8, and modify your `~/.gradle/gradle.properties` or the project specific `./gradle.properties`
to include the location of your Java 8 home, e.g. 

    org.gradle.java.home=/Library/Java/JavaVirtualMachines/jdk1.8.0_152.jdk/Contents/Home
    
You can determine the location to use by running `/usr/libexec/java_home -V` which will list available JVMs

It may also be helpful to set your default Java if executing from the shell:

    export JAVA_HOME=`/usr/libexec/java_home -v 1.8`

You may need to set the default JDK home for intelliJ products: https://stackoverflow.com/questions/31215452/intellij-idea-importing-gradle-project-getting-java-home-not-defined-yet

## Directories

    cache    # Holds various runtime caches for SLStudio
    data     # Holds various data files needed for the project
    gradle   # Holds the gradle wrapper
    libs     # Holds old JAR-based libraries for the project
    projects # Holds LXStudio project files (*.lxp)
    src      # Holds the Java and Kotlin source files for the project
    tools    # Holds any extra scripts and such
    
