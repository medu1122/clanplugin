package me.skibidi.clancore.commands;

import me.skibidi.clancore.team.TeamManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TeamCommand implements CommandExecutor {

    private final TeamManager teamManager;

    public TeamCommand(TeamManager teamManager) {
        this.teamManager = teamManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage("/team create");
            player.sendMessage("/team invite <player>");
            player.sendMessage("/team accept");
            player.sendMessage("/team leave");
            player.sendMessage("/team kick <player>");
            player.sendMessage("/team disband");
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "create" -> handleCreate(player);
            case "invite" -> handleInvite(player, args);
            case "accept" -> handleAccept(player);
            case "leave" -> handleLeave(player);
            case "kick" -> handleKick(player, args);
            case "disband" -> handleDisband(player);
            default -> player.sendMessage("Unknown subcommand.");
        }

        return true;
    }

    private void handleCreate(Player player) {
        if (teamManager.createTeam(player)) {
            player.sendMessage("Team created.");
        } else {
            player.sendMessage("You are already in a team.");
        }
    }

    private void handleInvite(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("Usage: /team invite <player>");
            return;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            player.sendMessage("Player not found.");
            return;
        }

        if (teamManager.invite(player, target)) {
            player.sendMessage("Invited " + target.getName() + " to your team.");
            target.sendMessage("You have been invited to a team. Use /team accept to join.");
        } else {
            player.sendMessage("Cannot invite that player.");
        }
    }

    private void handleAccept(Player player) {
        if (teamManager.accept(player)) {
            player.sendMessage("You joined the team.");
        } else {
            player.sendMessage("You have no pending team invite.");
        }
    }

    private void handleLeave(Player player) {
        if (teamManager.leave(player)) {
            player.sendMessage("You left your team.");
        } else {
            player.sendMessage("You are not in a team.");
        }
    }

    private void handleKick(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("Usage: /team kick <player>");
            return;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            player.sendMessage("Player not found.");
            return;
        }

        if (teamManager.kick(player, target)) {
            player.sendMessage("Kicked " + target.getName() + " from the team.");
            target.sendMessage("You have been kicked from the team.");
        } else {
            player.sendMessage("Cannot kick that player.");
        }
    }

    private void handleDisband(Player player) {
        if (teamManager.disband(player)) {
            player.sendMessage("Team disbanded.");
        } else {
            player.sendMessage("You are not the leader of a team.");
        }
    }
}

