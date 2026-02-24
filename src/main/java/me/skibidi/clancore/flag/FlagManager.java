package me.skibidi.clancore.flag;

import me.skibidi.clancore.clan.ClanManager;
import me.skibidi.clancore.clan.model.Clan;
import me.skibidi.clancore.config.ConfigManager;
import me.skibidi.clancore.flag.model.ClanFlag;
import me.skibidi.clancore.storage.repository.ClanFlagPoolRepository;
import me.skibidi.clancore.storage.repository.ClanFlagRepository;
import me.skibidi.clancore.storage.repository.FlagInventoryRepository;
import me.skibidi.clancore.storage.repository.FlagPermissionsRepository;
import org.bukkit.*;
import org.bukkit.inventory.ItemStack;
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
    /** Raid: cướp 5% đá, tối đa 300. Khi tổng đá < 300 không cướp thêm. */
    public static final int RAID_PERCENT = 5;
    public static final int RAID_MAX_GEMS = 300;
    /** Máu cờ = 5 lần máu người chơi (20), tránh spam tin nhắn khi bị tấn công. */
    public static final int FLAG_MAX_HP = 5 * 20;
    public static final int FLAG_DAMAGE_PER_HIT = 20;
    private static final String FLAG_LORE_PREFIX = "§6Cờ Clan §7- §e";

    private final ClanManager clanManager;
    private final ClanFlagRepository flagRepository;
    private final FlagInventoryRepository inventoryRepository;
    private final FlagPermissionsRepository permissionsRepository;
    private final ClanFlagPoolRepository poolRepository;
    private final ConfigManager configManager;
    private final Map<Integer, ClanFlag> flagsById = new ConcurrentHashMap<>();
    private final List<ClanFlag> flagsList = Collections.synchronizedList(new ArrayList<>());
    /** Player UUID -> set of flag IDs they are currently inside (for message on enter). */
    private final Map<UUID, Set<Integer>> playerInFlagIds = new ConcurrentHashMap<>();
    /** Player UUID -> last time we applied damage (ms). */
    private final Map<UUID, Long> lastDamageTime = new ConcurrentHashMap<>();
    /** flagId -> máu hiện tại (mặc định FLAG_MAX_HP). */
    private final Map<Integer, Integer> flagHealth = new ConcurrentHashMap<>();

    public FlagManager(ClanManager clanManager, ClanFlagRepository flagRepository,
                      FlagInventoryRepository inventoryRepository, FlagPermissionsRepository permissionsRepository,
                      ClanFlagPoolRepository poolRepository, ConfigManager configManager) {
        this.clanManager = clanManager;
        this.flagRepository = flagRepository;
        this.inventoryRepository = inventoryRepository;
        this.permissionsRepository = permissionsRepository;
        this.poolRepository = poolRepository;
        this.configManager = configManager;
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

    /** Máu hiện tại của cờ (mặc định full). */
    public int getFlagHealth(int flagId) {
        return flagHealth.getOrDefault(flagId, FLAG_MAX_HP);
    }

    /** Gây sát thương lên cờ, trả về máu còn lại sau khi trừ. */
    public int damageFlag(int flagId, int amount) {
        int current = getFlagHealth(flagId);
        int next = Math.max(0, current - amount);
        flagHealth.put(flagId, next);
        return next;
    }

    /** Reset máu cờ về đầy (sau khi raid xong, cờ hồi). */
    public void resetFlagHealth(int flagId) {
        flagHealth.put(flagId, FLAG_MAX_HP);
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

    /** Trả về cờ nếu block (bx, by, bz) là một block của cột cờ (pole/banner). */
    public ClanFlag getFlagByBlock(World world, int bx, int by, int bz) {
        if (world == null) return null;
        String wn = world.getName();
        for (ClanFlag f : flagsList) {
            if (!f.getWorldName().equals(wn)) continue;
            if (f.getX() != bx || f.getZ() != bz) continue;
            if (by >= f.getY() && by <= f.getY() + POLE_HEIGHT + 1) return f;
        }
        return null;
    }

    public Material getGemMaterial() {
        return configManager != null ? configManager.getGemMaterial() : Material.EMERALD;
    }

    public long getTotalGems(int flagId) {
        try {
            Map<Integer, Integer> inv = inventoryRepository.load(flagId);
            long total = 0;
            for (Integer amt : inv.values()) total += amt != null ? amt : 0;
            return total;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public Map<Integer, Integer> loadInventory(int flagId) {
        try {
            return inventoryRepository.load(flagId);
        } catch (SQLException e) {
            e.printStackTrace();
            Map<Integer, Integer> empty = new HashMap<>();
            for (int i = 0; i < 54; i++) empty.put(i, 0);
            return empty;
        }
    }

    public void saveInventory(int flagId, Map<Integer, Integer> slots) {
        try {
            inventoryRepository.save(flagId, slots);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean canOpenStorage(int flagId, UUID uuid) {
        ClanFlag f = flagsById.get(flagId);
        if (f == null) return false;
        Clan clan = clanManager.getClan(f.getClanName());
        if (clan == null) return false;
        if (clan.getOwner().equals(uuid)) return true;
        try {
            return permissionsRepository.loadCanOpen(flagId).contains(uuid);
        } catch (SQLException e) {
            return false;
        }
    }

    public void setCanOpenStorage(int flagId, UUID uuid, boolean canOpen) {
        try {
            permissionsRepository.setCanOpen(flagId, uuid, canOpen);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Set<UUID> getCanOpenList(int flagId) {
        try {
            return permissionsRepository.loadCanOpen(flagId);
        } catch (SQLException e) {
            return Set.of();
        }
    }

    /** Số cờ có thể lấy từ pool (level 5 = 1, level 6 = 2, ...). */
    public int getAvailableFlagsToTake(Clan clan) {
        if (clan == null || clan.getLevel() < 5) return 0;
        try {
            int taken = poolRepository.getTakenCount(clan.getName());
            int earned = clan.getLevel() - 4;
            return Math.max(0, earned - taken);
        } catch (SQLException e) {
            return 0;
        }
    }

    /** Lấy 1 cờ từ pool, trả về true nếu thành công. */
    public boolean takeFlagFromPool(Clan clan, Player player) {
        if (getAvailableFlagsToTake(clan) <= 0) return false;
        try {
            poolRepository.incrementTaken(clan.getName());
            org.bukkit.inventory.ItemStack flag = createFlagItem(clan.getName(), "RED");
            if (player.getInventory().firstEmpty() == -1)
                player.getWorld().dropItemNaturally(player.getLocation(), flag);
            else
                player.getInventory().addItem(flag);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
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
     * Trả về true nếu nên gây damage (player đang trong vùng cờ địch, không phải vùng giao thoa).
     * Vùng giao thoa: cờ đồng minh đã cắm trong vòng cờ địch → không gây damage.
     */
    public boolean shouldDamageAndTick(Player player) {
        Set<Integer> inFlags = playerInFlagIds.getOrDefault(player.getUniqueId(), Set.of());
        if (inFlags.isEmpty()) return false;
        Clan clan = clanManager.getClan(player);
        String myClan = clan != null ? clan.getName() : null;
        for (Integer id : inFlags) {
            ClanFlag f = flagsById.get(id);
            if (f == null || f.getClanName().equals(myClan)) continue;
            if (myClan != null && hasAllyFlagInRadius(f, myClan)) continue; // vùng giao thoa, không damage
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

    /** Khôi phục cột cờ (sau raid thường – cờ hồi). */
    public void restoreFlagBlocks(ClanFlag flag) {
        if (flag == null) return;
        World world = Bukkit.getWorld(flag.getWorldName());
        if (world == null) return;
        buildPole(world, flag.getX(), flag.getY(), flag.getZ(), flag.getBannerColor());
    }

    /** Xóa toàn bộ block cột cờ (set AIR). */
    public void removeFlagBlocks(ClanFlag flag) {
        if (flag == null) return;
        World world = Bukkit.getWorld(flag.getWorldName());
        if (world == null) return;
        int x = flag.getX(), y = flag.getY(), z = flag.getZ();
        for (int i = 0; i <= POLE_HEIGHT; i++) {
            world.getBlockAt(x, y + i, z).setType(Material.AIR);
        }
        world.getBlockAt(x, y + POLE_HEIGHT + 1, z).setType(Material.AIR);
    }

    /**
     * Trừ đá từ kho cờ, trả về số đá đã trừ (có thể ít hơn amount nếu không đủ).
     */
    public int removeGemsFromInventory(int flagId, int amount) {
        Map<Integer, Integer> inv = loadInventory(flagId);
        int left = amount;
        for (int slot = 0; slot < 54 && left > 0; slot++) {
            int amt = inv.getOrDefault(slot, 0);
            if (amt <= 0) continue;
            int take = Math.min(amt, left);
            inv.put(slot, amt - take);
            left -= take;
        }
        saveInventory(flagId, inv);
        return amount - left;
    }

    /**
     * Có cờ của clan allyClanName nằm trong bán kính TERRITORY_RADIUS của cờ enemyFlag (điều kiện war phá hoàn toàn).
     */
    public boolean hasAllyFlagInRadius(ClanFlag enemyFlag, String allyClanName) {
        if (enemyFlag == null || allyClanName == null) return false;
        int radiusSq = TERRITORY_RADIUS * TERRITORY_RADIUS;
        String wn = enemyFlag.getWorldName();
        int ex = enemyFlag.getX(), ey = enemyFlag.getY(), ez = enemyFlag.getZ();
        for (ClanFlag f : flagsList) {
            if (!f.getClanName().equals(allyClanName) || !f.getWorldName().equals(wn)) continue;
            if (f.getId() == enemyFlag.getId()) continue;
            double dx = f.getX() - ex, dy = f.getY() - ey, dz = f.getZ() - ez;
            if (dx * dx + dy * dy + dz * dz <= radiusSq) return true;
        }
        return false;
    }

    /**
     * Phá hủy cờ hoàn toàn: drop toàn bộ đá tại vị trí, xóa DB và block. Gọi sau khi đã kiểm tra war + ally flag trong vùng.
     */
    public void destroyFlagCompletely(ClanFlag flag, Location dropAt) {
        if (flag == null || dropAt == null || dropAt.getWorld() == null) return;
        int flagId = flag.getId();
        Map<Integer, Integer> inv = loadInventory(flagId);
        Material gem = getGemMaterial();
        for (Map.Entry<Integer, Integer> e : inv.entrySet()) {
            int amt = e.getValue() == null ? 0 : e.getValue();
            if (amt <= 0) continue;
            int stackSize = Math.min(amt, gem.getMaxStackSize());
            ItemStack stack = new ItemStack(gem, stackSize);
            dropAt.getWorld().dropItemNaturally(dropAt, stack);
            amt -= stackSize;
            while (amt > 0) {
                int next = Math.min(amt, gem.getMaxStackSize());
                dropAt.getWorld().dropItemNaturally(dropAt, new ItemStack(gem, next));
                amt -= next;
            }
        }
        try {
            inventoryRepository.deleteByFlagId(flagId);
            permissionsRepository.deleteByFlagId(flagId);
            flagRepository.delete(flagId);
        } catch (java.sql.SQLException ex) {
            ex.printStackTrace();
        }
        flagsById.remove(flagId);
        flagsList.removeIf(f -> f.getId() == flagId);
        flagHealth.remove(flagId);
        removeFlagBlocks(flag);
    }
}
