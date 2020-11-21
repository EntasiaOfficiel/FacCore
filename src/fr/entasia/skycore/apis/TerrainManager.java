package fr.entasia.skycore.apis;

import com.boydti.fawe.FaweAPI;
import com.boydti.fawe.util.EditSessionBuilder;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.math.BlockVector3Imp;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockTypes;
import fr.entasia.apis.other.CodePasser;
import fr.entasia.apis.utils.ServerUtils;
import fr.entasia.apis.utils.TextUtils;
import fr.entasia.skycore.Main;
import fr.entasia.skycore.Utils;
import fr.entasia.skycore.apis.mini.Dimensions;
import fr.entasia.skycore.objs.IslandShematics;
import fr.entasia.skycore.objs.IslandType;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

	/*
	400/16 = 25 chunks par île, ca tombe pile. Faudra faire un système si jamais le diamètre de l'île est pas un nombre entier de chunks (réfléchis)
	 */

public class TerrainManager {

	public static BaseBlock bedrockBlock = new BaseBlock(BlockTypes.BEDROCK.getDefaultState());
	public static BaseBlock airBlock = new BaseBlock(BlockTypes.AIR.getDefaultState());
	public static HashMap<Material, Integer> blockValues = new HashMap<>();
	public static HashMap<Tag<Material>, Integer> catValues = new HashMap<>();
	private static final Random r = new Random();


	private static int getRandom() {
		int radius = 15;

		int possibilities = Utils.ISSIZE - radius*2;
		int a = r.nextInt(possibilities);
		if (a >= possibilities /2) a += radius;
		return a+10;
	}

	public static EditSession getSession(World w){
		return new EditSessionBuilder(FaweAPI.getWorld(w.getName())).fastmode(true).allowedRegionsEverywhere().build();
	}

	protected static ArrayList<ChunkSnapshot> getChunks(BaseIsland is, Dimensions dim){
		return getChunks(is.getMinXBuild(), is.getMaxXBuild(), is.getMinZBuild(), is.getMaxZBuild(), dim);
	}

	protected static ArrayList<ChunkSnapshot> getChunks(ISID isid, Dimensions dim){
		return getChunks(isid.getMinXTotal(), isid.getMaxXTotal(), isid.getMinZTotal(), isid.getMaxZTotal(), dim);
	}

	protected static ArrayList<ChunkSnapshot> getChunks(int minx, int maxx, int minz, int maxz, Dimensions dim){
		ServerUtils.wantMainThread();
		ArrayList<ChunkSnapshot> chunks = new ArrayList<>();

		Chunk ch;
		for(int x=minx;x<maxx;x+=16){
			for(int z=minz;z<maxz;z+=16) {
				ch = dim.world.getBlockAt(x, 70, z).getChunk();
				if (ch.isLoaded()) chunks.add(ch.getChunkSnapshot());
				else {
					ch.load();
					chunks.add(ch.getChunkSnapshot());
					ch.unload();
				}
			}
		}
		return chunks;
	}

	// LE BORDEL COMMENCE ICI


	public static void tryGenerateIsland(SkyPlayer sp, IslandType type){
		if(sp.getOwnerIsland()!=null){
			sp.p.sendMessage("§cTu es déja chef d'une île !");
			return;
		}
		if(sp.getIslands().size()>=5) {
			sp.p.sendMessage("§cTu as déja trop d'îles ! Quitte en pour pouvoir en créer une");
			return;
		}
		int ts = (int) (System.currentTimeMillis()/1000);
		int a = 60*60*24*5-(ts-sp.lastGenerated);
		if(a>0){ // 5 jours
			sp.p.sendMessage("§cTu dois encore attendre "+ TextUtils.secondsToTime(a)+" pour générer une nouvelle île !");
			return;
		}
		sp.setLastGenerated(ts);
		generateIsland(sp, type);
	}

	protected static void generateIsland(SkyPlayer sp, IslandType type){
		sp.p.sendMessage("§eGénération de ton île en cours.. Merci de patienter !");

		new BukkitRunnable(){
			@Override
			public void run() {
				try {

					ISID isid = CooManager.findFreeSpot();
					ISPLink link = BaseAPI.registerIsland(new BaseIsland(isid, type), sp);
					if(link==null)return;


					genOW(isid, type);
//					sp.p.sendMessage("§6Génération overworld terminée !");
//					genDimension(link.is, Dimensions.NETHER.world, Dimensions.NETHER.schems);
//					sp.p.sendMessage("§6Génération nether terminée !");
//					genDimension(link.is, Dimensions.END.world, Dimensions.END.schems);
//					sp.p.sendMessage("§6Génération end terminée !");

					new BukkitRunnable() {
						@Override
						public void run() {
							calcPoints(link.is, new CodePasser.Arg<Points>() {
								@Override
								public void run(Points p) {
									link.is.setMalus((int)p.rawpoints);
									sp.p.sendMessage("§aFin de création de ton île ! Téléportation au cours.. §eBonne aventure !");
									link.is.teleportHome(sp.p);
								}
							});
						}
					}.runTaskLater(Main.main, 20);

				}catch(Throwable e){
					e.printStackTrace();
					sp.p.sendMessage("§cUne erreur s'est produite lors de la création de ton île ! Contacte un Membre du Staff");
				}
			}
		}.runTaskAsynchronously(Main.main);
	}

