package fr.entasia.faccore.events;

import fr.entasia.apis.utils.PlayerUtils;
import fr.entasia.faccore.Main;
import fr.entasia.faccore.Utils;
import fr.entasia.faccore.apis.BaseAPI;
import fr.entasia.faccore.apis.FacPlayer;
import fr.entasia.faccore.apis.OthersAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowman;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class BaseEvents implements Listener {

	@EventHandler(priority = EventPriority.LOW)
	public void onJoin(PlayerJoinEvent e) {
		try {
			FacPlayer fp = BaseAPI.getFacPlayer(e.getPlayer().getUniqueId());
			if (fp == null) {
				fp = BaseAPI.registerFacPlayer(e.getPlayer());
				Bukkit.broadcastMessage("§3Bienvenue à §7" + e.getPlayer().getDisplayName() + "§6 sur le Factions ! Souhaitons-lui la bienvenue !");
			}
			fp.p = e.getPlayer();
			fp.p.setMetadata("FacPlayer", new FixedMetadataValue(Main.main, fp));

			if (fp.getFaction() != null && fp.getFaction().getHome() != null) fp.p.teleport(fp.getFaction().getHome());
			else fp.p.teleport(Utils.spawn);

		} catch (Exception e2) {
			e2.printStackTrace();
			e.getPlayer().kickPlayer("§c): Une erreur est survenue lors de la lecture de ton profil Factions ! Contacte un Membre du Staff");
		}
	}

	@EventHandler
	public static void a(PlayerArmorStandManipulateEvent e){
		if(e.getRightClicked().getWorld()==Utils.spawn.getWorld()){
			if(OthersAPI.isMasterEdit(e.getPlayer()))return;
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void a(AsyncPlayerChatEvent e) {
		FacPlayer fp = BaseAPI.getOnlineFP(e.getPlayer());
		assert fp != null;
		if(fp.internalChat) {
			e.setCancelled(true);
			fp.getFaction().islandChat(fp, String.join(" ", e.getMessage()));
		}
	}

	@EventHandler
	public static void antiSpawn(EntitySpawnEvent e){
		if(e.getLocation().getWorld()==Utils.spawn.getWorld()){
			if(e.getEntity() instanceof Creature)e.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public static void onDamage(EntityDamageEvent e){
		if(Utils.spawnRegion.containsLocation(e.getEntity().getLocation())) {
			e.setCancelled(true);
			return;
		}
		if(e.getEntity() instanceof Snowman){
			if(e.getCause()==EntityDamageEvent.DamageCause.MELTING){
				e.setCancelled(true);
			}
		}
	}


	@EventHandler(priority = EventPriority.MONITOR)
	public void onRespawn(PlayerRespawnEvent e){ // pas sensé se produire mais bon
		e.setRespawnLocation(Utils.spawn);
	}


	@EventHandler
	public void a(EntityExplodeEvent e){
		e.blockList().clear();
	}

	@EventHandler
	public void a(BlockExplodeEvent e){
		e.blockList().clear();
	}

}
