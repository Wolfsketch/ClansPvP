package me.jason.clanspvp.commands;

import me.jason.clanspvp.ClansPvP;
import me.jason.clanspvp.managers.ClanManager;
import me.jason.clanspvp.models.Clan;
import me.jason.clanspvp.models.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class ClanTabCompleter implements TabCompleter {

    private final ClansPvP plugin = ClansPvP.getInstance();

    /** Hoofdsub-commando’s na “/clan ” */
    private final List<String> rootSubs = Arrays.asList(
            "create", "info", "vault", "invite", "join",
            "cancel", "leave", "disband", "promote", "demote",
            "claim", "unclaim", "raid", "setwarp", "warp",
            "friendlyfire", "ally", "reload",
            "issue");

    @Override
    public List<String> onTabComplete(CommandSender sender,
            Command cmd,
            String alias,
            String[] args) {

        if (!(sender instanceof Player))
            return Collections.emptyList();

        Player player = (Player) sender;
        ClanManager cm = plugin.getClanManager();
        PlayerData pd = plugin.getPlayerData(player.getUniqueId());
        Clan myClan = pd.getClan();

        /* ───────────── /clan <sub> ───────────── */
        if (args.length == 1) {
            String prefix = args[0].toLowerCase();
            return rootSubs.stream()
                    .filter(s -> s.startsWith(prefix))
                    .collect(Collectors.toList());
        }

        /* ───────────── /clan issue … ─────────── */
        if (args[0].equalsIgnoreCase("issue")) {
            if (args.length == 2) {
                String prefix = args[1].toLowerCase();
                return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(n -> n.toLowerCase().startsWith(prefix))
                        .collect(Collectors.toList());
            }
            return Collections.emptyList(); // titel/omschrijving: vrij typen
        }

        /* ───────────── /clan setwarp … ───────── */
        if (args[0].equalsIgnoreCase("setwarp")) {
            return Collections.emptyList(); // warp-naam vrij choose
        }

        /* ───────────── /clan warp … ──────────── */
        if (args[0].equalsIgnoreCase("warp") && args.length == 2 && myClan != null) {
            String prefix = args[1].toLowerCase();
            return myClan.getWarps().keySet().stream()
                    .filter(w -> w.startsWith(prefix))
                    .sorted()
                    .collect(Collectors.toList());
        }

        /* ───────────── /clan ally … ──────────── */
        if (args[0].equalsIgnoreCase("ally")) {

            /* /clan ally <…> (arg 2) */
            if (args.length == 2) {
                String prefix = args[1].toLowerCase();

                // vaste opties + clan-namen
                List<String> opts = new ArrayList<>(Arrays.asList(
                        "accept", "deny", "trust", "setwarp", "warp"));

                opts.addAll(
                        cm.getAllClans().stream()
                                .map(Clan::getName)
                                .filter(n -> myClan == null || !n.equalsIgnoreCase(myClan.getName()))
                                .collect(Collectors.toList()));

                return opts.stream()
                        .filter(o -> o.toLowerCase().startsWith(prefix))
                        .sorted()
                        .collect(Collectors.toList());
            }

            /* /clan ally setwarp <naam> -> geen hints */
            if (args.length == 3 && args[1].equalsIgnoreCase("setwarp"))
                return Collections.emptyList();

            /* /clan ally warp <naam> -> suggest ally-warps */
            if (args.length == 3 && args[1].equalsIgnoreCase("warp") && myClan != null) {
                String prefix = args[2].toLowerCase();

                // eigen ally-warps + die van bondgenoten
                Set<String> names = new HashSet<>(myClan.getAllyWarps().keySet());
                cm.getAllClans().stream()
                        .filter(c -> cm.areAllies(myClan.getName(), c.getName()))
                        .forEach(c -> names.addAll(c.getAllyWarps().keySet()));

                return names.stream()
                        .filter(w -> w.startsWith(prefix))
                        .sorted()
                        .collect(Collectors.toList());
            }

            /* /clan ally <clan> trust … */
            if (args.length == 3) {
                return startsWith(args[2], "trust");
            }
            if (args.length == 4 && args[2].equalsIgnoreCase("trust")) {
                return startsWith(args[3], "on", "off");
            }
        }

        /* ───────────── /clan raid … ──────────── */
        if (args[0].equalsIgnoreCase("raid") && args.length == 2) {
            return startsWith(args[1], "start", "stop", "check");
        }

        /* ───────────── /clan friendlyfire … ──── */
        if (args[0].equalsIgnoreCase("friendlyfire") && args.length == 2) {
            return startsWith(args[1], "on", "off");
        }

        /* ───────────── player-gerichte subs ───── */
        if (args.length == 2) {
            String sub = args[0].toLowerCase();
            if (Arrays.asList("invite", "promote", "demote", "accept", "deny")
                    .contains(sub)) {

                String prefix = args[1].toLowerCase();
                return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(n -> n.toLowerCase().startsWith(prefix))
                        .collect(Collectors.toList());
            }
        }

        return Collections.emptyList();
    }

    /* ───── helper-methodes ───── */
    private List<String> startsWith(String partial, String... opts) {
        String p = partial.toLowerCase();
        return Arrays.stream(opts)
                .filter(o -> o.startsWith(p))
                .collect(Collectors.toList());
    }
}
