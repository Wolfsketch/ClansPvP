package me.jason.clanspvp.models;

import java.util.*;
import java.util.stream.Collectors;
import me.jason.clanspvp.ClansPvP;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class Clan {

    private final String name;
    private final String tag;
    private final UUID leader;
    private Inventory vault;

    // Role mapping: UUID -> ROLE (bv. LEADER, OFFICER, MEMBER, RECRUIT)
    private final Map<UUID, String> members = new HashMap<>();

    // Raid
    private double raidX = 0;
    private double raidY = 0;
    private double raidZ = 0;
    private boolean raidActive = false;

    public Clan(String name, String tag, UUID leader) {
        this.name = name;
        this.tag = tag;
        this.leader = leader;
        members.put(leader, "LEADER"); // Leader bijmaken bij creatie
    }

    public String getName() {
        return name;
    }

    public String getTag() {
        return tag;
    }

    public UUID getLeader() {
        return leader;
    }

    public void addMember(UUID uuid, String role) {
        members.put(uuid, role);
    }

    public void removeMember(UUID uuid) {
        members.remove(uuid);
    }

    public void setMemberRole(UUID uuid, String role) {
        members.put(uuid, role);
    }

    public Map<UUID, String> getMembers() {
        return members;
    }

    public String getRole(UUID uuid) {
        return members.getOrDefault(uuid, "RECRUIT");
    }

    public List<UUID> getMembersByRole(String role) {
        return members.entrySet().stream()
                .filter(e -> e.getValue().equalsIgnoreCase(role))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    public List<UUID> getAllMembers() {
        return new ArrayList<>(members.keySet());
    }

    public double getPower() {
        return members.keySet().stream()
                .map(uuid -> ClansPvP.getInstance().getPlayerData(uuid))
                .filter(Objects::nonNull)
                .mapToDouble(PlayerData::getPower)
                .sum();
    }

    public double getMaxPower() {
        return members.size() * 5.0;
    }

    public void setRaidLocation(double x, double y, double z) {
        this.raidX = x;
        this.raidY = y;
        this.raidZ = z;
        this.raidActive = true;
    }

    public double getRaidX() {
        return raidX;
    }

    public double getRaidY() {
        return raidY;
    }

    public double getRaidZ() {
        return raidZ;
    }

    public boolean isRaidActive() {
        return raidActive;
    }

    public void stopRaid() {
        this.raidActive = false;
    }

    public Inventory getVault() {
        if (vault == null || vault.getSize() != getVaultSize()) {
            vault = Bukkit.createInventory(null, getVaultSize(), ChatColor.GOLD + "Clan Vault");
        }
        return vault;
    }

    public int getVaultSize() {
        int baseSlots = 20;
        int bonusSlots = 0;

        for (UUID uuid : members.keySet()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.hasPermission("clanspvp.vault.bonus")) {
                bonusSlots += 10;
            }
        }

        int totalSlots = baseSlots + bonusSlots;
        return Math.min(54, ((int) Math.ceil(totalSlots / 9.0)) * 9);
    }
}
