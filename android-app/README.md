# Schemaappen – Storgatan

Android-app för personalens schemahantering på restaurang Storgatan.
Personalen kan se sina pass, lägga ut pass till kollegor och ta lediga pass – utan att chefen behöver godkänna.

## Teknik

- **Kotlin** · Jetpack Compose · Material 3
- **MVVM**: ViewModel + StateFlow
- **Hilt** för dependency injection
- **Retrofit** + kotlinx.serialization (REST)
- **DataStore** för inloggningstoken
- minSdk 26 · targetSdk 36

## Köra appen

1. Öppna projektet i Android Studio.
2. Synka Gradle (File → Sync Project with Gradle Files).
3. Kör `app`-modulen på en emulator eller enhet (minst API 26).
4. Logga in med **erik@storgatan.se** / **storgatan** (alla anställda funkar, `<förnamn>@storgatan.se`).

Appen använder FakeRepository med testdata som standard – du behöver ingen backend.

## Byta till riktig backend

1. I `app/build.gradle.kts`, ändra `USE_FAKE_BACKEND` till `false`.
2. Ändra `BASE_URL` till din Payara-server, t.ex. `"http://10.0.2.2:8080/schema/api/"`.
3. Synka Gradle och kör appen.
4. API-kontraktet finns i `docs/openapi.yaml`.

## Projektstruktur

```
app/src/main/java/se/storgatan/schema/
├── di/              Hilt-moduler
├── data/
│   ├── fake/        FakeRepository med testdata
│   ├── remote/      Retrofit API, DTOs, interceptor, RemoteRepository
│   └── session/     SessionStore (DataStore)
├── domain/          Modeller, repository-interfaces, felklasser, formatering
└── ui/
    ├── theme/       Färger, typografi, former
    ├── components/  Delade UI-komponenter (ShiftCard, badges)
    ├── navigation/  Routes, NavHost, BottomBar
    └── screens/     login, myshifts, detail, release, available, confirmation, notifications
```

## Tester

```bash
./gradlew app:test
```

Enhetstester för FakeRepository, ShiftConflicts och ViewModels (Login, MyShifts, AvailableShifts).
