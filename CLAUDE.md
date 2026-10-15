# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

A **declarative Watch Face Format (WFF)** watch face for the Galaxy Watch 6 (Wear OS 4+, round AMOLED). There is **no Kotlin/Java code** — the entire watch face is one XML file. The Gradle project exists only to package that XML into an installable APK (`android:hasCode="false"`, `enableKotlin = false`).

The single source of truth for the watch face is:

    watchface/src/main/res/raw/watchface.xml

Everything else (build scripts, manifest, strings, preview) is scaffolding around it. The `*.html` files at the repo root are design mockups/references, not build inputs — `mockup-o1-final.html` is the layout reference the XML was derived from.

## The build/install loop

When the user says "update" / "빌드" / "빌드해줘", run this exact sequence without asking (it's the established loop):

1. **Validate** against Google's WFF validator. The version arg **must** match the manifest's `com.google.wear.watchface.format.version` (currently **2**):
   ```bash
   java -jar <scratchpad>/wff-validator.jar 2 watchface/src/main/res/raw/watchface.xml
   ```
   Download to scratchpad if missing: https://github.com/google/watchface/releases/download/latest/wff-validator.jar
2. **Build release** (not debug):
   ```bash
   JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew :watchface:assembleRelease
   ```
   → `watchface/build/outputs/apk/release/watchface-release.apk`. Release uses the debug signing config, so it installs on-device without a keystore.
3. **Install** to the Galaxy Watch 6 (`SM_R935N`) over Wi-Fi debugging:
   ```bash
   adb connect 192.168.45.146:44185 && adb -s 192.168.45.146:44185 install -r <release apk>
   ```
   (adb lives at `~/Library/Android/sdk/platform-tools`)

Report pass/build/install status concisely.

**Requirements:** JDK 17 (AGP 9.2.1 needs it), Android platform 36 / build-tools 36. The watch pairs via wireless debugging only (no USB); the pairing port from the popup differs from the connect port (44185, shown on the main screen) — `adb connect` uses the connect port.

### When you change `<UserConfigurations>` (editor settings)

Bump `versionCode` in `watchface/build.gradle.kts` and do a **clean reinstall** (`adb uninstall com.bwatch.o1` then `adb install`). The launcher caches the config schema and won't re-read it on a same-version `install -r`. The "Customize/꾸미기" button only appears for the *currently applied* face (long-press the active face), not faces being browsed.

## watchface.xml architecture

`<WatchFace width="450" height="450" clipShape="CIRCLE">` — a 450×450 canvas the system auto-scales to 432 (40mm) / 480 (44mm). Center is (225,225), radius 225.

Two cross-cutting mechanisms drive almost every element:

- **Two `BooleanConfiguration` user settings** (declared once, then branched on throughout the file):
  - `time_gradient` — gradient vs solid-white time text (default TRUE)
  - `is_korean` — Korean vs English labels/text (default FALSE). Nearly every text element has a TRUE/FALSE pair.
- **`Variant mode="AMBIENT"`** — AOD (always-on) overrides. The pattern is: filled card/bar shapes and the gradient clock set `alpha=0` in ambient, and a **separate thin dim-white clock** (hidden normally, `alpha=255` in ambient) renders instead — minimizing burn-in and power.

Scene layout, top to bottom: radial-gradient background ellipse → large AM/PM text → 12-hour `h:mm` time (center) → three glass cards (DATE / DAY / BATTERY) → step count.

### Non-obvious constraints — read before editing layout

- **Round safe zone.** Every element must stay inside the inscribed circle or the round frame clips it. For the bottom card row the binding constraint is the outer bottom corners; rounded corners (radius ~29) pull the drawn area inward (~218px from center, not the ~229px a sharp corner would reach). The 3-card row is near the width limit — it can't grow ~20% in both dimensions without clipping or a 2-row rearrange.
- **Gradient time text uses `renderMode` clipping, not blend modes.** A Group holds a gradient `Rectangle` (SOURCE) plus a `DigitalClock renderMode="MASK"`, so the gradient shows only through the glyphs and the background is untouched. This is why the manifest needs **format version 2** (was v1). `blendMode` on a Group is **not** allowed by the schema — don't use it.
- **English text is hard-coded via ternaries because the device locale is Korean.** DATE maps `[MONTH]` (1=Jan…12=Dec) and DAY maps `[DAY_OF_WEEK]` (1=Mon…7=Sun) through explicit `? :` expressions. Do **not** use locale-dependent `[MONTH_S]` / `[DAY_OF_WEEK_S]` — they render Korean regardless of the `is_korean` setting.
- **Battery bar fill** width is data-bound: `<Transform target="width" value="[BATTERY_PERCENT] / 100.0 * 86" />`.

## Caveats on existing docs

`README.md` is partly stale: it predates format version 2 (says v1), the gradient-via-MASK time text, the `is_korean`/step-count features, and the release-APK workflow (it documents `assembleDebug`). Trust `watchface.xml`, the manifest, and this file over the README. The displayed watch face name is **"Brown Watch"** (`strings.xml`), not "Bwatch O1".
