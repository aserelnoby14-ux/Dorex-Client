#!/usr/bin/env bash
set -euo pipefail

PROJ="$(cd "$(dirname "$0")" && pwd)"
cd "$PROJ"

# --- locate ANDROID_HOME ---
if [ -z "${ANDROID_HOME:-}" ]; then
  for p in /usr/local/lib/android/sdk "$HOME/Android/Sdk" /opt/android-sdk /usr/lib/android-sdk; do
    if [ -d "$p" ]; then
      export ANDROID_HOME="$p"
      break
    fi
  done
fi

if [ -z "${ANDROID_HOME:-}" ] || [ ! -d "$ANDROID_HOME" ]; then
  echo "ERROR: ANDROID_HOME not found"
  exit 1
fi

echo "ANDROID_HOME: $ANDROID_HOME"

# --- locate build-tools (newest) ---
if [ ! -d "$ANDROID_HOME/build-tools" ]; then
  echo "ERROR: $ANDROID_HOME/build-tools missing"
  exit 1
fi

BT=$(ls -1 "$ANDROID_HOME/build-tools" | sort -V | tail -1)
BT="$ANDROID_HOME/build-tools/$BT"

AAPT2="$BT/aapt2"
D8="$BT/d8"
ZIPALIGN="$BT/zipalign"
APKSIGNER="$BT/apksigner"

for tool in "$AAPT2" "$D8" "$ZIPALIGN" "$APKSIGNER"; do
  if [ ! -f "$tool" ]; then
    echo "ERROR: missing $tool"
    exit 1
  fi
done

echo "aapt2:     $AAPT2"
echo "d8:        $D8"
echo "zipalign:  $ZIPALIGN"
echo "apksigner: $APKSIGNER"

# --- locate android.jar ---
if [ -n "${ANDROID_JAR:-}" ] && [ -f "$ANDROID_JAR" ]; then
  :
else
  ANDROID_JAR=$(find "$ANDROID_HOME/platforms" -name android.jar | sort -V | tail -1)
fi

if [ -z "$ANDROID_JAR" ] || [ ! -f "$ANDROID_JAR" ]; then
  echo "ERROR: android.jar not found under $ANDROID_HOME/platforms"
  exit 1
fi

echo "android.jar: $ANDROID_JAR"

# --- clean ---
rm -rf build
mkdir -p build/gen build/classes build/dex build/compiled

echo "[1/7] aapt2 compile"
"$AAPT2" compile --dir app/res -o build/compiled/res.zip

echo "[2/7] aapt2 link"
"$AAPT2" link \
  -I "$ANDROID_JAR" \
  --manifest app/AndroidManifest.xml \
  --java build/gen \
  -o build/unsigned.apk \
  build/compiled/res.zip

echo "[3/7] javac"
javac -source 8 -target 8 \
  -bootclasspath "$ANDROID_JAR" \
  -d build/classes \
  $(find app/src build/gen -name '*.java')

echo "[4/7] d8 -> dex"
"$D8" --lib "$ANDROID_JAR" --min-api 26 --output build/dex \
  $(find build/classes -name '*.class')

echo "[5/7] inject classes.dex"
cd build/dex
zip -q ../unsigned.apk classes.dex
cd "$PROJ"

echo "[6/7] zipalign"
"$ZIPALIGN" -f 4 build/unsigned.apk build/aligned.apk

echo "[7/7] apksigner"
if [ ! -f keystore.jks ]; then
  keytool -genkeypair -v \
    -keystore keystore.jks \
    -alias dorex \
    -keyalg RSA -keysize 2048 -validity 10000 \
    -storepass android -keypass android \
    -dname "CN=Dorex Client, OU=Dev, O=Dorex, L=Cairo, C=EG"
fi

"$APKSIGNER" sign \
  --ks keystore.jks \
  --ks-key-alias dorex \
  --ks-pass pass:android \
  --key-pass pass:android \
  --out build/dorex-client-signed.apk \
  build/aligned.apk

echo ""
echo "DONE: $PROJ/build/dorex-client-signed.apk"
ls -lh build/dorex-client-signed.apk