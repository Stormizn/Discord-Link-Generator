# Amethyst Development Bot

A multifunctional Discord bot for server management, originally built for **Amethyst Development**. Includes file uploading, FAQ management, thread management, anti-spam, anti-ping protection, and more.

## Features

- **`/upload`** — Upload text/log files to mclo.gs or Pastebin with auto-complete
- **Auto-upload** — Configured channels where file attachments are uploaded automatically
- **Fallback system** — If mclo.gs is unavailable, falls back to Pastebin
- **`/faq`** — Add, remove, list, and retrieve FAQ entries (with auto-complete)
- **`/thread`** — Manage threads: resolve (archive + tag), unresolve (reopen), mark stale
- **`/outdated`** — Show a configurable outdated version message
- **`/website`** — Show the project website link
- **Anti-ping** — Blocks pings to core team roles outside designated help channels
- **Anti-spam** — Sliding-window rate limiter (configurable threshold + time window)
- **`/config`** — Admin-only command to configure all settings via Discord
- **Rich embeds** — Clean Discord embed responses throughout
- **Auto-complete** — Tab completion on all relevant options

## Slash Commands

| Command | Description |
|---|---|
| `/upload file:<attachment> [service:mclogs\|pastebin]` | Upload a file |
| `/faq add <key> <answer>` | Add an FAQ entry |
| `/faq remove <key>` | Remove an FAQ entry |
| `/faq get <key>` | Get an FAQ entry |
| `/faq list` | List all FAQ entries |
| `/thread resolve` | Mark thread as resolved and archive |
| `/thread unresolve` | Reopen a resolved thread |
| `/thread stale` | Mark thread as stale |
| `/outdated` | Show outdated version message |
| `/website` | Show project website |
| `/config show` | View current bot configuration |
| `/config help-channel-add <channel>` | Designate a help channel |
| `/config help-channel-remove <channel>` | Remove a help channel |
| `/config core-role-add <role>` | Add a core team role |
| `/config core-role-remove <role>` | Remove a core team role |
| `/config spam-threshold <count>` | Set spam message threshold |
| `/config spam-window <seconds>` | Set spam detection window |
| `/config website <url>` | Set website URL |
| `/config outdated-message <message>` | Set outdated message |
| `/config stale-tag <name>` | Set stale thread tag |
| `/config resolved-tag <name>` | Set resolved thread tag |

## Configuration

### Environment Variables

| Variable | Description |
|---|---|
| `BOT_TOKEN` | Discord bot token (required) |
| `PASTEBIN_API_KEY` | Pastebin API developer key |

### config.json

Falls back to `config.json` in the project root if env vars are not set:

```json
{
  "token": "YOUR_BOT_TOKEN",
  "pastebinApiKey": "YOUR_PASTEBIN_API_KEY",
  "autoUploadChannels": ["channel_id_1"]
}
```

### Guild-Specific Config

All other settings are configured per-server via `/config` commands and stored in `guild_configs.json`.

## Auto-Upload

List channel IDs in `autoUploadChannels` within `config.json`. Any text/log file sent in those channels will be automatically uploaded to mclo.gs and linked in an embed reply.

Supported extensions: `.log`, `.txt`, `.yml`, `.yaml`, `.json`, `.xml`, `.cfg`, `.conf`, `.properties`, `.md`, `.java`, `.py`, `.js`, `.ts`, `.kt`, `.gradle`, `.toml`, `.ini`, `.sh`, `.bat`, `.env`, `.gitignore`, `.csv`

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
