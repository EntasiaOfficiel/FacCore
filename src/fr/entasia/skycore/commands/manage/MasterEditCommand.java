package fr.entasia.skycore.commands.manage;

import fr.entasia.skycore.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MasterEditCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
		if (!(sender instanceof Player)) return true;
		if (sender.hasPermission("admin.masteredit")) {
			if (args.length >= 1) {
				if (args[0].equalsIgnoreCase("list")) {
					sender.sendMessage("§7Liste : ");
					for (Player p : Utils.masterEditors) {
						sender.sendMessage("§7 - §r" + p.getName());
					}
				} else if (args[0].equalsIgnoreCase("clear")) {
					Utils.masterEditors.clear();
					sender.sendMessage("§6MasterEdit : §eTu as clear la liste !");
				} else if (sender.hasPermission("build.MasterEdit.others")) {
					Player p = Bukkit.getPlayer(args[0]);
					if (p == null) {
						sender.sendMessage("§4Erreur : §cCe joueur n'existe pas !");
					} else {
						if (Utils.masterEditors.removeIf(a->a.getName().equals(p.getName()))) {
							sender.sendMessage("§6MasterEdit : §cDésactivé §6pour §e" + p.getName());
							p.sendMessage("§6MasterEdit : §cDésactivé §6par §e" + sender.getName());
						} else {
							Utils.masterEditors.add(p);
							sender.sendMessage("§6MasterEdit : §aActivé §6pour §e" + p.getName());
							p.sendMessage("§6MasterEdit : §aActivé §6par §e" + sender.getName());
						}
					}
				} else sender.sendMessage("§4Erreur : §cTu ne peux pas modifier le MasterEdit des autres !");
			} else {
				if (Utils.masterEditors.contains(sender)) {
					Utils.masterEditors.remove(sender);
					sender.sendMessage("§6MasterEdit : §cDésactivé");
				} else {
					Utils.masterEditors.add((Player)sender);
					sender.sendMessage("§6MasterEdit : §aActivé");
				}
			}

		} else sender.sendMessage("&cTu n'as pas accès à cette commande !");
		return true;
	}
}
