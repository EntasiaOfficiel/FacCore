package fr.entasia.faccore.commands.base;

import fr.entasia.apis.other.ChatComponent;
import fr.entasia.faccore.Main;
import fr.entasia.faccore.apis.FacPlayer;
import fr.entasia.faccore.apis.Faction;
import fr.entasia.faccore.apis.InternalAPI;
import fr.entasia.faccore.apis.MemberRank;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;

public class FCmdUtils {

	protected static void sendInviteMsg(CommandSender sender, Faction fac){
		ChatComponent accept = new ChatComponent("§2[§aAccepter§2]");
		accept.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/f accept "+fac.id));
		accept.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§aClique pour accepter l'invitation !")));

		ChatComponent deny = new ChatComponent("§4[§cRefuser§4]");
		deny.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/f deny "+fac.id));
		deny.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§cClique pour refuser l'invitation !")));

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

	protected static FacPlayer argTeamCheck(FacPlayer fp, String[] args){
		if (fp.getRank().id < MemberRank.ADJOINT.id) fp.p.sendMessage("§cTu dois être au minimum adjoint pour gérer l'équipe de cette faction !");
		else {
			if (args.length < 2) fp.p.sendMessage("§cMet un joueur en argument !");
			else {
				FacPlayer target = InternalAPI.getArgSP(fp.p, args[1], false);
				if (target != null) {
					if (target.equals(fp)) fp.p.sendMessage("§cCe joueur est.. toi même ?");
					else return target;
				}
			}
		}
		return null;
	}
}
