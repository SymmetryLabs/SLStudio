#!/bin/bash


#sudo installer -package jdk-7u51-macos-x64.dmg -target /


# OR


VOLUME=`hdiutil attach $1 | grep Volumes | awk '{print $3}'`
cp -rf $VOLUME/*.app /Applications
hdiutil detach $VOLUME


#MOUNTDIR=$(echo `hdiutil mount Alfred_3.5.1_883.dmg | tail -1 \
#| awk '{$1=$2=""; print $0}'` | xargs -0 echo) \
#&& sudo installer -pkg "${MOUNTDIR}/"*.pkg -target / 
