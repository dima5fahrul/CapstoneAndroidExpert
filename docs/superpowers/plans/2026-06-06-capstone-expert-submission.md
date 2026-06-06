# Capstone Android Expert Submission Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement all mandatory submission criteria (CI, performance, security) and star-5 optimizations (unit tests, advanced security, CI pipeline enhancements).

**Architecture:** Multi-module Clean Architecture — `core` (library), `app`, `favorite` (dynamic feature), `setting` (dynamic feature). DI via Koin, Room database migrated to SQLCipher, Retrofit+OkHttp with certificate pinning.

**Tech Stack:** Kotlin, Koin, Room + SQLCipher, Retrofit, OkHttp, MockK, Turbine, JaCoCo, ktlint, OWASP dependency check, GitHub Actions.

---

## File Map

| File | Action | Purpose |
|------|--------|---------|
| `gradle/libs.versions.toml` | Modify | Add SQLCipher, MockK, Turbine, coroutines-test, core-testing, LeakCanary, RootBeer, ktlint, OWASP versions |
| `build.gradle.kts` (root) | Modify | Apply ktlint + OWASP plugins to all subprojects |
| `core/build.gradle.kts` | Modify | Enable BuildConfig, add SQLCipher + test deps, enable minify (release), buildConfigField for token |
| `app/build.gradle.kts` | Modify | Enable minify + shrinkResources (release), add LeakCanary + test deps |
| `core/consumer-rules.pro` | Modify | Add keep rules for Retrofit, Gson, Room, Koin, data classes |
| `app/proguard-rules.pro` | Modify | Add keep rules for app classes, Koin, navigation |
| `local.properties` | Modify | Add `tmdb.api.token=<your_token>` (not committed) |
| `core/utils/SecureKeyManager.kt` | **CREATE** | AndroidKeyStore AES-256 key generation + retrieval |
| `core/utils/RootDetectionUtil.kt` | **CREATE** | RootBeer wrapper for root detection |
| `core/di/DatabaseModule.kt` | Modify | Replace Room builder with SQLCipher SupportFactory |
| `core/di/NetworkModule.kt` | Modify | Add CertificatePinner, read token from BuildConfig |
| `app/src/main/res/xml/network_security_config.xml` | **CREATE** | Pin-set for api.themoviedb.org |
| `app/AndroidManifest.xml` | Modify | Reference networkSecurityConfig |
| `app/MainActivity.kt` | Modify | Add root detection check in onCreate |
| `core/src/test/.../DataMapperTest.kt` | **CREATE** | Pure function mapping tests |
| `core/src/test/.../MovieInteractorTest.kt` | **CREATE** | UseCase delegation tests |
| `core/src/test/.../MovieRepositoryTest.kt` | **CREATE** | Repository flow tests with mocks |
| `app/src/test/.../DetailViewModelTest.kt` | **CREATE** | ViewModel unit tests |
| `.github/workflows/ci.yml` | **CREATE** | GitHub Actions: lint → test → build → security |

---

## Task 1: Add Test Dependencies

**Files:**
- Modify: `gradle/libs.versions.toml`
- Modify: `core/build.gradle.kts`
- Modify: `app/build.gradle.kts`

- [ ] **Step 1: Add versions to libs.versions.toml**

```toml
# in [versions]
mockk = "1.13.10"
turbine = "1.1.0"
coroutinesTest = "1.11.0"
archCoreTesting = "2.2.0"
```

```toml
# in [libraries]
mockk = { group = "io.mockk", name = "mockk", version.ref = "mockk" }
turbine = { group = "app.cash.turbine", name = "turbine", version.ref = "turbine" }
kotlinx-coroutines-test = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-test", version.ref = "coroutinesTest" }
androidx-arch-core-testing = { group = "androidx.arch.core", name = "core-testing", version.ref = "archCoreTesting" }
```

- [ ] **Step 2: Add test deps to core/build.gradle.kts**

In the `dependencies` block:
```kotlin
testImplementation(libs.mockk)
testImplementation(libs.turbine)
testImplementation(libs.kotlinx.coroutines.test)
testImplementation(libs.androidx.arch.core.testing)
```

- [ ] **Step 3: Add test deps to app/build.gradle.kts**

In the `dependencies` block:
```kotlin
testImplementation(libs.mockk)
testImplementation(libs.turbine)
testImplementation(libs.kotlinx.coroutines.test)
testImplementation(libs.androidx.arch.core.testing)
```

- [ ] **Step 4: Sync and verify**

```bash
./gradlew :core:dependencies --configuration testDebugRuntimeClasspath | grep -E "mockk|turbine|coroutines-test|core-testing"
```
Expected: All 4 dependencies appear in output.

- [ ] **Step 5: Commit**

```bash
git add gradle/libs.versions.toml core/build.gradle.kts app/build.gradle.kts
git commit -m "test: add mockk, turbine, coroutines-test, arch-core-testing dependencies"
```

---

## Task 2: DataMapperTest

**Files:**
- Create: `core/src/test/java/com/example/capstoneandroidexpert/core/utils/DataMapperTest.kt`

`DataMapper` has 4 pure functions: `mapResponsesToEntities`, `mapResponsesToDomain`, `mapEntitiesToDomain`, `mapDomainToEntity`. No mocks needed.

- [ ] **Step 1: Create test file**

