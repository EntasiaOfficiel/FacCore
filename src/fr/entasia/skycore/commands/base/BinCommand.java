package fr.entasia.skycore.commands.base;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BinCommand implements CommandExecutor {
	public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
		if(!(sender instanceof Player))return true;
		((Player)sender).openInventory(Bukkit.createInventory(null, 54, "§8Poubelle"));
		return true;
	}
}
