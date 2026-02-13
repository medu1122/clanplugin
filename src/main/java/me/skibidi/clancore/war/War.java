package me.skibidi.clancore.war;

import me.skibidi.clancore.clan.model.Clan;

public class War {

    private final Clan clanA;
    private final Clan clanB;

    public War(Clan clanA, Clan clanB) {
        this.clanA = clanA;
        this.clanB = clanB;
    }

    public Clan getClanA() {
        return clanA;
    }

    public Clan getClanB() {
        return clanB;
    }

    public boolean involves(Clan clan) {
        return clanA == clan || clanB == clan;
    }
}
