package fr.entasia.faccore.events;

import fr.entasia.apis.utils.PlayerUtils;
import fr.entasia.faccore.Main;
import fr.entasia.faccore.Utils;
import fr.entasia.faccore.apis.*;
import fr.entasia.faccore.apis.Dimensions;
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
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class BaseEvents implements Listener {


	@EventHandler(priority = EventPriority.LOW)
	public void onJoin(PlayerJoinEvent e) {
		if(InternalAPI.isFullyEnabled()){
			try{
				FacPlayer fp = BaseAPI.getFacPlayer(e.getPlayer().getUniqueId());
				if (fp==null) {
					fp = BaseAPI.registerFacPlayer(e.getPlayer());
					Bukkit.broadcastMessage("§3Bienvenue à §7" + e.getPlayer().getDisplayName() + "§63sur le Factions ! Souhaitons-lui la bienvenue !");
				}
				fp.p = e.getPlayer();
				fp.p.setMetadata("FacPlayer", new FixedMetadataValue(Main.main, fp));

				if(fp.hasFaction()&&fp.getFaction().getHome()!=null)fp.p.teleport(fp.getFaction().getHome());
				else fp.p.teleport(Utils.spawn);

			}catch(Exception e2){
				e2.printStackTrace();
				e.getPlayer().kickPlayer("§c): Une erreur est survenue lors de la lecture de ton profil Skyblock ! Contacte un Membre du Staff");
			}
		}else e.getPlayer().kickPlayer("§cLe Post-Loading du plugin Skyblock n'est pas terminé ! Si tu ne peux pas te connecter dans une minute, contacte un Membre du Staff");
	}

	@EventHandler
	public static void a(PlayerArmorStandManipulateEvent e){
		if(e.getRightClicked().getWorld()==Utils.spawnWorld){
			if(OthersAPI.isMasterEdit(e.getPlayer()))return;
			e.setCancelled(true);
		}
	}

	@EventHandler
	public static void antiSpawn(EntitySpawnEvent e){
		if(e.getLocation().getWorld()==Utils.spawnWorld){
			if(e.getEntity() instanceof Creature)e.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public static void onDamage(EntityDamageEvent e){
		if(e.getEntity().getWorld()==Utils.spawnWorld) {
			e.setCancelled(true);
			if(e.getEntity() instanceof Player) {
				if (e.getCause() == EntityDamageEvent.DamageCause.VOID) {
					e.getEntity().teleport(Utils.spawn);
				}
			}
			return;
		}
		if(e.getEntity() instanceof Player){
			Player p = (Player) e.getEntity();
			if (e.getCause() == EntityDamageEvent.DamageCause.VOID){
				e.setCancelled(true);
				p.teleport(Utils.spawn);
				p.sendMessage("§cTu y as échappé belle... ne refait pas ca s'il te plait");
			}else if (e.getCause() == EntityDamageEvent.DamageCause.FALL) e.setDamage(e.getDamage() / 2);
		}else if(e.getEntity() instanceof Snowman){
			if(e.getCause()==EntityDamageEvent.DamageCause.MELTING){
				e.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public static void antiKill(EntityDamageEvent e) {
		if (e.getEntity() instanceof Player) {
			Player p = (Player) e.getEntity();
			if (e.getFinalDamage() >= p.getHealth()) {
				e.setCancelled(true);
				p.sendMessage("§cTu es mort ! ):");
				for (PotionEffect pe : p.getActivePotionEffects()) {
					p.removePotionEffect(pe.getType());
				}

				FacPlayer sp = BaseAPI.getOnlineFP(p);
				assert sp != null;

				Location loc = Utils.spawn;
				// TODO CHECK S'IL EST SUR UN CLAIM DE SA FAC POUR LE TP AU HOME
//				if (Dimensions.isGameWorld(p.getWorld())) {
//					Faction is = BaseAPI.getFaction(p.getLocation());
//					if (is != null) loc = is.getHome();
//				}

				PlayerUtils.reset(p);
				p.setNoDamageTicks(80); // c'est pas des ticks
				p.teleport(loc);

				new BukkitRunnable() {
					@Override
					public void run() {
						p.setFireTicks(0);
						p.setVelocity(new Vector(0, 0, 0));
					}
				}.runTask(Main.main);
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onRespawn(PlayerRespawnEvent e){ // pas sensé se produire mais bon
		e.setRespawnLocation(Utils.spawn);
	}

	@EventHandler
	public void initEvent(WorldInitEvent e){
		switch(e.getWorld().getName().toLowerCase()){
			case "factions":
				Dimensions.OVERWORLD.world = e.getWorld();
				break;
			case "factions_nether":
				Dimensions.NETHER.world = e.getWorld();
				break;
			case "factions_the_end":
				Dimensions.END.world = e.getWorld();
				break;
			default:
				return;
		}
		Main.main.getLogger().info("loaded "+e.getWorld().getName());
		for(Dimensions d : Dimensions.values()){
			if(d.world==null)return;
		}
		InternalAPI.onPostEnable();
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
