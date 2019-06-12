#!/bin/sh
# this script is meant to be run from the ZIP distribution created with "gradle dist"

MAC_ARGS="-XstartOnFirstThread -Djava.awt.headless=true"
EXTRA_ARGS=""
if [ "$(uname)" == "Darwin" ]
then
    EXTRA_ARGS=MAC_ARGS
fi

java -Xmx1G $EXTRA_ARGS -Djava.library.path=libs -jar SLStudio-1.0.5-all.jar volume
