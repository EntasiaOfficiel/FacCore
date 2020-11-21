package fr.entasia.skycore.apis;

import fr.entasia.apis.other.CodePasser;
import fr.entasia.apis.utils.ServerUtils;
import fr.entasia.skycore.Main;
import fr.entasia.skycore.Utils;
import fr.entasia.skycore.apis.mini.Dimensions;
import fr.entasia.skycore.apis.mini.MemberRank;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import static fr.entasia.skycore.Utils.playerCache;

public class BaseAPI {

	// BORDEL


	// GET

	public static BaseIsland getIsland(Location loc) {
		return getIsland(CooManager.getIslandID(loc));
	}

	public static BaseIsland getIsland(ISID isid) {
		for(BaseIsland bis : Utils.islandCache){
			if(bis.isid.equals(isid))return bis;
		}
		return null;
	}

	public static SkyPlayer getOnlineSP(UUID uuid){ // TODO FAIRE METADATA
		Player p = Bukkit.getPlayer(uuid);
		if(p==null)return null;
		else return getOnlineSP(p);
	}
	public static SkyPlayer getOnlineSP(Player p){ // TODO FAIRE METADATA
		List<MetadataValue> meta = p.getMetadata("SkyPlayer");
		if(meta.size()==0)return null;
		else return (SkyPlayer) meta.get(0).value();
	}

	public static SkyPlayer getSkyPlayer(UUID uuid){
		for(SkyPlayer sp : playerCache){
			if(sp.uuid.equals(uuid))return sp;
		}
		return null;
	}

	public static SkyPlayer getSkyPlayer(OfflinePlayer p) {
		return getSkyPlayer(p.getUniqueId());
	}

	// FIRST SAVE - REGISTER

	public static SkyPlayer registerSkyPlayer(Player p) throws SQLException {
		SkyPlayer sp = new SkyPlayer(p);
		playerCache.add(sp);
		if(InternalAPI.SQLEnabled()){
			Main.sql.fastUpdateUnsafe("INSERT INTO sky_players (uuid) VALUES (?)", p.getUniqueId());
		}

		return sp;
	}

	public static ISPLink registerIsland(BaseIsland is, SkyPlayer sp) throws SQLException {
		if(sp.getOwnerIsland()!=null)return null;
		if(InternalAPI.SQLEnabled()){
			Main.sql.checkConnect();
			PreparedStatement ps = Main.sql.connection.prepareStatement("INSERT INTO sky_islands (x, z, type) VALUES (?, ?, ?)");
			ps.setInt(1, is.isid.x);
			ps.setInt(2, is.isid.z);
			ps.setInt(3, is.type.id);
			ps.execute();
		}
//		ps = Main.sqlConnection.connection.prepareStatement("INSERT INTO sky_pis (uuid, x, z, rank) VALUES (?, ?, ?, 5)");
//		ps.setString(1, sp.p.getUniqueId().toString());
//		ps.setInt(2, is.isid.x);
//		ps.setInt(3, is.isid.z);
//		ps.execute();


		ISPLink link = new ISPLink(is, sp, MemberRank.CHEF);
		is.members.add(link);
		is.owner = link;
		sp.islands.add(link);
		sp.ownerIsland = link;
		link.setRank(MemberRank.CHEF);
		if(InternalAPI.SQLEnabled())Main.sql.fastUpdate("INSERT INTO sky_pis (rank, x, z, uuid) VALUES (?, ?, ?, ?)", MemberRank.CHEF.id, is.isid.x, is.isid.z, sp.uuid);

		Utils.islandCache.add(is);
		return link;
	}
	// DELETE

	public static void deleteIsland(BaseIsland is, CodePasser.Arg<Boolean> code){
		ServerUtils.wantMainThread();
		if(InternalAPI.SQLEnabled()){
			if(Main.sql.fastUpdate("DELETE FROM sky_islands WHERE x=? AND z=?", is.isid.x, is.isid.z)==-1||
					Main.sql.fastUpdate("DELETE FROM sky_pis WHERE x=? AND z=?", is.isid.x, is.isid.z)==-1){
				code.run(true);
				return;
			}
		}

		int minx = is.isid.getMinXTotal();
		int maxx = is.isid.getMaxXTotal();
		int minz = is.isid.getMinZTotal();
		int maxz = is.isid.getMaxZTotal();

		if(is.owner!=null)is.owner.sp.ownerIsland = null;
		for (ISPLink link : is.getMembers()) {
			link.rank = MemberRank.DEFAULT;
			link.sp.islands.remove(link);
		}

		is.members.clear();

		is.delHolos();


		for (Player p : Bukkit.getOnlinePlayers()) {
			Location loc = p.getLocation();
			if (loc.getX() > minx && loc.getZ() > minz && loc.getX() < maxx && loc.getZ() < maxz) {
				p.sendMessage("§cL'île sur laquelle tu étais viens d'être supprimée, tu as été téléporté au Spawn !");
				p.teleport(Utils.spawn);
			}
		}


		new BukkitRunnable() {
			@Override
			public void run() {
				boolean a = true;
				if(!TerrainManager.clearTerrain(is.isid, TerrainManager.getSession(Dimensions.OVERWORLD.world)))a = false;
				else if(!TerrainManager.clearTerrain(is.isid, TerrainManager.getSession(Dimensions.NETHER.world)))a = false;
				else if(!TerrainManager.clearTerrain(is.isid, TerrainManager.getSession(Dimensions.END.world)))a = false;
				Utils.islandCache.remove(is);
				code.run(a);
			}
		}.runTaskAsynchronously(Main.main);
	}

	public static boolean deleteSkyPlayer(SkyPlayer sp) {

		int a = Main.sql.fastUpdate("DELETE FROM sky_players WHERE uuid=?", sp.uuid);
		if(a==-1)return false;
		a = Main.sql.fastUpdate("DELETE FROM sky_pis WHERE uuid=?", sp.uuid.toString());
		if(a==-1)return false;

		playerCache.remove(sp);
		if(sp.isOnline())sp.p.kickPlayer("§cTon compte Skyblock à été supprimé. Merci de te reconnecter pour procéder à la regénération d'un compte");
		return true;
	}

}
