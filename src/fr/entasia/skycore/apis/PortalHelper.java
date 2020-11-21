package fr.entasia.skycore.apis;

import fr.entasia.skycore.Main;
import fr.entasia.skycore.apis.mini.Dimensions;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;

public class PortalHelper {

	protected static void findNetherPortal(BaseIsland is, Player p, Dimensions in) {
		ArrayList<ChunkSnapshot> chunks = TerrainManager.getChunks(is, in);
		new BukkitRunnable() {
			@Override
			public void run() {
				Location loc = p.getLocation();
				final boolean y_axis = loc.getBlock().getData() == 2;

				Material type;
				for (ChunkSnapshot cs : chunks) {
					for (int x = 0; x <= 15; x++) {
						for (int y = 0; y < 256; y += 3) {
							for (int z = 0; z <= 15; z++) {
								type = cs.getBlockType(x, y, z);
								if (type == Material.NETHER_PORTAL) { // PORTAIL TROUVE : DETECTION SENS
									int finalX = x;
									int finalY = y;
									int finalZ = z;
									new BukkitRunnable() {
										@Override
										public void run() {
											Location finalLoc = new Location(in.world, cs.getX() * 16 + finalX, finalY, cs.getZ() * 16 + finalZ,
													loc.getYaw(), loc.getPitch());
											final boolean dest_y_axis = loc.getBlock().getData() == 2;
											if (dest_y_axis != y_axis) {
												float yaw = finalLoc.getYaw();
												yaw += 90;
												finalLoc.setYaw(yaw);
											}
											while (finalLoc.getBlock().getType() == Material.NETHER_PORTAL) {
												finalLoc = finalLoc.add(0, -1, 0);
											}
											finalLoc = finalLoc.add(0.5, 1.2, 0.5);
											if (in == Dimensions.OVERWORLD) is.OWNetherPortal = finalLoc;
											else if (in == Dimensions.NETHER) is.netherPortal = finalLoc;
											else{
												InternalAPI.warn("Dimension invalide !", true);
												return;
											}
											p.teleport(finalLoc);
										}
									}.runTask(Main.main);
									return;
								}
							}
						}
					}
				}
				new BukkitRunnable() {
					@Override
					public void run() {
						if(in== Dimensions.OVERWORLD)p.teleport(is.isid.getMiddleLoc(Dimensions.OVERWORLD));
						else if(in== Dimensions.NETHER) p.teleport(is.isid.getMiddleLoc(Dimensions.NETHER));
						else InternalAPI.warn("Dimension invalide !", true);
					}
				}.runTask(Main.main);
			}
		}.runTaskAsynchronously(Main.main);
	}

	protected static void findEndPortal(BaseIsland is, Player p, Dimensions in) {
		ArrayList<ChunkSnapshot> chunks = TerrainManager.getChunks(is, in);
		new BukkitRunnable() {
			@Override
			public void run() {
				Material type;
				for (ChunkSnapshot cs : chunks) {
					for (int x = 0; x <= 15; x++) {
						for (int y = 0; y < 256; y += 3) {
							for (int z = 0; z <= 15; z++) {
								type = cs.getBlockType(x, y, z);
								if (type == Material.END_PORTAL) {
									Location loc = new Location(in.world, cs.getX() * 16 + x, y, cs.getZ() * 16 + z,
											p.getLocation().getYaw(), p.getLocation().getPitch());

									if(loc.clone().add(1, 0, 0).getBlock().getType()==Material.END_PORTAL_FRAME){
										loc.add(-1, 0, 0);
									}else if(loc.clone().add(-1, 0, 0).getBlock().getType()==Material.END_PORTAL_FRAME) {
										loc.add(1, 0, 0);
									}

									while(loc.getBlock().getType()==Material.END_PORTAL){
										loc.add(0, 0, 1);
									}

									Location finalLoc = loc.add(0.5, 0.2, 1.5);
									new BukkitRunnable() {
										@Override
										public void run() {
											if (in == Dimensions.OVERWORLD) is.OWNetherPortal = finalLoc;
											else if (in == Dimensions.END) is.endPortal = finalLoc;
											else{
												InternalAPI.warn("Dimension invalide !", true);
												return;
											}
											p.teleport(finalLoc);
										}
									}.runTask(Main.main);
									return;
								}
							}
						}
					}
				}
				new BukkitRunnable() {
					@Override
					public void run() {
						if(in==Dimensions.OVERWORLD)p.teleport(is.isid.getMiddleLoc(Dimensions.OVERWORLD));
						else if(in== Dimensions.END)p.teleport(is.isid.getMiddleLoc(Dimensions.END));
						else InternalAPI.warn("Dimension invalide ! "+in, true);
					}
				}.runTask(Main.main);
			}
		}.runTaskAsynchronously(Main.main);
	}
}
