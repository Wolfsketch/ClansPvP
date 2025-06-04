package me.jason.clanspvp.managers;

import me.jason.clanspvp.models.Clan;

import java.util.*;

public class ClanManager {

    private final Map<String, Clan> clans = new HashMap<>(); // naam → Clan
    private final Map<String, List<UUID>> clanMembers = new HashMap<>(); // naam → leden

    public void registerClan(Clan clan) {
        String name = clan.getName().toLowerCase();
        clans.put(name, clan);
        clanMembers.put(name, new ArrayList<>(List.of(clan.getLeader()))); // Leader toevoegen als eerste lid
    }

    public void unregisterClan(String name) {
        name = name.toLowerCase();
        clans.remove(name);
        clanMembers.remove(name);
    }

    public boolean clanExists(String name) {
        return clans.containsKey(name.toLowerCase());
    }

    public Clan getClan(String name) {
        return clans.get(name.toLowerCase());
    }

    public void addMember(Clan clan, UUID uuid) {
        String name = clan.getName().toLowerCase();
        clanMembers.computeIfAbsent(name, k -> new ArrayList<>()).add(uuid);
    }

    public List<UUID> getMembers(Clan clan) {
        return clanMembers.getOrDefault(clan.getName().toLowerCase(), new ArrayList<>());
    }

    public Clan getClanByMember(UUID uuid) {
        for (Map.Entry<String, List<UUID>> entry : clanMembers.entrySet()) {
            if (entry.getValue().contains(uuid)) {
                return clans.get(entry.getKey());
            }
        }
        return null;
    }

    public Collection<Clan> getAllClans() {
        return clans.values();
    }
}
