package fr.entasia.faccore.events;

import fr.entasia.faccore.apis.BaseAPI;
import fr.entasia.faccore.apis.FacPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatEvents implements Listener {

	@EventHandler
	public void onChat(AsyncPlayerChatEvent e){
		FacPlayer sp  = BaseAPI.getOnlineFP(e.getPlayer());
		assert sp != null;
		ISPLink link = sp.referentIsland(false);
		if(link!=null){
			if(sp.islandChat){
				e.setCancelled(true);
				link.is.islandChat(link, String.join(" ", e.getMessage()));
			}else{
				e.setFormat("[" + link.is.getLevel() + "] " + e.getFormat());
				// SUITE EVENT ICI
			}
		}

	}


}
