# WikiTok - Последовательность открытия окна со статьей Википедии

## Обзор

Данный документ описывает подробную последовательность открытия окна со статьей Википедии в приложении WikiTok, включая все этапы от клика пользователя до отображения статьи.

## Архитектура приложения

WikiTok использует современную архитектуру Android с Jetpack Compose, Room для локальной базы данных, и Retrofit для работы с API Википедии.

### Ключевые компоненты:
- **UI Layer**: Jetpack Compose с навигацией
- **Data Layer**: Repository pattern с Room и Retrofit
- **Settings**: DataStore для хранения настроек пользователя
- **Navigation**: Compose Navigation с поддержкой аргументов

## Последовательность открытия статьи

### 1. Инициация открытия статьи

**Файл**: `app/src/main/java/com/example/wikitok/ui/ArticleCard.kt`

```kotlin
Box(
    Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
        .clickable { onOpen() }  // ← Клик пользователя
) {
    // Содержимое карточки
}
```

- Пользователь нажимает на карточку статьи
- Срабатывает модификатор `.clickable { onOpen() }`
- Вызывается обработчик `onOpen()`

### 2. Передача параметров

**Файл**: `app/src/main/java/com/example/wikitok/ui/FeedScreen.kt`

```kotlin
ArticleCard(
    a = article,
    onOpen = { openArticle(context, article.url, settings) },  // ← Вызов функции
    // другие параметры...
)
```

Передаются параметры:
- `context` - контекст приложения
- `article.url` - URL статьи Википедии
- `settings` - настройки приложения

### 3. Формирование URL статьи

**Файл**: `app/src/main/java/com/example/wikitok/data/WikiRepository.kt`

```kotlin
private fun WikiSummaryDto.toArticle(): Article {
    val encoded = try {
        java.net.URLEncoder.encode(title, Charsets.UTF_8.name()).replace('+', '_')
    } catch (_: Throwable) {
        title.replace(' ', '_')
    }
    val lang = java.util.Locale.getDefault().language.takeIf { it.isNotBlank() } ?: "ru"
    val link = "https://${lang}.wikipedia.org/wiki/${encoded}"
    return Article(
        id = title,
        title = title,
        description = description,
        extract = extract,
        imageUrl = thumbnail?.source,
        url = link  // ← Сформированный URL
    )
}
```

**Процесс формирования URL:**
1. Кодирование заголовка статьи в URL-формат
2. Определение языка по умолчанию (fallback на "ru")
3. Создание полного URL: `https://{lang}.wikipedia.org/wiki/{encoded_title}`

### 4. Проверка настроек и выбор способа открытия

**Файл**: `app/src/main/java/com/example/wikitok/ui/FeedScreen.kt`

```kotlin
private fun openArticle(context: android.content.Context, url: String, settings: com.wikitok.settings.Settings) {
    if (settings.customTabs) {
        // Custom Tabs (рекомендуется)
        val intent = androidx.browser.customtabs.CustomTabsIntent.Builder()
            .setShowTitle(true)
            .build()
        intent.launchUrl(context, Uri.parse(url))
    } else {
        // Внешнее приложение
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    }
}
```

### 5. Варианты открытия статьи

#### 5.1 Custom Tabs (Рекомендуется)

**Когда**: `settings.customTabs = true` (по умолчанию)

**Преимущества**:
- Быстрая загрузка
- Нативный интерфейс
- Поддержка жестов
- Безопасность

**Реализация**:
```kotlin
val intent = androidx.browser.customtabs.CustomTabsIntent.Builder()
    .setShowTitle(true)
    .build()
intent.launchUrl(context, Uri.parse(url))
```

#### 5.2 Внешнее приложение

**Когда**: `settings.customTabs = false`

**Поведение**:
- Открывается системный браузер по умолчанию
- Или приложение, выбранное пользователем для URL

**Реализация**:
```kotlin
val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
context.startActivity(intent)
```

#### 5.3 Встроенный WebView

**Маршрут**: `"webview?url={url}"`

**Файл**: `app/src/main/java/com/example/wikitok/ui/NavGraph.kt`

```kotlin
composable(
    route = "webview?url={url}",
    arguments = listOf(navArgument("url") { type = NavType.StringType })
) { backStackEntry ->
    val raw = backStackEntry.arguments?.getString("url").orEmpty()
    val decoded = try { 
        java.net.URLDecoder.decode(raw, Charsets.UTF_8.name()) 
    } catch (_: Throwable) { raw }
    WebViewScreen(url = decoded, modifier = Modifier.fillMaxSize())
}
```

