package me.jason.clanspvp.models;

import java.util.*;
import java.util.stream.Collectors;
import me.jason.clanspvp.ClansPvP;
import me.jason.clanspvp.models.PlayerData;

public class Clan {

    private final String name;
    private final String tag;
    private final UUID leader;

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

    // --- Nieuw: Lid management ---
    public void addMember(UUID uuid, String role) {
        members.put(uuid, role);
    }

    public void removeMember(UUID uuid) {
        members.remove(uuid);
    }

    public Map<UUID, String> getMembers() {
        return members;
    }

    public String getRole(UUID uuid) {
        return members.getOrDefault(uuid, "RECRUIT");
    }

    // Leden per rol
    public List<UUID> getMembersByRole(String role) {
        return members.entrySet().stream()
                .filter(e -> e.getValue().equalsIgnoreCase(role))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    // --- Nieuw: Power systeem ---
    public double getPower() {
        // Optel alle power van alle leden (geef bij PlayerData een .getPower() method)
        return members.keySet().stream()
                .map(uuid -> ClansPvP.getInstance().getPlayerData(uuid))
                .filter(Objects::nonNull)
                .mapToDouble(PlayerData::getPower)
                .sum();
    }

    public double getMaxPower() {
        // Stel per member max 5.0 power (pas aan naar wens)
        return members.size() * 5.0;
    }

    // Raid locatie setters/getters (ongewijzigd)
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
}
