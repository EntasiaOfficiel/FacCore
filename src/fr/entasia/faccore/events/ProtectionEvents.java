package fr.entasia.faccore.events;

import com.destroystokyo.paper.Title;
import fr.entasia.apis.regionManager.api.Region;
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
			if (Dimension.isGameWorld(p.getWorld())){
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
		if(Dimension.isGameWorld(p.getWorld())&&e.hasBlock()){
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
		if(Dimension.isGameWorld(p.getWorld())) {
			Faction fac = BaseAPI.getFaction(b.getLocation());
			if (fac != null) {
				FacPlayer fp = fac.getMember(p.getUniqueId());
				if (fp == null) p.sendMessage("§cTu n'es pas membre de cette faction !");
				else {
					if (fp.getRank() == MemberRank.RECRUE && containers.contains(b.getType())) {
						p.sendMessage("§cTu es seulement une recrue sur cette ile ! Tu ne peux pas intéragir avec les containers");
					} else return false;
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
		if(Dimension.isGameWorld(e.getBlock().getWorld())){
			if(e.getEntityType()== EntityType.WITHER){
				e.setCancelled(true);
			}
		}
	}



	private static String checkIs(Faction f, Player p){ // temporaire ? ou pas
		if(f.getMember(p.getUniqueId())==null){
			return "La faction §6"+f.getName()+"§f";
		}else{
			return "Ta faction";
		}
	}

	@EventHandler
	public void onMove(PlayerMoveEvent e){
		Player p = e.getPlayer();
		if(Dimension.isGameWorld(p.getWorld())){




			Faction fr = BaseAPI.getFaction(e.getFrom());
			Faction to = BaseAPI.getFaction(e.getTo());
			if(fr!=to){ // changement de Faction
				if (to != null) {
					Title title = new Title("§cTu es entré dans la faction "+to.getName(),null,1,20,1);
					e.getPlayer().sendTitle(title);
				} else {

					Region reg =
					e.getPlayer().sendActionBar("§fTu es rentré sur "+ checkIs(to, p)+" !");
				}
			}
		}
	}
}
