package me.skibidi.clancore.war;

import me.skibidi.clancore.clan.model.Clan;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

public class WarManager {

    private final Set<War> activeWars = new HashSet<>();

    public War startWar(Clan a, Clan b) {
        if (a == null || b == null || a == b) return null;
        if (isAtWar(a, b)) return null;

        War war = new War(a, b);
        activeWars.add(war);
        return war;
    }

    public boolean isAtWar(Clan a, Clan b) {
        if (a == null || b == null) return false;
        for (War war : activeWars) {
            if ((war.getClanA() == a && war.getClanB() == b) ||
                (war.getClanA() == b && war.getClanB() == a)) {
                return true;
            }
        }
        return false;
    }

    public boolean isClanAtWar(Clan clan) {
        if (clan == null) return false;
        for (War war : activeWars) {
            if (war.involves(clan)) return true;
        }
        return false;
    }

    public boolean isAtWar(Player a, Player b, Clan clanA, Clan clanB) {
        return isAtWar(clanA, clanB);
    }

    public void endWar(Clan a, Clan b) {
        activeWars.removeIf(war ->
                (war.getClanA() == a && war.getClanB() == b) ||
                (war.getClanA() == b && war.getClanB() == a));
    }
}

