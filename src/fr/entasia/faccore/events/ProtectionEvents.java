package fr.entasia.faccore.events;

import fr.entasia.faccore.Utils;
import fr.entasia.faccore.apis.*;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
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

import java.util.EnumSet;
import java.util.Set;

public class ProtectionEvents implements Listener { // TODO A RVOIR TOTALEMENT

	private static final Set<Material> containers = EnumSet.of( // TODO Openable ou Container
			Material.CHEST,
			Material.TRAPPED_CHEST,

			Material.DROPPER,
			Material.DISPENSER,
			Material.HOPPER,

			Material.FURNACE,
			Material.BLAST_FURNACE,

			Material.BREWING_STAND,
			Material.BEACON
	);

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public static void onDamage(EntityDamageByEntityEvent e) {
		if (e.getEntity() instanceof Player){
			Player p = (Player) e.getEntity();
			if (Dimensions.isGameWorld(p.getWorld())){
				if (e.getDamager() instanceof Player) {
					e.setCancelled(true);
					return;
				}
				if (e.getDamager() instanceof Firework) e.setCancelled(true);
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
		if(Dimensions.isGameWorld(p.getWorld())&&e.hasBlock()){
			if(e.getAction()==Action.RIGHT_CLICK_BLOCK) {
				Block b = e.getClickedBlock();
				Material m = b.getType();

				if(m==Material.CAKE) {
					if (isBlockDenied(p, b)) e.setCancelled(true);
				} else if (Tag.SHULKER_BOXES.isTagged(m)){
					if (isBlockDenied(p, b)) e.setCancelled(true);
				}else if (containers.contains(m)) {
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
		if(Utils.masterEditors.contains(p)&&p.getGameMode()==GameMode.CREATIVE)return false;
		if(Dimensions.isGameWorld(p.getWorld())) {
			Faction fac = BaseAPI.getIsland(b.getLocation());
			if (fac != null) {
				FacPlayer fp = fac.getMember(p.getUniqueId());
				if (fp == null) p.sendMessage("§cTu n'est pas membre de cette ile !");
				else {
					if (fac.hasDimension(Dimensions.getDimension(p.getWorld()))) {
						int m = fac.facID.distanceFromIS(b.getLocation());
						if ((fac.getExtension() + 1) * 50 < m) {
							p.sendMessage("§cL'extension de ton ile n'est pas suffisante !");
						} else if (fp.getRank() == MemberRank.RECRUE && containers.contains(b.getType())) {
							p.sendMessage("§cTu es seulement une recrue sur cette ile ! Tu ne peux pas intéragir avec les containers");
						} else return false;
					} else p.sendMessage("§Ton île n'a pas encore débloqué cette dimension ! Utilise un portail pour la débloquer");
				}
			}
		}
		return true;
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
		if(e.getPlayer().getLocation().getWorld()==Utils.spawnWorld){
			if(e.getAction()==Action.PHYSICAL)return;

			if(!OthersAPI.isMasterEdit(e.getPlayer())){
				switch(e.getPlayer().getInventory().getItemInMainHand().getType()){
					case BUCKET:
					case LAVA_BUCKET:
					case WATER_BUCKET:
						e.setCancelled(true);
				}
				e.setUseInteractedBlock(Event.Result.DENY);
			}
		}
	}

	@EventHandler
	public void WitherEatBlocks(EntityChangeBlockEvent e) {
		if(Dimensions.isGameWorld(e.getBlock().getWorld())){
			if(e.getEntityType()== EntityType.WITHER){
				e.setCancelled(true);
			}
		}
	}



	private static String checkIs(Faction is, Player p){ // temporaire ? ou pas
		if(is.getMember(p.getUniqueId())==null){
			return "l'ile de §6"+is.getOwner().sp.name+"§f";
		}else{
			return "ton ile";
		}
	}

	@EventHandler
	public void onMove(PlayerMoveEvent e){
		Player p = e.getPlayer();
		if(Dimensions.isGameWorld(p.getWorld())){
			Faction fr = BaseAPI.getIsland(e.getFrom());
			Faction to = BaseAPI.getIsland(e.getTo());
			if(fr==to){ // on est sur la même île
				if(fr!=null){
					int ext = (fr.getExtension()+1)*50;
					int m1 = fr.facID.distanceFromIS(e.getFrom());
					int m2 = fr.facID.distanceFromIS(e.getTo());
					if(m1<ext){
						if(m2>=ext){
							e.getPlayer().sendActionBar("§fTu es sorti de la zone de "+ checkIs(fr, p)+" !");
						}
					}else if (m2<ext){
						e.getPlayer().sendActionBar("§fTu es rentré dans la zone de "+ checkIs(fr, e.getPlayer())+" !");
					}
				}
			}else{
				if (to == null) {
					e.getPlayer().sendActionBar("§fTu es sorti de "+ checkIs(fr, p)+" !");
				} else {
					e.getPlayer().sendActionBar("§fTu es rentré sur "+ checkIs(to, p)+" !");
				}
			}
		}
	}
}
