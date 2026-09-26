# Ssukhibhavaa Uphar / Fauget Burger — Android App

A lightweight native Android food-ordering app generated from the supplied menu JSON.

## Included
- Modern food-delivery style UI
- Search with real-time filtering
- Horizontal category navigation
- Veg-only filter
- Veg/non-veg indicators
- Best Seller tags
- Dynamic ADD / quantity +/− controls
- Sticky cart bar with live count and total
- Checkout dialog with order summary and total
- Menu JSON embedded in `app/src/main/assets/menu.json`
- Portrait Android app, no third-party runtime dependencies

## Build APK
Open the project in **Android Studio** and let Gradle sync. Then use:

```bash
./gradlew assembleDebug
```

The debug APK will be under:
`app/build/outputs/apk/debug/app-debug.apk`

If Gradle wrapper files are not present, Android Studio can generate/sync them, or run the project with an installed Gradle 8.x distribution compatible with Android Gradle Plugin 8.5.2.

## GitHub Actions
A workflow can be added to build the APK on GitHub using an Android SDK runner.
