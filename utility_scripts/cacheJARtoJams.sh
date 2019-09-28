#!/bin/bash

echo "Building a JAR of SLBerryJelly..."
./gradlew shadowJar
DATE_FMT=$(date "+%Y-%m-%d_%H-%M-%S")
FILE_STR=cached_JARs_of_jams/$DATE_FMT.jar
mkdir -p cached_JARs_of_jams && cp ./build/libs/SLStudio-1.0-all.jar $FILE_STR
echo
echo
echo "You got some... drumroll..."
echo "$FILE_STR"
echo "Yummmm.... Enjoi responsibly with:"
echo "java -jar $FILE_STR"
