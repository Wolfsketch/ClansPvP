package me.jason.clanspvp.models;

import java.util.UUID;

/**
 * Houdt alle per-speler data bij die ClansPvP nodig heeft.
 * Wordt in ClansPvP#getPlayerData(...) in-memory beheerd en
 * bij save/reload naar disk (YAML/JSON) opgeslagen.
 */
public class PlayerData {

    /* ── Identiteit ───────────────────────────────────────────── */
    private final UUID uuid;

    /* ── Combat / power ───────────────────────────────────────── */
    private double kills = 0;
    private double deaths = 0;
    private double power = 5.0;

    /* ── Clan & invites ───────────────────────────────────────── */
    private Clan clan;
    private String clanRole; // LEADER, OFFICER, MEMBER, …
    private String pendingInvite; // clan-naam
    private String pendingJoinRequest; // clan-naam

    /* ── Warp cooldowns (epoch-seconden) ──────────────────────── */
    private long nextWarpTime = 0; // persoonlijke cooldown voor /clan warp
    private long nextAllyWarpTime = 0; // cooldown voor /clan ally warp

    /* ─────────────────────────────────────────────────────────── */

    public PlayerData(UUID uuid) {
        this.uuid = uuid;
    }

    /* ── Identiteit ───────────────────────────────────────────── */
    public UUID getUuid() {
        return uuid;
    }

    /* ── Clan data ────────────────────────────────────────────── */
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

    /* ── Combat / power ───────────────────────────────────────── */
    public void addKill() {
        kills++;
    }

    public void addDeath() {
        deaths++;
    }

    public double getKills() {
        return kills;
    }

    public void setKills(double kills) {
        this.kills = kills;
    }

    public double getDeaths() {
        return deaths;
    }

    public void setDeaths(double deaths) {
        this.deaths = deaths;
    }

    public double getKDR() {
        return deaths == 0 ? kills : kills / deaths;
    }

    public double getPower() {
        return power;
    }

    public void setPower(double p) {
        this.power = p;
    }

    /* ── Invites / Join requests ──────────────────────────────── */
    public String getPendingInvite() {
        return pendingInvite;
    }

    public void setPendingInvite(String c) {
        this.pendingInvite = c;
    }

    public void clearPendingInvite() {
        this.pendingInvite = null;
    }

    public String getPendingJoinRequest() {
        return pendingJoinRequest;
    }

    public void setPendingJoinRequest(String c) {
        this.pendingJoinRequest = c;
    }

    public void clearPendingJoinRequest() {
        this.pendingJoinRequest = null;
    }

    /* ── Warp cooldowns ───────────────────────────────────────── */
    /** Unix-tijd in seconden waarop de speler weer mag /clan warp-en */
    public long getNextWarpTime() {
        return nextWarpTime;
    }

    public void setNextWarpTime(long epoch) {
        this.nextWarpTime = epoch;
    }

    /** Unix-tijd in seconden waarop de speler weer /clan ally warp mag */
    public long getNextAllyWarpTime() {
        return nextAllyWarpTime;
    }

    public void setNextAllyWarpTime(long epoch) {
        this.nextAllyWarpTime = epoch;
    }
}