```kotlin
package com.example.capstoneandroidexpert.core.utils

import com.example.capstoneandroidexpert.core.data.source.local.entity.MovieEntity
import com.example.capstoneandroidexpert.core.data.source.remote.response.MovieResponse
import com.example.capstoneandroidexpert.core.domain.model.Movie
import org.junit.Assert.assertEquals
import org.junit.Test

class DataMapperTest {

    private val fakeResponse = MovieResponse(
        id = 1,
        title = "Test Movie",
        overview = "Overview",
        posterPath = "/poster.jpg",
        backdropPath = "/backdrop.jpg",
        releaseDate = "2024-01-01",
        voteAverage = 8.5
    )

    private val fakeEntity = MovieEntity(
        movieId = 1,
        title = "Test Movie",
        overview = "Overview",
        posterPath = "https://image.tmdb.org/t/p/w500/poster.jpg",
        backdropPath = "https://image.tmdb.org/t/p/original/backdrop.jpg",
        releaseDate = "2024-01-01",
        voteAverage = 8.5,
        isFavorite = false
    )

    private val fakeDomain = Movie(
        movieId = 1,
        title = "Test Movie",
        overview = "Overview",
        posterPath = "https://image.tmdb.org/t/p/w500/poster.jpg",
        backdropPath = "https://image.tmdb.org/t/p/original/backdrop.jpg",
        releaseDate = "2024-01-01",
        voteAverage = 8.5,
        isFavorite = false
    )

    @Test
    fun `mapResponsesToEntities maps all fields correctly`() {
        val result = DataMapper.mapResponsesToEntities(listOf(fakeResponse))

        assertEquals(1, result.size)
        assertEquals(fakeResponse.id, result[0].movieId)
        assertEquals(fakeResponse.title, result[0].title)
        assertEquals("https://image.tmdb.org/t/p/w500/poster.jpg", result[0].posterPath)
        assertEquals("https://image.tmdb.org/t/p/original/backdrop.jpg", result[0].backdropPath)
        assertEquals(false, result[0].isFavorite)
    }

    @Test
    fun `mapResponsesToEntities handles null posterPath`() {
        val responseWithNullPoster = fakeResponse.copy(posterPath = null)
        val result = DataMapper.mapResponsesToEntities(listOf(responseWithNullPoster))

        assertEquals("https://image.tmdb.org/t/p/w500null", result[0].posterPath)
    }

    @Test
    fun `mapResponsesToEntities handles null releaseDate`() {
        val responseNullDate = fakeResponse.copy(releaseDate = null)
        val result = DataMapper.mapResponsesToEntities(listOf(responseNullDate))

        assertEquals("N/A", result[0].releaseDate)
    }

    @Test
    fun `mapResponsesToEntities returns empty list for empty input`() {
        val result = DataMapper.mapResponsesToEntities(emptyList())
        assertEquals(0, result.size)
    }

    @Test
    fun `mapResponsesToDomain maps all fields correctly`() {
        val result = DataMapper.mapResponsesToDomain(listOf(fakeResponse))

        assertEquals(1, result.size)
        assertEquals(fakeResponse.id, result[0].movieId)
        assertEquals(fakeResponse.title, result[0].title)
        assertEquals("https://image.tmdb.org/t/p/w500/poster.jpg", result[0].posterPath)
        assertEquals(false, result[0].isFavorite)
    }

    @Test
    fun `mapEntitiesToDomain maps all fields correctly`() {
        val result = DataMapper.mapEntitiesToDomain(listOf(fakeEntity))

        assertEquals(1, result.size)
        assertEquals(fakeEntity.movieId, result[0].movieId)
        assertEquals(fakeEntity.title, result[0].title)
        assertEquals(fakeEntity.isFavorite, result[0].isFavorite)
    }

    @Test
    fun `mapDomainToEntity maps all fields correctly`() {
        val result = DataMapper.mapDomainToEntity(fakeDomain)

        assertEquals(fakeDomain.movieId, result.movieId)
        assertEquals(fakeDomain.title, result.title)
        assertEquals(fakeDomain.isFavorite, result.isFavorite)
        assertEquals(fakeDomain.posterPath, result.posterPath)
    }
}
```

- [ ] **Step 2: Run tests**

```bash
./gradlew :core:testDebugUnitTest --tests "com.example.capstoneandroidexpert.core.utils.DataMapperTest"
```
Expected: `BUILD SUCCESSFUL` — all 7 tests PASS.

- [ ] **Step 3: Commit**

```bash
git add core/src/test/
git commit -m "test: add DataMapperTest covering all mapping functions"
```

---

## Task 3: MovieInteractorTest

**Files:**
- Create: `core/src/test/java/com/example/capstoneandroidexpert/core/domain/usecase/MovieInteractorTest.kt`

`MovieInteractor` delegates every call to `IMovieRepository`. Tests verify delegation — no logic to test beyond "correct method called with correct args."

- [ ] **Step 1: Create test file**

```kotlin
package com.example.capstoneandroidexpert.core.domain.usecase

import app.cash.turbine.test
import com.example.capstoneandroidexpert.core.data.Resource
import com.example.capstoneandroidexpert.core.domain.model.Movie
import com.example.capstoneandroidexpert.core.domain.repository.IMovieRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class MovieInteractorTest {

    private val repository: IMovieRepository = mockk()
    private lateinit var interactor: MovieInteractor

    private val fakeMovie = Movie(1, "Title", "Overview", "poster", "backdrop", "2024-01-01", 7.5, false)
    private val fakeMovieList = listOf(fakeMovie)

    @Before
    fun setUp() {
        interactor = MovieInteractor(repository)
    }

    @Test
    fun `getAllMovie delegates to repository`() = runTest {
        every { repository.getAllMovie() } returns flowOf(Resource.Success(fakeMovieList))

        interactor.getAllMovie().test {
            val item = awaitItem()
            assertEquals(Resource.Success(fakeMovieList), item)
            cancelAndIgnoreRemainingEvents()
        }

        verify { repository.getAllMovie() }
    }

    @Test
    fun `getNowPlayingMovies delegates to repository`() = runTest {
        every { repository.getNowPlayingMovies() } returns flowOf(Resource.Success(fakeMovieList))

        interactor.getNowPlayingMovies().test {
            awaitItem()
            cancelAndIgnoreRemainingEvents()
        }

        verify { repository.getNowPlayingMovies() }
    }

    @Test
    fun `getPopularMovies delegates to repository`() = runTest {
        every { repository.getPopularMovies() } returns flowOf(Resource.Success(fakeMovieList))

        interactor.getPopularMovies().test {
            awaitItem()
            cancelAndIgnoreRemainingEvents()
        }

        verify { repository.getPopularMovies() }
    }

    @Test
    fun `getFavoriteMovie delegates to repository`() = runTest {
        every { repository.getFavoriteMovie() } returns flowOf(fakeMovieList)

        interactor.getFavoriteMovie().test {
            val item = awaitItem()
            assertEquals(fakeMovieList, item)
            cancelAndIgnoreRemainingEvents()
        }

        verify { repository.getFavoriteMovie() }
    }

    @Test
    fun `setFavoriteMovie delegates to repository`() {
        every { repository.setFavoriteMovie(fakeMovie, true) } returns Unit

        interactor.setFavoriteMovie(fakeMovie, true)

        verify { repository.setFavoriteMovie(fakeMovie, true) }
    }

    @Test
    fun `searchMovies delegates to repository with query`() = runTest {
        every { repository.searchMovies("batman") } returns flowOf(Resource.Success(fakeMovieList))

        interactor.searchMovies("batman").test {
            awaitItem()
            cancelAndIgnoreRemainingEvents()
        }

        verify { repository.searchMovies("batman") }
    }
}
```

