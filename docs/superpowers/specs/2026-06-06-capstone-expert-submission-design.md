# Capstone Android Expert — Submission Design

**Date:** 2026-06-06  
**Target:** Star-5 submission (all mandatory + all star-5 optimizations)  
**Project:** Multi-module Clean Architecture movie app (TMDB API)  
**Modules:** `app`, `core`, `favorite` (dynamic), `setting` (dynamic)  
**DI:** Koin | **DB:** Room → SQLCipher | **Network:** Retrofit + OkHttp

---

## 1. Security

### 1.1 ProGuard / R8
- `app/build.gradle.kts` release: `isMinifyEnabled = true`, `shrinkResources = true`
- `core/build.gradle.kts` release: `isMinifyEnabled = true`
- `core/consumer-rules.pro`: keep rules for Retrofit, Gson, Room, Koin, data classes
- `app/proguard-rules.pro`: keep rules for app-level classes

### 1.2 Database Encryption (SQLCipher)
- Dependencies: `net.zetetic:android-database-sqlcipher`, `androidx.sqlite:sqlite-ktx`
- `core/utils/SecureKeyManager.kt` — generate AES-256 key via `AndroidKeyStore`, retrieve as `ByteArray` passphrase
- `core/di/DatabaseModule.kt` — replace `Room.databaseBuilder` with `SupportFactory(passphrase)` from SQLCipher
- `core/data/source/local/room/MovieDatabase.kt` — no change needed to schema

### 1.3 Certificate Pinning
- Target host: `api.themoviedb.org`
- **Retrieve SHA-256 pins at implementation time:**
  ```bash
  openssl s_client -connect api.themoviedb.org:443 -showcerts </dev/null 2>/dev/null \
    | openssl x509 -pubkey -noout | openssl pkey -pubin -outform DER \
    | openssl dgst -sha256 -binary | openssl enc -base64
  ```
  Pin both leaf cert and intermediate CA (backup pin)
- `core/di/NetworkModule.kt` — add `CertificatePinner` to `OkHttpClient.Builder` with retrieved SHA-256 pins
- `app/src/main/res/xml/network_security_config.xml` — add `<pin-set>` as defense-in-depth
- `AndroidManifest.xml` — reference `networkSecurityConfig`

