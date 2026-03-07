## Обзор проекта

Это многомодульный Android‑проект на Kotlin с Jetpack Compose и Material3, построенный по принципам Clean Architecture и MVI. Проект использует Koin для DI, Ktor для сети, Room для БД, DataStore для локальных настроек и type‑safe Navigation Compose на базе `kotlinx.serialization`. Сборка и зависимость модулей стандартизированы через `build-logic` (convention plugins) и `libs.versions.toml` (Version Catalog), что упрощает поддержку и добавление новых модулей.

Основная цель архитектуры — сделать код легко расширяемым, тестируемым и понятным для любого нового разработчика в команде.

---

## Структура модулей

Высокоуровневая схема:

```text
app
├── core
│   ├── navigation
│   ├── ui
│   └── common
├── data
│   ├── network
│   ├── database
│   └── repository
├── domain
└── feature
    ├── auth
    ├── register
    └── ... (home, profile, etc.)
```

Роли модулей:

- **`:app`**: точка входа приложения, инициализация Koin, создание `NavHost` (главный граф навигации), сбор всех модулей.
- **`:build-logic`**: набор convention plugins (precompiled script plugins) для Gradle — стандартизует конфигурацию Android/Kotlin модулей.
- **`:core:navigation`**: объявление всех маршрутов навигации (`sealed interface Route`), type‑safe routes через `kotlinx.serialization`.
- **`:core:ui`**: общая тема, Material3, дизайн‑система, переиспользуемые Compose‑компоненты.
- **`:core:common`**: утилиты, extensions, общие типы, helpers для coroutines, `kotlinx-datetime` и т.п.
- **`:data:network`**: конфигурация Ktor `HttpClient`, API‑сервисы, DTO для сети.
- **`:data:database`**: Room (entities, DAO, миграции, конфигурация Room Gradle plugin).
- **`:data:repository`**: реализации интерфейсов репозиториев из `:domain`, объединяющие сеть/БД/кеш.
- **`:domain`**: доменные модели, интерфейсы репозиториев, use case‑ы; не зависит от Android/Compose.
- **`:feature:*`**: независимые фиче‑модули (экраны, flows): `feature:home`, `feature:profile`, `feature:auth`, `feature:register` и т.д.

Ключевая идея: **feature‑модули зависят только от core + domain + data**, но не знают друг о друге напрямую.

---

## Build-logic и convention plugins

### Зачем нужен `build-logic`

Многомодульный проект быстро зарастает дублирующимися `build.gradle.kts` — одинаковые настройки `android`, `composeOptions`, `kotlinOptions`, зависимости Koin/Coroutines/Navigation и т.д.  
`build-logic` решает это через **precompiled script plugins**:

- все общие настройки Android/Kotlin вынесены в плагины;
- новые модули подключают один‑два плагина вместо копипасты;
- легко менять версию Java/Compose/AGP централизованно.

Примеры convention plugins (имена абстрактные, подстроены под проект):

- `android.fruits.application` — Android application + Kotlin + общие зависимости;
- `android.fruits.library` — Android library + Kotlin + общие зависимости;
- `android.fruits.compose` — включает Compose, BOM, базовый набор UI зависимостей;
- `android.fruits.feature` — shortcut для UI‑фичи: `library + compose + Koin + navigation`;
- `android.fruits.test` — базовый набор тестовых зависимостей;
- `tech.fruits.koin` — зависимости Koin;
- `tech.fruits.ktor` — зависимости Ktor‑клиента;
- `tech.fruits.room` — Room + KSP + настройка `schemaDirectory(...)`;
- `tech.fruits.datastore` — DataStore + Coroutines.

### Какой plugin взять для нового модуля

Типичные варианты:

- **Application (только один модуль)**  
  `:app`:

```kotlin
plugins {
    id("android.fruits.application")
    id("android.fruits.compose")
    id("android.fruits.test")
}
```

- **UI‑фича (экран / flow)**  
  `:feature:home`, `:feature:profile`, `:feature:auth` и т.п.:

```kotlin
plugins {
    id("android.fruits.feature")   // library + compose + Koin + navigation helpers
    id("android.fruits.test")
}
```

- **Библиотека домена / общая логика (без UI)**  
  `:domain`, `:core:common`:

```kotlin
plugins {
    id("android.fruits.library")
    id("tech.fruits.koin")        // опционально, если модуль объявляет свои DI-модули
    id("android.fruits.test")
}
```

- **Network‑модуль** (`:data:network`):

