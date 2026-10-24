# SLStudio

## Build / run

Gradle must run under JDK 17 (the default `java` on this machine is 1.8 and fails):

```
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home && ./gradlew run
```

Use the same `JAVA_HOME` export for `./gradlew compileJava` when only checking compilation.

## Shows

- Shows live in `src/main/java/com/symmetrylabs/shows/<name>/` and are registered in `ShowRegistry.java`.
- Patterns/effects are discovered by classpath scan (`LXClassLoader`), so per-show pattern classes need no manual registration.
- `cuddlefish`, `flash`, `kaleidoscope` are clones of the same strip-mapping show (modeling tool in `ui/`, mapping persisted to `data/<name>-mapping.json`).
