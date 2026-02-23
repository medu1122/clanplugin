package me.skibidi.clancore.flag;

import me.skibidi.clancore.clan.ClanManager;
import me.skibidi.clancore.clan.model.Clan;
import me.skibidi.clancore.flag.model.ClanFlag;
import me.skibidi.clancore.storage.repository.ClanFlagRepository;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Quản lý cờ clan: cắm cờ, vùng 16 block, effect, damage.
 */
public class FlagManager {

    public static final int TERRITORY_RADIUS = 16;
    public static final int POLE_HEIGHT = 5;
    private static final String FLAG_LORE_PREFIX = "§6Cờ Clan §7- §e";

    private final ClanManager clanManager;
    private final ClanFlagRepository flagRepository;
    private final Map<Integer, ClanFlag> flagsById = new ConcurrentHashMap<>();
    private final List<ClanFlag> flagsList = Collections.synchronizedList(new ArrayList<>());
    /** Player UUID -> set of flag IDs they are currently inside (for message on enter). */
    private final Map<UUID, Set<Integer>> playerInFlagIds = new ConcurrentHashMap<>();
    /** Player UUID -> last time we applied damage (ms). */
    private final Map<UUID, Long> lastDamageTime = new ConcurrentHashMap<>();

    public FlagManager(ClanManager clanManager, ClanFlagRepository flagRepository) {
        this.clanManager = clanManager;
        this.flagRepository = flagRepository;
        load();
    }

