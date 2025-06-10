# ⚔️ ClansPvP

**ClansPvP** is a feature-rich Minecraft plugin built for PvP servers, offering a complete clan system, land claiming, raid mechanics, power management, shared vaults, scoreboard integration, and an ASCII-style map. Ideal for structured group gameplay on competitive servers with a clean and modern UI.

---

## 🚀 Features

✔️ **Full Clan System**  
Create clans, manage roles (Leader, Officer, Member, Recruit), invite players, promote/demote members, and disband clans with confirmation.

✔️ **Rank Management**  
Promote or demote members safely with `/clan promote` and `/clan demote`. Leaders cannot demote themselves if they’re the last leader.

✔️ **Land Claiming (16x16 Chunks)**  
Secure territory for your clan using `/clan claim`. Claim limits scale with the number of clan members.

✔️ **Live Scoreboard Integration**  
Displays clan power, member count, and land claims directly on the sidebar scoreboard and via `/clan info`.

✔️ **Shared Clan Vault**  
Access a central storage vault with `/clan vault`. Donors can receive expanded slots.

✔️ **Visual ASCII Clan Map**  
Use `/clanmap` to see a stylized grid showing:  
- 🟩 Your territory (`[TAG]`)  
- 🟥 Enemy territory  
- 🟪 Allies  
- ⬜ Unclaimed land  
- ➕ Your location  

✔️ **KDR Requirement (Optional)**  
Require players to meet a minimum Kill/Death Ratio to create a clan.

✔️ **Clan Raids**  
Leaders and Officers can manage raid sessions using `/clan raid start`, `check`, or `stop`.

✔️ **Donor Benefits**  
Grant special vault sizes or extra power to specific permission groups.

✔️ **Clean Modern UI**  
Styled messages with Unicode separators, color-coded output, and clear feedback.

---

## 💬 Main Commands

```
/clan create <name> [tag] → Create a new clan
/clan info → View your clan details
/clan vault → Access shared clan storage
/clan invite <player> → Invite a player to your clan
/clan join <clan> → Request to join a clan
/clan cancel → Cancel a join request
/clan accept <player> → Accept a player’s join request
/clan deny <player> → Deny a player’s join request
/clan leave → Leave your current clan
/clan promote <player> → Promote a member (e.g., RECRUIT → MEMBER)
/clan demote <player> → Demote a member
/clan disband → Disband your clan (leader only)
/clan confirm → Confirm disband action
/clan claim → Claim land
/clan unclaim → Unclaim land
/clan raid start|stop|check → Manage raid events
/clanmap → View clan map overview
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
