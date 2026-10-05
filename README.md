# Product Catalog

An Android app that browses products from the [DummyJSON Products API](https://dummyjson.com/docs/products), with live search, a product details screen, and a shopping cart that is stored locally and works fully offline.

## Features

- **Product list** – image, name, price and rating, with loading, empty, error and retry states.
- **Search** – results update as you type (debounced by 400 ms). Clearing the query shows the full list again.
- **Product details** – image, name, description, price, rating, category, brand, stock and an "Add to cart" button.
- **Cart** – add, increase or decrease the quantity (decreasing at 1 removes the item), remove an item, and see the total item count and total price. Adding a product that is already in the cart increases its quantity.
- **Offline cart** – the cart is saved in Room, survives app restarts and never needs the network.
- **Cart badge** – a cart icon with an item count in the product list's top bar. It stays reachable even when the product list fails to load.

## Setup and build

**Requirements**

- Android Studio Meerkat (2024.3.1) or newer, needed for Android Gradle Plugin 8.9, or the Android SDK with platform 35 installed
- JDK 17 (the one bundled with Android Studio works)
- An emulator or device running Android 7.0 (API 24) or higher

**Build and run from the terminal**

```bash
# Windows
gradlew.bat installDebug

# macOS / Linux
./gradlew installDebug
```

`assembleDebug` builds the APK without installing it. The APK is written to `app/build/outputs/apk/debug/`.

You can also open the project in Android Studio and press **Run**. No API key is needed.

## Architecture

The app uses **MVVM** with a one-way data flow:

```
Compose Screen  ──events──▶  ViewModel  ──▶  Repository  ──▶  Retrofit API (products)
      ▲                         │                    └─────▶  Room DAO     (cart)
      └──── StateFlow<UiState> ─┘
```

- **Screens** (Jetpack Compose) only draw the current state and forward user actions to the ViewModel.
- **ViewModels** expose a single `StateFlow` of a sealed `UiState` (for example `Loading`, `Success`, `Empty` and `Error`). Each screen uses a `when` to show exactly one state.
- **Repositories** are the only classes that talk to data sources:
  - `ProductRepository` wraps every network call in `safeApiCall`, which returns `AppResult.Success` or `AppResult.Error` with a user-friendly message.
  - `CartRepository` works only with Room.
- **Dependency injection** is done by hand. `AppContainer` creates the database, the Retrofit client and the repositories once (lazily). `ProductCatalogApplication` owns the container, and each ViewModel gets its dependencies through a small `viewModelFactory`.

### Project structure

```
com.surya.productcatalog
├── ProductCatalogApplication.kt   Creates AppContainer at app start
├── AppContainer.kt                Manual DI: database, API, repositories
├── MainActivity.kt                Hosts the Compose navigation graph
├── data
│   ├── AppResult.kt               Success/Error result + exception → message mapping
│   ├── ProductRepository.kt
│   ├── CartRepository.kt
│   ├── model/Product.kt           Clean domain model + DTO mapper
│   ├── remote/                    ProductApi, NetworkModule (Retrofit/OkHttp), DTOs
│   └── local/                     CartItemEntity, CartDao, AppDatabase (Room)
└── ui
    ├── navigation/AppNavHost.kt   Routes: products, products/{productId}, cart
    ├── productlist/               Screen, ViewModel, UiState (list + search + badge)
    ├── productdetail/             Screen, ViewModel, UiState (details + add to cart)
    ├── cart/                      Screen, ViewModel, UiState (quantities + totals)
    ├── components/                Shared loading/empty/error views, price formatting
    └── theme/                     Material 3 theme
```

## Libraries

| Library | Used for |
|---|---|
| Kotlin 2.0.21, Coroutines + Flow | Language, async work, reactive streams |
| Jetpack Compose + Material 3 | UI |
| Lifecycle ViewModel / runtime-compose | ViewModels and `collectAsStateWithLifecycle` |
| Navigation Compose 2.8.9 | Moving between the three screens |
| Retrofit 2.11 + Gson converter | REST API calls and JSON parsing |
| OkHttp 4.12 logging interceptor | Request and response logging (debug builds only), 15 s timeouts |
| Room 2.6.1 (with KSP) | Local cart database |
| Coil 2.7 | Loading and caching images |

## Local storage

The cart is a single Room table, `cart_items`:

| Column | Notes |
|---|---|
| `product_id` | **Primary key**, so a product can never appear twice |
| `title`, `price`, `thumbnail` | A snapshot of the product taken when it was added |
| `quantity` | Always ≥ 1 |
| `added_at` | Keeps the items in the order they were added |

- **Offline by design.** The cart stores its own copy of the product data it needs, so the cart screen never calls the API.
- **Reactive totals.** `CartDao` exposes the items, `SUM(quantity)` and `SUM(price * quantity)` as `Flow`s. Room re-emits them whenever the table changes, so the cart screen and the badge update automatically.
- **Safe updates.** "Add to cart" (insert or increase) and "decrease" (decrease or delete at 1) are each a single `@Transaction`, so rapid taps can't produce duplicate rows or negative quantities.

## Design decisions

- **Search cancellation.** The query flow goes through `debounce` → `distinctUntilChanged` → `flatMapLatest`. `flatMapLatest` cancels the request for an older query when a new one arrives, so a slow, outdated response can never overwrite newer results. An empty query skips the debounce, so the first load and clearing the search are instant.
- **Search text held as Compose state.** The search text is kept in `mutableStateOf` inside the ViewModel and turned into a Flow with `snapshotFlow`. A `TextField` needs synchronous updates; driving it from an asynchronously collected `StateFlow` can make the cursor jump.
- **Error mapping.** Exceptions are mapped in this order:
  - `SocketTimeoutException` → "Request timed out". This check comes first because it is a subclass of `IOException`.
  - `IOException` → "No internet connection".
  - `HttpException` → "Server error, please try again".
  - Anything else → a generic message.

  `CancellationException` is re-thrown, so cancelled searches are not shown as errors.
- **Nullable DTOs.** Gson creates objects without Kotlin's null checks, so every DTO field that could be missing is nullable. `toProduct()` replaces missing values with safe defaults. `brand` stays nullable because it is genuinely absent for some products; the UI shows "Not specified".
- **The list survives navigation.** The product list `StateFlow` uses `SharingStarted.Eagerly`, so returning from the details screen keeps the results and scroll position instead of reloading.
- **Edge-to-edge.** Bottom bars use `navigationBarsPadding()` so buttons are never hidden behind the system navigation bar.
- **Dependency versions.** The Android Studio template pinned `core-ktx 1.18`, `activity-compose 1.13` and lifecycle `2.10`. These require AGP 8.9.1 and compile SDK 36, so the template did not build. They were lowered to the latest versions compatible with AGP 8.9.0 and SDK 35. KSP `2.0.21-1.0.28` matches the Kotlin version.

## Known limitations

- **No pagination.** The list requests all products at once (`limit=0`, about 190 items). That is fine for DummyJSON, but a larger catalog would need Paging 3.
- **The product list needs the network.** Only the cart is offline. Products are not cached in Room, so the list and details screens show an error with Retry when offline. Images that Coil has already cached may still appear in the cart.
- **Cart prices are a snapshot.** If a product's price changes on the server, items already in the cart keep the price they were added at.
- **Prices use `Double`.** This is acceptable for display, but a real store should use integer cents or `BigDecimal` to avoid rounding errors.
- **No stock limit.** The quantity can go above the product's stock, and out-of-stock products can still be added.
- **No automated tests.** The repositories and ViewModels take their dependencies through the constructor, so unit tests with fakes would be straightforward to add.
- **The categories endpoint is unused.** `ProductApi.getCategories()` exists, but there is no category filter in the UI.
