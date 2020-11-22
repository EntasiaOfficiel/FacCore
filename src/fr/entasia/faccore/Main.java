package fr.entasia.faccore;

import fr.entasia.apis.sql.SQLConnection;
import fr.entasia.faccore.commands.base.*;
import fr.entasia.faccore.commands.manage.*;
import fr.entasia.faccore.events.BaseEvents;
import fr.entasia.faccore.events.ProtectionEvents;
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

			loadConfigs();
			
			sql = new SQLConnection(dev).mariadb("faccore", "playerdata");

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
		// TODO voir si ca sert à un truc ?
	}
}
