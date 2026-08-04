#!/bin/bash
# Packages DefibrillatorShow as a standalone, double-clickable macOS app
# (Defibrillator.app) that boots directly into the "defibrillator" show.
#
# Requires: JDK 17+ (for jpackage). Produces a native arm64 app when run on
# an Apple Silicon Mac (the bundled JRE matches the JDK jpackage is run with).
#
# Usage: ./scripts/package-defibrillator-app.sh
# Output: build/jpackage-out/Defibrillator.app
#         build/jpackage-out/Defibrillator.app.zip  (for copying to another Mac)

set -euo pipefail
cd "$(dirname "$0")/.."

VERSION="$(sed -n "s/^version = '\(.*\)'/\1/p" build.gradle)"

echo "==> Building shadow jar..."
./gradlew shadowJar -q

echo "==> Staging jpackage input..."
rm -rf build/jpackage-input build/jpackage-out
mkdir -p build/jpackage-input/defibrillator_clean/lx_app
cp "build/libs/SLStudio-${VERSION}-all.jar" build/jpackage-input/
cp -R defibrillator_clean/lx_app/Fixtures build/jpackage-input/defibrillator_clean/lx_app/Fixtures

echo "==> Running jpackage..."
jpackage \
  --type app-image \
  --dest build/jpackage-out \
  --input build/jpackage-input \
  --name Defibrillator \
  --main-jar "SLStudio-${VERSION}-all.jar" \
  --main-class com.symmetrylabs.slstudio.SymmetryLauncher \
  --icon src/main/resources/application.icns \
  --app-version "${VERSION}" \
  --mac-package-identifier com.symmetrylabs.defibrillator \
  --java-options "-Dcom.symmetrylabs.app=slstudio" \
  --java-options "-Dcom.symmetrylabs.show=defibrillator" \
  --java-options '-Dcom.symmetrylabs.defibrillator.fixturesRoot=$APPDIR/defibrillator_clean/lx_app/Fixtures' \
  --java-options '-Duser.dir=$APPDIR' \
  --java-options "-Xmx2048m"

echo "==> Zipping for transfer..."
cd build/jpackage-out
ditto -c -k --sequesterRsrc --keepParent Defibrillator.app Defibrillator.app.zip
cd - > /dev/null

echo
echo "Done: build/jpackage-out/Defibrillator.app"
echo "      build/jpackage-out/Defibrillator.app.zip  (copy this to another Mac and unzip)"
