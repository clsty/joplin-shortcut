# Joplin Shortcut

An Android app that lets you browse, manage, and create shortcuts and home screen widgets for your [Joplin](https://joplinapp.org/) notes.

## Features

- **Browse notes and notebooks** via the Joplin Web Clipper REST API
- **Favorites** — organize notes and notebooks into named lists
- **Home screen widgets** — display a list of notes from a notebook or favorites list
- **Launcher shortcuts** — pin individual notes or notebooks to the home screen
- **Background sync** — periodic sync via WorkManager
- **Hide/restore items** — hide notes or notebooks from the main view
- **Split-pane UI** — adaptive layout (auto/vertical/horizontal)
- **i18n** — English and Simplified Chinese (zh-rCN)

## Requirements

- Android 8.0+ (API 26+)
- Joplin desktop/mobile with Web Clipper enabled (default port 41184)

## Setup

1. Open the app and go to **Settings → API Configurations**
2. Tap **Add Configuration**
3. Enter a name, the base URL (e.g. `http://192.168.1.100:41184`), and your API token
4. Tap **Test** to verify the connection, then **Sync** to fetch your notes

## Building

```bash
./gradlew assembleDebug
```

## License

GPL-3.0 — see [LICENSE](LICENSE)
