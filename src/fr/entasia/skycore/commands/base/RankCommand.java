package fr.entasia.skycore.commands.base;

import fr.entasia.skycore.invs.OtherMenus;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RankCommand implements CommandExecutor {
	public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
		if(!(sender instanceof Player))return true;
		OtherMenus.topRankOpen((Player)sender);

		return true;
	}
}
