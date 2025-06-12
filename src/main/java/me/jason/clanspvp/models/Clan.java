package me.jason.clanspvp.models;

import me.jason.clanspvp.ClansPvP;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.*;
import java.util.stream.Collectors;

public class Clan {

    /*
     * ────────────────────────────────────────────────────────────
     * Core fields
     * ──────────────────────────────────────────────────────────
     */
    private final String name;
    private final String tag;
    private final UUID leader;

    // members : UUID → role ( LEADER / OFFICER / MEMBER / RECRUIT )
    private final Map<UUID, String> members = new HashMap<>();

    // allies : lowercase-clan-name → true (simple presence means allied)
    private final Set<String> allies = new HashSet<>();

    // build-trust : lowercase-clan-name → true / false (defaults to false)
    private final Map<String, Boolean> allyBuildTrust = new HashMap<>();

    /*
     * ────────────────────────────────────────────────────────────
     * Flags / volatile data
     * ──────────────────────────────────────────────────────────
     */
    private boolean allowFriendlyFire = false;

    // Raid
    private double raidX = 0, raidY = 0, raidZ = 0;
    private boolean raidActive = false;

    // Non-persistent vault inventory
    private transient Inventory vault;

    /*
     * ────────────────────────────────────────────────────────────
     * Constructors
     * ──────────────────────────────────────────────────────────
     */
    public Clan(String name, String tag, UUID leader) {
        this.name = name;
        this.tag = tag;
        this.leader = leader;
        members.put(leader, "LEADER");
    }

    /*
     * ────────────────────────────────────────────────────────────
     * Basic getters
     * ──────────────────────────────────────────────────────────
     */
    public String getName() {
        return name;
    }

    public String getTag() {
        return tag;
    }

    public UUID getLeader() {
        return leader;
    }

    /*
     * ────────────────────────────────────────────────────────────
     * Member handling
     * ──────────────────────────────────────────────────────────
     */
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

    /*
     * ────────────────────────────────────────────────────────────
     * Power helpers
     * ──────────────────────────────────────────────────────────
     */
    public double getPower() {
        return members.keySet().stream()
                .map(id -> ClansPvP.getInstance().getPlayerData(id))
                .filter(Objects::nonNull)
                .mapToDouble(p -> p.getPower())
                .sum();
    }

    public double getMaxPower() {
        return members.size() * 5.0;
    }

    /*
     * ────────────────────────────────────────────────────────────
     * Raid helpers
     * ──────────────────────────────────────────────────────────
     */
    public void setRaidLocation(double x, double y, double z) {
        raidX = x;
        raidY = y;
        raidZ = z;
        raidActive = true;
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
        raidActive = false;
    }

    /*
     * ────────────────────────────────────────────────────────────
     * Vault
     * ──────────────────────────────────────────────────────────
     */
    public Inventory getVault() {
        if (vault == null || vault.getSize() != getVaultSize()) {
            vault = Bukkit.createInventory(null, getVaultSize(), ChatColor.GOLD + "Clan Vault");
        }
        return vault;
    }

    public int getVaultSize() {
        int base = 20, bonus = 0;
        for (UUID id : members.keySet()) {
            Player p = Bukkit.getPlayer(id);
            if (p != null && p.hasPermission("clanspvp.vault.bonus"))
                bonus += 10;
        }
        int total = base + bonus;
        return Math.min(54, ((int) Math.ceil(total / 9.0)) * 9);
    }

    /*
     * ────────────────────────────────────────────────────────────
     * Allies
     * ──────────────────────────────────────────────────────────
     */
    public Set<String> getAllies() {
        return allies;
    }

    public void addAlly(String clan) {
        String key = clan.toLowerCase();
        allies.add(key);
        allyBuildTrust.putIfAbsent(key, false); // default: not trusted to build
    }

    public void removeAlly(String clan) {
        String key = clan.toLowerCase();
        allies.remove(key);
        allyBuildTrust.remove(key);
    }

    public boolean isAlliedWith(String clan) {
        return allies.contains(clan.toLowerCase());
    }

    /* ── Build-trust helpers ─────────────────────────────────── */
    public void setAllyBuildTrust(String clan, boolean allow) {
        String key = clan.toLowerCase();
        if (isAlliedWith(key)) {
            allyBuildTrust.put(key, allow);
        }
    }

    public boolean isAllyAllowedToBuild(String clan) {
        return allyBuildTrust.getOrDefault(clan.toLowerCase(), false);
    }

    /*
     * ────────────────────────────────────────────────────────────
     * Friendly Fire toggle
     * ──────────────────────────────────────────────────────────
     */
    public boolean isAllowFriendlyFire() {
        return allowFriendlyFire;
    }

    public void setAllowFriendlyFire(boolean b) {
        allowFriendlyFire = b;
    }
}