```kotlin
plugins {
    id("android.fruits.library")
    id("tech.fruits.ktor")
    id("tech.fruits.koin")
    id("android.fruits.test")
}
```

- **Database‑модуль** (`:data:database`):

```kotlin
plugins {
    id("android.fruits.library")
    id("tech.fruits.room")
    id("tech.fruits.koin")
    id("android.fruits.test")
}
```

Таким образом, при добавлении модуля достаточно выбрать правильный набор плагинов, а не ручками настраивать AGP/Kotlin/Compose.

---

## Version Catalog (`libs.versions.toml`)

Все версии библиотек и плагины Gradle вынесены в `gradle/libs.versions.toml`:

- секция `[versions]` — номера версий (`kotlin`, `agp`, `composeBom`, `koin`, `ktor`, `room`, …);
- секция `[libraries]` — зависимости (`core-ktx`, `navigation-compose`, `compose-material3`, …);
- секция `[bundles]` — готовые наборы для convention plugins (`compose-core`, `ktor-client`, `koin-android-full`, …);
- секция `[plugins]` — id + версия для Gradle plugins (`android-application`, `kotlin-android`, `ksp`, `room`, …).

**Правило:** новая зависимость добавляется только через `libs.versions.toml` и используется как `libs.some.library` или `libs.bundles.someBundle` в Gradle.

---

## Архитектура и зависимости модулей

### Однонаправленные зависимости

Схема:

```text
feature:*   ──►  domain  ◄──  data:repository  ◄──  data:network / data:database
    │                    ▲
    └──────► core:* ◄────┘
```

- **feature → domain**: UI зависит от доменных моделей и use case‑ов.
- **data → domain**: реализации репозиториев живут в data и реализуют интерфейсы из domain.
- **feature не зависит от других feature**: обмен между экранами только через навигацию и общие core‑модули.

Это даёт:

- независимую разработку фич;
- возможность переиспользования домена в других клиентах;
- минимальные циклические зависимости.

---

## Навигация: Route + extension‑функции

### Route в `:core:navigation`

В `:core:navigation` хранится **единственный источник правды** по маршрутам:

```kotlin
package com.fruits.navigation

import kotlinx.serialization.Serializable

sealed interface Route {

    @Serializable
    data object Auth : Route

    @Serializable
    data object Register : Route

    // Примеры:
    // @Serializable data object Home : Route
    // @Serializable data class Details(val id: Int) : Route
}
```

Особенности:

- `sealed interface Route` — список всех возможных направлений навигации.
- Каждый маршрут помечен `@Serializable`, что позволяет использовать type‑safe navigation (`composable<Route.X>`).

### Extension‑функции в фичах

Каждая фича определяет свою функцию расширения для `NavGraphBuilder`:

```kotlin
// feature:auth
fun NavGraphBuilder.authScreen(
    onNavigateToRegister: () -> Unit,
) {
    composable<Route.Auth> {
        AuthRoute(
            onNavigateToRegister = onNavigateToRegister,
        )
    }
}
```

```kotlin
// feature:register
fun NavGraphBuilder.registerScreen(
    onNavigateBackToAuth: () -> Unit,
) {
    composable<Route.Register> {
        RegisterRoute(
            onNavigateBackToAuth = onNavigateBackToAuth,
        )
    }
}
```

Фича **не знает** ни про `NavHostController`, ни про другие фичи — только про свой маршрут и callback-и.

### Главный граф в `:app`

В `:app` собирается главный граф:

```kotlin
@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = Route.Auth,
    ) {
        authScreen(
            onNavigateToRegister = {
                navController.navigate(Route.Register)
            },
        )

        registerScreen(
            onNavigateBackToAuth = {
                navController.popBackStack()
            },
        )
    }
}
```

Инициализация в `MainActivity`:

```kotlin
setContent {
    AppTheme {
        AppNavGraph()
    }
}
```

**Важно:** только `:app` знает обо всех фичах и решает, какие маршруты куда ведут.

---

## DI: Koin и модульность

### Общий принцип

- Каждый модуль (data/domain/feature) объявляет **свой** Koin‑module (`Module`).
- В `:app` есть точка сборки (например, `KoinModules.kt`), где мы объединяем их в один список и передаём в `startKoin`.

Пример Koin‑модулей (абстрактно):

```kotlin
// domain
val domainModule = module {
    factory { GetItemsUseCase(repository = get()) }
}

// data:repository
val repositoryModule = module {
    single<ItemRepository> { ItemRepositoryImpl(api = get(), dao = get()) }
}

// feature:home
val homeFeatureModule = module {
    viewModel { HomeViewModel(getItems = get()) }
}
```

