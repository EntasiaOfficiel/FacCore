package fr.entasia.skycore.commands.manage;

import fr.entasia.apis.other.CodePasser;
import fr.entasia.skycore.apis.*;
import fr.entasia.skycore.apis.mini.Dimensions;
import fr.entasia.skycore.apis.mini.MemberRank;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class IsAdminCommand implements CommandExecutor {

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


	private static BaseIsland getIS(CommandSender sender, String[] args) {
		if (args.length == 1) sender.sendMessage("§cMet un ID d'île !");
		else {
			ISID isid = ISID.parse(args[1]);
			if (isid == null) sender.sendMessage("§cID d'ile invalide !");
			else {
				BaseIsland is = BaseAPI.getIsland(isid);
				if (is == null) sender.sendMessage("§cIle non existante !");
				else return is;
			}
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
							SkyPlayer target = InternalAPI.getArgSP(p, args[1], false);
							if(target==null)return true;
							p.sendMessage("§8Joueur : §b" + target.name);
							p.sendMessage("§8îles :");
							for(ISPLink ll : target.getIslands()){
								p.sendMessage("§8- île §b"+ll.is.isid.str()+" §8(§b"+ll.getRank().getName()+"§8)");
							}
							p.sendMessage("§7Monnaie : §b" + target.getMoney());
						}
						break;
					}

					case "resetgen":{
						if(args.length==1)p.sendMessage("§cMet un pseudo/UUID !");
						else {
							SkyPlayer target = InternalAPI.getArgSP(p, args[1], false);
							if (target != null) {
								target.setLastGenerated(0);
								p.sendMessage("§aSuccès !");
							}
						}
						break;
					}

					case "infois": {
						ISID isid;
						if (args.length == 1) {
							if (Dimensions.isIslandWorld(p.getWorld())) {
								isid = CooManager.getIslandID(p.getLocation().getBlockX(), p.getLocation().getBlockZ());
							} else{
								p.sendMessage("§cTu n'es pas dans un monde Skyblock !");
								return true;
							}
						} else isid = ISID.parse(args[1]);
						if (isid == null) p.sendMessage("§cID d'ile invalide !");
						else {
							BaseIsland is = BaseAPI.getIsland(isid);
							if (is == null) p.sendMessage("§cIle non existante !");
							else {
								p.sendMessage("§8Global");
								p.sendMessage("§7ID : §b"+isid.str());
								ISPLink link = is.getOwner();
								p.sendMessage("§7Owner UUID : §b"+link.sp.uuid);
								if(link.sp.p!=null)p.sendMessage("§7Owner Name : §b"+link.sp.p.getName());
								p.sendMessage("§7Membres :");
								for(ISPLink ll : link.is.getSortedMembers()){
									p.sendMessage("§8- §b"+ll.getName());
								}
								p.sendMessage("§7Points purs : §b" + is.getRawpoints());
								p.sendMessage("§7Malus : §b" + is.getMalus());
								p.sendMessage("§7Niveau : §b" + is.getLevel());
								p.sendMessage("§8Dimensions :");
								p.sendMessage("§7Nether : §b"+is.hasDimension(Dimensions.NETHER));
								p.sendMessage("§7End : §b"+is.hasDimension(Dimensions.END));
								p.sendMessage("§8Autres :");
								p.sendMessage("§7Extension : §bNiveau "+(is.getExtension()+1)+" §7("+is.getExtension()+"/3)");
								p.sendMessage("§7Mineurs : §b"+is.autominers.size());
								p.sendMessage("§7Banque d'île : §b" + is.getBank());
							}
						}
						break;
					}

					// DELETE

					case "deletep":{
						if(args.length==1)p.sendMessage("§cMet un pseudo/UUID !");
						else{
							SkyPlayer target = InternalAPI.getArgSP(p, args[1], false);
							if(target!=null){
								if(BaseAPI.deleteSkyPlayer(target))p.sendMessage("§cJoueur supprimé avec succès !");
								else p.sendMessage("§4Erreur lors de la suppression du joueur !");
							}
						}
						break;
					}

					case "deleteis":{
						if(args.length==1)p.sendMessage("§cMet un ID d'île !");
						else {
							ISID isid = ISID.parse(args[1]);
							if (isid == null) p.sendMessage("§cID d'ile invalide !");
							else{
								BaseIsland is = BaseAPI.getIsland(isid);
								if (is == null) p.sendMessage("§cIle non existante !");
								else {
									BaseAPI.deleteIsland(is, new CodePasser.Arg<Boolean>() {
										@Override
										public void run(Boolean err) {
											if(err)p.sendMessage("§cUne erreur s'est produite lors de la suppression de l'île !");
											else p.sendMessage("§cîle supprimé avec succès !");
										}
									});
								}
							}
						}
						break;
					}

					// TEAM UTILS

					case "setrange":{
						BaseIsland is = getIS(sender, args);
						if(is==null)return true;
						byte range;
						try{
							if(args.length==2)throw new NumberFormatException();
							range = Byte.parseByte(args[2]);
							if(range<0||range>3)throw new NumberFormatException();
						}catch(NumberFormatException ignore){
							p.sendMessage("§cMet un nombre entre 0 et 3 !");
							return true;
						}
						is.setExtension(range);
						p.sendMessage("§aNouvelle extension de l'île définie à "+range+" !");
						break;
					}
					case "tp":{
						BaseIsland is = getIS(sender, args);
						if(is==null)return true;
						is.teleportHome(p);
						p.sendMessage("§aSuccès !");
						break;
					}
					case "setrank":
					case "join":
					case "kick":{
						BaseIsland is = getIS(sender, args);
						if(is!=null){
							if(args.length==2)p.sendMessage("§cMet un joueur !");
							else{
								SkyPlayer target = InternalAPI.getArgSP(sender, args[2], false);
								if(target!=null){
									ISPLink targetLink = target.getIsland(is.isid);
									switch(args[0]){
										case "join":{
											if(targetLink==null){
												if(is.addMember(target)==null) p.sendMessage("§cUne erreur est survenue !");
												else p.sendMessage("§aSuccès !");
											}else p.sendMessage("§cCe joueur est déja membre sur cette île !");
											break;
										}
										case "kick":{
											if(targetLink==null){
												p.sendMessage("§cCe joueur n'est pas membre sur cette île !");
												return true;
											}
											if(targetLink.removeMember())p.sendMessage("§aSuccès !");
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
												if(targetLink==null){
													p.sendMessage("§cCe joueur n'est pas membre sur cette île !");
													return true;
												}
												try{
													MemberRank r = MemberRank.valueOf(args[3].toUpperCase());
													if(r==MemberRank.DEFAULT)p.sendMessage("§cUtilise /is kick pour exclure un membre de l'île !");
													else{
														byte ret = targetLink.setRank(r);
														if(ret==0)p.sendMessage("§aSuccès !");
														else if(ret==1)p.sendMessage("§cCe joueur est déja chef d'une île !");
														else p.sendMessage("§cUne erreur est survenue !");
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
