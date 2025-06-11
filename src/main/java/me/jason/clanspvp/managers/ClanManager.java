package me.jason.clanspvp.managers;

import me.jason.clanspvp.models.Clan;

import java.util.*;

public class ClanManager {

    private final Map<String, Clan> clans = new HashMap<>(); // name → Clan
    private final Map<String, List<UUID>> clanMembers = new HashMap<>(); // name → members
    private final Map<String, Set<String>> allies = new HashMap<>(); // name → set of ally names
    private final Map<String, String> pendingAllyRequests = new HashMap<>(); // targetClan → requestingClan

    // ------------------------
    // CLAN REGISTER/LOOKUP
    // ------------------------

    public void registerClan(Clan clan) {
        String name = clan.getName().toLowerCase();
        clans.put(name, clan);
        clanMembers.put(name, new ArrayList<>(List.of(clan.getLeader()))); // Add leader as first member
    }

    public void unregisterClan(String name) {
        final String lowerName = name.toLowerCase();

        clans.remove(lowerName);
        clanMembers.remove(lowerName);
        allies.remove(lowerName);
        pendingAllyRequests.remove(lowerName);

        for (Set<String> allySet : allies.values()) {
            allySet.remove(name);
        }

        pendingAllyRequests.entrySet().removeIf(entry -> entry.getValue().equalsIgnoreCase(name));
    }

    public Clan getClan(String name) {
        return clans.get(name.toLowerCase());
    }

    public boolean clanExists(String name) {
        return clans.containsKey(name.toLowerCase());
    }

    public Collection<Clan> getAllClans() {
        return clans.values();
    }

    // ------------------------
    // CLAN MEMBERS
    // ------------------------

    public void addMember(Clan clan, UUID uuid) {
        String name = clan.getName().toLowerCase();
        List<UUID> members = clanMembers.computeIfAbsent(name, k -> new ArrayList<>());
        if (!members.contains(uuid)) {
            members.add(uuid);
        }
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

    public Clan getClanByPlayer(UUID uuid) {
        return getClanByMember(uuid);
    }

    // ------------------------
    // ALLY MANAGEMENT
    // ------------------------

    public void sendAllyRequest(String fromClan, String toClan) {
        pendingAllyRequests.put(toClan.toLowerCase(), fromClan.toLowerCase());
    }

    public boolean hasPendingRequest(String targetClan) {
        return pendingAllyRequests.containsKey(targetClan.toLowerCase());
    }

    public String getRequestingClan(String targetClan) {
        return pendingAllyRequests.get(targetClan.toLowerCase());
    }

    public void removeAllyRequest(String targetClan) {
        pendingAllyRequests.remove(targetClan.toLowerCase());
    }

    public void addAlly(String clanA, String clanB) {
        Clan a = clans.get(clanA.toLowerCase());
        Clan b = clans.get(clanB.toLowerCase());
        if (a != null && b != null) {
            a.addAlly(clanB); // voeg toe aan A
            b.addAlly(clanA); // voeg ook toe aan B
        }
    }

    public boolean areAllies(String clanA, String clanB) {
        return allies.getOrDefault(clanA.toLowerCase(), Collections.emptySet()).contains(clanB.toLowerCase());
    }

    public Set<String> getAllies(String clanName) {
        return allies.getOrDefault(clanName.toLowerCase(), Collections.emptySet());
    }

    public void removeAlly(String clanA, String clanB) {
        allies.getOrDefault(clanA.toLowerCase(), Collections.emptySet()).remove(clanB.toLowerCase());
        allies.getOrDefault(clanB.toLowerCase(), Collections.emptySet()).remove(clanA.toLowerCase());
    }

}
