package me.jason.clanspvp.managers;

import org.bukkit.Chunk;

import java.util.HashMap;
import java.util.Map;

public class ClaimManager {

    private final Map<String, Integer> claimCounts = new HashMap<>(); // clanName → #claims
    private final Map<Chunk, String> claimedChunks = new HashMap<>(); // chunk → clanName

    public boolean isClaimed(Chunk chunk) {
        return claimedChunks.containsKey(chunk);
    }

    public String getClaimOwner(Chunk chunk) {
        return claimedChunks.get(chunk);
    }

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

    public boolean unclaimChunk(Chunk chunk) {
        String clan = claimedChunks.remove(chunk);
        if (clan != null) {
            int currentClaims = claimCounts.getOrDefault(clan, 1);
            claimCounts.put(clan, Math.max(0, currentClaims - 1));
            return true;
        }
        return false;
    }

    public int getClaimCount(String clanName) {
        return claimCounts.getOrDefault(clanName, 0);
    }

    public void removeAllClaims(String clanName) {
        claimedChunks.entrySet().removeIf(entry -> {
            boolean match = entry.getValue().equalsIgnoreCase(clanName);
            if (match)
                claimCounts.put(clanName, claimCounts.get(clanName) - 1);
            return match;
        });
        if (claimCounts.getOrDefault(clanName, 0) <= 0) {
            claimCounts.remove(clanName);
        }
    }

    public String getClaimKey(Chunk chunk) {
        return chunk.getWorld().getName() + ";" + chunk.getX() + ";" + chunk.getZ();
    }

    public void forceClaimChunk(String clanName, Chunk chunk) {
        claimedChunks.put(chunk, clanName);
        claimCounts.put(clanName, claimCounts.getOrDefault(clanName, 0) + 1);
    }

    public Map<Chunk, String> getAllClaims() {
        return claimedChunks;
    }
}
