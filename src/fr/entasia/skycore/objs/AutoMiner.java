package fr.entasia.skycore.objs;

import fr.entasia.skycore.apis.BaseIsland;
import fr.entasia.skycore.objs.tasks.AutoMinerTask;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class AutoMiner {

	public static final byte MAX = 8;
	public static final Vector normaliser = new Vector(0.5, -0.3, 0.5);

	public Block hopper;
	public Block toBreak;
	public ItemStack pickaxe;
	public BaseIsland is;

	public ArmorStand[] armorStands = new ArmorStand[4];

	public AutoMiner(){
	}

	public void init(BaseIsland is, Block hopper, ItemStack pickaxe){ // Armor stands not spawned here
		this.is = is;
		this.hopper = hopper;
		this.toBreak = hopper.getRelative(BlockFace.UP);
		this.pickaxe = pickaxe;
		is.autominers.add(this);
		AutoMinerTask.miners.add(this);
	}


	public void deleteFull() {
		AutoMinerTask.miners.remove(this);
		is.autominers.remove(this);
		deleteAM();
		deleteVars();
	}

	public void deleteVars(){
		pickaxe = null;
		toBreak = null;
		hopper = null;
	}

	public void deleteAM(){
		deleteAMByBlock(hopper);
	}

	public static void deleteAMByBlock(Block b){
		Location loc = b.getLocation().add(normaliser);
		for(Entity ent : loc.getNearbyEntitiesByType(ArmorStand.class, 0.4)) {
			if ("AMPickaxe".equals(ent.getCustomName())) {
				ent.remove();
			}
		}
	}

	private static final Vector[] dirs = new Vector[4];

	static{
		// correction centrer
		dirs[0] = new Vector(0, 0, 0.05);
		dirs[1] = new Vector(-0.05, 0, 0);
		dirs[2] = new Vector(0, 0, -0.05);
		dirs[3] = new Vector(0.05, 0, 0);

		// correction rapprocher du milieu
		dirs[0].add(new Vector(0.3, 0, 0));
		dirs[1].add(new Vector(0, 0, 0.3));
		dirs[2].add(new Vector(-0.3, 0, 0));
		dirs[3].add(new Vector(0, 0, -0.3));

	}

	public void spawn(){
		Location loc = hopper.getLocation().add(normaliser);
		for(int i=0;i<4;i++){
			loc.setYaw(i*90);
			armorStands[i] = (ArmorStand) hopper.getWorld().spawnEntity(loc.clone().add(dirs[i]), EntityType.ARMOR_STAND);

			armorStands[i].setCustomName("AMPickaxe");

			armorStands[i].setInvulnerable(true);
			armorStands[i].setVisible(false);
			armorStands[i].setMarker(true);

			armorStands[i].setRemoveWhenFarAway(false);
			armorStands[i].setCanMove(false);
			armorStands[i].setGravity(false);
			armorStands[i].setBasePlate(false);
			armorStands[i].setRightArmPose(AutoMinerTask.baseAngle);

			armorStands[i].setItem(EquipmentSlot.HAND, pickaxe);
		}
	}
}
