package fr.entasia.faccore.apis;

import fr.entasia.apis.other.CodePasser;
import fr.entasia.apis.utils.ServerUtils;
import fr.entasia.faccore.Main;
import fr.entasia.faccore.Utils;
import fr.entasia.faccore.apis.mini.Dimensions;
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

import static fr.entasia.faccore.Utils.playerCache;

public class BaseAPI {

	// BORDEL


	// GET

	public static Faction getIsland(Location loc) {
		return getIsland(CooManager.getIslandID(loc));
	}

	public static Faction getIsland(FacID facID) {
		for(Faction bis : Utils.islandCache){
			if(bis.facID.equals(facID))return bis;
		}
		return null;
	}

	public static FacPlayer getOnlineFP(UUID uuid){ // TODO FAIRE METADATA
		Player p = Bukkit.getPlayer(uuid);
		if(p==null)return null;
		else return getOnlineFP(p);
	}
	public static FacPlayer getOnlineFP(Player p){ // TODO FAIRE METADATA
		List<MetadataValue> meta = p.getMetadata("SkyPlayer");
		if(meta.size()==0)return null;
		else return (FacPlayer) meta.get(0).value();
	}

	public static FacPlayer getFacPlayer(UUID uuid){
		for(FacPlayer sp : playerCache){
			if(sp.uuid.equals(uuid))return sp;
		}
		return null;
	}

	public static FacPlayer getFacPlayer(OfflinePlayer p) {
		return getFacPlayer(p.getUniqueId());
	}

	// FIRST SAVE - REGISTER

	public static FacPlayer registerFacPlayer(Player p) throws SQLException {
		FacPlayer sp = new FacPlayer(p);
		playerCache.add(sp);
		if(InternalAPI.SQLEnabled()){
			Main.sql.fastUpdateUnsafe("INSERT INTO sky_players (uuid) VALUES (?)", p.getUniqueId());
		}

		return sp;
	}

	public static Faction registerFaction(FacPlayer sp) throws SQLException {
		if(sp.getFaction()!=null)return null;
		Faction fac = new Faction();
		if(InternalAPI.SQLEnabled()){
			Main.sql.checkConnect();
			PreparedStatement ps = Main.sql.connection.prepareStatement("INSERT INTO sky_islands (x, z, type) VALUES (?, ?, ?)");
			ps.setInt(1, is.facID.x);
			ps.setInt(2, is.facID.z);
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
		if(InternalAPI.SQLEnabled())Main.sql.fastUpdate("INSERT INTO sky_pis (rank, x, z, uuid) VALUES (?, ?, ?, ?)", MemberRank.CHEF.id, is.facID.x, is.facID.z, sp.uuid);

		Utils.islandCache.add(is);
		return link;
	}
	// DELETE

	public static void deleteIsland(Faction is, CodePasser.Arg<Boolean> code){
		ServerUtils.wantMainThread();
		if(InternalAPI.SQLEnabled()){
			if(Main.sql.fastUpdate("DELETE FROM sky_islands WHERE x=? AND z=?", is.facID.x, is.facID.z)==-1||
					Main.sql.fastUpdate("DELETE FROM sky_pis WHERE x=? AND z=?", is.facID.x, is.facID.z)==-1){
				code.run(true);
				return;
			}
		}

		int minx = is.facID.getMinXTotal();
		int maxx = is.facID.getMaxXTotal();
		int minz = is.facID.getMinZTotal();
		int maxz = is.facID.getMaxZTotal();

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
				if(!TerrainManager.clearTerrain(is.facID, TerrainManager.getSession(Dimensions.OVERWORLD.world)))a = false;
				else if(!TerrainManager.clearTerrain(is.facID, TerrainManager.getSession(Dimensions.NETHER.world)))a = false;
				else if(!TerrainManager.clearTerrain(is.facID, TerrainManager.getSession(Dimensions.END.world)))a = false;
				Utils.islandCache.remove(is);
				code.run(a);
			}
		}.runTaskAsynchronously(Main.main);
	}

	public static boolean deleteSkyPlayer(FacPlayer sp) {

		int a = Main.sql.fastUpdate("DELETE FROM sky_players WHERE uuid=?", sp.uuid);
		if(a==-1)return false;
		a = Main.sql.fastUpdate("DELETE FROM sky_pis WHERE uuid=?", sp.uuid.toString());
		if(a==-1)return false;

		playerCache.remove(sp);
		if(sp.isOnline())sp.p.kickPlayer("§cTon compte Skyblock à été supprimé. Merci de te reconnecter pour procéder à la regénération d'un compte");
		return true;
	}

}