    public void load() {
        flagsById.clear();
        flagsList.clear();
        try {
            List<ClanFlag> loaded = flagRepository.loadAll();
            for (ClanFlag f : loaded) {
                flagsById.put(f.getId(), f);
                flagsList.add(f);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<ClanFlag> getAllFlags() {
        return new ArrayList<>(flagsList);
    }

    public ClanFlag getFlagById(int id) {
        return flagsById.get(id);
    }

    /**
     * Cắm cờ tại block (block là nền, xây cột + cờ phía trên).
     * @return ClanFlag nếu thành công, null nếu thất bại.
     */
    public ClanFlag placeFlag(World world, int baseX, int baseY, int baseZ, String clanName, String bannerColor) {
        if (world == null || clanName == null) return null;
        try {
            int id = flagRepository.insert(clanName, world.getName(), baseX, baseY, baseZ, bannerColor);
            if (id <= 0) return null;
            ClanFlag flag = new ClanFlag(id, clanName, world.getName(), baseX, baseY, baseZ, bannerColor);
            flagsById.put(id, flag);
            flagsList.add(flag);
            buildPole(world, baseX, baseY, baseZ, bannerColor);
            return flag;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /** Xây cột cao + cờ (5 block fence + 1 banner). */
    private void buildPole(World world, int x, int y, int z, String bannerColor) {
        Material fence = Material.OAK_FENCE;
        for (int i = 1; i <= POLE_HEIGHT; i++) {
            Block b = world.getBlockAt(x, y + i, z);
            b.setType(fence);
        }
        Block top = world.getBlockAt(x, y + POLE_HEIGHT + 1, z);
        Material bannerType = getBannerMaterial(bannerColor);
        top.setType(bannerType);
    }

    private static Material getBannerMaterial(String color) {
        if (color == null) color = "RED";
        try {
            return Material.valueOf(color + "_BANNER");
        } catch (Exception e) {
            return Material.RED_BANNER;
        }
    }

    /**
     * Trả về clan đang sở hữu vùng tại vị trí (world, x, y, z), null nếu không thuộc cờ nào.
     */
    public Clan getClanAt(World world, int x, int y, int z) {
        if (world == null) return null;
        String worldName = world.getName();
        int radiusSq = TERRITORY_RADIUS * TERRITORY_RADIUS;
        for (ClanFlag f : flagsList) {
            if (!f.getWorldName().equals(worldName)) continue;
            if (f.isInTerritory(x, y, z, radiusSq))
                return clanManager.getClan(f.getClanName());
        }
        return null;
    }

    /**
     * Trả về cờ mà vị trí này nằm trong lãnh thổ (bất kỳ).
     */
    public ClanFlag getFlagAt(World world, int x, int y, int z) {
        if (world == null) return null;
        String worldName = world.getName();
        int radiusSq = TERRITORY_RADIUS * TERRITORY_RADIUS;
        for (ClanFlag f : flagsList) {
            if (!f.getWorldName().equals(worldName)) continue;
            if (f.isInTerritory(x, y, z, radiusSq)) return f;
        }
        return null;
    }

    public boolean isInTerritory(Player player, String clanName) {
        Location loc = player.getLocation();
        ClanFlag f = getFlagAt(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        return f != null && f.getClanName().equals(clanName);
    }

    /**
     * Tạo item Cờ Clan (để cắm) - lore chứa tên clan.
     */
    public static org.bukkit.inventory.ItemStack createFlagItem(String clanName, String bannerColor) {
        Material mat = getBannerMaterial(bannerColor);
        org.bukkit.inventory.ItemStack item = new org.bukkit.inventory.ItemStack(mat);
        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§6§lCờ Clan §7- §e" + clanName);
            meta.setLore(List.of(FLAG_LORE_PREFIX + clanName, "§7Click chuột phải vào block để cắm cờ."));
        }
        item.setItemMeta(meta);
        return item;
    }

    /** Lấy tên clan từ item (lore). */
    public static String getClanNameFromFlagItem(org.bukkit.inventory.ItemStack item) {
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasLore()) return null;
        List<String> lore = item.getItemMeta().getLore();
        if (lore == null) return null;
        for (String line : lore) {
            if (line != null && line.startsWith(FLAG_LORE_PREFIX)) {
                return line.substring(FLAG_LORE_PREFIX.length()).trim();
            }
        }
        return null;
    }

    public static boolean isFlagItem(org.bukkit.inventory.ItemStack item) {
        return getClanNameFromFlagItem(item) != null;
    }

    /**
     * Cập nhật trạng thái player trong vùng cờ: gửi tin nhắn khi vào (một lần), trả về set flag IDs đang đứng trong (không thuộc clan cờ đó).
     */
    public Set<Integer> updatePlayerInFlags(Player player) {
        UUID uuid = player.getUniqueId();
        Location loc = player.getLocation();
        if (loc.getWorld() == null) return Set.of();
        int radiusSq = TERRITORY_RADIUS * TERRITORY_RADIUS;
        Set<Integer> nowInside = new HashSet<>();
        for (ClanFlag f : flagsList) {
            if (!f.getWorldName().equals(loc.getWorld().getName())) continue;
            if (!f.isInTerritory(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), radiusSq)) continue;
            nowInside.add(f.getId());
        }
        Set<Integer> wasInside = playerInFlagIds.getOrDefault(uuid, Set.of());
        playerInFlagIds.put(uuid, nowInside);
        Clan playerClan = clanManager.getClan(player);
        String playerClanName = playerClan != null ? playerClan.getName() : null;
        for (Integer flagId : nowInside) {
            ClanFlag f = flagsById.get(flagId);
            if (f == null) continue;
            if (f.getClanName().equals(playerClanName)) continue;
            if (!wasInside.contains(flagId)) {
                player.sendMessage("§c§l[ CẢNH BÁO ] §eBạn đã vào lãnh địa của clan §6" + f.getClanName() + "§e. Người ngoài bị sát thương!");
            }
        }
        return nowInside;
    }

    /**
     * Trả về true nếu nên gây damage (player đang trong vùng cờ không phải clan mình, và đã qua 2 giây).
     */
    public boolean shouldDamageAndTick(Player player) {
        Set<Integer> inFlags = playerInFlagIds.getOrDefault(player.getUniqueId(), Set.of());
        if (inFlags.isEmpty()) return false;
        Clan clan = clanManager.getClan(player);
        String myClan = clan != null ? clan.getName() : null;
        for (Integer id : inFlags) {
            ClanFlag f = flagsById.get(id);
            if (f != null && !f.getClanName().equals(myClan))
                return true;
        }
        return false;
    }

    public void applyTerritoryDamage(Player player) {
        if (!shouldDamageAndTick(player)) return;
        long now = System.currentTimeMillis();
        if (now - lastDamageTime.getOrDefault(player.getUniqueId(), 0L) < 2000) return;
        lastDamageTime.put(player.getUniqueId(), now);
        player.damage(1.0);
        player.sendMessage("§c§lLãnh địa clan §7- §cBạn đang bị sát thương!");
    }

    public void playTerritoryEffects(World world) {
        if (world == null) return;
        for (ClanFlag f : flagsList) {
            if (!f.getWorldName().equals(world.getName())) continue;
            int cx = f.getX(), cy = f.getY(), cz = f.getZ();
            double r = TERRITORY_RADIUS;
            for (double angle = 0; angle < Math.PI * 2; angle += Math.PI / 16) {
                int px = (int) (cx + r * Math.cos(angle));
                int pz = (int) (cz + r * Math.sin(angle));
                world.spawnParticle(Particle.DUST, px + 0.5, cy + 0.5, pz + 0.5, 1, 0, 0.1, 0, 0,
                    new Particle.DustOptions(Color.RED, 1.5f));
            }
        }
    }

    /** Hiệu ứng xanh cho người cùng clan đứng trong lãnh thổ. */
    public void playAllyEffects(Player player) {
        Clan clan = clanManager.getClan(player);
        if (clan == null) return;
        Location loc = player.getLocation();
        ClanFlag at = getFlagAt(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        if (at == null || !at.getClanName().equals(clan.getName())) return;
        World w = player.getWorld();
        w.spawnParticle(Particle.DUST, loc.getX(), loc.getY() + 1, loc.getZ(), 3, 0.3, 0.3, 0.3, 0,
            new Particle.DustOptions(Color.BLUE, 1.2f));
    }
}
