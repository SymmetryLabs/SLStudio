#!/bin/bash
DD=$(date +%d)
MM=$(date +%m)
echo "Today is Day: $MM-$DD"

echo "removing any existing dmg files in build/distributions, and create-dmg/"
rm build/distributions/*.dmg
cd create-dmg
rm rw.SLS*
cd ..

./gradlew createDmg
open build/distributions/SLStudio-1.0.dmg
mkdir dmg_files
sleep 5
cp -r /Volumes/SLStudio-1.0/SLStudio.app dmg_files/
cp *.lxp dmg_files/
echo "copying hidden layout file... check to make sure it is populated with the right layout!"
echo "layout:"
tail .layout
cp .layout dmg_files/

cd create-dmg
echo "removing all existing dmg files in create-dmg folder"
rm *.dmg
./create-dmg SLStudio$MM-$DD.dmg ../dmg_files/
