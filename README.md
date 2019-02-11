# SLStudio

## Cloning this repo

This repo uses Git-LFS.  In order to clone it:

  - Git version 2.15 or higher must be installed.  Use `git --version` to check.

  - Git-LFS must be installed.

To install Git-LFS on a Mac, run these two commands:

    brew install git-lfs
    git lfs install

For other platforms, see [these installation instructions](https://help.github.com/articles/installing-git-large-file-storage/).

## Running the v1 UI from the command line

From the root of the project, execute:

    ./gradlew run

## Setting up the IntelliJ Project

1. In the IntelliJ startup dialog, click **Import new project**; or if a project is already open, click **File** > **New** > **Project from Existing Sources...**
2. Select the `build.gradle` file in the SLStudio folder and click **Open**
3. In the "Import Project from Gradle" dialog, click **OK**
4. Click **Run** > **Edit configurations...**
5. In the "Run/Debug Configurations" window, click the **+** and choose **Gradle**
6. For "Name", replace "Untittled" with `SLStudio`
7. For "Gradle project" choose **SLStudio**
8. For "Tasks" enter `run`
9. Click **OK**
10. Click **Run** > **Run 'SLStudio'**

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

## Running UIv2

Running UIv2 is as easy as running:

    ./gradlew runUIv2

If you get linker errors when you run it, it is probably the case that
the prebuilts for your platform need to be rebuilt. You can request a
rebuild from the #software channel on Slack, or rebuild them yourself if
you have a C++ compiler installed.

## Building the UIv2 native code

**You do not need to do this unless you changed C++ code, which is only necessary when making new UI widgets.**

If you're on a Mac, you probably need to set your `JAVA_HOME` variable.
You can do that by adding this to your `.bashrc` or by just running it
in whichever terminal you run gradle in:

    export JAVA_HOME=$(/usr/libexec/java_home)

If you forget to run this, gradle will prompt you to.

Once you've done that, you can rebuild the UI library (which is saved to
libs/libslimgui.{so,dylib,dll}) by running:

    ./gradlew genPrebuiltSlimgui

If you have a C++ development environment on your machine, you can just
always safely run:

    ./gradlew buildAndRunUIv2

Which will rebuild the prebuilts if needed, and then run UIv2.


## Directories

    cache    # Holds various runtime caches for SLStudio
    gradle   # Holds the gradle wrapper
    libs     # Holds prebuilt JAR-based libraries and native libraries for the project
    shows    # Holds LXStudio project files and other data files, organized by show (*.lxp)
    src      # Holds the Java and Kotlin source files for the project
    tools    # Holds any extra scripts and such
    
