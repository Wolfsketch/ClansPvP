# ⚔️ ClansPvP — Modern Clan Plugin

**ClansPvP** is an all-in-one clan system for Paper/Spigot servers.  
From land claims and power to raids, vaults, warps, alliances, and even in-game GitHub issue reporting — everything is wrapped in a clean, modern UI.

---

## 🚀 Feature Overview

| Category | Highlights |
|----------|------------|
| **Core Clans** | Create / disband, roles (Leader, Officer, Member, Recruit), invites & join requests, promote/demote |
| **Alliances** | `/clan ally <clan>` ↔ accept/deny; optional build-trust toggle |
| **Land Claiming** | Chunk-based claims with per-member limits, overclaiming, ASCII map viewer |
| **Raids** | Controlled raids with `/clan raid start/stop/check` |
| **Shared Vault** | GUI vault, donor slot bonuses via permissions |
| **Warps** | `/clan setwarp` or `/clan warp <name>` — per-player cooldown in config |
| **Ally-Warps (NEW)** | Share warps with allies → `/clan ally setwarp` or `/clan warp <name>` with separate limit/cooldown |
| **GitHub Issue Reporter** | `/clan issue <player> <title> <info>` opens an issue on your repo (token opt-in) |
| **Live Scoreboard** | Sidebar shows power, members, claims, allies, warp hint, etc. |
| **Friendly-Fire Toggle** | Leaders set `/clan friendlyfire on` or `off` |
| **K/D Requirement** | Optional minimum K/D to found a clan |
| **Donor Perks** | Extra power or vault slots via Vault groups |
| **Modern Chat UI** | Color-coded messages, Unicode symbols, configurable strings |

---

## 💬 Command Cheat-Sheet

```text
/clan create <name> [tag]        – Create a clan
/clan info                       – Show clan details (+ scoreboard)
/clan vault                      – Open shared vault
/clan invite <player>            – Invite player
/clan join <clan> / cancel       – Request / cancel join
/clan accept|deny <player>       – Handle join requests
/clan leave                      – Leave clan
/clan promote|demote <player>    – Rank management
/clan disband / confirm          – Disband clan

/clan claim|unclaim              – Claim / unclaim chunk
/clan raid start|stop |check     – Raid controls

/clan setwarp <name>             – Set clan warp
/clan warp <name>                – Teleport to clan warp

/clan ally setwarp <name>        – Set ally-warp (shared)
/clan ally warp <name>           – Teleport to ally-warp

/clan ally <clan|accept|deny>    – Alliance controls
/clan ally <clan> trust on|off   – Allow allies to build/break

/clan friendlyfire on|off        – Toggle intra-clan PvP
/clan issue <player> <title> <info>
                                 – Create GitHub issue (opt-in)

/clanmap                         – ASCII land map
/clan reload                     – Reload plugin (admin)
```

---

## 🔧 Installation

1. Build or download **`ClansPvP.jar`**.  
2. Drop it into your server’s `/plugins` folder.  
3. Restart or `/reload` the server – default configs will generate.  

> **Optional:** add a fine-grained GitHub PAT (Issues: RW) and repo in `config.yml`
> to enable in-game bug reporting.

---

## ⚙️ Config Highlights

* `claiming.*` – chunk size, overclaiming, per-member limit  
* `warp.*` & `warp-ally.*` – max warps per clan, cooldowns  
* `github.*` – repo, token, labels, cooldown (opt-in)  
* `donor-bonus-*` – extra power / vault slots via permission groups  
* All messages, colors & symbols under `messages:` and `ui:`  

---

## 📜 Permissions

| Node | Default | Purpose |
|------|---------|---------|
| `clanspvp.create` | ✅ | Create clan |
| `clanspvp.warp.set / use` | ❌ / ✅ | Clan warps |
| `clanspvp.warp.ally.set / use` | ❌ / ✅ | Ally warps |
| `clanspvp.issue` | ✅ | Use `/clan issue` |
| `clanspvp.raid.start|stop` | ❌ | Start/stop raids |
| `clanspvp.reload` | OP | `/clan reload` |

*(Full list in `plugin.yml`)*  

Requires **Vault** as permission bridge. Works out-of-the-box with **LuckPerms**.

---

## 📦 Requirements

* Java 17+  
* Spigot / Paper 1.20 – 1.21  
* Vault (permissions)  

---

## 🗺️ Example `/clanmap`

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

## 👤 Author

**Wolfsketch** – Minecraft plugin developer  
Questions or suggestions? Open an issue (or use `/clan issue` in-game 😉).

---

## 📄 License

ClansPvP is **closed-source**.  
Redistribution, modification, or resale is prohibited without permission.  
Enjoy the plugin? ⭐ Star the repo and spread the word!
