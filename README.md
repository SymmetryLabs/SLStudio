# SLStudio

## Build setup

Before you can build SL Studio with Gradle, you need to install the required
credentials to fetch builds from our private repositories, hosted by
jitpack.io. To install credentials:

1. Go to [jitpack.io/private](https://jitpack.io/private) and scroll down to “Private Repositories”
2. Click the "Authorize with Github" button and sign in to Github
3. You’ll be redirected back to Jitpack, and there will be a user token for you to copy. Follow the first half of step 2 on that page to dd the token to `$HOME/.gradle/gradle.properties`. You do not need to modify the `build.gradle` file; that’s already been done for you.

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


## Directories

    cache    # Holds various runtime caches for SLStudio
    data     # Holds various data files needed for the project
    gradle   # Holds the gradle wrapper
    libs     # Holds old JAR-based libraries for the project
    projects # Holds LXStudio project files (*.lxp)
    src      # Holds the Java and Kotlin source files for the project
    tools    # Holds any extra scripts and such
    
