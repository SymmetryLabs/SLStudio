#!/bin/sh
# this script is meant to be run from the ZIP distribution created with "gradle dist"

java -Xmx1G -jar SLStudio-$VERSION-all.jar