	public static void genOW(ISID isid, IslandType type) {
		EditSession session = getSession(Dimensions.OVERWORLD.world);
		genBaseDimension(isid, session, type.schems);
		setBiome(isid, Dimensions.OVERWORLD.world, type.biome);

		// Ile géante

		int x = isid.getMinXTotal();
		int z = isid.getMinZTotal();
		switch (r.nextInt(4)) {
			case 0:
				x+=50;
				z+= r.nextInt(Utils.ISSIZE-100)+50;
				break;
			case 1:
				x+= Utils.ISSIZE - 50;
				z+= r.nextInt(Utils.ISSIZE-100)+50;
				break;
			case 2:
				x+= r.nextInt(Utils.ISSIZE-100)+50;
				z+= 50;
				break;
			case 3:
				x+= r.nextInt(Utils.ISSIZE-100)+50;
				z+= Utils.ISSIZE - 50;
				break;
		}
		type.schems.structures[0].paste(session, BlockVector3Imp.at(x, 70, z), false);
		session.flushQueue();

	}

	public static void genNether(ISID isid) {
		EditSession session = getSession(Dimensions.NETHER.world);
		genBaseDimension(isid, session, Dimensions.NETHER.schems);
	}

	public static void genEnd(ISID isid) {
		EditSession session = getSession(Dimensions.END.world);
		genBaseDimension(isid, session, Dimensions.END.schems);
	}

	protected static void calcPoints(BaseIsland is, CodePasser.Arg<Points> code){
		ServerUtils.wantMainThread();
		ArrayList<ChunkSnapshot> chunks = getChunks(is.isid, Dimensions.OVERWORLD);

		new BukkitRunnable() {
			@Override
			public void run() {
				Material m;
				Integer i;
				long points = 0;
				for (ChunkSnapshot cs : chunks) {
					for (int x = 0; x < 16; x++) {
						for (int y = 0; y < 256; y++) {
							for (int z = 0; z < 16; z++) {
								m = cs.getBlockType(x, y, z);
								if (m != Material.AIR) {
									i = blockValues.get(m);
									if (i == null) {
										for (Map.Entry<Tag<Material>, Integer> e : catValues.entrySet()) {
											if (e.getKey().isTagged(m)) {
												points += e.getValue();
												break;
											}
										}
									} else points += i;
								}
							}
						}
					}
				}

				Points p = new Points();
				p.rawpoints = points - is.malus;
				is.rawpoints = points - is.malus;
				System.out.println(is.rawpoints);
				if (p.rawpoints < 0) { // security
					p.rawpoints = 0;
					p.level = 0;
					p.remaning = 50;
				} else {
					levelAlg(p);
				}

				new BukkitRunnable() {
					@Override
					public void run() {
						try {
							code.run(p);
						} catch (Throwable e) {
							e.printStackTrace();
						}
					}
				}.runTask(Main.main);
			}
		}.runTaskAsynchronously(Main.main);
	}

	public static void levelAlg(Points p){
		p.level = -1;
		int rem = 50;
		long raw = p.rawpoints;
		while(raw>0){
			p.level++;
			raw-=rem;
			if(p.level %100==0) rem*=1.1;
		}
		p.remaning = (int)-raw;
	}


	public static class Points{
		public long rawpoints;
		public int remaning;
		public int level;
	}



	public static boolean clearTerrain(ISID isid, EditSession editSession){
		ServerUtils.wantChildThread();
		try{
			editSession.setBlocks((Region) new CuboidRegion(
					BlockVector3Imp.at(isid.getMinXTotal(), 0, isid.getMinZTotal()),
					BlockVector3Imp.at(isid.getMaxXTotal(), 255, isid.getMaxZTotal())
			), airBlock);
			editSession.flushQueue();
			return true;
		}catch(Exception e){
			e.printStackTrace();
		}
		return false;
	}

	public static void setBiome(ISID isid, World w, Biome biome){
		int minx = isid.getMinXTotal();
		int maxx = isid.getMaxXTotal();
		int minz = isid.getMinZTotal();
		int maxz = isid.getMaxZTotal();

		for(int x=minx;x<=maxx;x++) {
			for (int y = minx; y < 256; y++) {
				for (int z = minz; z <= maxz; z++) {
					w.setBiome(x, y, z, biome);
				}
			}
		}
	}


	protected static void genBaseDimension(ISID isid, EditSession session, IslandShematics isc) {
		ServerUtils.wantChildThread();

		int x = isid.getMinXTotal();
		int z = isid.getMinZTotal();

		// FILL
		clearTerrain(isid, session);

		// GENERATION MINI ILES

		int j = 0;
		AffineTransform transform = new AffineTransform();
		BlockVector3Imp loc;
		for (int i = 0; i < 50; i++) {
			loc = BlockVector3Imp.at(x + getRandom(),70, z + getRandom());

			isc.miniIslands[j].paste(session, loc, false, transform.rotateY(90*r.nextInt(4)));
			j++;
			if (j == isc.miniIslands.length)j = 0;
		}
		session.flushQueue();

		// GENERATION ILE NORMALE
		x = isid.getMiddleX();
		z = isid.getMiddleZ();

		loc = BlockVector3Imp.at(x, 70, z);
		isc.island.paste(session, loc, true); // ca marche

		session.setBlock(x, 65, z, bedrockBlock);

		session.flushQueue();

	}
}
