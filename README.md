# Bwatch O1 — Galaxy Watch 6 디지털 워치페이스

갤럭시 워치 6(Wear OS 4+, 원형 AMOLED)용 디지털 워치페이스입니다.
**Watch Face Format(WFF)** 기반의 선언형 워치페이스로, 코드(Kotlin/Java)가 없습니다.

## 기능
- **12시간제 시각 + AM/PM** — 순백 굵은 글자로 최고 선명도
- **배터리 상태** — 퍼센트 + 그라데이션 진행 막대
- **날짜 / 요일** — `Jun 29` / `Mon` (영문), 글래스 카드로 정돈

## 디자인 (O1 "Gradient Glass Cards")
- 검정 배경 + 상단 다크블루 **RadialGradient** (AMOLED 번인·전력 유리)
- 상단 대형 시간(흰색) + 하단 글래스 카드 3장(DATE / DAY / BATTERY)
- 모든 요소를 **원의 세이프 존(내접 영역)** 안에 배치 → 곡면에서 잘리지 않음
- **AOD(상시표시)** 모드: 카드 배경·배터리 막대를 숨기고 시간을 얇은 굵기로 전환(번인 저감)
- 캔버스 450×450 기준으로 작성, 시스템이 432(40mm)/480(44mm)로 자동 스케일

> 시간 글자의 그라데이션은 WFF에서 `blendMode`(포맷 v3 / Wear OS 6)가 필요해, 호환성(v1=Wear OS 4)과
> "가장 선명한 글자" 요구를 위해 **순백 단색**으로 구현했습니다. 그라데이션 정체성은 배경·배터리 막대·강조색으로 유지됩니다.

## 프로젝트 구조
```
bwatch/
├── settings.gradle.kts / build.gradle.kts
├── gradle/libs.versions.toml          # AGP 9.0.0
├── local.properties                   # sdk.dir (로컬 SDK 경로)
└── watchface/
    ├── build.gradle.kts               # minSdk 33, compileSdk 36
    └── src/main/
        ├── AndroidManifest.xml        # hasCode=false, format.version=1
        └── res/
            ├── raw/watchface.xml       # ★ 워치페이스 본체(WFF)
            ├── xml/watch_face_info.xml # 미리보기 선언
            ├── drawable/preview.png    # 선택기 미리보기(450×450)
            └── values/strings.xml
```
디자인 시안: `mockups.html`, `mockups-more.html`, `mockups-fusion.html`, `mockup-o1-final.html`(세이프 존 토글), `preview-render.html`(미리보기 렌더 원본).

## 빌드
JDK 17이 필요합니다(AGP 9.0.0). Android SDK는 platform 36 / build-tools 36 사용.

```bash
export JAVA_HOME=/opt/homebrew/opt/openjdk@17
./gradlew :watchface:assembleDebug
# 결과: watchface/build/outputs/apk/debug/watchface-debug.apk
```

## WFF 검증 (선택)
구글 공식 검증기로 스키마/표현식을 점검할 수 있습니다.
```bash
# https://github.com/google/watchface/releases (release) 에서 wff-validator.jar 내려받기
java -jar wff-validator.jar 1 watchface/src/main/res/raw/watchface.xml
# → ✅ PASSED : ... valid against watch face format version #1
```

## 갤럭시 워치 6에 설치
1. 워치에서 **개발자 옵션 → 무선 디버깅** 활성화.
2. 폰과 같은 Wi-Fi에서 페어링/연결:
   ```bash
   adb pair <워치IP>:<페어링포트>      # 워치에 표시되는 코드 입력
   adb connect <워치IP>:<디버깅포트>
   adb install -r watchface/build/outputs/apk/debug/watchface-debug.apk
   ```
3. 워치페이스 길게 누르기 → 목록에서 **Bwatch O1** 선택.

## 에뮬레이터로 확인 (현재 환경에 Wear 이미지 미설치)
이 머신에는 Wear OS 시스템 이미지가 없어 에뮬레이터 실행 전 설치가 필요합니다.
```bash
sdkmanager "system-images;android-34;android-wear;arm64-v8a"   # cmdline-tools 필요
avdmanager create avd -n watch6 -k "system-images;android-34;android-wear;arm64-v8a" -d wearos_large_round
emulator -avd watch6
adb install -r watchface/build/outputs/apk/debug/watchface-debug.apk
# 배터리 막대 테스트: adb shell dumpsys battery set level 40
```

## 검증 현황
- ✅ Gradle 빌드 성공 (APK 생성)
- ✅ 공식 WFF 검증기 통과 (format version 1)
- ⏳ 실기기/Wear 에뮬레이터 시각 확인 — Wear 시스템 이미지 설치 후 위 절차로 진행
