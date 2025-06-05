package me.jason.clanspvp.commands;

import me.jason.clanspvp.ClansPvP;
import me.jason.clanspvp.managers.ClaimManager;
import me.jason.clanspvp.models.PlayerData;
import me.jason.clanspvp.models.Clan;
import me.jason.clanspvp.utils.ChatUtil;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ClanMapCommand implements CommandExecutor {

    private final ClansPvP plugin;

    public ClanMapCommand(ClansPvP plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatUtil.color("&cOnly players can use this command."));
            return true;
        }

        Player player = (Player) sender;
        PlayerData playerData = plugin.getPlayerData(player.getUniqueId());
        Clan playerClan = playerData.getClan();

        ClaimManager claimManager = plugin.getClaimManager();
        Location playerLocation = player.getLocation();
        Chunk centerChunk = playerLocation.getChunk();
        int centerX = centerChunk.getX();
        int centerZ = centerChunk.getZ();

        int width = 13; // columns
        int height = 9; // rows
        int halfWidth = width / 2;
        int halfHeight = height / 2;

        String indent = "                       ";
        String graySpacer = "§8                                               \n"; // clean horizontal bar

        StringBuilder map = new StringBuilder();
        map.append(graySpacer);
        map.append(ChatUtil.color("&6                      Clan Land Map &8(&7You are &a+&8)\n"));
        map.append(ChatUtil.color("&8\n"));

        for (int dz = -halfHeight; dz <= halfHeight; dz++) {
            StringBuilder row = new StringBuilder(indent + "  &8");

            for (int dx = -halfWidth; dx <= halfWidth; dx++) {
                int x = centerX + dx;
                int z = centerZ + dz;
                Chunk chunk = player.getWorld().getChunkAt(x, z);

                if (x == centerX && z == centerZ) {
                    row.append("&a+");
                } else if (claimManager.isClaimed(chunk)) {
                    String owner = claimManager.getClaimOwner(chunk);
                    if (playerClan != null && owner != null) {
                        if (owner.equalsIgnoreCase(playerClan.getName())) {
                            row.append("&a█"); // own land
                        } else {
                            row.append("&c█"); // enemy land
                        }
                    } else {
                        row.append("&c█");
                    }
                } else {
                    row.append("&7░");
                }
            }

            map.append(ChatUtil.color(row.toString())).append("\n");
        }

        map.append(ChatUtil.color("&8\n"));
        String tag = (playerClan != null && playerClan.getTag() != null) ? playerClan.getTag() : "CLN";
        map.append(ChatUtil.color("       &8Legend: &a+ You &a█ [&a" + tag + "&a] &d█ Allies &c█ Enemy &7░ Free\n"));
        map.append(graySpacer);

        player.sendMessage(map.toString());
        return true;
    }
}
