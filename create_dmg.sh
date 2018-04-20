#!/bin/bash
DD=$(date +%d)
MM=$(date +%m)
echo "Today is Day:$DD-:$MM"


./gradlew createDmg
open build/distributions/SLStudio-1.0.dmg
mkdir dmg_files
sleep 5
cp -r /Volumes/SLStudio-1.0/SLStudio.app dmg_files/
cp *.lxp dmg_files/
cp .layout
echo "copying hidden layout file... check to make sure it is populated with the right layout!"
cd create-dmg
./create-dmg SLStudio$MM-$DD.dmg ../dmg_files/