### 1.4 API Token — Remove Hardcode
- Current: Bearer token hardcoded in `NetworkModule.kt`
- Fix: move to `local.properties` → `buildConfigField("String", "TMDB_API_TOKEN", ...)` in `core/build.gradle.kts`
- `core/build.gradle.kts` must enable `buildFeatures { buildConfig = true }` (library modules don't generate BuildConfig by default)
- `NetworkModule.kt` reads from `com.example.capstoneandroidexpert.core.BuildConfig.TMDB_API_TOKEN`
- CI: generate `local.properties` from GitHub secret `TMDB_API_TOKEN` before build step
- `local.properties` already in `.gitignore`

### 1.5 Advanced Security (star-5)
- `RootBeer` library — root detection at app startup
- `core/utils/RootDetectionUtil.kt` — wraps RootBeer check
- `app/MainActivity.kt` — call check in `onCreate`, show dialog + finish if rooted

---

## 2. CI/CD Pipeline

### 2.1 GitHub Setup
- Create public repo `CapstoneAndroidExpert`
- Add secret: `TMDB_API_TOKEN`
- Push all code (ensure `local.properties` excluded)

### 2.2 Workflow File
**Path:** `.github/workflows/ci.yml`  
**Triggers:** `push` + `pull_request` to `main`

**Jobs (sequential, each depends on previous):**

| Job | Command | Purpose |
|-----|---------|---------|
| `lint` | `./gradlew ktlintCheck` | Code style enforcement |
| `test` | `./gradlew testDebugUnitTest jacocoDebugTestReport` | Unit tests + coverage report |
| `build` | `./gradlew assembleRelease` | **Mandatory** — must pass |
| `security` | `./gradlew dependencyCheckAnalyze` | OWASP NVD vulnerability scan |

> Note: `jacocoDebugTestReport` is a custom task registered in `build.gradle.kts` using `JacocoReport`. Must configure `classDirectories`, `sourceDirectories`, `executionData` and register it manually — not auto-generated.

**CI local.properties generation:**
```yaml
- name: Create local.properties
  run: echo "tmdb.api.token=${{ secrets.TMDB_API_TOKEN }}" >> local.properties
```

### 2.3 New Gradle Plugins
Add to `libs.versions.toml` + `build.gradle.kts` (root):
- `org.jlleitschuh.gradle.ktlint` — linter
- `jacoco` — built-in Gradle, configure per module
- `org.owasp.dependencycheck` — vulnerability scanning

### 2.4 JaCoCo Config
- Exclude: Room generated (`*_Impl*`), Koin modules, `BuildConfig`, data binding
- Reports uploaded as CI artifact (no enforcement threshold)

---

## 3. Performance

### 3.1 LeakCanary
- `app/build.gradle.kts`: `debugImplementation("com.squareup.leakcanary:leakcanary-android:2.14")`
- Auto-initialized via ContentProvider — no code change needed
- Run debug build, navigate all screens, verify zero leaks in notification

### 3.2 Inspect Code Cleanup
- Remove hardcoded token (done via 1.4)
- Fix unused imports across all modules
- `fallbackToDestructiveMigration()` — keep but suppress warning (acceptable for submission)
- ProGuard rules must not break Gson serialization for `MovieResponse` / `ListMovieResponse`

---

## 4. Unit Testing

### 4.1 New Dependencies
Add to `core/build.gradle.kts` and `app/build.gradle.kts`:
```
testImplementation("io.mockk:mockk:1.13.10")
testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.11.0")
testImplementation("androidx.arch.core:core-testing:2.2.0")
testImplementation("app.cash.turbine:turbine:1.1.0")
```

### 4.2 Test Classes

**`core/src/test/`**

| File | Scope |
|------|-------|
| `DataMapperTest.kt` | `toEntity()`, `toDomain()`, `toEntityList()` — pure, no mocks |
| `MovieInteractorTest.kt` | `getMovies()`, `getFavoriteMovies()`, `setFavoriteMovie()` — mock `IMovieRepository` |
| `MovieRepositoryTest.kt` | `getMovies()` flow success/error/empty — mock `LocalDataSource`, `RemoteDataSource` |

**`app/src/test/`**

| File | Scope |
|------|-------|
| `DetailViewModelTest.kt` | `movie` LiveData, `setFavoriteMovie()` — mock `MovieUseCase` |

### 4.3 Test Pattern
```kotlin
@get:Rule val instantRule = InstantTaskExecutorRule()
private val useCase: MovieUseCase = mockk()

@Test fun `getMovieById returns success`() = runTest {
    every { useCase.getMovieById(any()) } returns flowOf(Resource.Success(fakeMovie))
    val viewModel = DetailViewModel(useCase)
    viewModel.movie.test {
        assertEquals(Resource.Success(fakeMovie), awaitItem())
        cancelAndIgnoreRemainingEvents()
    }
}
```

### 4.4 Coverage Targets
| Layer | Target |
|-------|--------|
| `DataMapper` | ~100% |
| `MovieInteractor` | ~90% |
| `MovieRepository` | ~70% |
| `DetailViewModel` | ~80% |

---

## Implementation Order

1. **Security** — SQLCipher, cert pinning, ProGuard, BuildConfig token, root detection
2. **Performance** — LeakCanary, Inspect Code cleanup
3. **Tests** — All 4 test classes
4. **CI** — GitHub repo, Actions workflow, plugins
5. **Verify** — Full CI run passes, LeakCanary clean, Inspect Code clean

---

## Files Changed / Created

| File | Action |
|------|--------|
| `core/build.gradle.kts` | Add SQLCipher, test deps, BuildConfig field, ktlint, jacoco |
| `app/build.gradle.kts` | Enable minify, add LeakCanary, test deps |
| `core/consumer-rules.pro` | Add keep rules |
| `app/proguard-rules.pro` | Add keep rules |
| `gradle/libs.versions.toml` | Add sqlcipher, mockk, turbine, ktlint, owasp plugins |
| `build.gradle.kts` (root) | Add ktlint, owasp plugins |
| `core/utils/SecureKeyManager.kt` | NEW — AndroidKeyStore passphrase management |
| `core/utils/RootDetectionUtil.kt` | NEW — RootBeer wrapper |
| `core/di/DatabaseModule.kt` | Use SQLCipher SupportFactory |
| `core/di/NetworkModule.kt` | Add CertificatePinner, use BuildConfig token |
| `app/res/xml/network_security_config.xml` | Add pin-set |
| `app/AndroidManifest.xml` | Reference networkSecurityConfig |
| `app/MainActivity.kt` | Root detection check |
| `core/src/test/DataMapperTest.kt` | NEW |
| `core/src/test/MovieInteractorTest.kt` | NEW |
| `core/src/test/MovieRepositoryTest.kt` | NEW |
| `app/src/test/DetailViewModelTest.kt` | NEW |
| `.github/workflows/ci.yml` | NEW |
