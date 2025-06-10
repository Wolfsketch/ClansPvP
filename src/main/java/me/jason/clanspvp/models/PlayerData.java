package me.jason.clanspvp.models;

import java.util.UUID;

public class PlayerData {

    private final UUID uuid;
    private double kills = 0;
    private double deaths = 0;
    private double power = 5.0;
    private Clan clan;
    private String clanRole;
    private String pendingInvite;
    private String pendingJoinRequest;

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

    public double getPower() {
        return power;
    }

    public void setPower(double power) {
        this.power = power;
    }

    public void setKills(double kills) {
        this.kills = kills;
    }

    public double getKills() {
        return kills;
    }

    public void setDeaths(double deaths) {
        this.deaths = deaths;
    }

    public double getDeaths() {
        return deaths;
    }

    public String getPendingInvite() {
        return pendingInvite;
    }

    public void setPendingInvite(String pendingInvite) {
        this.pendingInvite = pendingInvite;
    }

    public void clearPendingInvite() {
        this.pendingInvite = null;
    }

    public String getPendingJoinRequest() {
        return pendingJoinRequest;
    }

    public void setPendingJoinRequest(String pendingJoinRequest) {
        this.pendingJoinRequest = pendingJoinRequest;
    }

    public void clearPendingJoinRequest() {
        this.pendingJoinRequest = null;
    }
}
