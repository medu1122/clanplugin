package me.skibidi.clancore.war;

import me.skibidi.clancore.clan.ClanManager;
import me.skibidi.clancore.clan.model.Clan;
import me.skibidi.clancore.storage.repository.ClanWarRepository;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class WarManager {

    private final ClanManager clanManager;
    private final ClanWarRepository warRepository;
    /** clanName -> set of target clan names (war toggle ON from this clan to target). */
    private final Map<String, Set<String>> warToggles = new ConcurrentHashMap<>();

    public WarManager(ClanManager clanManager, ClanWarRepository warRepository) {
        this.clanManager = clanManager;
        this.warRepository = warRepository;
        load();
    }

    public void load() {
        warToggles.clear();
        try {
            Map<String, Set<String>> loaded = warRepository.loadAllWarToggles();
            warToggles.putAll(loaded);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Bật/tắt war với clan đích (chỉ phía clan của bạn).
     */
    public boolean setWarEnabled(Clan myClan, Clan targetClan, boolean enabled) {
        if (myClan == null || targetClan == null || myClan == targetClan) return false;
        String my = myClan.getName();
        String target = targetClan.getName();
        warToggles.computeIfAbsent(my, k -> ConcurrentHashMap.newKeySet());
        if (enabled) {
            warToggles.get(my).add(target);
        } else {
            warToggles.get(my).remove(target);
        }
        try {
            warRepository.setWarEnabled(my, target, enabled);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            if (enabled) warToggles.get(my).remove(target);
            else warToggles.get(my).add(target);
            return false;
        }
    }

    /** War được bật từ phía myClan với targetClan (một chiều). */
    public boolean isWarEnabled(Clan myClan, Clan targetClan) {
        if (myClan == null || targetClan == null) return false;
        Set<String> set = warToggles.get(myClan.getName());
        return set != null && set.contains(targetClan.getName());
    }

    /** Cả hai clan đều bật war với nhau thì mới là đang chiến tranh. */
    public boolean isAtWar(Clan a, Clan b) {
        if (a == null || b == null) return false;
        return isWarEnabled(a, b) && isWarEnabled(b, a);
    }

    public boolean isClanAtWar(Clan clan) {
        if (clan == null) return false;
        String name = clan.getName();
        Set<String> myTargets = warToggles.get(name);
        if (myTargets != null) {
            for (String target : myTargets) {
                Clan other = clanManager.getClan(target);
                if (other != null && isWarEnabled(other, clan)) return true;
            }
        }
        return false;
    }

    public boolean isAtWar(Player a, Player b, Clan clanA, Clan clanB) {
        return isAtWar(clanA, clanB);
    }

    /** Trả về set tên clan mà myClan đang bật war với. */
    public Set<String> getWarTargets(Clan myClan) {
        if (myClan == null) return Collections.emptySet();
        Set<String> set = warToggles.get(myClan.getName());
        return set == null ? Collections.emptySet() : new HashSet<>(set);
    }
}