В `:app`:

```kotlin
fun initKoin() = startKoin {
    androidContext(app)
    modules(
        domainModule,
        repositoryModule,
        homeFeatureModule,
        // + другие модули
    )
}
```

**Правило:** фича‑модуль объявляет зависимости только для своих ViewModel и локальной логики, не лезет в чужие фичи.

---

## MVI: Intent → ViewModel (Reducer) → State / Effect → UI

### Базовый паттерн

Для экранов используется **MVI (Model–View–Intent)** поверх UDF:

- UI генерирует **Intent** (в коде — `HomeEvent`), описывающий намерение пользователя.
- ViewModel выступает как **Reducer**: получает Intent, вычисляет новый **State** и, при необходимости, эмитит **Effect**.
- UI подписывается на `State` (через `StateFlow`) и однонаправленно его отображает.
- Одноразовые события (навигация, снэкбары) идут через отдельный поток **Effect**, а не через `State`.

Терминология:

- `HomeEvent` — Intent;
- `HomeState` — Model/State;
- `HomeEffect` — Side Effect (одноразовое событие).

Пример (условный `HomeViewModel`):

```kotlin
data class HomeState(
    val isLoading: Boolean = false,
    val items: List<ItemUiModel> = emptyList(),
    val error: String? = null,
)

sealed interface HomeEvent { // Intent
    data object Refresh : HomeEvent
    data class ItemClicked(val id: String) : HomeEvent
}

sealed interface HomeEffect { // Side Effect
    data class NavigateToDetails(val id: String) : HomeEffect
    data object ShowErrorSnackbar : HomeEffect
}

class HomeViewModel(
    private val getItems: GetItemsUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    private val _effect = Channel<HomeEffect>(Channel.BUFFERED)
    val effect: Flow<HomeEffect> = _effect.receiveAsFlow()

    fun onEvent(intent: HomeEvent) {
        when (intent) {
            HomeEvent.Refresh -> loadItems()
            is HomeEvent.ItemClicked -> sendEffect(HomeEffect.NavigateToDetails(intent.id))
        }
    }

    private fun loadItems() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            // ...
            // при ошибке:
            // sendEffect(HomeEffect.ShowErrorSnackbar)
        }
    }

    private fun sendEffect(effect: HomeEffect) {
        viewModelScope.launch {
            _effect.send(effect)
        }
    }
}
```

В UI:

```kotlin
@Composable
fun HomeRoute(
    viewModel: HomeViewModel = koinViewModel(),
    onNavigateToDetails: (String) -> Unit,
    onShowSnackbar: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val effect by viewModel.effect.collectAsStateWithLifecycle(initialValue = null)

    // Обработка одноразовых эффектов
    LaunchedEffect(effect) {
        when (val e = effect) {
            is HomeEffect.NavigateToDetails -> onNavigateToDetails(e.id)
            HomeEffect.ShowErrorSnackbar -> onShowSnackbar()
            null -> Unit
        }
    }

    HomeScreen(
        state = state,
        onEvent = viewModel::onEvent,
    )
}
```

**Почему отдельный Effect‑канал, а не `UiState`?**

- `State` описывает **постоянное** состояние экрана (что можно восстановить после рекомпозиции/ротации).
- Навигация, тосты, снэкбары, one‑shot диалоги — **одноразовые события**, их нельзя хранить в `State` без риска многократного повторения.
- Канал `Effect` гарантирует, что каждый Side Effect будет обработан один раз, даже при множественных рекомпозициях.

---

## Single source of truth: Repository и Flow

### Repository как единственный источник данных

Репозитории живут в `:data:repository` и реализуют интерфейсы из `:domain`:

```kotlin
// domain
interface ItemRepository {
    fun observeItems(): Flow<List<Item>>
    suspend fun refresh()
}
```

```kotlin
// data:repository
class ItemRepositoryImpl(
    private val api: ItemsApi,
    private val dao: ItemDao,
) : ItemRepository {

    override fun observeItems(): Flow<List<Item>> =
        dao.observeItems().map { it.toDomain() }

    override suspend fun refresh() {
        val remote = api.getItems()
        dao.replaceAll(remote.toEntities())
    }
}
```

UI **никогда напрямую** не ходит в БД/сеть — только через use case‑ы/репозитории из `:domain`.

---

## Соглашения по именованию

