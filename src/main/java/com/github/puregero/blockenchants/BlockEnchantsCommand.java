package com.github.puregero.blockenchants;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class BlockEnchantsCommand implements CommandExecutor {
    private final BlockEnchantsPlugin plugin;

    public BlockEnchantsCommand(BlockEnchantsPlugin plugin) {
        this.plugin = plugin;
        Objects.requireNonNull(plugin.getCommand("blockenchants")).setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        String subcommand = args.length > 0 ? args[0] : "";

        if (subcommand.equalsIgnoreCase("reload")) {
            for (Component component : plugin.loadConfig()) {
                sender.sendMessage(component);
            }
            sender.sendMessage(Component.text("[" + plugin.getName() + "] Config reloaded").color(NamedTextColor.GREEN));
        } else if (subcommand.equalsIgnoreCase("version")) {
            sender.sendMessage(Component.text("[" + plugin.getName() + "] Version: " + plugin.getDescription().getVersion()).color(NamedTextColor.GREEN));
        } else {
            sender.sendMessage(ChatColor.RED + "Usage: /" + label + " <reload/version>");
            return false;
        }

        return true;
    }
}
