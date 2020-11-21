package fr.entasia.faccore.commands.base;

import fr.entasia.faccore.Utils;
import fr.entasia.faccore.apis.BaseAPI;
import fr.entasia.faccore.apis.InternalAPI;
import fr.entasia.faccore.apis.FacPlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MoneyCommand implements CommandExecutor {
	public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
		FacPlayer sp;
		if(args.length==0){
			if(sender instanceof Player){
				sp = BaseAPI.getFacPlayer(((Player) sender));
				sender.sendMessage("§aTa monnaie : §2"+ Utils.formatMoney(sp.getMoney())+"§a");
			}else sender.sendMessage("§cTu es la console ! Met un nom de joueur");
		}else{
			sp = InternalAPI.getArgSP(sender, args[0], false);
			if(sp==null)sender.sendMessage("§cCe joueur n'existe pas !");
			else sender.sendMessage("§aMonnaie de "+sp.name+" : "+Utils.formatMoney(sp.getMoney()));
		}
		return true;
	}
}
