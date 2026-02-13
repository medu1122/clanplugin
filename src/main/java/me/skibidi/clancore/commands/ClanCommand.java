package me.skibidi.clancore.commands;

import me.skibidi.clancore.clan.ClanManager;
import me.skibidi.clancore.clan.model.Clan;
import me.skibidi.clancore.esp.EspManager;
import me.skibidi.clancore.war.WarManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ClanCommand implements CommandExecutor {

    private final ClanManager clanManager;
    private final WarManager warManager;
    private final EspManager espManager;

    public ClanCommand(ClanManager clanManager, WarManager warManager, EspManager espManager) {
        this.clanManager = clanManager;
        this.warManager = warManager;
        this.espManager = espManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage("/clan create <name>");
            player.sendMessage("/clan join <name>");
            player.sendMessage("/clan invite <player>");
            player.sendMessage("/clan accept|deny");
            player.sendMessage("/clan raccept <player> | /clan rdeny <player>");
            player.sendMessage("/clan requests");
            player.sendMessage("/clan leave");
            player.sendMessage("/clan kick <player>");
            player.sendMessage("/clan war <clan>");
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "create" -> handleCreate(player, args);
            case "join" -> handleJoin(player, args);
            case "invite" -> handleInvite(player, args);
            case "accept" -> handleAccept(player);
            case "deny" -> handleDeny(player);
            case "raccept" -> handleRequestAccept(player, args);
            case "rdeny" -> handleRequestDeny(player, args);
            case "requests" -> handleRequests(player);
            case "leave" -> handleLeave(player);
            case "kick" -> handleKick(player, args);
            case "war" -> handleWar(player, args);
            default -> player.sendMessage("Unknown subcommand.");
        }

        return true;
    }

    private void handleCreate(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("Usage: /clan create <name>");
            return;
        }

        String name = args[1];

        if (clanManager.createClan(name, player)) {
            player.sendMessage("Clan created!");
            espManager.updateFor(player);
        } else {
            player.sendMessage("Cannot create clan. Maybe you already have one or name is taken.");
        }
    }

    private void handleJoin(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("Usage: /clan join <name>");
            return;
        }

        Clan clan = clanManager.getClan(args[1]);
        if (clan == null) {
            player.sendMessage("Clan not found.");
            return;
        }

        if (clanManager.requestJoin(player, clan)) {
            player.sendMessage("Join request sent to clan " + clan.getName() + ".");
        } else {
            player.sendMessage("Cannot send join request.");
        }
    }

    private void handleInvite(Player player, String[] args) {
        Clan clan = clanManager.getClan(player);
        if (clan == null) {
            player.sendMessage("You are not in a clan.");
            return;
        }
        if (!clanManager.isOwner(player, clan)) {
            player.sendMessage("Only clan owner can invite.");
            return;
        }

        if (args.length < 2) {
            player.sendMessage("Usage: /clan invite <player>");
            return;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            player.sendMessage("Player not found.");
            return;
        }

        if (clanManager.invitePlayer(clan, target)) {
            player.sendMessage("Invited " + target.getName() + " to clan.");
            target.sendMessage("You have been invited to join clan " + clan.getName() + ". Use /clan accept or /clan deny.");
        } else {
            player.sendMessage("Cannot invite that player.");
        }
    }

    private void handleAccept(Player player) {
        if (clanManager.acceptInvite(player)) {
            player.sendMessage("You joined the clan.");
            espManager.updateFor(player);
        } else {
            player.sendMessage("You have no pending clan invite.");
        }
    }

    private void handleDeny(Player player) {
        if (clanManager.denyInvite(player)) {
            player.sendMessage("Invite denied.");
        } else {
            player.sendMessage("You have no pending clan invite.");
        }
    }

    private void handleRequestAccept(Player player, String[] args) {
        Clan clan = clanManager.getClan(player);
        if (clan == null || !clanManager.isOwner(player, clan)) {
            player.sendMessage("Only clan owner can accept join requests.");
            return;
        }

        if (args.length < 2) {
            player.sendMessage("Usage: /clan raccept <player>");
            return;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            player.sendMessage("Player must be online to accept.");
            return;
        }

        UUID uuid = target.getUniqueId();
        if (clanManager.acceptRequest(clan, uuid)) {
            player.sendMessage("Accepted join request from " + target.getName() + ".");
            target.sendMessage("Your join request to clan " + clan.getName() + " was accepted.");
            espManager.updateFor(target);
        } else {
            player.sendMessage("No join request from that player.");
        }
    }

    private void handleRequestDeny(Player player, String[] args) {
        Clan clan = clanManager.getClan(player);
        if (clan == null || !clanManager.isOwner(player, clan)) {
            player.sendMessage("Only clan owner can deny join requests.");
            return;
        }

        if (args.length < 2) {
            player.sendMessage("Usage: /clan rdeny <player>");
            return;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            player.sendMessage("Player must be online to deny.");
            return;
        }

        if (clanManager.denyRequest(clan, target.getUniqueId())) {
            player.sendMessage("Denied join request from " + target.getName() + ".");
            target.sendMessage("Your join request to clan " + clan.getName() + " was denied.");
        } else {
            player.sendMessage("No join request from that player.");
        }
    }

    private void handleRequests(Player player) {
        Clan clan = clanManager.getClan(player);
        if (clan == null || !clanManager.isOwner(player, clan)) {
            player.sendMessage("Only clan owner can view requests.");
            return;
        }

        if (clan.getJoinRequests().isEmpty()) {
            player.sendMessage("No join requests.");
            return;
        }

        player.sendMessage("Join requests:");
        for (UUID uuid : clan.getJoinRequests()) {
            Player p = Bukkit.getPlayer(uuid);
            player.sendMessage("- " + (p != null ? p.getName() : uuid));
        }
    }

    private void handleLeave(Player player) {
        if (!clanManager.isInClan(player)) {
            player.sendMessage("You are not in a clan.");
            return;
        }
        clanManager.leaveClan(player);
        espManager.clear(player);
        player.sendMessage("You left your clan.");
    }

    private void handleKick(Player player, String[] args) {
        Clan clan = clanManager.getClan(player);
        if (clan == null || !clanManager.isOwner(player, clan)) {
            player.sendMessage("Only clan owner can kick members.");
            return;
        }

        if (args.length < 2) {
            player.sendMessage("Usage: /clan kick <player>");
            return;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            player.sendMessage("Player must be online to kick.");
            return;
        }

        if (clanManager.kickMember(clan, target.getUniqueId())) {
            player.sendMessage("Kicked " + target.getName() + " from clan.");
            target.sendMessage("You have been kicked from clan " + clan.getName() + ".");
            espManager.clear(target);
        } else {
            player.sendMessage("Cannot kick that player.");
        }
    }

    private void handleWar(Player player, String[] args) {
        Clan own = clanManager.getClan(player);
        if (own == null || !clanManager.isOwner(player, own)) {
            player.sendMessage("Only clan owner can declare war.");
            return;
        }

        if (args.length < 2) {
            player.sendMessage("Usage: /clan war <clan>");
            return;
        }

        Clan targetClan = clanManager.getClan(args[1]);
        if (targetClan == null) {
            player.sendMessage("Target clan not found.");
            return;
        }

        if (warManager.startWar(own, targetClan) != null) {
            player.sendMessage("You declared war on " + targetClan.getName() + "!");
            espManager.updateAll();
        } else {
            player.sendMessage("You are already at war with that clan or invalid target.");
        }
    }
}

