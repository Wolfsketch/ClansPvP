package me.jason.clanspvp.managers;

import me.jason.clanspvp.models.Clan;

import java.util.*;

/**
 * Houdt het centrale clan-register én de pending ally-requests bij.
 * Ally-relaties zitten nu volledig in het {@code Clan}-object zelf.
 */
public class ClanManager {

    /*
     * ────────────────────────────────────────────────────────────
     * Kern-tabellen
     * ──────────────────────────────────────────────────────────
     */
    private final Map<String, Clan> clans = new HashMap<>(); // name → Clan
    private final Map<String, List<UUID>> clanMembers = new HashMap<>(); // name → leden
    private final Map<String, String> pendingAllyRequests = new HashMap<>(); // target → requester

    /*
     * ────────────────────────────────────────────────────────────
     * Register / lookup
     * ──────────────────────────────────────────────────────────
     */
    public void registerClan(Clan clan) {
        String key = clan.getName().toLowerCase();
        clans.put(key, clan);
        clanMembers.put(key, new ArrayList<>(List.of(clan.getLeader())));
    }

    public void unregisterClan(String name) {
        String key = name.toLowerCase();

        /* verwijder uit centrale tabellen */
        clans.remove(key);
        clanMembers.remove(key);
        pendingAllyRequests.remove(key);

        /* verwijder als ally uit alle overgebleven clans */
        for (Clan c : clans.values()) {
            c.removeAlly(name);
        }

        /* verwijder eventuele openstaande requests waar deze clan requester was */
        pendingAllyRequests.entrySet()
                .removeIf(e -> e.getValue().equalsIgnoreCase(name));
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

    /*
     * ────────────────────────────────────────────────────────────
     * Leden-helpers
     * ──────────────────────────────────────────────────────────
     */
    public void addMember(Clan clan, UUID uuid) {
        clanMembers.computeIfAbsent(clan.getName().toLowerCase(), k -> new ArrayList<>())
                .add(uuid);
    }

    public List<UUID> getMembers(Clan clan) {
        return clanMembers.getOrDefault(clan.getName().toLowerCase(), Collections.emptyList());
    }

    public Clan getClanByMember(UUID uuid) {
        for (var entry : clanMembers.entrySet()) {
            if (entry.getValue().contains(uuid)) {
                return clans.get(entry.getKey());
            }
        }
        return null;
    }

    /*
     * ────────────────────────────────────────────────────────────
     * Pending ally-requests
     * ──────────────────────────────────────────────────────────
     */
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

    /*
     * ────────────────────────────────────────────────────────────
     * Ally-management (gebaseerd op Clan#getAllies)
     * ──────────────────────────────────────────────────────────
     */
    public void addAlly(String clanA, String clanB) {
        Clan a = clans.get(clanA.toLowerCase());
        Clan b = clans.get(clanB.toLowerCase());
        if (a != null && b != null) {
            a.addAlly(clanB);
            b.addAlly(clanA);
        }
    }

    public boolean areAllies(String clanA, String clanB) {
        Clan a = clans.get(clanA.toLowerCase());
        return a != null && a.isAlliedWith(clanB);
    }

    public Set<String> getAllies(String clanName) {
        Clan c = clans.get(clanName.toLowerCase());
        return (c != null) ? c.getAllies() : Collections.emptySet();
    }

    public void removeAlly(String clanA, String clanB) {
        Clan a = clans.get(clanA.toLowerCase());
        Clan b = clans.get(clanB.toLowerCase());
        if (a != null)
            a.removeAlly(clanB);
        if (b != null)
            b.removeAlly(clanA);
    }
}
