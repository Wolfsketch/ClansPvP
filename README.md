# ⚔️ ClansPvP

**ClansPvP** is a powerful Minecraft plugin for PvP servers that delivers a full-featured clan system, land claiming, raid mechanics, shared vaults, ASCII map, live scoreboard, K/D requirements, **alliances**, and a clan-level friendly-fire toggle – all wrapped in a modern, clean UI.

---

## 🚀 Features

| Category | Highlights |
|----------|------------|
| **Core Clan System** | Create clans, manage roles (Leader, Officer, Member, Recruit), invite / accept / deny, promote, demote, disband with confirmation |
| **Alliances** | Send requests with `/clan ally <clan>`, accept/deny, allies show on scoreboard, map, info |
| **Friendly Fire Toggle** | Leaders toggle PvP inside the clan with `/clan friendlyfire on|off` |
| **Land Claiming** | Chunk-based (16 × 16) claims, per-member limits, `/clan claim` & `/clan unclaim` |
| **Raid System** | `/clan raid start|stop|check` for controlled raids (leader/officer) |
| **Shared Vault** | `/clan vault` with donor-based slot boosts |
| **Live Scoreboard** | Sidebar shows power, members, claims, allies, etc. |
| **ASCII Map** | `/clanmap` visualizes: `█` own, `█` allies, `█` enemies, `░` free; `+` is you |
| **K/D Requirement** | Optional minimum K/D to create a clan |
| **Donor Perks** | Extra power or vault slots via Vault permission groups |
| **Modern UI** | Styled messages, Unicode separators, color coding |

---

## 💬 Command Cheat-Sheet

```
/clan create <name> [tag]           – Create a clan
/clan info                           – Show clan details (+ scoreboard)
/clan vault                          – Open shared vault
/clan invite <player>               – Invite player
/clan join <clan> / cancel          – Request / cancel join
/clan accept|deny <player>          – Handle join requests
/clan leave                          – Leave clan
/clan promote|demote <player>       – Rank management
/clan disband / confirm             – Disband clan
/clan claim | unclaim               – Manage land
/clan raid start|stop|check         – Raid controls
/clan ally <clan|accept|deny>       – Alliance controls
/clan friendlyfire on|off           – Toggle intra-clan PvP
/clanmap                            – View ASCII land map
/clan reload                        – Reload plugin (admin)
```

---

## 🔧 Installation

1. Build or download `ClansPvP.jar`.
2. Drop it into `/plugins` on your Spigot/Paper server.
3. Restart the server – config files generate automatically.

---

## ⚙️ Config Highlights (`config.yml`)

* Power & regen settings  
* Claim limits per member  
* Donor-group bonuses (power / vault)  
* Friendly-fire defaults (clan & allies)  
* K/D requirement for creation  
* Raid role list & messages  
* Fully customizable messages / colors / symbols

---

## 📦 Requirements

* **Java 17+**  
* **Vault** plugin for permissions  
* Spigot / Paper 1.20-1.21+

---

## 🗺️ `/clanmap` Example

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

## 👨‍💻 Author

**Wolfsketch** – passionate Minecraft plugin developer  
Feel free to reach out on GitHub or Discord.

---

## 📜 License

This plugin is **not open source**. Using, modifying, or redistributing it without permission is prohibited.

> ⭐ Enjoy ClansPvP? Star the repo to support future updates!
