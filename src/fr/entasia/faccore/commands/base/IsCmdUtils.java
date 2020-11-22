package fr.entasia.faccore.commands.base;

import fr.entasia.apis.other.ChatComponent;
import fr.entasia.faccore.Main;
import fr.entasia.faccore.apis.FacPlayer;
import fr.entasia.faccore.apis.Faction;
import fr.entasia.faccore.apis.InternalAPI;
import fr.entasia.faccore.apis.MemberRank;
import fr.entasia.faccore.invs.FacMenus;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;

public class IsCmdUtils {

	protected static void sendInviteMsg(CommandSender sender, Faction is){
		ChatComponent accept = new ChatComponent("§2[§aAccepter§2]");
		accept.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/is accept "+is.facID.str()));
		accept.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, ChatComponent.create("§aClique pour accepter l'invitation !")));

		ChatComponent deny = new ChatComponent("§4[§cRefuser§4]");
		deny.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/is deny "+is.facID.str()));
		deny.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, ChatComponent.create("§cClique pour refuser l'invitation !")));

		sender.sendMessage(accept.append("   ").append(deny).create());
	}

	protected static class WaitConfirm extends BukkitRunnable {

		public Player p;
		public HashMap<Player, ConfirmObj> list;
		public WaitConfirm(Player p, HashMap<Player, ConfirmObj> list){
			this.p = p;
			this.list = list;
		}

		@Override
		public void run() {
			p.sendMessage("§cTemps de confirmation expiré !");
			list.remove(p);
		}
	}

	protected static class ConfirmObj{
		public long when;
		public Faction is;
		public BukkitTask task;
		public String info;

		public ConfirmObj(long when, Faction is){
			this.when = when;
			this.is = is;
		}
	}

	protected static ConfirmObj startConfirm(Player p, Faction is){
		ConfirmObj co = new ConfirmObj(System.currentTimeMillis(), is);
		co.task = new WaitConfirm(p, confirmPassOwner).runTaskLaterAsynchronously(Main.main, 300); // 15*20 = 300 ticks
		return co;
	}

	protected static HashMap<Player, ConfirmObj> confirmDelete = new HashMap<>();
	protected static HashMap<Player, ConfirmObj> confirmPassOwner = new HashMap<>();

	protected static FacPlayer teamCheck(FacPlayer link, String[] args){
		if (link.getRank().id < MemberRank.ADJOINT.id) link.sp.p.sendMessage("§cTu dois être au minimum adjoint pour gérer l'équipe de cette île !");
		else {
			if (args.length < 2) link.sp.p.sendMessage("§cMet un joueur en argument !");
			else {
				FacPlayer target = InternalAPI.getArgSP(link.sp.p, args[1], false);
				if (target != null) {
					if (target.equals(link.sp)) link.sp.p.sendMessage("§cCe joueur est.. toi même ?");
					else return target;
				}
			}
		}
		return null;
	}


	protected static FacPlayer isCheck(FacPlayer sp){
		if (sp.getIslands().size()==0){
			FacMenus.startIslandChooseOpen(sp);
		}
		else {
			FacPlayer link = sp.referentIsland(true);
			if (link == null){
				sp.p.sendMessage("§cTu dois d'abord choisir une île préférée pouvoir choisir ces options ! (On ne sait pas de laquelle tu parles !)");
				FacMenus.islandsListOpen(sp, true);
			}
			return link;
		}
		return null;
	}

}
