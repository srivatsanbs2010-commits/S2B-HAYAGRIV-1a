# S2B Hayagriva Android V1

Clean Android/Jetpack Compose foundation for S2B Hayagriva.

## Build

This is a normal Android multi-module Gradle project:

```text
./gradlew :app:assembleDebug
```

The GitHub Actions workflow downloads Gradle 8.9 and builds the debug APK.

## V1 included

- S2B Hayagriva dark futuristic UI
- Provided Hayagriva orb as the central visual
- Animated listening/pulse state
- Tap-to-speak using Android SpeechRecognizer
- Text-to-speech acknowledgement
- Camera permission hook
- English UI foundation

Gemini/API-backed intelligence should be connected through a secure backend or GitHub/Android secrets; API keys must not be hard-coded into the APK.
