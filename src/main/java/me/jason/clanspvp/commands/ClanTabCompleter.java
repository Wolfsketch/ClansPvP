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
    private final List<String> rootSubs = Arrays.asList(
            "create", "info", "vault", "invite", "join",
            "cancel", "leave", "disband", "promote", "demote",
            "claim", "unclaim", "raid", "setwarp", "warp",
            "friendlyfire", "ally", "reload");

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

        /*
         * ──────────────────
         * /clan <sub>
         * ──────────────────
         */
        if (args.length == 1) {
            String prefix = args[0].toLowerCase();
            return rootSubs.stream()
                    .filter(s -> s.startsWith(prefix))
                    .collect(Collectors.toList());
        }

        /*
         * ──────────────────
         * /clan ally …
         * ──────────────────
         */
        if (args[0].equalsIgnoreCase("ally")) {

            /* /clan ally <…> */
            if (args.length == 2) {
                String prefix = args[1].toLowerCase();

                // vaste opties
                List<String> fixed = Arrays.asList("accept", "deny");

                // dynamisch: alle clans die nog géén ally zijn + niet je eigen
                List<String> others = cm.getAllClans().stream()
                        .map(Clan::getName)
                        .filter(name -> myClan == null || (!name.equalsIgnoreCase(myClan.getName()) &&
                                !cm.areAllies(myClan.getName(), name)))
                        .filter(name -> name.toLowerCase().startsWith(prefix))
                        .collect(Collectors.toList());

                return concatStartsWith(fixed, prefix, others);
            }

            /* /clan ally <clan> trust … */
            if (args.length == 3) {
                return startsWith(args[2], "trust");
            }

            /* /clan ally <clan> trust <on|off> */
            if (args.length == 4 && args[2].equalsIgnoreCase("trust")) {
                return startsWith(args[3], "on", "off");
            }
        }

        /*
         * ──────────────────
         * /clan raid …
         * ──────────────────
         */
        if (args[0].equalsIgnoreCase("raid") && args.length == 2) {
            return startsWith(args[1], "start", "stop", "check");
        }

        /*
         * ──────────────────
         * /clan friendlyfire …
         * ──────────────────
         */
        if (args[0].equalsIgnoreCase("friendlyfire") && args.length == 2) {
            return startsWith(args[1], "on", "off");
        }

        /*
         * ──────────────────
         * player-gerichte subs
         * ──────────────────
         */
        if (args.length == 2) {
            String sub = args[0].toLowerCase();
            if (Arrays.asList("invite", "promote", "demote", "accept", "deny")
                    .contains(sub)) {

                String prefix = args[1].toLowerCase();
                return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(name -> name.toLowerCase().startsWith(prefix))
                        .collect(Collectors.toList());
            }
        }

        return Collections.emptyList();
    }

    /* helpers */
    private List<String> startsWith(String partial, String... opts) {
        String p = partial.toLowerCase();
        return Arrays.stream(opts)
                .filter(o -> o.startsWith(p))
                .collect(Collectors.toList());
    }

    private List<String> concatStartsWith(List<String> base,
            String prefix,
            List<String> extra) {

        List<String> out = new ArrayList<>();
        base.stream()
                .filter(b -> b.startsWith(prefix))
                .forEach(out::add);
        out.addAll(extra);
        return out;
    }
}
