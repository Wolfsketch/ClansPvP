package me.jason.clanspvp.managers;

import me.jason.clanspvp.ClansPvP;
import me.jason.clanspvp.models.Clan;
import me.jason.clanspvp.utils.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class VaultManager {

    private final ClansPvP plugin;
    private final Map<String, Inventory> clanVaults = new HashMap<>();

    public VaultManager(ClansPvP plugin) {
        this.plugin = plugin;
    }

    /**
     * Open the vault GUI for a player
     */
    public void openVault(Player player, Clan clan) {
        if (clan == null) {
            player.sendMessage(ChatUtil.color("&c✗ You are not in a clan."));
            return;
        }

        String clanName = clan.getName().toLowerCase();
        Inventory vault = clanVaults.computeIfAbsent(clanName, key -> {
            int slots = getVaultSize(clan);
            return Bukkit.createInventory(null, slots, ChatUtil.color("&bClan Vault"));
        });

        player.openInventory(vault);
        player.sendMessage(ChatUtil.color(plugin.getConfig().getString("messages.vault-opened")));
    }

    /**
     * Get vault size based on config + donor bonuses
     */
    public int getVaultSize(Clan clan) {
        int baseSize = plugin.getConfig().getInt("claiming.max-vault-slots", 20);

        // Bereken extra slots per donor in de clan
        int bonus = 0;
        FileConfiguration config = plugin.getConfig();

        if (config.getBoolean("donor-bonus-vault.enabled")) {
            for (UUID member : plugin.getClanManager().getMembers(clan)) {
                String group = plugin.getPermissionGroup(member);
                bonus += config.getInt("donor-bonus-vault.groups." + group, 0);
            }
        }

        int total = baseSize + bonus;

        // Vault size must be a multiple of 9 for Inventory GUI
        return Math.min(54, Math.max(9, ((total + 8) / 9) * 9));
    }

    /**
     * Optional: Save vaults to disk (if persistent storage needed)
     */
    public void saveVaults() {
        // Optional uitbreiding: schrijf vault-inhoud naar file
    }

    /**
     * Optional: Load vaults from disk (if persistent storage needed)
     */
    public void loadVaults() {
        // Optional uitbreiding: laad vault-inhoud uit file
    }

    public Map<String, Inventory> getVaults() {
        return clanVaults;
    }
}