- **Пакеты** — в единственном числе: `feature.home`, `data.network`, `data.database`, `core.navigation`.
- **ViewModel** — `SomethingViewModel` (фича: `HomeViewModel`, `ProfileViewModel`).
- **Состояние** — `SomethingState` (`HomeState`).
- **События** — `SomethingEvent` или `SomethingAction` (`HomeEvent`).
- **Эффекты** (опционально) — `SomethingEffect` (`HomeEffect`).
- **Use case** — глагол в начале: `GetItemsUseCase`, `UpdateProfileUseCase`.
- **Repository** — `SomethingRepository` + `SomethingRepositoryImpl` в data.
- **Фичи** — модуль `feature:home` + корневой пакет `com.example.feature.home` (в проекте — свой `applicationId`).
- **Gradle файлы** — `build.gradle.kts` в каждом модуле, минимальное количество конфигурации; логика — в `build-logic`.

---

## Git-процесс

Проект рассчитан на команду из ~2 разработчиков и использует упрощённый **Git Flow**.

### Ветки

- **`main`** — защищённая ветка, всегда в стабильном состоянии (релизы / демо‑сборки).
- **`dev`** — общая ветка разработки; сюда мёржатся feature‑ветки после ревью.
- **`feature/*`** — ветка под каждую задачу:
  - `feature/auth-screen`
  - `feature/home-screen`
  - `fix/login-crash`
  - `refactor/build-logic`

### Жизненный цикл feature‑ветки

1. Обновить локальный `dev`:
   ```bash
   git checkout dev
   git pull origin dev
   ```
2. Создать ветку под задачу:
   ```bash
   git checkout -b feature/home-screen
   ```
3. Работать, делать коммиты.
4. Запушить ветку:
   ```bash
   git push -u origin feature/home-screen
   ```
5. Создать PR из `feature/home-screen` в `dev`.
6. Пройти ревью, поправить замечания (дополнительные коммиты в ту же ветку).
7. После approve — мерж в `dev` (через GitHub).
8. Удалить ветку (`Delete branch` в GitHub или `git branch -d feature/home-screen` локально).

### Соглашение по коммитам (Conventional Commits)

Используем упрощённый формат:

- `feat: описание` — новая функциональность;
- `fix: описание` — исправление бага;
- `refactor: описание` — рефакторинг без изменения поведения;
- `chore: описание` — инфраструктура, скрипты, конфиг;
- `docs: описание` — изменения в документации;
- по желанию: `test: ...`, `build: ...`.

Примеры:

- `feat: add home screen navigation`
- `fix: handle null token in auth interceptor`
- `refactor: extract base repository`

### Синхронизация с напарником (rebase на dev)

Перед тем как продолжать работу или открывать PR:

```bash
git checkout dev
git pull origin dev        # забрать последние изменения

git checkout feature/home-screen
git rebase dev             # накатить изменения dev поверх своей ветки
```

Если есть конфликты:

1. Исправить их в файлах.
2. `git add <file>` для каждого файла.
3. `git rebase --continue`.

После успешного rebase:

```bash
git push --force-with-lease
```

Rebase даёт **линейную историю**, упрощая чтение и поиск регрессий.

### Конфликты и многомодульная архитектура

Конфликты чаще всего возникают, когда:

- два человека редактируют один и тот же файл (или строку) в разных ветках;
- вносятся массовые изменения в общие модули (`core:*`, `domain`, `build-logic`).

Многомодульная архитектура это минимизирует:

- большинство задач касается **локальных фич** (`feature:*`) или отдельных data‑модулей;
- каждый разработчик чаще работает в своём модуле и не задевает чужие файлы;
- изменения в `build-logic` и `core` планируются отдельно и реже.

Тем не менее, изменения в общих слоях (например, изменение сигнатуры use case‑а в `:domain`) должны быть согласованы заранее.

### Рекомендуемые настройки GitHub

- **Branch protection для `main`**:
  - запрет прямых пушей (только через PR);
  - обязательный минимум: 1 review;
  - опционально: требование успешного CI (сборка / тесты).
- Для `dev` можно оставить более мягкие правила (например, разрешить мердж без review в форс‑мажоре), но в идеале — тоже через PR.

---

## Как добавить новую фичу (чеклист)

Пример: хотим добавить `feature:home`.

1. **Создать модуль**
   - Добавь модуль `feature:home` через Android Studio или вручную.
   - В `settings.gradle.kts`:

     ```kotlin
     include(":feature:home")
     ```

   - В `feature/home/build.gradle.kts`:

     ```kotlin
     plugins {
         id("android.fruits.feature")
         id("android.fruits.test")
     }

     android {
         namespace = "com.fruits.feature.home"
     }

     dependencies {
         implementation(projects.core.navigation)
         // при необходимости: implementation(projects.domain), implementation(projects.data.repository)
     }
     ```

