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
| `/tpa <player>` | Send a request to teleport to a player | `No permission required` | 
| `/tpahere <player>` | Request a player to teleport to you | `No permission required` | 
| `/tpaccept` | Accept an incoming teleport request | `No permission required` | 
| `/tpacancel` | Cancel an outgoing teleport request | `No permission required` | 
| `/back` | Teleport back to your last death location | `No permission required` | 

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
| `/sethome [name]` | Save your current location as a home | `No permission required` | 
| `/home [name]` | Teleport to a saved home location | `No permission required` | 

### 🛠️ Admin Utilities

| Command | Description | Permission | 
 | ----- | ----- | ----- | 
| `/invsee <player>` | View and manage a player's inventory | `gmse.admin` | 
| `/fly [player]` | Toggle flight mode for yourself or others | `gmse.admin` | 
| `/size <player> <scale>` | Change a player's scale attribute *(1.20.5+)* | `gmse.admin` | 

## 🔑 Master Permissions

* `gmse.admin` — Grants access to `/invsee`, `/fly`, `/size`, and target parameters for gamemode commands.

## ⚙️ Configuration

### `config.yml`

```yaml
# GMSEssentials Configuration

messages:
  # {coords} is replaced with the player's death coordinates (x, y, z).
  # '&' color codes are supported.
  death-message: "You died at {coords}. Type /back to teleport to your death coordinates."
```

### `homes.yml`

Saved homes are stored separately in `plugins/GMSEssentials/homes.yml`, keyed by player UUID. This file is managed automatically; you do not need to edit it by hand.

## 🤝 Contributing & Support

Found a bug or have a feature request? Please open an issue or submit a pull request on our GitHub repository.