### 6. Настройки WebView

**Файл**: `app/src/main/java/com/example/wikitok/ui/WebViewScreen.kt`

```kotlin
@Composable
fun WebViewScreen(url: String, modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                settings.apply {
                    javaScriptEnabled = true      // JavaScript включен
                    domStorageEnabled = true      // DOM Storage включен
                    useWideViewPort = true        // Wide ViewPort включен
                    loadWithOverviewMode = true   // Обзорный режим
                    builtInZoomControls = true    // Zoom контролы встроены
                    displayZoomControls = false   // Zoom контролы скрыты
                }
                loadUrl(url)
            }
        },
        update = { webView ->
            if (webView.url != url) {
                webView.loadUrl(url)
            }
        }
    )
}
```

## Управление настройками

### Структура настроек

**Файл**: `app/src/main/java/com/wikitok/settings/SettingsRepository.kt`

```kotlin
data class Settings(
    val theme: AppTheme = AppTheme.SYSTEM,
    val customTabs: Boolean = true,        // ← Настройка способа открытия
    val wikiLang: String = "ru",
    val autoScroll: Boolean = false,
    val saveHistory: Boolean = true
)
```

### Интерфейс настроек

**Файл**: `app/src/main/java/com/wikitok/settings/SettingsScreen.kt`

```kotlin
SettingSwitch(
    "Открывать в Custom Tabs", 
    state.customTabs, 
    vm::onCustomTabsChange, 
    "Рекомендовано"
)
```

### Сохранение настроек

**Файл**: `app/src/main/java/com/wikitok/settings/SettingsRepository.kt`

```kotlin
suspend fun setCustomTabs(v: Boolean) = context.dataStore.edit { 
    it[Keys.CUSTOM_TABS] = v 
}
```

## Диаграмма потока

```
┌─────────────────┐
│   Пользователь  │
│   кликает на    │
│   карточку      │
└─────────┬───────┘
          │
          ▼
┌─────────────────┐
│  ArticleCard    │
│  .clickable     │
│  { onOpen() }   │
└─────────┬───────┘
          │
          ▼
┌─────────────────┐
│   FeedScreen    │
│ openArticle(    │
│   context,      │
│   article.url,  │
│   settings)     │
└─────────┬───────┘
          │
          ▼
┌─────────────────┐
│  Проверка       │
│  settings.      │
│  customTabs     │
└─────────┬───────┘
          │
          ▼
    ┌─────────┐
    │  true?  │
    └────┬────┘
         │
    ┌────▼────┐        ┌──────────────┐
    │   ДА    │        │     НЕТ      │
    │         │        │              │
    ▼         │        ▼              │
┌─────────────┐        │              │
│CustomTabs   │        │              │
│Intent       │        │              │
│.launchUrl() │        │              │
└─────────────┘        │              │
                       │              │
                       ▼              │
              ┌─────────────────┐     │
              │Intent.ACTION_   │     │
              │VIEW             │     │
              │.startActivity() │     │
              └─────────────────┘     │
                                     │
                                     ▼
                            ┌─────────────────┐
                            │   Внешнее       │
                            │   приложение    │
                            │   (браузер)     │
                            └─────────────────┘
```

## Ключевые файлы проекта

| Файл | Описание |
|------|----------|
| `ArticleCard.kt` | Обработка клика пользователя |
| `FeedScreen.kt` | Функция `openArticle()` и логика выбора способа открытия |
| `WikiRepository.kt` | Формирование URL статьи |
| `SettingsRepository.kt` | Управление настройкой `customTabs` |
| `WebViewScreen.kt` | Альтернативный способ через встроенный WebView |
| `NavGraph.kt` | Маршрут для WebView |
| `SettingsScreen.kt` | Интерфейс настроек |

## Заключение

Приложение WikiTok предоставляет гибкие возможности открытия статей Википедии:

1. **Custom Tabs** (рекомендуется) - быстрая загрузка с нативным интерфейсом
2. **Внешние приложения** - использование системного браузера
3. **Встроенный WebView** - отображение внутри приложения

Пользователь может легко переключаться между режимами через настройки приложения. По умолчанию используется Custom Tabs как наиболее оптимальный вариант для мобильных устройств.

## Технические детали

- **Язык**: Kotlin
- **UI Framework**: Jetpack Compose
- **Навигация**: Compose Navigation
- **Настройки**: DataStore Preferences
- **Сеть**: Retrofit + OkHttp
- **База данных**: Room
- **Архитектура**: MVVM с Repository pattern
