package fr.entasia.faccore.commands.base;

import fr.entasia.apis.other.ChatComponent;
import fr.entasia.apis.other.CodePasser;
import fr.entasia.faccore.Utils;
import fr.entasia.faccore.apis.*;
import fr.entasia.faccore.invs.FacMenus;
import fr.entasia.faccore.invs.OtherMenus;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;

import static fr.entasia.faccore.commands.base.FCmdUtils.*;

public class FacCommand implements CommandExecutor {

	public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
		if(!(sender instanceof Player))return true;
		Player p = ((Player)sender);
		FacPlayer fp = BaseAPI.getOnlineFP(p);
		if(fp==null){
			p.sendMessage("§cTon profil est mal chargé ! Contacte un Membre du Staff");
			return true;
		}
		if (args.length == 0) {
			FacMenus.baseIslandOpen(fp);
			return true;
		}

		args[0] = args[0].toLowerCase();
		switch (args[0]) {
			case "create": {
				if (fp.getFaction()==null) {
					try {
						BaseAPI.registerFaction(fp);
						p.sendMessage("§cTu as créé ta faction ! Bonne chance, Chef");
					} catch (Exception e) {
						e.printStackTrace();
						p.sendMessage("§cUne erreur est survenue durant la création de ta faction ! Contacte un Membre du Staff");
					}
				} else {
					p.sendMessage("§cTu as déja une faction !");
				}
				return true;
			}
			case "list": {
				FacMenus.baseIslandOpen(fp);
				break;
			}
			case "tp": {
				OtherMenus.topRankOpen(p);
				break;
			}

			case "invites": {
				if (fp.getFaction() == null) {
					if (fp.invites.size() == 0) {
						p.sendMessage("§cTu n'as aucune invitation !");
					} else {
						p.sendMessage("§aTes invitations : ");
						for (Faction fac : fp.invites) {
							p.sendMessage("§e- §aFaction " + fac.getGenName() + " (Chef : " + fac.getOwner().name + ")");
							sendInviteMsg(p, fac);
							p.sendMessage(" ");
						}
						p.sendMessage(" ");
					}
				} else {
					if (fp.getFaction().invites.size() == 0) {
						p.sendMessage("§cTu n'as aucune invitation à gérer !");
					} else {
						p.sendMessage("§aInvitations de la Faction : ");

						ChatComponent uninvite = new ChatComponent("§4[§cAnnuler§4]");
						uninvite.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§cClique pour annuler l'invitation !")));

						ChatComponent cc;
						for (FacPlayer lfp : fp.getFaction().invites) {
							uninvite.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/is uninvite " + lfp.uuid));
							cc = new ChatComponent("§e- §2" + lfp.name + "  ");
							if (fp.getRank().id >= MemberRank.ADJOINT.id) cc.append(uninvite);

							p.sendMessage(cc.create());
						}
						p.sendMessage(" ");
					}
				}
				break;
			}

			case "accept": {
				if (fp.getFaction() != null) {
					p.sendMessage("§cTu es déja dans une faction !");
					return true;
				}
				if (args.length == 1) p.sendMessage("§cMet le chef de la faction en argument !");
				else {
					Faction fac = null;
					try {
						int id = Integer.parseInt(args[1]);
						for (Faction lf : fp.invites) {
							if (lf.id == id) {
								fac = lf;
								break;
							}
						}
						if (fac == null) {
							p.sendMessage("§cCette faction a annulé son invitation !");
							return true;
						}
					} catch (NumberFormatException e) {
						args[0] = args[0].toLowerCase();
						for (Faction lf : fp.invites) {
							if (lf.getOwner().name.toLowerCase().equals(args[0])) {
								fac = lf;
								break;
							}
						}
						if (fac == null) {
							p.sendMessage("§cCe chef de faction ne t'a pas envoyé d'invitation !");
							return true;
						}
					}
					if (fac.invites.remove(fp) && fac.addMember(fp)) {
						fp.invites.clear();
						p.sendMessage("§aInvitation acceptée ! Bienvenue, §dRecrue§a !");
						fac.teleportHome(p);
					} else p.sendMessage("§cUne erreur s'est produite lors de l'acceptation de l'invitation !");
					return true;
				}
				break;
			}

			case "deny":{
				if (args.length == 1) p.sendMessage("§cMet le chef de la faction en argument !");
				else {
					Faction fac = null;
					try {
						int id = Integer.parseInt(args[1]);
						for (Faction lf : fp.invites) {
							if (lf.id == id) {
								fac = lf;
								break;
							}
						}
						if (fac == null) {
							p.sendMessage("§cCette faction a annulé son invitation !");
							return true;
						}
					} catch (NumberFormatException e) {
						args[0] = args[0].toLowerCase();
						for (Faction lf : fp.invites) {
							if (lf.getOwner().name.toLowerCase().equals(args[0])) {
								fac = lf;
								break;
							}
						}
						if (fac == null) {
							p.sendMessage("§cCe chef de faction ne t'a pas envoyé d'invitation !");
							return true;
						}
					}
					p.sendMessage("§cTu as refusé l'invitation de la faction "+fac.getGenName()+" !");
					fac.sendTeamMsg("§3"+fp.p.getDisplayName()+" §cà refusé l'invitation !");
					if (fac.invites.remove(fp) && fac.addMember(fp)) {
						fp.invites.clear();
						p.sendMessage("§aInvitation acceptée ! Bienvenue, §dRecrue§a !");
						fac.teleportHome(p);
					} else p.sendMessage("§cUne erreur s'est produite lors de l'acceptation de l'invitation !");
					return true;
				}
				break;
			}
			default:{
				Faction fac = fp.getFaction();
				if(fac==null)return true;

				switch (args[0]) { // DANS TOUTES CES OPTIONS IL A UNE FACTION
					case "go":
					case "h":
					case "home":
						if (fp.getFaction()==null) {
							p.sendMessage("§cTu n'a pas de faction !");
						} else {
							if (fp.getFaction().getHome() == null) {
								p.sendMessage("§cTu n'as pas de home !");
							} else {
								fp.getFaction().teleportHome(fp.p);
							}
						}
						break;

					case "team": {
						FacMenus.manageTeamOpen(fac);
						break;
					}
					case "sethome": {
						if (fac.getRank() == MemberRank.RECRUE)
							p.sendMessage("§cTu es une recrue, tu ne peux pas redéfinir le spawn de la faction !");
						else {
							fac.getFaction().setHome(p.getLocation());
							p.sendMessage("§aLe spawn de la faction à été redéfini avec succès !");
						}
						break;
					}

					case "setname": {
						if (fac.getRank() == MemberRank.RECRUE)
							p.sendMessage("§cTu es une recrue, tu ne peux pas changer le nom de l'île !");
						if (args.length == 1) {
							p.sendMessage("§cMet un nom d'île !");
							return true;
						}
						String name = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
						if (fac.is.setName(name)) p.sendMessage("§aNouveau nom d'île : §d" + name);
						else
							p.sendMessage("§cCe nom est trop grand ! Maximum : 20 caractères (" + name.length() + " actuellement)");
						break;
					}

					case "c":
					case "chat": {
						if (args.length == 1) {
							if (fac.getFaction().islandChat(fp);) {
								fac.sp.islandChat = false;
								p.sendMessage("§cChat d'île désactivé !");
							} else {
								fac.sp.islandChat = true;
								p.sendMessage("§aChat d'île activé !");
							}
						} else {
							fac.is.islandChat(fac, String.join(" ", Arrays.copyOfRange(args, 1, args.length)));
						}
						break;
					}

					case "calc":
					case "level":
					case "leave":
					case "quit": {
						if (fac.getRank() == MemberRank.CHEF) {
							p.sendMessage("§cTu es le chef de cette île, tu ne peux pas la quitter !");
							p.sendMessage("§cUtilise /is setowner pour transférer la propriété de l'île");
						} else {
							String name = fac.getName();
							if (fac.) {
								fac.is.sendTeamMsg(name + "§e à quitté l'île !");
								p.sendMessage("§cTu as quitté l'île !");
							} else p.sendMessage("§cUne erreur s'est produite !");
							break;
						}
					}

					case "invite": {
						FacPlayer target = FCmdUtils.argTeamCheck(fp, args);
						if (target == null) return true;
						if (fac.getMember(target.uuid) == null) {
							if (fac.invites.contains(target)) {
								p.sendMessage("§cCe joueur à déja été invité !");
							} else {
								fac.sendTeamMsg(MemberRank.DEFAULT.getName() + "§3 " + target.name + "§e à été invité sur l'île par " + fac.getName() + "§e !");
								if (target.isOnline()) {
									target.p.sendMessage("§eTu as été invité dans la faction " + fac.getGenName() + " par " + fp.name + " !");
									sendInviteMsg(target.p, fac);
									target.p.sendMessage("§eTu peux à tout moment regarder tes invitations avec la commande §6/f invites");
								}
							}
						} else p.sendMessage("§cCe joueur est déja membre sur cette île !");
						break;
					}
					case "uninvite": {
						FacPlayer target = FCmdUtils.argTeamCheck(fp, args);
						if (target == null) return true;
						if (fac.getMember(target.uuid) == null) {
							if (fac.invites.contains(target)) {
								fac.invites.remove(target);
								target.invites.remove(fac);
								fac.sendTeamMsg("§3L'invitation de " + target.name + "§e à été annulée par " + fac.getName() + "§e !");
								if (target.isOnline()) {
									target.p.sendMessage("§cL'invitation de la faction §4" + fac.getGenName() + "§c à été annulée !");
								}
							} else p.sendMessage("§cCe joueur n'est pas invité !");
						} else p.sendMessage("§cCe joueur est un membre de l'île ! Utilise §4/is kick");
						break;
					}

					case "kick":
					case "demote":
					case "promote": {
						FacPlayer target = FCmdUtils.argTeamCheck(fac, args);
						if (target == null) return true;
						FacPlayer targetLink = fac.setRank();
						if (targetLink == null) {
							p.sendMessage("§cCe joueur n'est pas membre sur cette île !");
							return true;
						}
						switch (args[0]) {
							case "kick": {
								if (targetLink.getRank().id < fac.getRank().id) {
									String n = targetLink.getName();
									if (targetLink.getFaction()) {
										fac.is.sendTeamMsg("§7" + n + "§e à été expulsé de l'île par " + fac.getName() + "§e !");
										if (target.isOnline()) {
											target.p.sendMessage("§cTu as été exclu de l'île par §3" + fac.getName() + "§c !");
											target.p.teleport(Utils.spawn);
										}
									} else p.sendMessage("§cUne erreur s'est produite !");
								} else p.sendMessage("§cCette personne est trop haut gradée !");
								break;
							}

							case "promote": { // TODO FAIRE GAFFE AU FUTUR, POUR PROMOTE
								if (targetLink.getRank().id + 1 < fac.getRank().id) {
									MemberRank nrank = MemberRank.getType(targetLink.getRank().id + 1);
									byte ret = targetLink.setRank(nrank);
									if (ret == 0) {
										fac.is.sendTeamMsg(targetLink.getName() + "§e à été promu par " + fac.getName() + "§e !");
									} else if (ret == 1) {
										p.sendMessage("§cCe joueur est déja chef d'une autre île !");
									} else {
										p.sendMessage("§cUne erreur s'est produite !");
									}
								} else {
									p.sendMessage("§cCette personne est trop haut gradée !");
								}
								break;
							}

							case "demote": {
								if (targetLink.getRank().id < fac.getRank().id) {
									if (targetLink.getRank().id <= MemberRank.RECRUE.id)
										p.sendMessage("§cCette personne à déjà le rôle minimum ! Utilise §4/is kick§c pour l'exclure ");
									else {
										MemberRank nrank = MemberRank.getType(targetLink.getRank().id - 1);
										if (targetLink.setRank(nrank) == 0)
											fac.is.sendTeamMsg(targetLink.getName() + "§e à été demote par " + fac.getName() + "§e !");
										else p.sendMessage("§cUne erreur s'est produite !");
									}
								} else p.sendMessage("§cCette personne est trop haut gradée !");
								break;
							}
						}
						break;
					}

					case "warp": {
						if (args.length == 1) p.sendMessage("§cMet un nom de joueur !");
						else {
							FacPlayer target = InternalAPI.getArgSP(sender, args[1], false);
							if (target == null) return true;
							ArrayList<FacPlayer> list = target.getIslands();
							FacPlayer targetLink;
							if (list.size() == 0) {
								p.sendMessage("§cCe joueur n'a aucune île !");
								return true;
							} else if (list.size() == 1) targetLink = list.get(0);
							else {
								targetLink = target.getFaction();
								if (targetLink == null) {
									p.sendMessage("§cCe joueur à plusieurs îles !");
									return true;
								}
							}
							if (targetLink.is.isBanned(fp)) p.sendMessage("§cTu es banni de cette île !");
							else {
								targetLink.is.teleportHome(p);
								p.sendMessage("§aTéléportation à l'île de §2" + target.name + " §a!");
							}
						}
						break;
					}

					case "deposit":
					case "withdraw": {
						if (fac.getRank() == MemberRank.RECRUE) {
							p.sendMessage("§cTu es seulement une recrue ! Tu ne peux pas intéragir à la banque d'île");
							return true;
						}
						if (args.length == 1) p.sendMessage("§cMet un chiffre !");
						else {
							try {
								int n = Integer.parseInt(args[1]);
								if (args[0].equals("withdraw")) {
									if (fac.is.withdrawBank(n)) {
										fp.addMoney(n);
										p.sendMessage("§aTu as retiré §2" + Utils.formatMoney(n) + "§a de la banque d'île !");
									} else {
										p.sendMessage("§cIl n'y a pas assez d'argent dans la banque d'île !");
									}
								} else {
									if (fp.withdrawMoney(n)) {
										fac.is.addBank(n);
										p.sendMessage("§aTu as ajouté §2" + Utils.formatMoney(n) + "§a à la banque d'île !");
									} else p.sendMessage("§cTu n'as pas assez d'argent !");

								}

							} catch (NumberFormatException ignore) {
								p.sendMessage("§cLe chiffre §4" + args[1] + "§c est invalide !");
							}
						}
						break;
					}
					case "bank":
					case "money": {
						p.sendMessage("§eValeur de la banque d'île actuellement : §6" + Utils.formatMoney(fac.is.getBank()));
						break;
					}

					case "setowner": {
						if (args.length < 2) {
							p.sendMessage("§cMet un joueur en argument !");
							return true;
						}
						if (fac.sp.p.getWorld() != Dimension.OVERWORLD.world) {
							p.sendMessage("§cTu n'es pas dans l'overworld des îles !");
						}
						Faction is = BaseAPI.getIsland(fac.sp.p.getLocation());
						if (is == null) {
							p.sendMessage("§cTu n'es sur aucune île !");
							return true;
						}
						if (is.getOwner() != fac) {
							p.sendMessage("§cTu n'es pas le chef de cette île !");
							return true;
						}
						ConfirmObj co = confirmPassOwner.get(p);
						if (args.length == 3 && args[2].equals("confirm")) {
							if (co == null || (System.currentTimeMillis() - co.when > 10000)) {
								p.sendMessage("§cLe temps de confirmation est écoulé !");
								return true;
							}
							co.task.cancel();
							confirmPassOwner.remove(p);
							if (!co.is.facID.equals(is.facID)) {
								p.sendMessage("§cL'île sur laquelle tu es n'est pas la même que celle ou tu as fait la première commande ! Annulation");
								return true;
							}
							if (!args[1].equals(co.info)) {
								p.sendMessage("§cCe joueur n'est pas le même que celui de ta première commande ! Annulation");
								return true;
							}
							FacPlayer target = InternalAPI.getArgSP(fac.sp.p, args[1], true);
							if (target == null) return true;
							FacPlayer newLink = target.getIsland(is.facID);
							if (newLink == null) {
								p.sendMessage("§cCe joueur n'est plus membre sur cette île !");
							} else {
								byte ret = newLink.setRank(MemberRank.CHEF);
								if (ret == 0) {
									is.sendTeamMsg("§3Passage du chef sur cette île à §c" + target.name + " §3!");
								} else if (ret == 1) {
									p.sendMessage("§cCe joueur est déja chef d'une île !");
								} else {
									p.sendMessage("§cUne erreur est survenue ! Contacte un Membre du Staff");
								}
							}
						} else {
							if (co == null) {
								FacPlayer target = InternalAPI.getArgSP(p, args[1], true);
								if (target == null) return true;
								if (target.equals(fac.sp)) {
									p.sendMessage("§cCe joueur est.. toi-même ?");
								} else if (target.getIsland(is.facID) == null) {
									p.sendMessage("§cCe joueur n'est pas membre sur cette île !");
								} else if (target.getFaction() == null) {
									p.sendMessage("§cVeut-tu passer " + target.name + " chef de cette île ? " + is.facID.str());
									p.sendMessage("§cTape la commande §4/" + command.getName() + " setowner " + args[1] + " confirm§c dans les 15 secondes pour confirmer.");
									p.sendMessage("§cATTENTION : Tu ne sera plus le chef de cette île, tu deviendra Adjoint !");
									co = startConfirm(p, is);
									co.info = args[1];
									confirmPassOwner.put(p, co);
								} else {
									p.sendMessage("§cCe joueur est déja chef d'une île !");
								}
							} else {
								int time = (int) (15 - (Math.ceil(System.currentTimeMillis() - co.when) / 1000f));
								p.sendMessage("§cTape la commande §4/" + command.getName() + " setowner " + args[1] + " confirm§c dans les " + time +
										" secondes pour confirmer le changement de chef de l'île " + is.facID.str());
							}
						}
						break;
					}


					case "delete": {
						if (fac.sp.p.getWorld() != Dimension.OVERWORLD.world) {
							p.sendMessage("§cTu n'es pas dans l'overworld des îles !");
							return true;
						}
						Faction is = BaseAPI.getIsland(fac.sp.p.getLocation());
						if (is == null) {
							p.sendMessage("§cTu n'es sur aucune île !");
							return true;
						}
						if (is.getOwner() != fac) {
							p.sendMessage("§cTu n'es pas le chef de cette île !");
							return true;
						}
						ConfirmObj co = confirmDelete.get(p);
						if (args.length == 2 && args[1].equals("confirm")) {
							if (co == null || (System.currentTimeMillis() - co.when > 10000)) {
								p.sendMessage("§cLe temps de confirmation est écoulé !");
								return true;
							}
							co.task.cancel();
							confirmDelete.remove(p);
							if (co.is.facID.equals(is.facID)) {
								p.sendMessage("§cSuppression de l'île " + is.facID.x + ";" + is.facID.z + " en cours...");
								BaseAPI.deleteFaction(is, new CodePasser.Arg<Boolean>() {
									@Override
									public void run(Boolean err) {
										if (err) p.sendMessage("§cîle supprimée avec succès !");
										else p.sendMessage("§cUne erreur est survenue !");
									}
								});
							} else
								p.sendMessage("§cL'île sur laquelle tu es n'est pas la même que celle ou tu as fait la première commande ! Annulation");
						} else {
							if (co == null) {
								p.sendMessage("§cVeut tu supprimer cette île ? " + is.facID.str());
								p.sendMessage("§cTape la commande §4/" + command.getName() + " delete confirm§c dans les 15 secondes pour confirmer.");
								p.sendMessage("§cATTENTION : La suppression est instantanée et sans espoir de retour !");
								co = startConfirm(p, is);
								confirmDelete.put(p, co);
							} else {
								int time = (int) (15 - Math.floor((System.currentTimeMillis() - co.when) / 1000f));
								p.sendMessage("§cTape la commande §4/" + command.getName() + " delete confirm§c dans les " + time + " secondes pour confirmer la suppression de l'île " + is.facID.str());
							}
						}
						break;
					}


					case "help": {
						p.sendMessage("§6Liste des sous-commandes :");
						p.sendMessage("§bCommandes de bases :");
						p.sendMessage(new ChatComponent("§e- create").setTextHover("§6pour créer ton île ! (Limite : 1 par joueur)").create());
						p.sendMessage(new ChatComponent("§e- go/home [numero]").setTextHover("§6pour te téléporter à ton île").create());
						p.sendMessage(new ChatComponent("§e- setname").setTextHover("§6pour renommer ton île").create());
						p.sendMessage(new ChatComponent("§e- chat").setTextHover("§6pour parler avec les membres de l'île").create());
						p.sendMessage(new ChatComponent("§e- list").setTextHover("§6voir tes îles, et choisir l'île par défaut").create());
						p.sendMessage(new ChatComponent("§e- sethome").setTextHover("§6pour redéfinir le spawn de ton île").create());
						p.sendMessage(new ChatComponent("§e- top").setTextHover("§6pour voir le top 10 des îles !").create());
						p.sendMessage(new ChatComponent("§e- help").setTextHover("§6pour voir cette liste. Très surprenant.").create());
						p.sendMessage("§bCommandes d'équipe :");
						p.sendMessage(new ChatComponent("§e- team").setTextHover("§6pour voir l'équipe de ton île").create());
						p.sendMessage(new ChatComponent("§e- invite").setTextHover("§6pour inviter un joueur sur l'île").create());
						p.sendMessage(new ChatComponent("§e- kick").setTextHover("§6pour exclure un membre de l'île").create());
						p.sendMessage(new ChatComponent("§e- promote").setTextHover("§6pour augmenter le grade d'un membre").create());
						p.sendMessage(new ChatComponent("§e- demote").setTextHover("§6pour diminuer le grade d'un membre").create());
						p.sendMessage(new ChatComponent("§e- ban").setTextHover("§6pour bannir quelqu'un de île").create());
						p.sendMessage(new ChatComponent("§e- unban").setTextHover("§6pour débannir quelqu'un de l'île").create());
						p.sendMessage("§bBanque d'île :");
						p.sendMessage(new ChatComponent("§e- money/bank").setTextHover("§6pour voir la valeur de la banque d'île").create());
						p.sendMessage(new ChatComponent("§e- deposit").setTextHover("§6pour poser de l'argent bien au chaud dans la banque d'île").create());
						p.sendMessage(new ChatComponent("§e- withdraw").setTextHover("§6pour récupérer de l'argent de la banque d'île").create());
						p.sendMessage("§cCommandes dangereuses :");
						p.sendMessage(new ChatComponent("§e- setowner").setTextHover("§6pour changer la propriété de l'île").create());
						p.sendMessage(new ChatComponent("§e- delete").setTextHover("§6pour supprimer l'île").create());
						break;
					}
					default: {
						p.sendMessage("§cL'argument §4" + args[0] + "§c n'existe pas ! Fait /is help pour voir la liste des commandes");
						break;
					}
				}
			}
		}
	return true;
	}
}
