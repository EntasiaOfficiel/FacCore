package fr.entasia.faccore.apis;

import fr.entasia.apis.regionManager.api.RegionManager;
import fr.entasia.apis.utils.BasicLocation;
import fr.entasia.apis.utils.PlayerUtils;
import fr.entasia.apis.utils.ServerUtils;
import fr.entasia.errors.EntasiaException;
import fr.entasia.faccore.Main;
import fr.entasia.faccore.Utils;
import fr.entasia.faccore.objs.RankTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;

import java.sql.ResultSet;
import java.util.UUID;

public class InternalAPI {

	public static byte postenable=0;

	public static boolean isFullyEnabled(){
		return InternalAPI.postenable==2;
	}

	public static void warn(String msg, boolean stack) {
		if(stack)new EntasiaException("Warning FacCore").printStackTrace();
		Main.main.getLogger().warning(msg);
		ServerUtils.permMsg("logs.warn", "§6Warning FacCore : §c"+msg);
	}



	public static void a(String a) {
		Main.main.getLogger().warning(a);


//		if(Main.enableDev){
//			Main.main.getLogger().warning(a);
//			Main.main.getLogger().warning("Une erreur à été rencontrée, mais le mode développement est actif");
//		}else throw new RuntimeException(a);
	}

	public static void onPostEnable(){ // besoin que les mondes soient chargés, voir BaseEvents
		try{
			if(postenable==0){
				postenable=1;
				Main.main.getLogger().info("Activation POST du plugin");

				loadSQLData();



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


				new RankTask().runTaskTimerAsynchronously(Main.main, 0, 20*60*5); // full cycle

				postenable=2;
			}
		}catch(Throwable e){
			e.printStackTrace();
			if(!Main.dev){
				Main.main.getLogger().severe("Erreur lors du chargement POST du plugin ! ARRET DU SERVEUR");
				Main.main.getServer().shutdown();
			}
		}
	}

	public static void loadSQLData() throws Throwable{
		long time = System.currentTimeMillis();


		Faction fac=null;
		FacPlayer fp;
		int facID;

		ResultSet rs = Main.sql.fastSelectUnsafe("SELECT * FROM factions");
		while(rs.next()){ // FACTIONS
			fac = new Faction(rs.getInt("id"));

			fac.name = rs.getString("name");
			fac.bank = rs.getLong("bank");

			Utils.factionCache.add(fac);
		}

		rs = Main.sql.fastSelectUnsafe("SELECT * FROM fac_claims");
		while(rs.next()) { // CLAIMS
			facID = rs.getInt("faction");
			if(facID!=fac.id)fac = BaseAPI.getFaction(facID);

			fac.claims.add(new ChunkID(Dimension.get(rs.getInt("dimension")), rs.getInt("x"), rs.getInt("z")));
		}

		rs = Main.sql.connection.prepareStatement("SELECT global.name, sky_players.* from sky_players INNER JOIN global ON sky_players.uuid = global.uuid").executeQuery();

		while(rs.next()){ // FACPLAYER
			fp = new FacPlayer(UUID.fromString(rs.getString("uuid")), rs.getString("name"));
			fp.money = rs.getLong("money");


			facID = rs.getInt("faction");
			fp.rank = MemberRank.getType(rs.getInt("rank"));

			assert fac != null; // tkt
			if(fac.id!=facID)fac = BaseAPI.getFaction(facID);

			if(fac==null){
				Main.main.getLogger().severe("Tentative de récupération d'une faction non existante ! (par membre)");
				Main.main.getLogger().severe("UUID="+rs.getString("uuid"));
				Main.main.getLogger().severe("ISID="+ facID);
				Main.main.getLogger().severe("RANK="+fp.rank);
				continue;
			}

			fac.members.add(fp);
			fp.faction = fac;
			if(fp.rank==MemberRank.CHEF) {
				fac.owner = fp;
			}

			Utils.playerCache.add(fp);


		}

		Main.main.getLogger().info("Données chargées en "+(System.currentTimeMillis()-time)+"ms :");
		Main.main.getLogger().info(Utils.factionCache.size()+" factions");
		Main.main.getLogger().info(Utils.playerCache.size()+" joueurs");

	}



	private static UUID parseArg(String str, boolean exact) {
		try{
			return UUID.fromString(str);
		}catch(IllegalArgumentException ignore){
			if(!exact) {
				OfflinePlayer p = Bukkit.getPlayer(str);
				if (p != null) return PlayerUtils.getUUID(str);
			}
			return PlayerUtils.getUUID(str);
		}
	}


	public static FacPlayer getArgSP(CommandSender p, String str, boolean exact) {
		UUID uuid = parseArg(str, exact);
		if(uuid==null)p.sendMessage("§cCe joueur n'existe pas ou n'est pas inscrit en Faction !");
		else{
			FacPlayer sp = BaseAPI.getFacPlayer(uuid);
			if(sp==null)p.sendMessage("§cCe joueur n'existe pas ou n'est pas inscrit en Faction !");
			else return sp;
		}
		return null;
	}
}
