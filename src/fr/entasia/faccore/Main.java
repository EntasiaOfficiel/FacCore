package fr.entasia.faccore;

import fr.entasia.apis.regionManager.api.RegionManager;
import fr.entasia.apis.sql.SQLConnection;
import fr.entasia.apis.utils.BasicLocation;
import fr.entasia.faccore.apis.Dimension;
import fr.entasia.faccore.apis.InternalAPI;
import fr.entasia.faccore.commands.base.*;
import fr.entasia.faccore.commands.manage.*;
import fr.entasia.faccore.events.BaseEvents;
import fr.entasia.faccore.events.ProtectionEvents;
import fr.entasia.faccore.objs.RankTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Random;

public class Main extends JavaPlugin {

	public static Main main;
	public static boolean dev;
	public static Random r = new Random();

	public static SQLConnection sql;

	@Override
	public void onEnable(){
		try{
			main = this;
			getLogger().info("Activation du plugin méga-badass...");

			dev = main.getConfig().getBoolean("dev", false);
			Dimension.OVERWORLD.world = Bukkit.getWorld("factions");
			Dimension.NETHER.world = Bukkit.getWorld("factions_nether");
			Dimension.END.world = Bukkit.getWorld("factions_the_end");

			loadConfigs();

			sql = new SQLConnection(dev).mariadb("dev", "dev");
			InternalAPI.loadSQLData();

			getServer().getPluginManager().registerEvents(new BaseEvents(), this);
			getServer().getPluginManager().registerEvents(new ProtectionEvents(), this);

			getCommand("fadmin").setExecutor(new FacAdminCommand());
			getCommand("f").setExecutor(new FacCommand());

			getCommand("faccore").setExecutor(new FacCoreCommand());
			getCommand("setspawn").setExecutor(new SetSpawnCommand());
			getCommand("masteredit").setExecutor(new MasterEditCommand());

			getCommand("baltop").setExecutor(new BaltopCommand());
			getCommand("money").setExecutor(new MoneyCommand());
			getCommand("pay").setExecutor(new PayCommand());
			getCommand("eco").setExecutor(new EcoCommand());
			getCommand("bin").setExecutor(new BinCommand());
			getCommand("spawn").setExecutor(new SpawnCommand());
			getCommand("rank").setExecutor(new RankCommand());


			new RankTask().runTaskTimerAsynchronously(Main.main, 0, 20*60*5); // full cycle

		}catch(Throwable e){
			e.printStackTrace();
			if(!dev){
				getLogger().severe("Erreur lors du chargement du plugin ! ARRET DU SERVEUR");
				getServer().shutdown();
			}
		}

//		RegionManager.registerRegion("name", Utils.spawnWorld, new BasicLocation(1,1,1), new BasicLocation(2,2,2));

	}

	public static void loadConfigs() throws Throwable {
		main.saveDefaultConfig();
		main.reloadConfig();



		ConfigurationSection sec = Main.main.getConfig().getConfigurationSection("spawn");
		assert sec != null;
		Utils.spawn = new Location(Dimension.OVERWORLD.world, sec.getInt("x")+0.5, sec.getInt("y") + 0.2,
				sec.getInt("z")+0.5, sec.getInt("yaw"), sec.getInt("pitch"));

		BasicLocation corner1 = new BasicLocation(sec.getInt("corner1.x"), sec.getInt("corner1.y"), sec.getInt("corner1.z"));
		BasicLocation corner2 = new BasicLocation(sec.getInt("corner2.x"), sec.getInt("corner2.y"), sec.getInt("corner2.z"));

		Utils.spawnRegion = RegionManager.registerRegion("spawn", Dimension.OVERWORLD.world, corner1, corner2);


		sec = Main.main.getConfig().getConfigurationSection("warzone");
		assert sec != null;

		corner1 = new BasicLocation(sec.getInt("corner1.x"), sec.getInt("corner1.y"), sec.getInt("corner1.z"));
		corner2 = new BasicLocation(sec.getInt("corner2.x"), sec.getInt("corner2.y"), sec.getInt("corner2.z"));

		Utils.warzone = RegionManager.registerRegion("warzone", Dimension.OVERWORLD.world, corner1, corner2);
	}
}
