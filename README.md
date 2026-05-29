# Discord Link Generator

A Discord bot that automatically uploads text and log files to **mclo.gs** or **Pastebin**, designed for server administrators and developers who frequently share diagnostic logs.

Originally developed for **Amethyst Development** to streamline log sharing across their community.

## Features

- **`/upload` slash command** — Upload any text/log file to mclo.gs or Pastebin
- **Auto-upload** — Designate channels where file attachments are uploaded automatically
- **Fallback system** — If mclo.gs is unavailable, falls back to Pastebin
- **Rich embeds** — Clean Discord embed responses with file info and upload links
- **Broad format support** — `.log`, `.txt`, `.yml`, `.json`, `.cfg`, `.java`, `.py`, `.js`, `.md`, and more

## Usage

### Slash Command

```
/upload file:<attachment> [service:pastebin]
```

- `file` — The file to upload (required)
- `service` — Upload target: `mclogs` (default) or `pastebin`

### Auto-Upload

List channel IDs in `autoUploadChannels` within `config.json`. Any text/log file sent in those channels will be automatically uploaded and linked.

## Configuration

| Variable | Description |
|---|---|
| `BOT_TOKEN` | Discord bot token |
| `PASTEBIN_API_KEY` | Pastebin API developer key |

Configuration is read from environment variables first, falling back to `config.json`:

```json
{
  "token": "YOUR_BOT_TOKEN",
  "pastebinApiKey": "YOUR_PASTEBIN_API_KEY",
  "autoUploadChannels": ["channel_id_1"]
}
```

## Build & Run

```bash
# Build
./gradlew build

# Run
./gradlew run
```

Requires Java 17+.

## Tech Stack

- **JDA 5.3.2** — Discord API client
- **OkHttp 4.12.0** — HTTP client
- **Gson 2.11.0** — JSON parsing
- **Gradle** — Build tool
