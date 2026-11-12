<p align="center">
  <img src="/.github/logo.png" alt="GMSEssentials Logo" width="800">
</p>

**GMSEssentials** is a lightweight, high-performance alternative to EssentialsX for modern Paper servers. Designed with clean features in mind, it provides essential player commands, teleportation systems, homes, and utility tools for admins—without the bloated feature set.

## ✨ Features

* **Teleportation System**: Modern TPA commands with support for standard, reverse, and cancellation requests.

* **Death Recovery**: Return instantly to your last death point with `/back`.

* **Gamemode Shortcuts**: Quickly toggle gamemodes using concise shorthand commands (`/gms`, `/gmc`, etc.).

* **Homes System**: Simple location-saving system to quickly return to your base.

* **Admin Utilities**: Inventory inspection (`/invsee`), flight controls (`/fly`), and dynamic player resizing (`/size`).

## 📥 Installation

**Requirements:** Paper 26.1+ and Java 25+. This is a native Paper plugin (using `paper-plugin.yml` and Paper's Brigadier command API), so it will not load on Spigot or Bukkit.

1. Download the latest `GMSEssentials.jar` release.

2. Place the `.jar` file into your server's `plugins/` directory.

3. Restart or start your server.

4. *(Optional)* Modify `plugins/GMSEssentials/config.yml` to adjust settings to your liking.

## 📜 Commands & Permissions

### 🌌 Teleportation System

| Command | Description | Permission | 
 | ----- | ----- | ----- | 
| `/tpa <player>` | Send a request to teleport to a player | `gmse.tpa` | 
| `/tpahere <player>` | Request a player to teleport to you | `gmse.tpa` | 
| `/tpaccept` | Accept an incoming teleport request | `gmse.tpa` | 
| `/tpacancel` | Cancel a pending teleport request you've received | `gmse.tpa` | 
| `/tpdeny` | Deny a pending teleport request you've received | `gmse.tpa` | 
| `/back` | Teleport back to your last death location | `gmse.back` | 

### 🎮 Gamemode Shortcuts

| Command | Gamemode | Permission | 
 | ----- | ----- | ----- | 
| `/gms [player]` | Survival | `gmse.admin` | 
| `/gmc [player]` | Creative | `gmse.admin` | 
| `/gma [player]` | Adventure | `gmse.admin` | 
| `/gmsp [player]` | Spectator | `gmse.admin` | 

### 🏠 Homes

| Command | Description | Permission | 
 | ----- | ----- | ----- | 
| `/sethome [name]` | Save your current location as a home (up to `homes.max-homes`, default 3) | `gmse.home` | 
| `/home [name]` | Teleport to a saved home location | `gmse.home` | 
| `/delhome <name>` | Delete a saved home | `gmse.home` | 
| `/homes` | List your saved homes | `gmse.home` | 
| `/echest` | Open your own ender chest | `gmse.echest` | 

### 🛠️ Admin Utilities

| Command | Description | Permission | 
 | ----- | ----- | ----- | 
| `/invsee <player>` | View and manage a player's inventory | `gmse.admin` | 
| `/fly [player]` | Toggle flight mode for yourself or others | `gmse.admin` | 
| `/size <player> <scale>` | Change a player's scale attribute *(1.20.5+)* | `gmse.admin` | 
| `/workbench` | Open a portable crafting table | `gmse.workbench` | 

## 🔑 Master Permissions

* `gmse.admin` — Grants access to `/invsee`, `/fly`, `/size`, `/gmse reload`, and the gamemode commands.
* `gmse.home` — `/sethome`, `/home`, `/delhome`, `/homes` *(default: everyone)*
* `gmse.back` — `/back` *(default: everyone)*
* `gmse.tpa` — `/tpa`, `/tpahere`, `/tpaccept`, `/tpacancel`, `/tpdeny` *(default: everyone)*
* `gmse.echest` — `/echest` *(default: everyone)*
* `gmse.workbench` — `/workbench` *(default: everyone)*

Revoke any of these with a permissions plugin like LuckPerms to restrict a feature.

## ⚙️ Configuration

### `config.yml`

```yaml
homes:
  # The maximum number of named homes a player can set with /sethome.
  max-homes: 3
```

Every player-facing message the plugin sends is also customizable here, grouped under `messages:` by the command it belongs to (`generic`, `gamemode`, `size`, `fly`, `home`, `back`, `tpa`, `gmse`, `invsee`). `'&'` color codes are supported, and messages like `back.death-message` and `home.saved` support `{placeholder}` substitution (see the comments in the shipped file for which placeholders each message supports). Run `/gmse reload` after editing to apply your changes without restarting.

```yaml
messages:
  home:
    saved: "Home '{name}' saved successfully."
    teleported: "Teleported to home '{name}'."
    # ...and so on for every other message. See the full config.yml for the complete list.
```

### `homes.yml`

Saved homes are stored separately in `plugins/GMSEssentials/homes.yml`, keyed by player UUID and then by home name. This file is managed automatically; you do not need to edit it by hand.

## 🤝 Contributing & Support

Found a bug or have a feature request? Please open an issue or submit a pull request on our GitHub repository.
