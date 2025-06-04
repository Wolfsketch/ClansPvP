package me.jason.clanspvp.models;

import java.util.UUID;

public class Clan {

    private final String name;
    private final String tag;
    private final UUID leader;

    // Coördinaten voor actieve raid, indien ingesteld met /clan raid start
    private double raidX = 0;
    private double raidY = 0;
    private double raidZ = 0;
    private boolean raidActive = false;

    public Clan(String name, String tag, UUID leader) {
        this.name = name;
        this.tag = tag;
        this.leader = leader;
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

    // Raid locatie setters/getters
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
