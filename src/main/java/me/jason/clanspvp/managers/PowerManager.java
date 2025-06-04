package me.jason.clanspvp.managers;

import java.util.HashMap;
import java.util.UUID;

public class PowerManager {

    private final HashMap<UUID, Integer> playerPower = new HashMap<>();

    public int getPower(UUID player) {
        return playerPower.getOrDefault(player, 0);
    }

    public void setPower(UUID player, int power) {
        playerPower.put(player, power);
    }

    public void addPower(UUID player, int amount) {
        playerPower.put(player, getPower(player) + amount);
    }

    public void removePower(UUID player, int amount) {
        playerPower.put(player, Math.max(0, getPower(player) - amount));
    }

    public boolean hasEnoughPower(UUID player, int required) {
        return getPower(player) >= required;
    }
}
