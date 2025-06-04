package me.jason.clanspvp.managers;

import org.bukkit.Chunk;

import java.util.HashMap;
import java.util.Map;

public class ClaimManager {

    private final Map<String, Integer> claimCounts = new HashMap<>(); // clanName → aantal claims
    private final Map<Chunk, String> claimedChunks = new HashMap<>(); // chunk → clanName

    // Check of een chunk al geclaimd is
    public boolean isClaimed(Chunk chunk) {
        return claimedChunks.containsKey(chunk);
    }

    // Krijg de eigenaar van een chunk
    public String getClaimOwner(Chunk chunk) {
        return claimedChunks.get(chunk);
    }

    // Claimen van een chunk
    public boolean claimChunk(String clanName, Chunk chunk, int maxChunksPerClan) {
        if (isClaimed(chunk))
            return false;

        int currentClaims = claimCounts.getOrDefault(clanName, 0);
        if (currentClaims >= maxChunksPerClan)
            return false;

        claimedChunks.put(chunk, clanName);
        claimCounts.put(clanName, currentClaims + 1);
        return true;
    }

    // Unclaim een chunk
    public boolean unclaimChunk(Chunk chunk) {
        String clan = claimedChunks.remove(chunk);
        if (clan != null) {
            int currentClaims = claimCounts.getOrDefault(clan, 1);
            claimCounts.put(clan, Math.max(0, currentClaims - 1));
            return true;
        }
        return false;
    }

    // Totaal aantal claims van een clan
    public int getClaimCount(String clanName) {
        return claimCounts.getOrDefault(clanName, 0);
    }

    // Verwijder alle claims van een clan (optioneel bij disband)
    public void removeAllClaims(String clanName) {
        claimedChunks.entrySet().removeIf(entry -> entry.getValue().equalsIgnoreCase(clanName));
        claimCounts.remove(clanName);
    }
}
