package me.skibidi.clancore.flag.model;

import org.bukkit.DyeColor;
import org.bukkit.World;
import org.bukkit.block.Block;

/**
 * Cờ đã cắm - lãnh địa clan (vùng tròn bán kính 16).
 */
public class ClanFlag {

    private final int id;
    private final String clanName;
    private final String worldName;
    private final int x, y, z;
    private final String bannerColor;

    public ClanFlag(int id, String clanName, String worldName, int x, int y, int z, String bannerColor) {
        this.id = id;
        this.clanName = clanName;
        this.worldName = worldName;
        this.x = x;
        this.y = y;
        this.z = z;
        this.bannerColor = bannerColor != null ? bannerColor : "RED";
    }

    public int getId() { return id; }
    public String getClanName() { return clanName; }
    public String getWorldName() { return worldName; }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getZ() { return z; }
    public String getBannerColor() { return bannerColor; }

    public DyeColor getDyeColor() {
        try {
            return DyeColor.valueOf(bannerColor);
        } catch (Exception e) {
            return DyeColor.RED;
        }
    }

    /** Khoảng cách 3D từ (bx, by, bz) tới tâm cờ. */
    public double distanceSq(int bx, int by, int bz) {
        double dx = x - bx;
        double dy = y - by;
        double dz = z - bz;
        return dx * dx + dy * dy + dz * dz;
    }

    public boolean isInTerritory(int bx, int by, int bz, int radiusSq) {
        return getWorldName() != null && distanceSq(bx, by, bz) <= radiusSq;
    }
}
