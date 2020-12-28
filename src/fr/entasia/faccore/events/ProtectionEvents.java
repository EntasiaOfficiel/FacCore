package fr.entasia.faccore.events;

import com.destroystokyo.paper.Title;
import fr.entasia.apis.regionManager.api.Region;
import fr.entasia.apis.regionManager.api.RegionManager;
import fr.entasia.faccore.Utils;
import fr.entasia.faccore.apis.*;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.Lockable;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.projectiles.ProjectileSource;

import java.util.List;

public class ProtectionEvents implements Listener {

//	private static final Set<Material> containers = EnumSet.of(
//			Material.CHEST,
//			Material.TRAPPED_CHEST,
//
//			Material.DROPPER,
//			Material.DISPENSER,
//			Material.HOPPER,
//
//			Material.FURNACE,
//			Material.BLAST_FURNACE,
//
//			Material.BREWING_STAND,
//			Material.BEACON
//	);

	public static boolean isContainer(Block b) { // TODO CHECK SI CA MARCHE
		return  b.getState() instanceof Lockable; // il a l'air plus puissant que Container
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public static void onDamage(EntityDamageByEntityEvent e) {
		if (e.getEntity() instanceof Player){
			Player victim = (Player) e.getEntity();
			FacPlayer fplayer = BaseAPI.getFacPlayer(victim);
			Faction faction = fplayer.getFaction();
			if (Dimension.isGameWorld(victim.getWorld())){




				Player pDamager = null;
				if (e.getDamager() instanceof Player) {
					pDamager = (Player) e.getDamager();
				}
				if (e.getDamager() instanceof Arrow){
					Arrow arrow = (Arrow) e.getEntity();
					ProjectileSource damager = arrow.getShooter();
					if(damager instanceof Player){
						pDamager = (Player) damager;

					}
				}

				if(Utils.spawnRegion.containsLocation(victim.getLocation())){
					e.setCancelled(true);
				}

				if(pDamager != null){
					FacPlayer fDamager = BaseAPI.getFacPlayer(pDamager);
					Faction dFaction = fDamager.getFaction();

					if(dFaction == null || faction == null)return;

					if(faction == dFaction){
						e.setCancelled(true);
						pDamager.sendMessage("§cCette personne fait partie de ta faction !");
					}

					FactionRelation relation = faction.getSideRelation(dFaction);
					FactionRelation dRelation = dFaction.getSideRelation(faction);

					if(relation == dRelation && (relation == FactionRelation.ALLY || relation == FactionRelation.TRUCE )){
						e.setCancelled(true);
						pDamager.sendMessage("§cVos factions sont alliées");
					}



				}
			}
		}
	}


	@EventHandler
	public void interact(PlayerBucketFillEvent e){
		if(isBlockDenied(e.getPlayer(), e.getBlockClicked()))e.setCancelled(true);
	}

	@EventHandler
	public void interact(PlayerBucketEmptyEvent e){
		if(isBlockDenied(e.getPlayer(), e.getBlockClicked()))e.setCancelled(true);
	}

	@EventHandler
	public void interact(PlayerInteractEvent e){
		Player p = e.getPlayer();
		if(Dimension.isGameWorld(p.getWorld())){
			if(e.getAction()==Action.RIGHT_CLICK_BLOCK) {
				Block b = e.getClickedBlock();
				assert b != null;
				Material m = b.getType();

				if(m==Material.CAKE) {
					if (isBlockDenied(p, b)) e.setCancelled(true);
				} else if (Tag.SHULKER_BOXES.isTagged(m)){
					if (isBlockDenied(p, b)) e.setCancelled(true);
				}else if (isContainer(b)) {
					if (isBlockDenied(p, b)) e.setCancelled(true);
				}
			}else if(e.getAction()==Action.LEFT_CLICK_BLOCK) {
				// fire check
				for(Block lb : p.getLineOfSight(null, 5)){
					if(lb.getType()==Material.FIRE){
						if(isBlockDenied(p, lb))e.setCancelled(true);
						return;
					}
				}
			}
		} // on bloque pas les interactions dans les autres mondes (pour le moment ?)
	}

	private static boolean isBlockDenied(Player p, Block b){
		if(OthersAPI.isMasterEdit(p))return false;
		if(Dimension.isGameWorld(p.getWorld())) {

			if(Utils.spawnRegion.containsLocation(b.getLocation()) || Utils.warzone.containsLocation(b.getLocation())){
				p.sendMessage("§cCette zone est protégée !");
				return true;
			}
			Faction fac = BaseAPI.getFaction(b.getLocation());
			if (fac != null) {
				FacPlayer fp = BaseAPI.getOnlineFP(p);
				Faction playerFac = fp.getFaction();


				if(playerFac != fac){
					p.sendMessage("§cTu n'es pas membre de cette faction !");
					return true;
				} else if (fac.getMembers().contains(fp) && fp.getRank() == MemberRank.RECRUE && isContainer(b)) {
					p.sendMessage("§cTu es seulement une recrue dans cette Faction ! Tu ne peux pas intéragir avec les containers");
				} else return false;

				if (fac.getSideRelation(playerFac).equals(FactionRelation.ALLY) && playerFac.getSideRelation(fac).equals(FactionRelation.ALLY)) {
					return false;
				}
			}



			return false;
		}
		return false;
	}

	@EventHandler
	public void blockBreak(BlockBreakEvent e){
		if(isBlockDenied(e.getPlayer(), e.getBlock())) e.setCancelled(true);
	}

	@EventHandler
	public void blockPlace(BlockPlaceEvent e){
		if(isBlockDenied(e.getPlayer(), e.getBlock()))e.setCancelled(true);
	}

	@EventHandler
	public void InteractEvent(PlayerInteractEvent e){
		if(e.getPlayer().getLocation().getWorld()==Dimension.OVERWORLD.world){
			if(e.getAction()==Action.PHYSICAL)return;

			if(!OthersAPI.isMasterEdit(e.getPlayer())){
				switch(e.getPlayer().getInventory().getItemInMainHand().getType()){
					case BUCKET:
					case LAVA_BUCKET:
					case WATER_BUCKET:
						e.setCancelled(true);
				}

			}
		}
	}

	@EventHandler
	public void BlockChange(EntityChangeBlockEvent e) {
		if(Dimension.isGameWorld(e.getBlock().getWorld())){
			if(Utils.spawnRegion.containsLocation(e.getBlock().getLocation())){
				e.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onMove(PlayerMoveEvent e){
		Player p = e.getPlayer();
		if(Dimension.isGameWorld(p.getWorld())){

			if(e.getFrom().getChunk() == e.getTo().getChunk()) return;

			Faction fr = BaseAPI.getFaction(e.getFrom());
			Faction to = BaseAPI.getFaction(e.getTo());
			if(fr!=to){ // changement de Faction
				Title title;
				if (to == null) {
					List<Region> regs = RegionManager.getRegionsAt(e.getTo());

					if(regs.contains(Utils.warzone)){
						title = new Title("§cZarzone ","§cAu combat !",1,20,1);
					}else{
						title = new Title("§2Zone libre ",null,1,20,1);
					}
				} else {
					title = new Title("§c"+to.getName(), null, 1, 20, 1);
				}
				e.getPlayer().sendTitle(title);
			}
		}
	}
}
