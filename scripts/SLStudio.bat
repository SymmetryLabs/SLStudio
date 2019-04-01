:: this script meant to be run from the ZIP distribution created with "gradle dist"

java -Xmx1G -XX:+UseParNewGC -XX:+UseConcMarkSweepGC -jar SLStudio-$VERSION-all.jar
