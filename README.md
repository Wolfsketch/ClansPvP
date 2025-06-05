# ⚔️ ClansPvP

**ClansPvP** is a powerful Minecraft plugin for PvP servers featuring clans, land claiming, raids, power mechanics, vaults, and an ASCII-style territory map. Perfect for hardcore PvP servers with structured group gameplay and modern, styled UI.

---

## 🚀 Features

✔️ **Complete Clan System**  
Advanced roles, invitations, promotions, demotions, and clan disbanding.

✔️ **Chunk-Based Land Claiming**  
Conquer and protect 16x16 chunks for your clan using `/clan claim`.

✔️ **Clan Vault System**  
Shared storage for your entire clan using `/clan vault`.

✔️ **Clan Land ASCII Map**  
Visual map overview using `/clanmap`, showing:
- Your territory (green with clan tag)
- Enemies (red)
- Allies (purple)
- Free land (gray)

✔️ **KDR Requirement** *(optional)*  
Require a minimum Kill/Death Ratio to create a clan.

✔️ **Raid System**  
Start, check, and stop raids with `/clan raid start|check|stop`. Only authorized roles can control raids.

✔️ **Donor Bonuses**  
Extra vault slots and power for users with special permissions.

✔️ **Modern UI**  
Professionally styled messages with color codes, Unicode separators, and icons.

---

## 💬 Main Commands

```
/clan create <name> [tag]     → Create a new clan
/clan info                    → View your current clan
/clan vault                   → Open the clan vault
/clan invite <player>         → Invite a player
/clan leave                   → Leave your clan
/clan promote <player>        → Promote a member
/clan demote <player>         → Demote a member
/clan disband                 → Disband your clan (leader only)
/clan confirm                 → Confirm disband action
/clan claim                   → Claim the land you’re standing on
/clan raid start|stop|check   → Manage clan raids
/clanmap                      → View ASCII map of clan territories
```

---

## 🔧 Installation

1. Download `ClansPvP.jar` (from `target/ClansPvP-1.0.0.jar`)
2. Place it in your Minecraft server's `/plugins` folder
3. Restart the server
4. Configuration files will be generated automatically

---

## ⚙️ Configuration (`config.yml`)

- Power system toggle
- Max claims per clan member
- Donor group perks (power/vaults)
- KDR requirement for creation
- Raid permissions
- UI text formatting
- Unicode separators

Everything is fully customizable.

---

## 📦 Requirements

- Java 17 or higher
- [Vault plugin](https://www.spigotmc.org/resources/vault.34315/) for permissions
- Spigot, Paper, or forks (Minecraft 1.21+)

---

## 🗺️ Clan Map Example

In-game example of the `/clanmap` command:

```
                        Clan Land Map (You are +)

                       ░░░░░░░░░░░░░
                       ░░░░░█░░░░░░░
                       ░░░░█+█░░░░░░
                       ░░░░░█░░░░░░░
                       ░░░░░░░░░░░░░

Legend: + You  █ [TAG] Yours  █ Allies  █ Enemy  ░ Free
```

---

## 👨‍💻 Developed by

**Wolfsketch**  
🎮 Passionate Minecraft plugin developer  
📬 Reach out via GitHub or Discord

---

## 📄 License

Released under a **Non-Open Source License**. Not free to use, modify, or distribute without permission.

---

⭐ Enjoy this project? Star it on GitHub to support future development!
