package me.jason.clanspvp.commands;

import me.jason.clanspvp.ClansPvP;
import me.jason.clanspvp.models.PlayerData;
import me.jason.clanspvp.utils.ChatUtil;
import me.jason.clanspvp.managers.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class VaultCommand implements CommandExecutor {

    private final ClansPvP plugin;

    public VaultCommand() {
        this.plugin = ClansPvP.getInstance();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can access the clan vault.");
            return true;
        }

        Player player = (Player) sender;
        PlayerData data = plugin.getPlayerData(player.getUniqueId());

        if (data.getClan() == null) {
            player.sendMessage(ChatUtil.color(ConfigManager.getMessage("player-not-in-clan")));
            return true;
        }

        // Vault grootte uit config
        int size = ConfigManager.get().getInt("claiming.max-vault-slots");
        int roundedSize = Math.min(54, ((int) Math.ceil(size / 9.0)) * 9); // Minecraft inventory moet veelvoud zijn van
                                                                           // 9

        Inventory vault = Bukkit.createInventory(null, roundedSize,
                ChatUtil.color(ConfigManager.getMessage("vault-header")));

        // TODO: Vault opslag per clan implementeren en laden

        player.openInventory(vault);
        player.sendMessage(ChatUtil.color(ConfigManager.getMessage("vault-opened")));
        return true;
    }
}
