#!/bin/bash
cd "$(dirname "$0")"
XSOCK=/tmp/.X11-unix
XAUTH=/tmp/.docker.xauth
xauth nlist $DISPLAY | sed -e 's/^..../ffff/' | xauth -f $XAUTH nmerge -
mkdir -p docker-gradle
docker run --rm -ti \
	--user "$(id -u):$(id -g)" \
    --device /dev/snd \
    --device /dev/dri/card0 \
    -e DISPLAY=$DISPLAY -e XAUTHORITY=$XAUTH -v $XSOCK:$XSOCK -v $XAUTH:$XAUTH \
	-v "$PWD/docker-gradle":/.gradle -e GRADLE_USER_HOME=/.gradle \
    -v "$PWD":/app -w /app $*
