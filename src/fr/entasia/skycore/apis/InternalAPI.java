package fr.entasia.skycore.apis;

import fr.entasia.apis.utils.PlayerUtils;
import fr.entasia.apis.utils.ServerUtils;
import fr.entasia.errors.EntasiaException;
import fr.entasia.skycore.Main;
import fr.entasia.skycore.Utils;
import fr.entasia.skycore.apis.mini.Dimensions;
import fr.entasia.skycore.apis.mini.MemberRank;
import fr.entasia.skycore.objs.IslandType;
import fr.entasia.skycore.objs.tasks.AutoMinerTask;
import fr.entasia.skycore.objs.tasks.RankTask;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.sql.ResultSet;
import java.util.UUID;

public class InternalAPI {

	public static byte postenable=0;
	public static boolean enableIGSQL=true;


	public static boolean SQLEnabled(){
		return InternalAPI.postenable==2&&InternalAPI.enableIGSQL&&Main.sql !=null;
	}
	public static boolean isFullyEnabled(){
		return InternalAPI.postenable==2;
	}

	public static void warn(String msg, boolean stack) {
		if(stack)new EntasiaException("Warning SkyCore").printStackTrace();
		Main.main.getLogger().warning(msg);
		ServerUtils.permMsg("logs.warn", "§6Warning SkyCore : §c"+msg);
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
				Main.main.getLogger().info("Activation POST du plugin méga-badass");

				if(Main.sql!=null) loadData();

				new AutoMinerTask().runTaskTimerAsynchronously(Main.main, 0, 20*6); // full cycle
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

	public static void loadData() throws Throwable{
		long time = System.currentTimeMillis();


		BaseIsland is = null;
		SkyPlayer sp = null;
		ISPLink link;

		ResultSet rs = Main.sql.fastSelectUnsafe("SELECT * FROM sky_islands");
		while(rs.next()){ // BASEISLAND
			is = new BaseIsland(new ISID(rs.getInt("x"), rs.getInt("z")), IslandType.getType(rs.getInt("type")));

			is.name = rs.getString("name");
			is.bank = rs.getLong("bank");
			is.extension = rs.getByte("extension");
			is.malus = rs.getInt("malus");
			is.level = rs.getInt("lvl");

			if(rs.getByte("hasNether")==1)is.hasNether = true;
			if(rs.getByte("hasEnd")==1)is.hasEnd = true;
			Utils.islandCache.add(is);
		}

		rs = Main.sql.connection.prepareStatement("SELECT global.name, sky_players.* from sky_players INNER JOIN global ON sky_players.uuid = global.uuid").executeQuery();
		while(rs.next()){ // SKYPLAYER
			sp = new SkyPlayer(UUID.fromString(rs.getString("uuid")), rs.getString("name"));
			sp.money = rs.getLong("money");
			sp.lastGenerated = rs.getInt("lastgen");
			Utils.playerCache.add(sp);
		}
		int i = 0;
		int rID;
		if(is==null) a("Aucune ile en mémoire !");
		else if(sp==null) a("Aucun joueur en mémoire !");
		else{
			rs = Main.sql.connection.prepareStatement("SELECT * FROM sky_pis").executeQuery();
			ISID isid;
			while(rs.next()){ // ISPLINK
				i++;
				isid = new ISID(rs.getInt("x"), rs.getInt("z"));
				rID = rs.getInt("rank");

				assert is != null; // tkt
				if(!is.isid.equals(isid))is = BaseAPI.getIsland(isid);

				if(is==null){
					Main.main.getLogger().severe("Tentative de récupération du lien d'une île non existante !");
					Main.main.getLogger().severe("UUID="+rs.getString("uuid"));
					Main.main.getLogger().severe("ISID="+isid);
					Main.main.getLogger().severe("RANK="+rID);
					continue;
				}


				sp = BaseAPI.getSkyPlayer(UUID.fromString(rs.getString("uuid")));
				if(sp==null){
					Main.main.getLogger().severe("Tentative de récupération du lien d'un joueur non existant !");
					Main.main.getLogger().severe("UUID="+rs.getString("uuid"));
					Main.main.getLogger().severe("ISID="+isid);
					Main.main.getLogger().severe("RANK="+rID);
					continue;
				}


				if(rID==0)is.banneds.add(sp);
				else{
					link = new ISPLink(is, sp, MemberRank.getType(rID));
					is.members.add(link);
					sp.islands.add(link);
					if(link.rank==MemberRank.CHEF){
						is.owner = link;
						sp.ownerIsland = link;
					}
				}
				if(rs.getByte("def")==1){
					if(sp.getDefaultIS()!=null){
						Main.main.getLogger().warning("Redéfinition de île par défaut pour "+sp.name+" !");
					}
					sp.setDefaultIS(isid);
				}
			}
		}
		Main.main.getLogger().info("Données chargées en "+(System.currentTimeMillis()-time)+"ms :");
		Main.main.getLogger().info(Utils.islandCache.size()+" iles");
		Main.main.getLogger().info(Utils.playerCache.size()+" joueurs");
		Main.main.getLogger().info(i+" liens");

	}



	public static boolean allowDimension(BaseIsland is, Dimensions d) {
		ServerUtils.wantChildThread();
		try{
			if(d==Dimensions.NETHER){
				TerrainManager.genNether(is.isid);
				is.hasNether = true;
				if(InternalAPI.SQLEnabled())Main.sql.fastUpdate("UPDATE sky_islands SET hasNether=1 WHERE x=? and z=?", is.isid.x, is.isid.z);
			}else if(d==Dimensions.END){
				TerrainManager.genEnd(is.isid);
				is.hasEnd = true;
				if(InternalAPI.SQLEnabled())Main.sql.fastUpdate("UPDATE sky_islands SET hasEnd=1 WHERE x=? and z=?", is.isid.x, is.isid.z);
			}else{
				InternalAPI.warn("Activation d'une dimension invalide : "+d, true);
			}
		}catch(Exception e){
			e.printStackTrace();
			InternalAPI.warn("Erreur d'activation de dimension "+d+" ! "+is.isid, false);
			return false;
		}
		return true;
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


	public static SkyPlayer getArgSP(CommandSender p, String str, boolean exact) {
		UUID uuid = parseArg(str, exact);
		if(uuid==null)p.sendMessage("§cCe joueur n'existe pas ou n'est pas inscrit en Skyblock !");
		else{
			SkyPlayer sp = BaseAPI.getSkyPlayer(uuid);
			if(sp==null)p.sendMessage("§cCe joueur n'existe pas ou n'est pas inscrit en Skyblock !");
			else return sp;
		}
		return null;
	}
}