- [ ] **Step 2: Run tests**

```bash
./gradlew :core:testDebugUnitTest --tests "com.example.capstoneandroidexpert.core.domain.usecase.MovieInteractorTest"
```
Expected: `BUILD SUCCESSFUL` — all 6 tests PASS.

- [ ] **Step 3: Commit**

```bash
git add core/src/test/
git commit -m "test: add MovieInteractorTest verifying delegation to IMovieRepository"
```

---

## Task 4: MovieRepositoryTest

**Files:**
- Create: `core/src/test/java/com/example/capstoneandroidexpert/core/data/MovieRepositoryTest.kt`

Tests focus on `getFavoriteMovie()` (simple flow) and `setFavoriteMovie()` (executor dispatch). `getAllMovie()` uses `NetworkBoundResource` which runs on `AppExecutors.diskIO()` — test with a direct executor.

- [ ] **Step 1: Create test file**

```kotlin
package com.example.capstoneandroidexpert.core.data

import app.cash.turbine.test
import com.example.capstoneandroidexpert.core.data.source.local.LocalDataSource
import com.example.capstoneandroidexpert.core.data.source.local.entity.MovieEntity
import com.example.capstoneandroidexpert.core.data.source.remote.RemoteDataSource
import com.example.capstoneandroidexpert.core.domain.model.Movie
import com.example.capstoneandroidexpert.core.utils.AppExecutors
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.concurrent.Executor

class MovieRepositoryTest {

    private val localDataSource: LocalDataSource = mockk()
    private val remoteDataSource: RemoteDataSource = mockk()
    private val appExecutors: AppExecutors = mockk()
    private lateinit var repository: MovieRepository

    private val fakeEntity = MovieEntity(
        movieId = 1,
        title = "Test Movie",
        overview = "Overview",
        posterPath = "https://image.tmdb.org/t/p/w500/poster.jpg",
        backdropPath = "https://image.tmdb.org/t/p/original/backdrop.jpg",
        releaseDate = "2024-01-01",
        voteAverage = 8.5,
        isFavorite = true
    )

    private val expectedDomain = Movie(
        movieId = 1,
        title = "Test Movie",
        overview = "Overview",
        posterPath = "https://image.tmdb.org/t/p/w500/poster.jpg",
        backdropPath = "https://image.tmdb.org/t/p/original/backdrop.jpg",
        releaseDate = "2024-01-01",
        voteAverage = 8.5,
        isFavorite = true
    )

    @Before
    fun setUp() {
        repository = MovieRepository(remoteDataSource, localDataSource, appExecutors)
    }

    @Test
    fun `getFavoriteMovie returns mapped domain list`() = runTest {
        every { localDataSource.getFavoriteMovie() } returns flowOf(listOf(fakeEntity))

        repository.getFavoriteMovie().test {
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals(expectedDomain.movieId, result[0].movieId)
            assertEquals(expectedDomain.title, result[0].title)
            assertEquals(true, result[0].isFavorite)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getFavoriteMovie returns empty list when no favorites`() = runTest {
        every { localDataSource.getFavoriteMovie() } returns flowOf(emptyList())

        repository.getFavoriteMovie().test {
            val result = awaitItem()
            assertEquals(0, result.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setFavoriteMovie executes on disk IO executor`() {
        val directExecutor = Executor { it.run() }
        every { appExecutors.diskIO() } returns directExecutor
        every { localDataSource.setFavoriteMovie(any(), any()) } returns Unit

        repository.setFavoriteMovie(expectedDomain, true)

        verify { appExecutors.diskIO() }
        verify { localDataSource.setFavoriteMovie(any(), true) }
    }
}
```

- [ ] **Step 2: Run tests**

```bash
./gradlew :core:testDebugUnitTest --tests "com.example.capstoneandroidexpert.core.data.MovieRepositoryTest"
```
Expected: `BUILD SUCCESSFUL` — all 3 tests PASS.

- [ ] **Step 3: Commit**

```bash
git add core/src/test/
git commit -m "test: add MovieRepositoryTest for getFavoriteMovie and setFavoriteMovie"
```

---

## Task 5: DetailViewModelTest

**Files:**
- Create: `app/src/test/java/com/example/capstoneandroidexpert/DetailViewModelTest.kt`

`DetailViewModel` only has `setFavoriteMovie()` which delegates to `MovieUseCase`. Simple delegation test.

- [ ] **Step 1: Create test file**

```kotlin
package com.example.capstoneandroidexpert

import com.example.capstoneandroidexpert.core.domain.model.Movie
import com.example.capstoneandroidexpert.core.domain.usecase.MovieUseCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class DetailViewModelTest {

    private val movieUseCase: MovieUseCase = mockk()
    private lateinit var viewModel: DetailViewModel

    private val fakeMovie = Movie(
        movieId = 1,
        title = "Test Movie",
        overview = "Overview",
        posterPath = "https://image.tmdb.org/t/p/w500/poster.jpg",
        backdropPath = "https://image.tmdb.org/t/p/original/backdrop.jpg",
        releaseDate = "2024-01-01",
        voteAverage = 8.0,
        isFavorite = false
    )

    @Before
    fun setUp() {
        viewModel = DetailViewModel(movieUseCase)
    }

    @Test
    fun `setFavoriteMovie with true delegates to useCase`() {
        every { movieUseCase.setFavoriteMovie(fakeMovie, true) } returns Unit

        viewModel.setFavoriteMovie(fakeMovie, true)

        verify { movieUseCase.setFavoriteMovie(fakeMovie, true) }
    }

    @Test
    fun `setFavoriteMovie with false delegates to useCase`() {
        every { movieUseCase.setFavoriteMovie(fakeMovie, false) } returns Unit

        viewModel.setFavoriteMovie(fakeMovie, false)

        verify { movieUseCase.setFavoriteMovie(fakeMovie, false) }
    }
}
```

- [ ] **Step 2: Run tests**

```bash
./gradlew :app:testDebugUnitTest --tests "com.example.capstoneandroidexpert.DetailViewModelTest"
```
Expected: `BUILD SUCCESSFUL` — both tests PASS.

- [ ] **Step 3: Run all unit tests**

```bash
./gradlew testDebugUnitTest
```
Expected: `BUILD SUCCESSFUL` — all tests in all modules PASS.

- [ ] **Step 4: Commit**

```bash
git add app/src/test/
git commit -m "test: add DetailViewModelTest for setFavoriteMovie delegation"
```

---

## Task 6: Enable ProGuard / R8

**Files:**
- Modify: `app/build.gradle.kts`
- Modify: `core/build.gradle.kts`
- Modify: `app/proguard-rules.pro`
- Modify: `core/consumer-rules.pro`

- [ ] **Step 1: Enable minify in app/build.gradle.kts**

Replace the release buildType block:
```kotlin
buildTypes {
    release {
        isMinifyEnabled = true
        isShrinkResources = true
        proguardFiles(
            getDefaultProguardFile("proguard-android-optimize.txt"),
            "proguard-rules.pro"
        )
    }
}
```

- [ ] **Step 2: Enable minify in core/build.gradle.kts**

Replace the release buildType block:
```kotlin
buildTypes {
    release {
        isMinifyEnabled = true
        proguardFiles(
            getDefaultProguardFile("proguard-android-optimize.txt"),
            "proguard-rules.pro"
        )
    }
}
```

- [ ] **Step 3: Write core/consumer-rules.pro**

```proguard
# Retrofit
-keepattributes Signature
-keepattributes Exceptions
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# Gson / JSON serialization — keep all response/entity data classes
-keep class com.example.capstoneandroidexpert.core.data.source.remote.response.** { *; }
-keep class com.example.capstoneandroidexpert.core.data.source.local.entity.** { *; }
-keep class com.example.capstoneandroidexpert.core.domain.model.** { *; }
-keepattributes SerializedName

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Koin
-keep class org.koin.** { *; }
-keepnames class * implements org.koin.core.module.Module

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
```

- [ ] **Step 4: Write app/proguard-rules.pro**

```proguard
# Keep line numbers for crash reports
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Koin ViewModels
-keep class com.example.capstoneandroidexpert.di.** { *; }

# Navigation safe args
-keep class * extends androidx.navigation.NavArgs { *; }

# Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class com.bumptech.glide.** { *; }
```

- [ ] **Step 5: Build release APK to verify ProGuard does not break app**

```bash
./gradlew assembleRelease
```
Expected: `BUILD SUCCESSFUL`. If build fails with "duplicate class" or "missing class" errors, add corresponding `-keep` or `-dontwarn` rules to the relevant .pro file.

- [ ] **Step 6: Commit**

```bash
git add app/build.gradle.kts core/build.gradle.kts app/proguard-rules.pro core/consumer-rules.pro
git commit -m "security: enable R8 minification and add ProGuard keep rules for all modules"
```

---

## Task 7: Move API Token to BuildConfig

**Files:**
- Modify: `local.properties`
- Modify: `core/build.gradle.kts`
- Modify: `core/di/NetworkModule.kt`

The TMDB Bearer token is currently hardcoded in `NetworkModule.kt`. Move it to `local.properties` → `BuildConfig`.

- [ ] **Step 1: Add token to local.properties**

Open `local.properties` (at project root) and add:
```properties
tmdb.api.token=eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiI1ZWRlYmM0YmNjZWNkNGY4MTIxNTY4MTlhYzE1MjZlZiIsIm5iZiI6MTY4MTcyMjQ2MS45OTYsInN1YiI6IjY0M2QwYzVkMmVhNmI5MDU0NjUxZjAwYyIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.8_FVTdfmtdNI6ycSoyKDlug6yrjfLTUtpS1dz8tmbwk
```

Verify `local.properties` is in `.gitignore`:
```bash
grep "local.properties" .gitignore
```
Expected: `local.properties` line exists. If not, add it.

- [ ] **Step 2: Enable BuildConfig and add buildConfigField in core/build.gradle.kts**

In the `android` block, add `buildFeatures`:
```kotlin
buildFeatures {
    viewBinding = true
    buildConfig = true
}
```

In the `defaultConfig` block, add:
```kotlin
val localProperties = java.util.Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
}
buildConfigField(
    "String",
    "TMDB_API_TOKEN",
    "\"${localProperties.getProperty("tmdb.api.token", "")}\""
)
```

- [ ] **Step 3: Update NetworkModule.kt to use BuildConfig**

Replace the hardcoded token in `core/src/main/java/com/example/capstoneandroidexpert/core/di/NetworkModule.kt`:

```kotlin
package com.example.capstoneandroidexpert.core.di

import com.example.capstoneandroidexpert.core.BuildConfig
import com.example.capstoneandroidexpert.core.data.source.remote.network.ApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

val networkModule = module {
    single {
        OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer ${BuildConfig.TMDB_API_TOKEN}")
                    .addHeader("accept", "application/json")
                    .build()
                chain.proceed(request)
            }
            .connectTimeout(120, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .build()
    }
    single {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.themoviedb.org/3/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(get())
            .build()
        retrofit.create(ApiService::class.java)
    }
}
```

- [ ] **Step 4: Build debug to verify token loads correctly**

```bash
./gradlew assembleDebug
```
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 5: Commit**

```bash
git add core/build.gradle.kts core/src/main/java/com/example/capstoneandroidexpert/core/di/NetworkModule.kt
git commit -m "security: move TMDB API token from hardcode to BuildConfig via local.properties"
```

---

## Task 8: SQLCipher Database Encryption

**Files:**
- Modify: `gradle/libs.versions.toml`
- Modify: `core/build.gradle.kts`
- Create: `core/src/main/java/com/example/capstoneandroidexpert/core/utils/SecureKeyManager.kt`
- Modify: `core/src/main/java/com/example/capstoneandroidexpert/core/di/DatabaseModule.kt`

- [ ] **Step 1: Add SQLCipher dependencies to libs.versions.toml**

```toml
# in [versions]
sqlcipher = "4.5.4"
sqliteKtx = "2.4.0"
```

```toml
# in [libraries]
sqlcipher = { group = "net.zetetic", name = "android-database-sqlcipher", version.ref = "sqlcipher" }
sqlite-ktx = { group = "androidx.sqlite", name = "sqlite-ktx", version.ref = "sqliteKtx" }
```

- [ ] **Step 2: Add to core/build.gradle.kts dependencies**

```kotlin
implementation(libs.sqlcipher)
implementation(libs.sqlite.ktx)
```

- [ ] **Step 3: Create SecureKeyManager.kt**

```kotlin
package com.example.capstoneandroidexpert.core.utils

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

object SecureKeyManager {

    private const val KEY_ALIAS = "capstone_db_key"
    private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
    private const val KEY_SIZE = 256

    fun getOrCreateDatabaseKey(): ByteArray {
        val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply { load(null) }

        if (!keyStore.containsAlias(KEY_ALIAS)) {
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                KEYSTORE_PROVIDER
            )
            keyGenerator.init(
                KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(KEY_SIZE)
                    .build()
            )
            keyGenerator.generateKey()
        }

        val secretKey = keyStore.getKey(KEY_ALIAS, null) as SecretKey
        return secretKey.encoded
    }
}
```

- [ ] **Step 4: Update DatabaseModule.kt to use SQLCipher**

```kotlin
package com.example.capstoneandroidexpert.core.di

import androidx.sqlite.db.SupportSQLiteOpenHelper
import com.example.capstoneandroidexpert.core.data.source.local.room.MovieDatabase
import com.example.capstoneandroidexpert.core.utils.SecureKeyManager
import net.sqlcipher.database.SupportFactory
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {
    factory { get<MovieDatabase>().movieDao() }
    single {
        val passphrase = SecureKeyManager.getOrCreateDatabaseKey()
        val factory: SupportSQLiteOpenHelper.Factory = SupportFactory(passphrase)
        androidx.room.Room.databaseBuilder(
            androidContext(),
            MovieDatabase::class.java,
            "Movie.db"
        )
            .openHelperFactory(factory)
            .fallbackToDestructiveMigration()
            .build()
    }
}
```

- [ ] **Step 5: Build and run debug to verify database opens correctly**

```bash
./gradlew assembleDebug
```
Expected: `BUILD SUCCESSFUL`. Install on device/emulator, launch app — data must load (confirms SQLCipher is set up correctly and DB is accessible).

- [ ] **Step 6: Commit**

```bash
git add gradle/libs.versions.toml core/build.gradle.kts \
  core/src/main/java/com/example/capstoneandroidexpert/core/utils/SecureKeyManager.kt \
  core/src/main/java/com/example/capstoneandroidexpert/core/di/DatabaseModule.kt
git commit -m "security: encrypt Room database with SQLCipher using AndroidKeyStore-backed passphrase"
```

---

## Task 9: Certificate Pinning

**Files:**
- Modify: `core/src/main/java/com/example/capstoneandroidexpert/core/di/NetworkModule.kt`
- Create: `app/src/main/res/xml/network_security_config.xml`
- Modify: `app/src/main/AndroidManifest.xml`

**Important:** Steps 1-2 require a machine with internet access to retrieve live SHA-256 pins. Do NOT skip this — wrong pins will cause all API calls to fail.

- [ ] **Step 1: Retrieve SHA-256 pins for api.themoviedb.org**

Run this command to get the leaf certificate pin:
```bash
openssl s_client -connect api.themoviedb.org:443 -showcerts </dev/null 2>/dev/null \
  | openssl x509 -pubkey -noout \
  | openssl pkey -pubin -outform DER \
  | openssl dgst -sha256 -binary \
  | openssl enc -base64
```

Run this to get the intermediate CA pin (backup):
```bash
openssl s_client -connect api.themoviedb.org:443 -showcerts </dev/null 2>/dev/null \
  | awk '/BEGIN CERTIFICATE/{c++} c==2{print}' \
  | openssl x509 -pubkey -noout \
  | openssl pkey -pubin -outform DER \
  | openssl dgst -sha256 -binary \
  | openssl enc -base64
```

Save both base64 strings — you will need them in Steps 2 and 3. They look like: `abc123DEF+xyz789/==`

- [ ] **Step 2: Add CertificatePinner to NetworkModule.kt**

Replace the `OkHttpClient.Builder()` single block in `NetworkModule.kt`. Replace `LEAF_PIN` and `INTERMEDIATE_PIN` with the base64 values from Step 1:

```kotlin
single {
    val certificatePinner = okhttp3.CertificatePinner.Builder()
        .add("api.themoviedb.org", "sha256/LEAF_PIN_FROM_STEP1")
        .add("api.themoviedb.org", "sha256/INTERMEDIATE_PIN_FROM_STEP1")
        .build()

    OkHttpClient.Builder()
        .certificatePinner(certificatePinner)
        .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer ${BuildConfig.TMDB_API_TOKEN}")
                .addHeader("accept", "application/json")
                .build()
            chain.proceed(request)
        }
        .connectTimeout(120, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .build()
}
```

- [ ] **Step 3: Create network_security_config.xml**

Create `app/src/main/res/xml/network_security_config.xml`. Replace `LEAF_PIN_FROM_STEP1` and `INTERMEDIATE_PIN_FROM_STEP1` with same values from Step 1:

```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <domain-config cleartextTrafficPermitted="false">
        <domain includeSubdomains="true">api.themoviedb.org</domain>
        <pin-set>
            <pin digest="SHA-256">LEAF_PIN_FROM_STEP1</pin>
            <pin digest="SHA-256">INTERMEDIATE_PIN_FROM_STEP1</pin>
        </pin-set>
    </domain-config>
</network-security-config>
```

- [ ] **Step 4: Reference in AndroidManifest.xml**

In `app/src/main/AndroidManifest.xml`, add `networkSecurityConfig` attribute to `<application>`:

```xml
<application
    android:name=".MyApplication"
    android:networkSecurityConfig="@xml/network_security_config"
    android:allowBackup="true"
    android:dataExtractionRules="@xml/data_extraction_rules"
    android:fullBackupContent="@xml/backup_rules"
    android:icon="@mipmap/ic_launcher"
    android:label="@string/app_name"
    android:roundIcon="@mipmap/ic_launcher_round"
    android:supportsRtl="true"
    android:theme="@style/Theme.CapstoneAndroidExpert">
```

- [ ] **Step 5: Install on device/emulator and verify API calls succeed**

```bash
./gradlew assembleDebug
```
Install the APK on a device/emulator with internet access. Open the app and verify movies load. If all API calls fail with `SSLPeerUnverifiedException`, the pins are wrong — re-run Step 1 commands and update pins.

- [ ] **Step 6: Commit**

```bash
git add core/src/main/java/com/example/capstoneandroidexpert/core/di/NetworkModule.kt \
  app/src/main/res/xml/network_security_config.xml \
  app/src/main/AndroidManifest.xml
git commit -m "security: add certificate pinning for api.themoviedb.org via OkHttp and NetworkSecurityConfig"
```

---

## Task 10: Root Detection

**Files:**
- Modify: `gradle/libs.versions.toml`
- Modify: `app/build.gradle.kts`
- Create: `core/src/main/java/com/example/capstoneandroidexpert/core/utils/RootDetectionUtil.kt`
- Modify: `app/src/main/java/com/example/capstoneandroidexpert/MainActivity.kt`

- [ ] **Step 1: Add RootBeer to libs.versions.toml**

```toml
# in [versions]
rootbeer = "0.1.0"
```

```toml
# in [libraries]
rootbeer = { group = "com.scottyab", name = "rootbeer-lib", version.ref = "rootbeer" }
```

- [ ] **Step 2: Add dependency to app/build.gradle.kts**

```kotlin
implementation(libs.rootbeer)
```

- [ ] **Step 3: Create RootDetectionUtil.kt**

```kotlin
package com.example.capstoneandroidexpert.core.utils

import android.content.Context
import com.scottyab.rootbeer.RootBeer

object RootDetectionUtil {
    fun isDeviceRooted(context: Context): Boolean {
        return RootBeer(context).isRooted
    }
}
```

- [ ] **Step 4: Add root check in MainActivity.kt**

```kotlin
package com.example.capstoneandroidexpert

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.capstoneandroidexpert.core.utils.RootDetectionUtil
import com.example.capstoneandroidexpert.databinding.ActivityMainBinding
import com.example.capstoneandroidexpert.presentation.home.MainViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {

    private val mainViewModel: MainViewModel by viewModel()
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (RootDetectionUtil.isDeviceRooted(this)) {
            AlertDialog.Builder(this)
                .setTitle("Security Warning")
                .setMessage("This app cannot run on a rooted device.")
                .setCancelable(false)
                .setPositiveButton("Exit") { _, _ -> finish() }
                .show()
            return
        }

        mainViewModel.getThemeSettings().observe(this) { isDarkModeActive ->
            if (isDarkModeActive) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        navView.setupWithNavController(navController)
    }

    companion object {
        const val EXTRA_DATA = "extra_data"
    }
}
```

- [ ] **Step 5: Build and verify**

```bash
./gradlew assembleDebug
```
Expected: `BUILD SUCCESSFUL`. Test on non-rooted device — app launches normally. On rooted device/emulator with root enabled — dialog appears and app exits.

- [ ] **Step 6: Commit**

```bash
git add gradle/libs.versions.toml app/build.gradle.kts \
  core/src/main/java/com/example/capstoneandroidexpert/core/utils/RootDetectionUtil.kt \
  app/src/main/java/com/example/capstoneandroidexpert/MainActivity.kt
git commit -m "security: add RootBeer root detection — blocks app launch on rooted devices"
```

---

## Task 11: LeakCanary

**Files:**
- Modify: `app/build.gradle.kts`

- [ ] **Step 1: Add LeakCanary to libs.versions.toml**

```toml
# in [versions]
leakcanary = "2.14"
```

```toml
# in [libraries]
leakcanary = { group = "com.squareup.leakcanary", name = "leakcanary-android", version.ref = "leakcanary" }
```

- [ ] **Step 2: Add debugImplementation to app/build.gradle.kts**

```kotlin
debugImplementation(libs.leakcanary)
```

- [ ] **Step 3: Build debug and run all screens**

```bash
./gradlew assembleDebug
```
Install on device. Navigate through all screens: Home → Detail → Favorite → Setting. Pull down notification shade — LeakCanary notification shows "0 leaks detected" if clean.

If leaks are reported, LeakCanary shows the exact chain. Common fix: remove listener references in `onDetach()` / `onDestroy()`.

- [ ] **Step 4: Commit**

```bash
git add gradle/libs.versions.toml app/build.gradle.kts
git commit -m "perf: add LeakCanary for memory leak detection in debug builds"
```

---

## Task 12: CI Gradle Plugins (ktlint + JaCoCo + OWASP)

**Files:**
- Modify: `gradle/libs.versions.toml`
- Modify: `build.gradle.kts` (root)
- Modify: `core/build.gradle.kts`
- Modify: `app/build.gradle.kts`

- [ ] **Step 1: Add plugin versions to libs.versions.toml**

```toml
# in [versions]
ktlint = "12.1.1"
owasp = "10.0.3"
```

```toml
# in [plugins]
ktlint = { id = "org.jlleitschuh.gradle.ktlint", version.ref = "ktlint" }
owasp = { id = "org.owasp.dependencycheck", version.ref = "owasp" }
```

- [ ] **Step 2: Apply plugins in root build.gradle.kts**

```kotlin
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.android.dynamic.feature) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.parcelize) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.owasp)
}

subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
}
```

- [ ] **Step 3: Add JaCoCo custom task to core/build.gradle.kts**

Add at the end of `core/build.gradle.kts`, after the `dependencies` block:

```kotlin
apply(plugin = "jacoco")

tasks.register<JacocoReport>("jacocoDebugTestReport") {
    dependsOn("testDebugUnitTest")
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
    val fileFilter = listOf(
        "**/R.class", "**/R\$*.class", "**/BuildConfig.*", "**/Manifest*.*",
        "**/*Test*.*", "android/**/*.*", "**/*_Impl*", "**/di/**"
    )
    val kotlinDebugTree = fileTree("${layout.buildDirectory.get()}/tmp/kotlin-classes/debug") {
        exclude(fileFilter)
    }
    classDirectories.setFrom(files(kotlinDebugTree))
    sourceDirectories.setFrom(files("${projectDir}/src/main/java"))
    executionData.setFrom(
        fileTree(layout.buildDirectory.get()) { include("**/*.exec", "**/*.ec") }
    )
}
```

- [ ] **Step 4: Add .editorconfig for ktlint**

Create `.editorconfig` at project root:
```ini
root = true

[*]
charset = utf-8
end_of_line = lf
indent_size = 4
indent_style = space
insert_final_newline = true
max_line_length = 120
trim_trailing_whitespace = true

[*.kt]
ktlint_standard_no-wildcard-imports = disabled
```

- [ ] **Step 5: Run ktlint check and fix issues**

```bash
./gradlew ktlintCheck 2>&1 | head -50
```

If violations found, auto-format:
```bash
./gradlew ktlintFormat
```

Then re-check:
```bash
./gradlew ktlintCheck
```
Expected: `BUILD SUCCESSFUL` with no violations.

- [ ] **Step 6: Run JaCoCo to verify task works**

```bash
./gradlew :core:jacocoDebugTestReport
```
Expected: `BUILD SUCCESSFUL`. Report generated at `core/build/reports/jacoco/jacocoDebugTestReport/html/index.html`.

- [ ] **Step 7: Commit**

```bash
git add gradle/libs.versions.toml build.gradle.kts core/build.gradle.kts app/build.gradle.kts .editorconfig
git commit -m "ci: add ktlint, jacoco, and OWASP dependency-check gradle plugins"
```

---

## Task 13: GitHub Actions Workflow

**Files:**
- Create: `.github/workflows/ci.yml`

- [ ] **Step 1: Create workflow directory**

```bash
mkdir -p .github/workflows
```

- [ ] **Step 2: Create .github/workflows/ci.yml**

```yaml
name: CI

on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]

jobs:
  lint:
    name: Lint
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Cache Gradle
        uses: actions/cache@v4
        with:
          path: |
            ~/.gradle/caches
            ~/.gradle/wrapper
          key: gradle-${{ hashFiles('**/*.gradle*', '**/gradle-wrapper.properties', '**/libs.versions.toml') }}
          restore-keys: gradle-

      - name: Create local.properties
        run: echo "tmdb.api.token=${{ secrets.TMDB_API_TOKEN }}" > local.properties

      - name: Run ktlint
        run: ./gradlew ktlintCheck

  test:
    name: Unit Tests
    runs-on: ubuntu-latest
    needs: lint
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Cache Gradle
        uses: actions/cache@v4
        with:
          path: |
            ~/.gradle/caches
            ~/.gradle/wrapper
          key: gradle-${{ hashFiles('**/*.gradle*', '**/gradle-wrapper.properties', '**/libs.versions.toml') }}
          restore-keys: gradle-

      - name: Create local.properties
        run: echo "tmdb.api.token=${{ secrets.TMDB_API_TOKEN }}" > local.properties

      - name: Run unit tests
        run: ./gradlew testDebugUnitTest

      - name: Generate JaCoCo coverage report
        run: ./gradlew :core:jacocoDebugTestReport

      - name: Upload coverage report
        uses: actions/upload-artifact@v4
        with:
          name: coverage-report
          path: core/build/reports/jacoco/jacocoDebugTestReport/html/

  build:
    name: Build APK
    runs-on: ubuntu-latest
    needs: test
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Cache Gradle
        uses: actions/cache@v4
        with:
          path: |
            ~/.gradle/caches
            ~/.gradle/wrapper
          key: gradle-${{ hashFiles('**/*.gradle*', '**/gradle-wrapper.properties', '**/libs.versions.toml') }}
          restore-keys: gradle-

      - name: Create local.properties
        run: echo "tmdb.api.token=${{ secrets.TMDB_API_TOKEN }}" > local.properties

      - name: Build release APK
        run: ./gradlew assembleRelease

      - name: Upload APK artifact
        uses: actions/upload-artifact@v4
        with:
          name: release-apk
          path: app/build/outputs/apk/release/*.apk

  security:
    name: Dependency Vulnerability Scan
    runs-on: ubuntu-latest
    needs: build
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Cache Gradle
        uses: actions/cache@v4
        with:
          path: |
            ~/.gradle/caches
            ~/.gradle/wrapper
          key: gradle-${{ hashFiles('**/*.gradle*', '**/gradle-wrapper.properties', '**/libs.versions.toml') }}
          restore-keys: gradle-

      - name: Cache OWASP NVD data
        uses: actions/cache@v4
        with:
          path: ~/.gradle/dependency-check-data
          key: owasp-${{ hashFiles('**/libs.versions.toml') }}
          restore-keys: owasp-

      - name: Create local.properties
        run: echo "tmdb.api.token=${{ secrets.TMDB_API_TOKEN }}" > local.properties

      - name: Run OWASP dependency check
        run: ./gradlew dependencyCheckAnalyze --info
        continue-on-error: true

      - name: Upload OWASP report
        uses: actions/upload-artifact@v4
        with:
          name: owasp-report
          path: build/reports/dependency-check-report.html
```

- [ ] **Step 3: Verify workflow file syntax**

```bash
cat .github/workflows/ci.yml | python3 -c "import sys, yaml; yaml.safe_load(sys.stdin); print('YAML valid')"
```
Expected: `YAML valid`

- [ ] **Step 4: Commit workflow**

```bash
git add .github/workflows/ci.yml
git commit -m "ci: add GitHub Actions workflow — lint, test, build APK, OWASP security scan"
```

---

## Task 14: GitHub Repository Setup and Push

**Files:** None (git operations only)

- [ ] **Step 1: Create GitHub repository**

In your browser, go to `https://github.com/new`:
- Repository name: `CapstoneAndroidExpert`
- Visibility: **Public** (required for Dicoding submission)
- Do NOT initialize with README (project already has commits)
- Click **Create repository**

- [ ] **Step 2: Add remote and push**

```bash
git remote add origin https://github.com/YOUR_GITHUB_USERNAME/CapstoneAndroidExpert.git
git branch -M main
git push -u origin main
```

Replace `YOUR_GITHUB_USERNAME` with your actual GitHub username.

- [ ] **Step 3: Add TMDB_API_TOKEN secret to GitHub**

In browser:
1. Go to repo → **Settings** → **Secrets and variables** → **Actions**
2. Click **New repository secret**
3. Name: `TMDB_API_TOKEN`
4. Value: paste your TMDB Bearer token (same value from `local.properties`)
5. Click **Add secret**

- [ ] **Step 4: Verify CI pipeline runs**

Go to `https://github.com/YOUR_GITHUB_USERNAME/CapstoneAndroidExpert/actions`.

Wait for the triggered workflow to complete. All 4 jobs (`lint`, `test`, `build`, `security`) must show green checkmarks.

If `build` job fails: check ProGuard rules — add missing `-keep` or `-dontwarn` for any class the error mentions.

If `test` job fails: run `./gradlew testDebugUnitTest` locally to reproduce, fix the failing test.

- [ ] **Step 5: Copy CI link for Dicoding submission**

The link format is:
```
https://github.com/YOUR_GITHUB_USERNAME/CapstoneAndroidExpert/actions
```

Paste this in the **Catatan** field when submitting to Dicoding.

---

## Submission Checklist

Before submitting, verify all items:

**Pre-submit: Inspect Code cleanup**
- [ ] Android Studio → **Analyze** → **Inspect Code** (whole project)
- [ ] Fix any "Unused import" warnings: delete flagged import lines
- [ ] Fix any "Redundant SAM-constructor" or "Can be replaced with lambda" warnings
- [ ] Re-run Inspect Code — zero Performance + Security issues remain
- [ ] `git add -p && git commit -m "refactor: fix unused imports and Inspect Code warnings"`

**Mandatory criteria:**
- [ ] CI pipeline last run: all jobs pass (green)
- [ ] LeakCanary: run app on debug build, navigate all screens, 0 leaks in notification
- [ ] Android Studio → **Analyze** → **Inspect Code** → no Performance issues
- [ ] ProGuard: `isMinifyEnabled = true` in both `app` and `core` release build types
- [ ] Database: `DatabaseModule.kt` uses `SupportFactory` (SQLCipher)
- [ ] Certificate pinning: `CertificatePinner` in `NetworkModule.kt` + `network_security_config.xml`

**Catatan field content for Dicoding:**
```
CI Link: https://github.com/<username>/CapstoneAndroidExpert/actions

Teknik Keamanan:
1. ProGuard/R8 — app/build.gradle.kts (isMinifyEnabled=true)
2. Database Encryption (SQLCipher AES-256) — core/di/DatabaseModule.kt, core/utils/SecureKeyManager.kt
3. Certificate Pinning (OkHttp) — core/di/NetworkModule.kt
4. Network Security Config — app/src/main/res/xml/network_security_config.xml
5. Root Detection (RootBeer) — core/utils/RootDetectionUtil.kt, app/MainActivity.kt
6. BuildConfig API token (no hardcode) — core/build.gradle.kts, core/di/NetworkModule.kt
```
