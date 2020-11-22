package fr.entasia.faccore.commands.manage;

import fr.entasia.faccore.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class FacCoreCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
		if(sender.hasPermission("plugin.skycore")){
			if(args.length==0){
				sender.sendMessage("§cMet un argument !");
				showArgs(sender);
			}else{
				if(args[0].equalsIgnoreCase("reload")) {
					try {
						Main.loadConfigs();
						sender.sendMessage("§aConfiguration rechargée avec succès !");
					} catch (Exception e) {
						e.printStackTrace();
						sender.sendMessage("§cConfiguration rechargée avec erreur ! ( voir console )");
					}
				}else {
					sender.sendMessage("§cArgument invalide ! Arguments disponibles :");
					showArgs(sender);
				}
			}
		}else sender.sendMessage("§cTu n'as pas accès à cette commande !");
		return true;
	}

	private static void showArgs(CommandSender sender){
		sender.sendMessage("§c- reload");
		sender.sendMessage("§c- sql");
	}
}
