package fr.entasia.faccore.commands.base;

import fr.entasia.faccore.Utils;
import fr.entasia.faccore.apis.BaseAPI;
import fr.entasia.faccore.apis.FacPlayer;
import fr.entasia.faccore.apis.InternalAPI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PayCommand implements CommandExecutor {
	public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
		if(!(sender instanceof Player))return true;
		if(args.length==2){
			FacPlayer target = InternalAPI.getArgSP(sender, args[0], true);
			if(target==null)return true;
			int money;
			try {
				money = Integer.parseInt(args[1]);
			}catch(NumberFormatException ignore) {
				sender.sendMessage("§cLe nombre " + args[1] + " est invalide !");
				return true;
			}
			if(money==0)sender.sendMessage("§cMet un nombre invalide !");
			else {
				if (money < 0) money = -money;
				FacPlayer sp = BaseAPI.getOnlineFP(((Player) sender));
				if (sp == null){
					sender.sendMessage("§cUne erreur s'est produite lors du chargement du ton profil Faction ! Contacte un Membre du Staff");
				} else {
					if (sp.getMoney() < money){
						sender.sendMessage("§cTu n'as pas assez d'argent ! Valeur actuelle : " + sp.getMoney());
					} else {
						sp.withdrawMoney(money);
						sender.sendMessage("§aTu as versé §2" + Utils.formatMoney(money) + "§a à §2" + args[0] + "§c !");
						target.addMoney(money);
						if(target.isOnline())target.p.sendMessage("§2"+sender.getName()+"§a t'a versé §2"+Utils.formatMoney(money)+"§a !");
					}
				}
			}
		}else sender.sendMessage("§cSyntaxe : /pay <joueur> <valeur>");
		return true;
	}
}