2. **Определить маршрут**
   - В `:core:navigation` добавить новый маршрут:

     ```kotlin
     @Serializable
     data object Home : Route
     ```

3. **Создать ViewModel и UDF**
   - В `feature:home` создать `HomeState`, `HomeEvent`, `HomeViewModel`.

4. **Экран и Route**
   - `HomeRoute` + `HomeScreen` на Compose.

5. **Навигационный экстеншен**

   ```kotlin
   fun NavGraphBuilder.homeScreen(
       onNavigateToDetails: (id: String) -> Unit,
   ) {
       composable<Route.Home> {
           HomeRoute(
               onNavigateToDetails = onNavigateToDetails,
           )
       }
   }
   ```

6. **Подключить в `AppNavGraph`**

   ```kotlin
   NavHost(
       navController = navController,
       startDestination = Route.Home,
   ) {
       homeScreen(
           onNavigateToDetails = { id ->
               navController.navigate(Route.Details(id))
           },
       )
       // другие фичи
   }
   ```

7. **DI**
   - В модуле объявить Koin‑модуль для ViewModel/use case‑ов.
   - Зарегистрировать модуль в `:app` при инициализации Koin.

---

## Быстрый старт (первые 30 минут)

1. **Собери проект**
   - Запусти `./gradlew assembleDebug` или через Android Studio — убедись, что всё собирается.

2. **Посмотри точки входа**
   - `:app`:
     - `MainActivity` — точка входа.
     - `AppNavGraph` — главный навигационный граф.
     - Файл инициализации Koin (например, `KoinModules.kt`).

3. **Пойми архитектуру модулей**
   - Просмотри `settings.gradle.kts` и структуру `core/*`, `data/*`, `domain`, `feature/*`.
   - Обрати внимание, что фичи не зависят друг от друга.

4. **Изучи одну фичу от и до**
   - Например, `feature:auth`:
     - `*State`, `*Event`, `*ViewModel`, `*Screen`, `*Navigation`.
     - Проверь, как фича подключена в `AppNavGraph`.

5. **Посмотри Version Catalog и build-logic**
   - `gradle/libs.versions.toml` — какие версии библиотек используются.
   - `build-logic` — как устроены convention plugins (чтобы понимать, что за тебя уже настроено).

После этого у тебя будет базовое понимание архитектуры, и можно спокойно браться за задачи.

---

## FAQ (частые вопросы)

**Q: Почему так много модулей, нельзя ли всё сделать в `:app`?**  
**A:** Многомодульность упрощает параллельную разработку, ускоряет сборку (через Gradle конфигурацию и кэш), и жёстко отделяет UI, домен и данные. Это делает код проще для рефакторинга и тестирования.

**Q: Как понять, в какой модуль класть новый код?**  
**A:** Условно:  
- UI/экраны → `feature:*`;  
- бизнес‑логика/правила → `:domain`;  
- доступ к сети/БД → `:data:*`;  
- общие утилиты/тема → `:core:*`;  
- всё, что касается сборки и плагинов Gradle → `:build-logic`.

**Q: Где объявлять новые маршруты навигации?**  
**A:** Только в `:core:navigation` в `Route`. Фича использует свой маршрут через `composable<Route.X>` и не знает про другие фичи.

**Q: Как добавить новую зависимость (библиотеку)?**  
**A:** Добавь её в `gradle/libs.versions.toml` (в нужную секцию), затем используй через `libs.*` или `libs.bundles.*` в Gradle‑скриптах. Не хардкодь версии в `build.gradle.kts`.

**Q: Где и как настраивается DI?**  
**A:** Каждый модуль объявляет Koin‑модуль, `:app` собирает всё в одном месте и вызывает `startKoin`. ViewModel‑ы и use case‑ы получают зависимости через `get()` / конструктор.

**Q: Почему навигация сделана через sealed `Route`, а не через строковые пути?**  
**A:** Sealed + `@Serializable` дают type‑safe навигацию: маршрут — это тип, а не строка; проще рефакторинг, меньше ошибок, удобно пробрасывать параметры.

**Q: Можно ли рефакторить фичу, не ломая остальные модули?**  
**A:** Да. Пока публичный контракт (маршрут в `Route`, публичные функции в domain/data) остаётся тем же, можно менять внутреннюю реализацию фичи без влияния на другие части системы.

