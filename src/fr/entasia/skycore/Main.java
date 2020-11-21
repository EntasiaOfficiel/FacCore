package fr.entasia.skycore;

import com.destroystokyo.paper.MaterialTags;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import fr.entasia.apis.sql.SQLConnection;
import fr.entasia.skycore.apis.TerrainManager;
import fr.entasia.skycore.apis.mini.Dimensions;
import fr.entasia.skycore.commands.base.*;
import fr.entasia.skycore.commands.manage.*;
import fr.entasia.skycore.events.*;
import fr.entasia.skycore.objs.IslandShematics;
import fr.entasia.skycore.objs.IslandType;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.util.*;

public class Main extends JavaPlugin {

	/*
	Stratégie actuelle :

	- Charger toutes les iles au démarrage
	- Charger tout les joueurs au démarragze
	 */

	public static Main main;
	public static boolean dev;

	public static SQLConnection sql;
	public static SQLConnection sqlite;

	public static File blockValuesFile;
	public static FileConfiguration blockValues;

	@Override
	public void onEnable(){
		try{
			main = this;
			getLogger().info("Activation du plugin méga-badass...");
			Utils.spawnWorld = Bukkit.getWorlds().get(0);

			dev = main.getConfig().getBoolean("dev", false);

			loadConfigs();

			sql = new SQLConnection(dev).mariadb("skycore", "playerdata");
			sqlite = new SQLConnection(dev).sqlite("plugins/"+getName()+"/database.db");

			getServer().getPluginManager().registerEvents(new BaseEvents(), this);
			getServer().getPluginManager().registerEvents(new IslandEvents(), this);
			getServer().getPluginManager().registerEvents(new DimensionEvents(), this);
			getServer().getPluginManager().registerEvents(new ChatEvents(), this);
			getServer().getPluginManager().registerEvents(new MiningEvents(), this);

			getCommand("skycore").setExecutor(new SkyCoreCommand());
			getCommand("isadmin").setExecutor(new IsAdminCommand());
			getCommand("setspawn").setExecutor(new SetSpawnCommand());
			getCommand("setspawn").setExecutor(new SetSpawnCommand());
			getCommand("masteredit").setExecutor(new MasterEditCommand());

			getCommand("baltop").setExecutor(new BaltopCommand());
			getCommand("money").setExecutor(new MoneyCommand());
			getCommand("pay").setExecutor(new PayCommand());
			getCommand("eco").setExecutor(new EcoCommand());
			getCommand("bin").setExecutor(new BinCommand());
			getCommand("is").setExecutor(new IsCommand());
			getCommand("spawn").setExecutor(new SpawnCommand());
			getCommand("rank").setExecutor(new RankCommand());

			loadIslandStructs();

		}catch(Throwable e){
			e.printStackTrace();
			if(!dev){
				getLogger().severe("Erreur lors du chargement du plugin ! ARRET DU SERVEUR");
				getServer().shutdown();
			}
		}
	}

	public static void main(String[] args){
		List<String> list = Collections.synchronizedList(new ArrayList<>());

		new Thread(){
			@Override
			public void run() {
				System.out.println("debut add");
				for(int i=0;i<Math.pow(10, 5);i++){
					list.add("a");
				}
				System.out.println("fin add");
			}
		}.start();

		new Thread(){
			@Override
			public void run() {
				System.out.println("debut rem");
				for(int i=0;i<Math.pow(10, 5);i++) {
					list.remove(0);
				}
				System.out.println("fin rem");
			}
		}.start();

		list.removeIf(i->{
			i.length();
			return false;
		});
//		for(String i : list){
//			i.length();
//		}
	}

	public static class VoidGenerator extends ChunkGenerator {

		@Override
		public ChunkData generateChunkData(@Nullable World world, @Nullable Random random, int chunkX, int chunkZ, @Nullable BiomeGrid biome) {
			return createChunkData(world);
		}
	}

	@Override
	public ChunkGenerator getDefaultWorldGenerator(@Nullable String worldName, String id) {
		return new VoidGenerator();
	}

