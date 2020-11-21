package fr.entasia.skycore.commands.base;

import fr.entasia.skycore.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpawnCommand implements CommandExecutor {

	public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
		if(!(sender instanceof Player))return true;

		((Player) sender).teleport(Utils.spawn);
		sender.sendMessage("§6Tu as été téléporté au Spawn !");
		return true;
	}
}
