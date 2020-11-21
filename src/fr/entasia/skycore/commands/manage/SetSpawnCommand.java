package fr.entasia.skycore.commands.manage;

import fr.entasia.skycore.Main;
import fr.entasia.skycore.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class SetSpawnCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
		if(!(sender instanceof Player))return true;
		if(sender.hasPermission("entasia.setspawn")){
			Player p = (Player)sender;

			Utils.spawn = p.getLocation().getBlock().getLocation();

			ConfigurationSection sec = Main.main.getConfig().getConfigurationSection("spawn");
			sec.set("x", Utils.spawn.getBlockX());
			sec.set("y", Utils.spawn.getBlockY());
			sec.set("z", Utils.spawn.getBlockZ());
			sec.set("yaw", Utils.spawn.getYaw());
			sec.set("pitch", Utils.spawn.getPitch());
			Main.main.saveConfig();

			p.sendMessage("§aSpawn changé avec succès !");


		}else sender.sendMessage("§cTu n'as pas accès à cette commande !");
		return true;
	}
}
