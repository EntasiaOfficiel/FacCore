package fr.entasia.faccore.commands.manage;

import fr.entasia.faccore.apis.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FacAdminCommand implements CommandExecutor {

	private static void args(CommandSender sender){
		sender.sendMessage("§cArguments disponibles : ");
		sender.sendMessage("§4Joueurs :");
		sender.sendMessage("§c- infop");
		sender.sendMessage("§c- deletep");
		sender.sendMessage("§c- resetgen");
		sender.sendMessage("§4îles :");
		sender.sendMessage("§c- infois");
		sender.sendMessage("§c- tp");
		sender.sendMessage("§c- deleteis");
		sender.sendMessage("§c- join");
		sender.sendMessage("§c- kick");
		sender.sendMessage("§c- setrange");
		sender.sendMessage("§c- setrank");
		sender.sendMessage("§4Autres :");
		sender.sendMessage("§c- help");
	}


	private static Faction getIS(CommandSender sender, String[] args) {
		if (args.length == 1) sender.sendMessage("§cMet un ID de Faction !");
		else {

				Faction is = BaseAPI.getFaction(Integer.parseInt(args[1]));
				if (is == null) sender.sendMessage("§cFaction non existante !");
				else return is;
		}
		return null;
	}

	public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
		if(!(sender instanceof Player))return true;
		if(sender.hasPermission("restricted.isadmin")){

			Player p = ((Player)sender);
			if (args.length==0)p.sendMessage("§cFait /isadmin help pour voir la liste des arguments !");
			else{
				args[0] = args[0].toLowerCase();
				switch (args[0]) {

					// INFOS

					case "infop": {
						if(args.length==1)p.sendMessage("§cMet un pseudo/UUID !");
						else {
							FacPlayer target = InternalAPI.getArgSP(p, args[1], false);
							if(target==null)return true;
							p.sendMessage("§8Joueur : §b" + target.name);
							p.sendMessage("§8Faction :");

							if(target.getFaction() != null){
								p.sendMessage("§8"+target.getFaction().getName());
							}else{
								p.sendMessage("§8Aucune");
							}
							p.sendMessage("§7Monnaie : §b" + target.getMoney());
						}
						break;
					}

					case "infofac": {
						Faction fac;
						if (args.length == 1) {
							if (Dimension.isGameWorld(p.getWorld())) {
								fac = BaseAPI.getFaction(p.getLocation());
							} else{
								p.sendMessage("§cTu n'es pas dans un monde Faction !");
								return true;
							}
						} else fac = BaseAPI.getFaction(Integer.parseInt(args[1]));
						if (fac == null) p.sendMessage("§cFaction invalide !");
						else {
								p.sendMessage("§8Global");
								FacPlayer link = fac.getOwner();
								p.sendMessage("§7Owner UUID : §b"+fac.getOwner().uuid);
								if(link.p!=null)p.sendMessage("§7Owner Name : §b"+fac.getOwner().p.getName());
								p.sendMessage("§7Membres :");
								for(FacPlayer ll : fac.getSortedMembers()){
									p.sendMessage("§8- §b"+ll.getName());
								}
								p.sendMessage("§7Banque de Faction : §b" + fac.getBank());

						}
						break;
					}

					// DELETE

					case "deletep":{
						if(args.length==1)p.sendMessage("§cMet un pseudo/UUID !");
						else{
							FacPlayer target = InternalAPI.getArgSP(p, args[1], false);
							if(target!=null){
								if(BaseAPI.deleteFacPlayer(target))p.sendMessage("§cJoueur supprimé avec succès !");
								else p.sendMessage("§4Erreur lors de la suppression du joueur !");
							}
						}
						break;
					}

					case "deletefac":{
						if(args.length==1)p.sendMessage("§cMet un ID de Faction !");
						else {
								Faction is = BaseAPI.getFaction(Integer.parseInt(args[1]));
								if (is == null) p.sendMessage("§cFaction non existante !");
								else {
									try {
										BaseAPI.deleteFaction(is);
									} catch (Exception e) {
										e.printStackTrace();
									}
								}

						}
						break;
					}

					// TEAM UTILS


					case "tp":{
						Faction is = getIS(sender, args);
						if(is==null)return true;
						is.teleportHome(p);
						p.sendMessage("§aSuccès !");
						break;
					}
					case "setrank":
					case "join":
					case "kick":{
						Faction is = getIS(sender, args);
						if(is!=null){
							if(args.length==2)p.sendMessage("§cMet un joueur !");
							else{
								FacPlayer target = InternalAPI.getArgSP(sender, args[2], false);
								if(target!=null){
									switch(args[0]){
										case "join":{
											p.sendMessage("§cCe joueur est déja membre sur cette Faction !");
											break;
										}
										case "kick":{
											if(target.getFaction().getMembers().remove(target))p.sendMessage("§aSuccès !");
											else p.sendMessage("§cUne erreur est survenue !");
											break;
										}
//										case "setowner":{
//											if(targetLink==null)p.sendMessage("§cCe joueur n'est pas membre sur cette île !");
//											else {
//												if (targetLink.setRank(MemberRank.CHEF)) p.sendMessage("§aSuccès !");
//												else p.sendMessage("§cUne erreur est survenue !");
//											}
//											break;
//										}
										case "setrank":{
											if(args.length==3)p.sendMessage("§cMet un rôle !");
											else{
												try{
													MemberRank r = MemberRank.valueOf(args[3].toUpperCase());
													if(r==MemberRank.DEFAULT)p.sendMessage("§cUtilise /is kick pour exclure un membre de la faction !");
													else{
														target.setRank(r);
														p.sendMessage("§aSuccès !");
													}
												}catch(IllegalArgumentException ignore){
													p.sendMessage("§cCe rôle n'existe pas ! Liste des rôles :");
													for(MemberRank rank : MemberRank.values()){
														if(rank==MemberRank.DEFAULT)continue;
														p.sendMessage("§c- "+rank.name()+" ("+rank.getName()+"§c)");
													}
												}
											}
											break;
										}
									}
								}
							}
						}
						break;
					}


					// AUTRES
					case "help": {
						args(sender);
						break;
					}
					default:{
						p.sendMessage("§cL'argument "+args[0]+" n'existe pas !");
						args(sender);
						break;
					}
				}
			}
		}else sender.sendMessage("§cTu n'as pas la permission d'utiliser cette commande !");
		return true;
	}
}
