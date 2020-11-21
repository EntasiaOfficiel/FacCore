package fr.entasia.skycore.events;

import fr.entasia.apis.other.Randomiser;
import fr.entasia.apis.utils.Serialization;
import fr.entasia.skycore.Main;
import fr.entasia.skycore.apis.BaseAPI;
import fr.entasia.skycore.apis.BaseIsland;
import fr.entasia.skycore.apis.OthersAPI;
import fr.entasia.skycore.apis.mini.Dimensions;
import fr.entasia.skycore.objs.AutoMiner;
import fr.entasia.skycore.objs.tasks.AutoMinerTask;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ArmorStand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Iterator;

public class MiningEvents implements Listener {


	public static ArrayList<Material> pickaxes = new ArrayList<>();

	static {
		pickaxes.add(Material.DIAMOND_PICKAXE);
		pickaxes.add(Material.GOLDEN_PICKAXE);
		pickaxes.add(Material.IRON_PICKAXE);
		pickaxes.add(Material.STONE_PICKAXE);
		pickaxes.add(Material.WOODEN_PICKAXE);
	}


	@EventHandler
	public void onClick(PlayerInteractEvent e){
		if(e.getHand()== EquipmentSlot.HAND&&e.getAction()== Action.RIGHT_CLICK_BLOCK&&e.getClickedBlock().getType()== Material.HOPPER&&e.getPlayer().isSneaking()){
			ItemStack item = e.getPlayer().getInventory().getItemInMainHand();
			if(pickaxes.contains(item.getType())){

				BaseIsland is = BaseAPI.getIsland(e.getClickedBlock().getLocation());
				if(is==null)return;
				if(is.getMember(e.getPlayer().getUniqueId())==null&&!OthersAPI.isMasterEdit(e.getPlayer())){
					e.getPlayer().sendMessage("§cTu n'es pas membre de cette île !");
				}else{
					e.setCancelled(true);
					for(AutoMiner am : is.autominers){
						if(e.getClickedBlock().equals(am.hopper)){
							e.getPlayer().getInventory().setItemInMainHand(am.pickaxe);
							am.pickaxe = item;
							for(ArmorStand as : am.armorStands){
								as.getEquipment().setItemInMainHand(am.pickaxe);
							}
							return;
						}
					}
					if (is.autominers.size()>=AutoMiner.MAX) {
						e.getPlayer().sendMessage("§cCette île à déja atteint son maximum de mineur automatiques ! (" + AutoMiner.MAX + ")");
						return;
					}

					new BukkitRunnable() {
						@Override
						public void run() {
							if(pickaxes.contains(item.getType())) {
								AutoMiner am = new AutoMiner();
								AutoMiner.deleteAMByBlock(e.getClickedBlock());

								am.init(is, e.getClickedBlock(), item);
								e.getPlayer().getInventory().setItemInMainHand(new ItemStack(Material.AIR));
								am.spawn();

								Location loc = am.hopper.getLocation();
								Main.sqlite.fastUpdate("INSERT INTO autominers (is_x, is_z, x, y, z, world, item) VALUES (?, ?, ?, ?, ?, ?, ?)",
										is.isid.x, is.isid.z, loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), loc.getWorld().getName(), Serialization.serialiseItem(item));
							}
						}
					}.runTaskLater(Main.main, 1);
				}
			}
		}
	}

	@EventHandler
	public void a(BlockBreakEvent e){
		if(e.getBlock().getType()==Material.HOPPER){
			AutoMiner.deleteAMByBlock(e.getBlock());
			BaseIsland is = BaseAPI.getIsland(e.getBlock().getLocation());
			if(is==null)return;
			Iterator<AutoMiner> ite = is.autominers.iterator();
			AutoMiner am;
			while(ite.hasNext()){
				am = ite.next();
				if(e.getBlock().equals(am.hopper)){
					am.toBreak.getWorld().dropItem(am.toBreak.getLocation(), am.pickaxe);
					am.deleteVars();
					AutoMinerTask.miners.remove(am);
					ite.remove();
					break;
				}
			}
		}
	}

	@EventHandler
	public void a(PlayerArmorStandManipulateEvent e){
		if("AMPickaxe".equals(e.getRightClicked().getCustomName())){
			e.setCancelled(true);
		}
	}

	public static final BlockFace[] directions = new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.UP};

	@EventHandler
	public void a(BlockFromToEvent e) {
		if (Dimensions.isIslandWorld(e.getBlock().getWorld())) {
			if (e.getBlock().getType() == Material.LAVA) {
				if (e.getToBlock().getType() == Material.AIR) { // cobblestone - a verif
					boolean nop = true;
					Material m;
					for (BlockFace bf : directions) {
						m = e.getToBlock().getRelative(bf).getType();
						if (m == Material.WATER) {
							nop = false;
							break;
						}
					}
					if (nop) return;
					e.setCancelled(true);
					e.getToBlock().setType(genBlock(Material.COBBLESTONE));
				} else if (e.getToBlock().getType() == Material.WATER) { // stone
					e.setCancelled(true);
					e.getToBlock().setType(genBlock(Material.STONE));
				}
			}
		}
	}

	public static Material genBlock(Material def){
		Randomiser r = new Randomiser();
		if(r.isInNext(6.2)){
			return Material.COAL_ORE;
		}else if(r.isInNext(5.2)){
			return Material.IRON_ORE;
		}else if(r.isInNext(0.8)){
			return Material.GOLD_ORE;
		}else if(r.isInNext(1.2)){
			return Material.LAPIS_ORE;
		}else if(r.isInNext(1.55)){
			return Material.REDSTONE_ORE;
		}else if(r.isInNext(0.1)){
			return Material.DIAMOND_ORE;
		}else if(r.isInNext(0.05)){
			return Material.EMERALD_ORE ;
		}else return def;
	}
}
