# CustomNicks

<img width="500" height="273" alt="unwatermarked_Gemini_Generated_Image_errp2lerrp2lerrp" src="https://github.com/user-attachments/assets/3f7e9a73-2cb0-4388-afca-ebae94c44f9f" />


A comprehensive nickname plugin for Hytale servers that allows players to set custom display names. These nicknames are integrated across the entire server experience, including chat, tab list, nameplates, and map markers.

## Features

- **Global Integration**: Nicknames appear everywhere:
  - **Chat**: Messages are formatted with the player's nickname.
  - **Tab List**: The player list updates to show the custom name.
  - **Nameplates**: The text above the player's head reflects their nickname.
  - **Map Markers**: (Optional) Updates the player's username internally so they appear correctly on the world map.
- **Persistence**: Nicknames are saved to `nicknames.json` and restored automatically when the server restarts or players rejoin.
- **Validation**:
  - Configurable minimum and maximum length.
  - Regex support (supports formatting codes, spaces, and Cyrillic characters by default).
  - Banned words list to prevent inappropriate names.
  - Unique nickname enforcement to prevent impersonation.
- **Smart Commands**: 
  - Supports multi-word nicknames properly (e.g., `/nick The Great One`).
  - Admin management commands.

## Commands

| Command | Permission | Description |
| :--- | :--- | :--- |
| `/nick <name>` | `customnicks.nick.use` | Set your own nickname. |
| `/nick <target> <name>` | `customnicks.nick.others` | Set another player's nickname. |
| `/nickreset` | `customnicks.nick.use` | Reset your own nickname to your original username. |
| `/nickreset <target>` | `customnicks.nick.others` | Reset another player's nickname. |

## Configuration

The plugin generates a `config.json` file in its data directory. You can customize the validation rules here:

```json
{
  "minLength": 3,
  "maxLength": 20,
  "allowCyrillic": true,
  "uniqueNicknames": true,
  "bannedWords": [
    "admin",
    "staff",
    "badword"
  ],
  "allowedRegex": "^[a-zA-Z0-9_\\u00C0-\\u017F\\u0400-\\u04FF ]+$"
}
```

## Permissions

- `customnicks.nick.use` - Allows a player to set their own nickname.
- `customnicks.nick.others` - Allows a player to manage other players' nicknames.
- `customnicks.bypass` - Bypasses validation checks (length, banned words, regex).

## Building

To build the plugin from source, run:

```sh
./gradlew build
```

The compiled JAR file will be located in `build/libs/`.

## Author

**AetherHyt**