	public static void loadConfigs() throws Exception {
		main.saveDefaultConfig();
		blockValuesFile = new File(main.getDataFolder(), "blockvalues.yml");
		if(!blockValuesFile.exists()) Files.copy(main.getResource("blockvalues.yml"), blockValuesFile.toPath());

		blockValues = YamlConfiguration.loadConfiguration(blockValuesFile);
		main.reloadConfig();

		ConfigurationSection sec = main.getConfig().getConfigurationSection("spawn");
		Utils.spawn = new Location(Utils.spawnWorld, sec.getInt("x")+0.5, sec.getInt("y") + 0.2,
				sec.getInt("z")+0.5, sec.getInt("yaw"), sec.getInt("pitch"));

		Material m;

		TerrainManager.blockValues.clear();
		TerrainManager.catValues.clear();

		for(Map.Entry<String, Object> e : blockValues.getConfigurationSection("blocks").getValues(false).entrySet()){
			m = Material.getMaterial(e.getKey());
			if(m==null){
				main.getLogger().warning("Material invalide : "+e.getKey());
				continue;
			}
			TerrainManager.blockValues.put(m, (int) e.getValue());
		}

		Field f;
		for(Map.Entry<String, Object> e : blockValues.getConfigurationSection("categories").getValues(false).entrySet()) {
			try {
				f = Tag.class.getDeclaredField(e.getKey());
			} catch (NoSuchFieldException ignore) {
				try {
					f = MaterialTags.class.getDeclaredField(e.getKey());
				} catch (NoSuchFieldException ignore2) {
					main.getLogger().warning("Catégorie invalide : " + e.getKey());
					continue;
				}
			}
			TerrainManager.catValues.put((Tag<Material>) f.get(null), (int) e.getValue());
		}

		ArrayList<String> tag = new ArrayList<>();
		for(Material m2 : Material.values()){
			tag.clear();

			// pas forcément besoin
//			i = TerrainManager.blockValues.get(m);
//			if(i!=null)tag.add("Normal");

			for(Map.Entry<Tag<Material>, Integer> e : TerrainManager.catValues.entrySet()){
				if(e.getKey().isTagged(m2)){
					tag.add(e.getKey().getKey().toString());
				}
			}
			if(tag.size()>1){
				main.getLogger().warning("Material "+m2+" représenté plusieurs fois :");
				System.out.println(tag);
			}
		}

	}

	public void loadIslandStructs() throws Exception {
		ArrayList<IslandShematics> allis = new ArrayList<>();
		String[] base = new String[]{"geante"};
		for(IslandType it : IslandType.values()){
			it.schems = new IslandShematics();
			it.schems.name = it.name().toLowerCase();
			it.schems.plans = base;
			allis.add(it.schems);
		}
		Dimensions.NETHER.schems = new IslandShematics();
		Dimensions.NETHER.schems.name = "nether";
		Dimensions.NETHER.schems.plans = new String[0];
		allis.add(Dimensions.NETHER.schems);

		Dimensions.END.schems = new IslandShematics();
		Dimensions.END.schems.name = "end";
		Dimensions.END.schems.plans = new String[0];
		allis.add(Dimensions.END.schems);

//		DimensionType.CLOUD.schems = new IslandShematics();
//		DimensionType.CLOUD.schems.name = "clouds";
//		DimensionType.CLOUD.schems.plans = new String[0];
//		allis.add(DimensionType.CLOUD.schems);

		for(IslandShematics isc : allis){
			getLogger().info("Chargement de l'ile "+isc.name+"...");

			File f = new File(Main.main.getDataFolder()+"/islands/schems/"+isc.name);
			if(f.isDirectory()){


				f = new File(Main.main.getDataFolder()+"/islands/schems/"+isc.name+"/ile.schem");
				if(f.exists()) isc.island = loadFile(f);
				else throw new Exception("Pas d'ile principale dans l'ile "+isc.name);

				f = new File(Main.main.getDataFolder()+"/islands/schems/"+isc.name+"/minis");
				File[] fi = f.listFiles();
				if(fi==null)throw new Exception("Pas de mini-iles dans l'ile "+isc.name);
				isc.miniIslands = new Clipboard[fi.length];
				for (int i=0;i<fi.length;i++) {
					isc.miniIslands[i] = loadFile(fi[i]);
				}

				f = new File(Main.main.getDataFolder()+"/islands/schems/"+isc.name+"/structures");
				fi = f.listFiles();
				if(fi==null)throw new Exception("Pas de structures dans l'ile "+isc.name);
				isc.structures = new Clipboard[isc.plans.length];
				for (File f2 : fi) {
					String a = f2.getName().split("\\.")[0].toLowerCase();
					int id = isc.indexOf(a);
					if(id==-1)throw new Exception("Structure "+a+" inconnue pour l'ile "+isc.name);
					isc.structures[id] = loadFile(f2);
				}

				for(int i=0;i<isc.structures.length;i++){
					if(isc.structures[i]==null)throw new Exception("La structure "+isc.plans[i]+" est manquante pour l'ile "+isc.name);
				}


			}else throw new Exception("Pas de dossier pour l'ile "+isc.name);
			getLogger().info("Ile "+isc.name+" chargée avec succès !");
		}
	}

	public static Clipboard loadFile(File f) throws IOException {
		ClipboardFormat format = ClipboardFormats.findByFile(f);
		return format.load(f);
	}
}
