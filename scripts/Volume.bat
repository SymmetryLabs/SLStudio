:: this script is meant to be run from the ZIP distribution created with "gradle dist"

java -Xmx1G -XX:+UseParNewGC -XX:+UseConcMarkSweepGC -Djava.library.path=libs -jar SLStudio-$VERSION-all.jar volume
