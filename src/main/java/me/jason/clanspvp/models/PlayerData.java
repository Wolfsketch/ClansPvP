package me.jason.clanspvp.models;

import java.util.UUID;

public class PlayerData {

    private final UUID uuid;
    private double kills = 0;
    private double deaths = 0;
    private Clan clan;
    private String clanRole;

    public PlayerData(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getUuid() {
        return uuid;
    }

    public Clan getClan() {
        return clan;
    }

    public void setClan(Clan clan) {
        this.clan = clan;
    }

    public String getClanRole() {
        return clanRole;
    }

    public void setClanRole(String role) {
        this.clanRole = role;
    }

    public void addKill() {
        kills++;
    }

    public void addDeath() {
        deaths++;
    }

    public double getKDR() {
        return deaths == 0 ? kills : kills / deaths;
    }

}
