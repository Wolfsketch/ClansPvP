package me.jason.clanspvp.commands;

import me.jason.clanspvp.ClansPvP;
import me.jason.clanspvp.models.Clan;
import me.jason.clanspvp.models.PlayerData;
import me.jason.clanspvp.utils.ChatUtil;
import me.jason.clanspvp.managers.ClanManager;
import me.jason.clanspvp.managers.ConfigManager;
import me.jason.clanspvp.managers.ClaimManager;
import me.jason.clanspvp.utils.ClanScoreboard;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.Location;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.kohsuke.github.GHRepository;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ClanCommand implements CommandExecutor {

    private final ClansPvP plugin;
    private final Map<UUID, Long> disbandConfirmations = new ConcurrentHashMap<>();

    public ClanCommand(ClansPvP plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use clan commands.");
            return true;
        }

        Player player = (Player) sender;
        ClanManager clanManager = plugin.getClanManager();
        ClaimManager claimManager = plugin.getClaimManager();
        PlayerData data = plugin.getPlayerData(player.getUniqueId());

        String line = ChatUtil.color("\u00a78\u00a7m                                                           ");

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            player.sendMessage(" ");
            player.sendMessage(ChatUtil.color("&6                         ClansPvP Help"));
            player.sendMessage(" ");
            player.sendMessage(ChatUtil.color("&e→ &7/clan create <name> [tag] &8– &fCreate a new clan"));
            player.sendMessage(ChatUtil.color("&e→ &7/clan info &8– &fView your clan information"));
            player.sendMessage(ChatUtil.color("&e→ &7/clan vault &8– &fOpen the clan vault"));
            player.sendMessage(ChatUtil.color("&e→ &7/clan invite | accept | deny <player> &8– &fManage invites"));
            player.sendMessage(ChatUtil.color("&e→ &7/clan join | cancel <clan> &8– &fJoin or cancel request"));
            player.sendMessage(ChatUtil.color("&e→ &7/clan leave &8– &fLeave your clan"));
            player.sendMessage(ChatUtil.color("&e→ &7/clan disband | confirm &8– &fDisband your clan"));
            player.sendMessage(ChatUtil.color("&e→ &7/clan promote | demote <player> &8– &fRank members"));
            player.sendMessage(ChatUtil.color("&e→ &7/clan claim | unclaim &8– &fManage land claims"));
            player.sendMessage(ChatUtil.color("&e→ &7/clan raid start | stop | check &8– &fRaid commands"));
            player.sendMessage(ChatUtil.color("&e→ &7/clan setwarp | warp <name> &8– &fTeleportation"));
            player.sendMessage(ChatUtil.color("&e→ &7/clan friendlyfire <on|off> &8– &fToggle PvP in clan"));
            player.sendMessage(ChatUtil.color("&e→ &7/clan ally <name | accept | deny> &8– &fManage allies"));
            player.sendMessage(ChatUtil.color("&e→ &7/clan reload &8– &fReload config (admin only)"));
            player.sendMessage(ChatUtil.color("&e→ &7/clan issue <player> <title> <info> &8– &fReport bug/suggestion"));
            player.sendMessage(" ");
            return true;
        }

        if (args[0].equalsIgnoreCase("vault")) {
            Clan clan = data.getClan();
            if (clan == null) {
                player.sendMessage(ChatUtil.color(ConfigManager.getMessage("not-in-clan")));
                return true;
            }

            player.openInventory(clan.getVault());
            player.sendMessage(ChatUtil.color("&a✓ Clan vault opened. You have access to &e"
                    + clan.getVaultSize() + " &aslots."));
            return true;
        }

        if (args[0].equalsIgnoreCase("claim")) {
            Clan clan = data.getClan();
            if (clan == null) {
                player.sendMessage(ChatUtil.color(ConfigManager.getMessage("not-in-clan")));
                return true;
            }

            Chunk chunk = player.getLocation().getChunk();
            if (claimManager.isClaimed(chunk)) {
                player.sendMessage(ChatUtil.color(ConfigManager.getMessage("clan-claim-already")));
                return true;
            }

            String clanName = clan.getName();
            int currentClaims = claimManager.getClaimCount(clanName);
            int maxClaims = ConfigManager.get().getInt("claiming.max-chunks-per-clan-member")
                    * clanManager.getMembers(clan).size();

            if (currentClaims >= maxClaims) {
                player.sendMessage(ChatUtil.color(ConfigManager.getMessage("claim-limit-reached")));
                return true;
            }

            boolean success = claimManager.claimChunk(clanName, chunk, maxClaims);
            if (success) {
                player.sendMessage(ChatUtil.color(ConfigManager.getMessage("clan-claim-success")));
            } else {
                player.sendMessage(ChatUtil.color(ConfigManager.getMessage("clan-claim-fail")));
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("raid")) {
            Clan clan = data.getClan();
            if (clan == null) {
                player.sendMessage(ChatUtil.color(ConfigManager.getMessage("not-in-clan")));
                return true;
            }
            if (args.length < 2) {
                player.sendMessage(ChatUtil.color("&c✗ Usage: /clan raid <start|stop|check>"));
                return true;
            }

            String sub = args[1];
            if (sub.equalsIgnoreCase("start")) {
                if (!"LEADER".equals(data.getClanRole()) && !"OFFICER".equals(data.getClanRole())) {
                    player.sendMessage(ChatUtil.color("&c✗ Only leaders or officers can start raids."));
                    return true;
                }

                Location loc = player.getLocation();
                int x = (int) loc.getX();
                int y = (int) loc.getY();
                int z = (int) loc.getZ();

                clan.setRaidLocation(x, y, z);
                player.sendMessage(ChatUtil.color("&a✓ Raid started at your current location!"));
                player.sendMessage(ChatUtil.color("&7Location: &fX: " + x + " Y: " + y + " Z: " + z));

                // Notify all other online clan members
                for (UUID member : clan.getAllMembers()) {
                    Player online = Bukkit.getPlayer(member);
                    if (online != null && !online.getUniqueId().equals(player.getUniqueId())) {
                        online.sendMessage(
                                ChatUtil.color("&c⚔ A raid has been started by &6" + player.getName() + "&c!"));
                        online.sendMessage(ChatUtil.color("&7Location: &fX: " + x + " Y: " + y + " Z: " + z));
                    }
                }
                return true;
            } else if (sub.equalsIgnoreCase("stop")) {
                if (!"LEADER".equals(data.getClanRole()) && !"OFFICER".equals(data.getClanRole())) {
                    player.sendMessage(ChatUtil.color("&c✗ Only leaders or officers can stop raids."));
                    return true;
                }
                clan.stopRaid();
                for (UUID member : clan.getAllMembers()) {
                    Player online = Bukkit.getPlayer(member);
                    if (online != null) {
                        online.sendMessage(
                                ChatUtil.color("&7⚠ The raid has been &cstopped&7 by &6" + player.getName() + "&7."));
                    }
                }
                return true;

            } else if (sub.equalsIgnoreCase("check")) {
                if (!clan.isRaidActive()) {
                    player.sendMessage(ChatUtil.color("&eNo raid is currently active."));
                    return true;
                }
                player.sendMessage(ChatUtil.color("&6Raid Location:"));
                player.sendMessage(ChatUtil.color("&7X: &f" + clan.getRaidX()));
                player.sendMessage(ChatUtil.color("&7Y: &f" + clan.getRaidY()));
                player.sendMessage(ChatUtil.color("&7Z: &f" + clan.getRaidZ()));
                return true;

            } else {
                player.sendMessage(ChatUtil.color(ConfigManager.getMessage("unknown-command")));
                return true;
            }
        }

        if (args[0].equalsIgnoreCase("join")) {
            if (data.getClan() != null) {
                player.sendMessage(ChatUtil.color(ConfigManager.getMessage("already-in-clan")));
                return true;
            }

            if (args.length < 2) {
                player.sendMessage(ChatUtil.color(ConfigManager.getMessage("usage-clan-join")));
                return true;
            }

            if (data.getPendingJoinRequest() != null) {
                player.sendMessage(ChatUtil.color(ConfigManager.getMessage("already-has-request")));
                return true;
            }

            String clanName = args[1];
            Clan clan = clanManager.getClan(clanName);
            if (clan == null) {
                player.sendMessage(ChatUtil.color(ConfigManager.getMessage("clan-not-exist")));
                return true;
            }

            data.setPendingJoinRequest(clanName);
            player.sendMessage(
                    ChatUtil.color(ConfigManager.getMessage("join-request-sent").replace("%clan%", clanName)));
            return true;
        }

        if (args[0].equalsIgnoreCase("cancel")) {
            if (data.getPendingJoinRequest() == null) {
                player.sendMessage(ChatUtil.color("&c✗ You have no pending join request."));
                return true;
            }

            data.clearPendingJoinRequest();
            player.sendMessage(ChatUtil.color(ConfigManager.getMessage("join-request-cancelled")));
            return true;
        }

        if (args[0].equalsIgnoreCase("unclaim")) {
            Clan clan = data.getClan();
            if (clan == null) {
                player.sendMessage(ChatUtil.color(ConfigManager.getMessage("not-in-clan")));
                return true;
            }

            Chunk chunk = player.getLocation().getChunk();

            if (!claimManager.isClaimed(chunk)) {
                player.sendMessage(ChatUtil.color(ConfigManager.getMessage("clan-unclaim-not-claimed")));
                return true;
            }

            String owner = claimManager.getClaimOwner(chunk);
            if (!clan.getName().equalsIgnoreCase(owner)) {
                player.sendMessage(ChatUtil.color(ConfigManager.getMessage("clan-unclaim-not-owned")));
                return true;
            }

            boolean success = claimManager.unclaimChunk(chunk);
            if (success) {
                player.sendMessage(ChatUtil.color(ConfigManager.getMessage("clan-unclaim-success")));
            } else {
                player.sendMessage(ChatUtil.color(ConfigManager.getMessage("clan-unclaim-fail")));
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("friendlyfire")) {
            Clan clan = data.getClan();
            if (clan == null) {
                player.sendMessage(ChatUtil.color(ConfigManager.getMessage("not-in-clan")));
                return true;
            }

            if (!"LEADER".equals(data.getClanRole())) {
                player.sendMessage(ChatUtil.color(ConfigManager.getMessage("only-leader-friendlyfire")));
                return true;
            }

            if (args.length < 2 ||
                    (!args[1].equalsIgnoreCase("on") && !args[1].equalsIgnoreCase("off"))) {
                player.sendMessage(ChatUtil.color(ConfigManager.getMessage("usage-clan-friendlyfire")));
                return true;
            }

            boolean enable = args[1].equalsIgnoreCase("on");
            clan.setAllowFriendlyFire(enable);
            plugin.saveClans();

            String messageKey = enable ? "friendlyfire-enabled" : "friendlyfire-disabled";
            player.sendMessage(ChatUtil.color(ConfigManager.getMessage(messageKey)));
            return true;
        }

        if (args[0].equalsIgnoreCase("info")) {
            if (data.getClan() == null) {
                ClanScoreboard.showNoClanScoreboard(player);
                player.sendMessage(ChatUtil.color(ConfigManager.getMessage("not-in-clan")));
                return true;
            }

            Clan clan = data.getClan();
            ClanScoreboard.showClanScoreboard(player, clan, data);
            player.sendMessage(ChatUtil.color("&aClan info shown on your scoreboard!"));

            player.sendMessage(line);
            player.sendMessage(ChatUtil.color("&6                         ✦ Clan Info ✦"));
            player.sendMessage(line);
            player.sendMessage(ChatUtil.color("&7Name:      &e" + clan.getName()));
            player.sendMessage(ChatUtil.color("&7Tag:       &a[" + clan.getTag() + "]"));
            player.sendMessage(ChatUtil.color("&7Leader:    &b" + Bukkit.getOfflinePlayer(clan.getLeader()).getName()));

            double clanPower = clan.getPower();
            double maxPower = clan.getMaxPower();
            double playerPower = data.getPower();

            player.sendMessage(ChatUtil.color("&7Power:     &c" + clanPower + "&7 / &a" + maxPower));
            player.sendMessage(ChatUtil.color("&7Your Power: &e" + playerPower));

            int claimedChunks = claimManager.getClaimCount(clan.getName());
            int maxClaims = ConfigManager.get().getInt("claiming.max-chunks-per-clan-member")
                    * clanManager.getMembers(clan).size();
            player.sendMessage(ChatUtil.color("&7Claims:     &f" + claimedChunks + "&7 / &f" + maxClaims));

            Set<String> allies = clan.getAllies();
            if (allies.isEmpty()) {
                player.sendMessage(ChatUtil.color("&7Allies:     &cNone"));
            } else {
                int limit = 3;
                List<String> allyList = new ArrayList<>(allies);
                String shown = allyList.stream().limit(limit).collect(Collectors.joining(", "));
                String suffix = allyList.size() > limit ? " &7+&f" + (allyList.size() - limit) + " more" : "";
                player.sendMessage(ChatUtil.color("&7Allies:     &b" + shown + suffix));
            }

            player.sendMessage(" ");

            player.sendMessage(ChatUtil.color("&6Members:"));
            sendMembersByRole(clan, "LEADER", "&eLeader:   ", player);
            sendMembersByRole(clan, "OFFICER", "&eOfficers: ", player);
            sendMembersByRole(clan, "MEMBER", "&eMembers:  ", player);
            sendMembersByRole(clan, "RECRUIT", "&eRecruits: ", player);
            player.sendMessage(line);
            return true;
        }

        if (args[0].equalsIgnoreCase("create")) {
            if (!ConfigManager.get().getBoolean("clan-creation.enabled")) {
                player.sendMessage(ChatUtil.color("&c✗ Clan creation is currently disabled."));
                return true;
            }

            if (ConfigManager.get().getBoolean("clan-creation.require-permission") &&
                    !player.hasPermission("clanspvp.create")) {
                player.sendMessage(ChatUtil.color(ConfigManager.getMessage("no-permission")));
                return true;
            }

            if (data.getClan() != null) {
                player.sendMessage(ChatUtil.color(ConfigManager.getMessage("player-already-in-clan")));
                return true;
            }

            if (args.length < 2) {
                player.sendMessage(ChatUtil.color("&c✗ Usage: /clan create <name> [tag]"));
                return true;
            }

            String name = args[1];
            String tag = args.length >= 3 ? args[2] : name.substring(0, Math.min(name.length(), 3)).toUpperCase();

            if (clanManager.clanExists(name)) {
                player.sendMessage(ChatUtil.color(ConfigManager.getMessage("clan-exists")));
                return true;
            }

            int maxTagLength = ConfigManager.get().getInt("clan-creation.max-tag-length");
            if (tag.length() > maxTagLength) {
                player.sendMessage(ChatUtil.color(ConfigManager.getMessage("clan-tag-too-long")
                        .replace("%max%", String.valueOf(maxTagLength))));
                return true;
            }

            double requiredKDR = ConfigManager.get().getDouble("clan-creation.min-kdr-to-create");
            if (data.getKDR() < requiredKDR) {
                player.sendMessage(ChatUtil.color(ConfigManager.getMessage("not-enough-kdr")
                        .replace("%kdr", String.valueOf(requiredKDR))));
                return true;
            }

            Clan clan = new Clan(name, tag, player.getUniqueId());
            clanManager.registerClan(clan);
            data.setClan(clan);
            data.setClanRole("LEADER");

            player.sendMessage(ChatUtil.color(ConfigManager.getMessage("clan-created")
                    .replace("%name%", name).replace("%tag%", tag)));
            ClanScoreboard.showClanScoreboard(player, clan, data);
            return true;
        }

        if (args[0].equalsIgnoreCase("leave")) {
            if (data.getClan() == null) {
                player.sendMessage(ChatUtil.color(ConfigManager.getMessage("not-in-clan")));
                return true;
            }

            if (data.getClanRole().equals("LEADER")) {
                player.sendMessage(ChatUtil.color("&c✗ As the leader, use &e/clan disband &cto disband your clan."));
                return true;
            }

            Clan clan = data.getClan();
            clan.removeMember(player.getUniqueId());
            data.setClan(null);
            data.setClanRole(null);
            player.sendMessage(ChatUtil.color("&aYou left your clan."));
            ClanScoreboard.showNoClanScoreboard(player);
            return true;
        }

        if (args[0].equalsIgnoreCase("disband")) {
            if (data.getClan() == null) {
                player.sendMessage(ChatUtil.color(ConfigManager.getMessage("not-in-clan")));
                return true;
            }
            if (!"LEADER".equals(data.getClanRole())) {
                player.sendMessage(ChatUtil.color(ConfigManager.getMessage("only-leader-disband")));
                return true;
            }

            disbandConfirmations.put(player.getUniqueId(), System.currentTimeMillis());
            player.sendMessage(ChatUtil.color(
                    "&a✓ Are you sure you want to disband the clan? &eType &6/clan confirm &ewithin 30 seconds."));

            new BukkitRunnable() {
                @Override
                public void run() {
                    disbandConfirmations.remove(player.getUniqueId());
                }
            }.runTaskLater(plugin, 20 * 30);

            return true;
        }

        /* ────────────────── /clan ally setwarp|warp … ───────────────── */
        if (args[0].equalsIgnoreCase("ally") &&
                (args[1].equalsIgnoreCase("setwarp") || args[1].equalsIgnoreCase("warp"))) {

            // basis-checks
            Clan myClan = data.getClan();
            if (myClan == null) {
                player.sendMessage(ChatUtil.color(ConfigManager.getMessage("not-in-clan")));
                return true;
            }
            if (args.length < 3) {
                player.sendMessage(ChatUtil.color("&c✗ Usage: /clan ally " + args[1] + " <name>"));
                return true;
            }

            String warpName = args[2].toLowerCase();
            boolean isSet = args[1].equalsIgnoreCase("setwarp");

            /* ---------- /clan ally setwarp <name> ---------- */
            if (isSet) {
                if (!Arrays.asList("LEADER", "OFFICER").contains(data.getClanRole())) {
                    player.sendMessage(ChatUtil.color("&c✗ Only leaders or officers can set ally warps."));
                    return true;
                }
                int max = ConfigManager.get().getInt("warp-ally.max-warps-per-clan");
                if (myClan.getAllyWarps().size() >= max && !myClan.getAllyWarps().containsKey(warpName)) {
                    player.sendMessage(ChatUtil.color("&c✗ Ally-warp limit reached (" + max + ")."));
                    return true;
                }

                myClan.addAllyWarp(warpName, player.getLocation());
                plugin.saveClans();
                player.sendMessage(ChatUtil.color("&a✓ Ally-warp &e" + warpName + " &aset."));
                return true;
            }

            /* ---------- /clan ally warp <name> ------------- */
            // mag gebruikt worden door:
            // 1) eigen clan
            // 2) bondgenoten mét wederzijdse ally
            Clan owner = myClan; // default: eigen clan
            if (!owner.getAllyWarps().containsKey(warpName)) {
                // kijk of een bondgenoot deze warp heeft
                owner = clanManager.getAllClans().stream()
                        .filter(c -> c.getAllyWarps().containsKey(warpName)
                                && c.getAllyWarps().get(warpName) != null
                                && clanManager.areAllies(c.getName(), myClan.getName()))
                        .findFirst().orElse(null);
                if (owner == null) {
                    player.sendMessage(ChatUtil.color("&c✗ Ally-warp not found."));
                    return true;
                }
            }

            int cd = ConfigManager.get().getInt("warp-ally.cooldown-seconds");
            long now = System.currentTimeMillis() / 1000;
            if (now < data.getNextAllyWarpTime()) {
                long left = data.getNextAllyWarpTime() - now;
                player.sendMessage(ChatUtil.color("&c✗ Wait " + left + " s before ally-warping again."));
                return true;
            }

            Location loc = owner.getAllyWarps().get(warpName);
            player.teleport(loc);
            data.setNextAllyWarpTime(now + cd);
            player.sendMessage(ChatUtil.color("&b✦ Teleported to ally-warp &f" + warpName));
            return true;
        }

        /* ───────────────────────── ISSUE SUB-COMMAND ───────────────────────── */
        if (args[0].equalsIgnoreCase("issue")) {

            // 0. Basis-validatie
            if (args.length < 4) {
                player.sendMessage(ChatUtil.color(
                        "&c✗ Use: /clan issue <username> <title> <description>"));
                return true;
            }

            /*
             * 1. Parameters uit elkaar trekken
             * args[1] = ingame-naam die in de issue-tekst moet verschijnen
             * args[2] = korte titel
             * args[3…] = volledige omschrijving
             */
            String reporterName = args[1];
            String title = args[2];
            String body = String.join(" ",
                    Arrays.copyOfRange(args, 3, args.length)) +
                    "\n\n---\n_Reported by **" + reporterName +
                    "** in-game (UUID: " + player.getUniqueId() + ")_";

            /*
             * 2. GitHub-credentials uit config (plugins/ClansPvP/config.yml)
             * github:
             * token: "ghp_…"
             * repository: "Wolfsketch/ClansPvP"
             */
            String token = ConfigManager.get().getString("github.token");
            String repo = ConfigManager.get().getString("github.repository");

            if (token == null || token.isEmpty() || repo == null || repo.isEmpty()) {
                player.sendMessage(ChatUtil.color("&c✗ GitHub-token of repo missing in config."));
                return true;
            }

            // 3. Netwerk-IO altijd asynchroon uitvoeren
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                try {
                    GitHub gh = new GitHubBuilder().withOAuthToken(token).build();
                    GHRepository repository = gh.getRepository(repo);

                    repository.createIssue(title)
                            .body(body)
                            .label("bug")
                            .create();

                    player.sendMessage(ChatUtil.color("&a✓ Issue uploaded to GitHub!"));
                } catch (IOException e) {
                    player.sendMessage(ChatUtil.color(
                            "&c✗ Could not upload issue: " + e.getMessage()));
                    plugin.getLogger().severe("GitHub-upload failed");
                    e.printStackTrace();
                }
            });
            return true;
        }

        /* ───────────────────────── ALLY SUB-COMMAND ───────────────────────── */
        if (args[0].equalsIgnoreCase("ally")) {

            /* ── 0. Basis-validaties ─────────────────────────────────────── */
            if (args.length < 2) {
                player.sendMessage(ChatUtil.color(
                        ConfigManager.getMessage("usage-clan-ally")));
                return true;
            }

            Clan myClan = data.getClan();
            if (myClan == null) {
                player.sendMessage(ChatUtil.color(
                        ConfigManager.getMessage("not-in-clan")));
                return true;
            }
            if (!"LEADER".equals(data.getClanRole())) {
                player.sendMessage(ChatUtil.color(
                        ConfigManager.getMessage("only-leader-can-ally")));
                return true;
            }

            /* ── 1. Verhinder ‘self-ally’ ────────────────────────────────── */
            String firstArg = args[1];
            if (firstArg.equalsIgnoreCase(myClan.getName()) ||
                    firstArg.equalsIgnoreCase(myClan.getTag())) {
                player.sendMessage(ChatUtil.color(
                        "&c✘ You cannot ally with your own clan."));
                return true;
            }

            /* ── 2. /clan ally <clan> trust <on|off> ────────────────────── */
            if (args.length == 4 && args[2].equalsIgnoreCase("trust")) {
                String allyName = args[1];
                String toggle = args[3].toLowerCase();

                if (!clanManager.clanExists(allyName)) {
                    player.sendMessage(ChatUtil.color(
                            ConfigManager.getMessage("clan-not-exist")));
                    return true;
                }
                if (!clanManager.areAllies(myClan.getName(), allyName)) {
                    player.sendMessage(ChatUtil.color(
                            "&c✘ Your clan is not allied with that clan."));
                    return true;
                }
                if (!toggle.equals("on") && !toggle.equals("off")) {
                    player.sendMessage(ChatUtil.color(
                            "&c✘ Usage: /clan ally <clan> trust <on|off>"));
                    return true;
                }

                boolean trust = toggle.equals("on");
                myClan.setAllyBuildTrust(allyName, trust);
                plugin.saveClans();

                player.sendMessage(ChatUtil.color(
                        trust
                                ? ConfigManager.getMessage("trust-enabled")
                                        .replace("%clan%", allyName)
                                : ConfigManager.getMessage("trust-disabled")
                                        .replace("%clan%", allyName)));
                return true;
            }

            /* ── 3. /clan ally accept | deny ────────────────────────────── */
            String sub = args[1].toLowerCase();
            if (sub.equals("accept") || sub.equals("deny")) {
                String requester = clanManager.getRequestingClan(myClan.getName());
                if (requester == null) {
                    player.sendMessage(ChatUtil.color(
                            ConfigManager.getMessage("no-pending-ally-request")));
                    return true;
                }

                if (sub.equals("accept")) {
                    clanManager.addAlly(myClan.getName(), requester);
                    plugin.saveClans();
                    clanManager.removeAllyRequest(myClan.getName());

                    player.sendMessage(ChatUtil.color(
                            ConfigManager.getMessage("ally-accepted")
                                    .replace("%clan%", requester)));

                    Clan other = clanManager.getClan(requester);
                    if (other != null) {
                        Player leader = Bukkit.getPlayer(other.getLeader());
                        if (leader != null) {
                            leader.sendMessage(ChatUtil.color(
                                    ConfigManager.getMessage("ally-request-accepted")
                                            .replace("%clan%", myClan.getName())));
                        }
                    }
                } else { // deny
                    clanManager.removeAllyRequest(myClan.getName());
                    player.sendMessage(ChatUtil.color(
                            ConfigManager.getMessage("ally-denied")
                                    .replace("%clan%", requester)));
                }
                return true;
            }

            /* ── 4. /clan ally <targetClan> (verzoek versturen) ─────────── */
            String targetClan = args[1];

            if (!clanManager.clanExists(targetClan)) {
                player.sendMessage(ChatUtil.color(
                        ConfigManager.getMessage("clan-not-exist")));
                return true;
            }
            if (clanManager.areAllies(myClan.getName(), targetClan)) {
                player.sendMessage(ChatUtil.color(
                        ConfigManager.getMessage("already-allies")
                                .replace("%clan%", targetClan)));
                return true;
            }
            if (clanManager.hasPendingRequest(targetClan)) {
                player.sendMessage(ChatUtil.color(
                        ConfigManager.getMessage("ally-request-already-sent")
                                .replace("%clan%", targetClan)));
                return true;
            }

            clanManager.sendAllyRequest(myClan.getName(), targetClan);

            Clan tgt = clanManager.getClan(targetClan);
            Player tgtLeader = Bukkit.getPlayer(tgt.getLeader());
            if (tgtLeader != null) {
                tgtLeader.sendMessage(ChatUtil.color(
                        ConfigManager.getMessage("ally-request-received")
                                .replace("%clan%", myClan.getName())));
            }

            player.sendMessage(ChatUtil.color(
                    ConfigManager.getMessage("ally-request-sent")
                            .replace("%clan%", targetClan)));
            return true;
        }

        if (args[0].equalsIgnoreCase("invite")) {
            if (data.getClan() == null) {
                player.sendMessage(ChatUtil.color(ConfigManager.getMessage("not-in-clan")));
                return true;
            }

            if (!"LEADER".equals(data.getClanRole()) && !"OFFICER".equals(data.getClanRole())) {
                player.sendMessage(ChatUtil.color("&c✗ Only the leader or officers can invite players."));
                return true;
            }

            if (args.length < 2) {
                player.sendMessage(ChatUtil.color("&c✗ Usage: /clan invite <player>"));
                return true;
            }

            Player target = Bukkit.getPlayer(args[1]);
            if (target == null || !target.isOnline()) {
                player.sendMessage(ChatUtil.color(ConfigManager.getMessage("player-not-online")));
                return true;
            }

            PlayerData targetData = plugin.getPlayerData(target.getUniqueId());
            if (targetData.getClan() != null) {
                player.sendMessage(ChatUtil.color(ConfigManager.getMessage("player-already-in-clan")));
                return true;
            }

            Clan clan = data.getClan();
            targetData.setPendingInvite(clan.getName());

            target.sendMessage(ChatUtil.color("&eYou have been invited to join the clan &6" + clan.getName() + "&e!"));
            target.sendMessage(ChatUtil.color("&7Type &a/clan accept &7to join."));
            player.sendMessage(ChatUtil.color("&a✓ Invitation sent to &e" + target.getName()));

            return true;
        }

        if (args[0].equalsIgnoreCase("accept")) {
            if (data.getClan() != null) {
                player.sendMessage(ChatUtil.color(ConfigManager.getMessage("already-in-clan")));
                return true;
            }

            String pendingInvite = data.getPendingInvite();
            if (pendingInvite == null) {
                player.sendMessage(ChatUtil.color("&c✗ You have no pending invites."));
                return true;
            }

            Clan clan = clanManager.getClan(pendingInvite);
            if (clan == null) {
                player.sendMessage(ChatUtil.color("&c✗ The clan that invited you no longer exists."));
                data.clearPendingInvite();
                return true;
            }

            clan.addMember(player.getUniqueId(), "RECRUIT");
            data.setClan(clan);
            data.setClanRole("RECRUIT");
            data.clearPendingInvite();

            player.sendMessage(ChatUtil.color("&a✓ You have joined the clan &e" + clan.getName()));
            ClanScoreboard.showClanScoreboard(player, clan, data);
            return true;
        }

        if (args[0].equalsIgnoreCase("deny")) {
            if (data.getPendingInvite() == null) {
                player.sendMessage(ChatUtil.color("&c✗ You have no pending invites."));
                return true;
            }

            data.clearPendingInvite();
            player.sendMessage(ChatUtil.color("&a✓ Clan invitation denied."));
            return true;
        }

        // Voeg dit toe in je ClanCommand.java bij de onCommand(...) methode
        if (args[0].equalsIgnoreCase("accept") || args[0].equalsIgnoreCase("deny")) {
            if (data.getClan() != null
                    && ("LEADER".equals(data.getClanRole()) || "OFFICER".equals(data.getClanRole()))) {
                // Clan leader/officer accepteert of weigert een join request
                if (args.length < 2) {
                    player.sendMessage(ChatUtil.color("&c✗ Usage: /clan " + args[0] + " <player>"));
                    return true;
                }

                Player target = Bukkit.getPlayer(args[1]);
                if (target == null || !target.isOnline()) {
                    player.sendMessage(ChatUtil.color(ConfigManager.getMessage("player-not-online")));
                    return true;
                }

                PlayerData targetData = plugin.getPlayerData(target.getUniqueId());

                if (targetData.getClan() != null) {
                    player.sendMessage(ChatUtil.color(ConfigManager.getMessage("player-already-in-clan")));
                    return true;
                }

                if (!data.getClan().getName().equals(targetData.getPendingJoinRequest())) {
                    player.sendMessage(ChatUtil.color("&c✗ That player has not requested to join your clan."));
                    return true;
                }

                if (args[0].equalsIgnoreCase("accept")) {
                    Clan clan = data.getClan();
                    targetData.setClan(clan);
                    targetData.setClanRole("RECRUIT");
                    clan.addMember(target.getUniqueId(), "RECRUIT");
                    targetData.clearPendingJoinRequest();

                    player.sendMessage(
                            ChatUtil.color("&a✓ You have accepted &e" + target.getName() + "&a into your clan."));
                    target.sendMessage(
                            ChatUtil.color("&a✓ Your join request has been accepted by &e" + player.getName()));
                    ClanScoreboard.showClanScoreboard(target, clan, targetData);
                } else {
                    targetData.clearPendingJoinRequest();
                    player.sendMessage(ChatUtil.color("&a✓ You have denied the join request of &e" + target.getName()));
                    target.sendMessage(ChatUtil
                            .color("&c✗ Your join request to &e" + data.getClan().getName() + " &cwas denied."));
                }

                return true;
            }

            // Speler accepteert of weigert een invite
            if ("accept".equalsIgnoreCase(args[0])) {
                if (data.getClan() != null) {
                    player.sendMessage(ChatUtil.color(ConfigManager.getMessage("already-in-clan")));
                    return true;
                }

                String pendingInvite = data.getPendingInvite();
                if (pendingInvite == null) {
                    player.sendMessage(ChatUtil.color("&c✗ You have no pending invites."));
                    return true;
                }

                Clan clan = clanManager.getClan(pendingInvite);
                if (clan == null) {
                    player.sendMessage(ChatUtil.color("&c✗ The clan that invited you no longer exists."));
                    data.clearPendingInvite();
                    return true;
                }

                clan.addMember(player.getUniqueId(), "RECRUIT");
                data.setClan(clan);
                data.setClanRole("RECRUIT");
                data.clearPendingInvite();

                player.sendMessage(ChatUtil.color("&a✓ You have joined the clan &e" + clan.getName()));
                ClanScoreboard.showClanScoreboard(player, clan, data);
                return true;

            } else if ("deny".equalsIgnoreCase(args[0])) {
                if (data.getPendingInvite() == null) {
                    player.sendMessage(ChatUtil.color("&c✗ You have no pending invites."));
                    return true;
                }

                data.clearPendingInvite();
                player.sendMessage(ChatUtil.color("&a✓ Clan invitation denied."));
                return true;
            }
        }

        if (args[0].equalsIgnoreCase("promote") || args[0].equalsIgnoreCase("demote")) {
            if (data.getClan() == null) {
                player.sendMessage(ChatUtil.color(ConfigManager.getMessage("not-in-clan")));
                return true;
            }

            Clan clan = data.getClan();

            if (!"LEADER".equals(data.getClanRole()) && !"OFFICER".equals(data.getClanRole())) {
                player.sendMessage(ChatUtil.color("&c✗ Only the leader or officers can promote or demote members."));
                return true;
            }

            if (args.length < 2) {
                player.sendMessage(ChatUtil.color("&c✗ Usage: /clan " + args[0] + " <player>"));
                return true;
            }

            Player target = Bukkit.getPlayer(args[1]);
            if (target == null || !target.isOnline()) {
                player.sendMessage(ChatUtil.color(ConfigManager.getMessage("player-not-online")));
                return true;
            }

            PlayerData targetData = plugin.getPlayerData(target.getUniqueId());

            if (targetData.getClan() == null || !targetData.getClan().getName().equals(clan.getName())) {
                player.sendMessage(ChatUtil.color(ConfigManager.getMessage("player-not-in-your-clan")));
                return true;
            }

            String currentRole = targetData.getClanRole();
            List<String> roles = Arrays.asList("RECRUIT", "MEMBER", "OFFICER", "LEADER");

            int currentIndex = roles.indexOf(currentRole);
            int newIndex = args[0].equalsIgnoreCase("promote") ? currentIndex + 1 : currentIndex - 1;

            // Blokkeer promotie naar LEADER (gebruik liever overdrachtsmechanisme)
            if (newIndex < 0 || newIndex >= roles.size() || "LEADER".equals(roles.get(newIndex))) {
                player.sendMessage(ChatUtil.color("&c✗ Cannot " + args[0] + " this player further."));
                return true;
            }

            // Verhindert dat de laatste leider zichzelf demote
            if (args[0].equalsIgnoreCase("demote") && currentRole.equals("LEADER")) {
                long leaderCount = clan.getMembers().entrySet().stream()
                        .filter(entry -> "LEADER".equals(entry.getValue()))
                        .count();

                if (leaderCount <= 1 && target.getUniqueId().equals(player.getUniqueId())) {
                    player.sendMessage(ChatUtil.color("&c✗ You are the only leader. You cannot demote yourself."));
                    return true;
                }
            }

            String newRole = roles.get(newIndex);
            targetData.setClanRole(newRole);
            clan.setMemberRole(target.getUniqueId(), newRole);
            target.sendMessage(ChatUtil.color("&a✓ You have been "
                    + (args[0].equalsIgnoreCase("promote") ? "promoted" : "demoted") + " to &e" + roles.get(newIndex)));
            player.sendMessage(ChatUtil.color("&a✓ " + target.getName() + " is now a(n) &e" + roles.get(newIndex)));

            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            if (!player.isOp()) {
                player.sendMessage(ChatUtil.color("&c✗ Only server operators can use this command."));
                return true;
            }

            plugin.reload(); // Zie stap 2 hieronder
            player.sendMessage(ChatUtil.color("&a✓ ClansPvP plugin has been reloaded."));
            return true;
        }

        player.sendMessage(ChatUtil.color("&c✗ Unknown subcommand. Use &7/clan help"));
        return true;
    }

    private void sendMembersByRole(Clan clan, String role, String prefix, Player toSend) {
        List<UUID> members = clan.getMembersByRole(role);
        String names = members.isEmpty() ? ChatColor.GRAY + "None"
                : members.stream()
                        .map(uuid -> Bukkit.getOfflinePlayer(uuid).getName())
                        .collect(Collectors.joining(", "));

        toSend.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + names));
    }
}
