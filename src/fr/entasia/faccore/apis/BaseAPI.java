package fr.entasia.faccore.apis;

import fr.entasia.apis.utils.ServerUtils;
import fr.entasia.faccore.Main;
import fr.entasia.faccore.Utils;
import fr.entasia.faccore.objs.FacException;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import static fr.entasia.faccore.Utils.playerCache;

public class BaseAPI {


	// GET


	public static Faction getFaction(int id) {
		for(Faction lf : Utils.factionCache){
			if(lf.id==id)return lf;
		}
		return null;
	}

	public static Faction getFaction(UUID owner) {
		for(Faction lf : Utils.factionCache){
			if(lf.owner.uuid==owner)return lf;
		}
		return null;
	}

	public static FacPlayer getOnlineFP(UUID uuid){ // TODO FAIRE METADATA
		Player p = Bukkit.getPlayer(uuid);
		if(p==null)return null;
		else return getOnlineFP(p);
	}
	public static FacPlayer getOnlineFP(Player p){ // TODO FAIRE METADATA
		List<MetadataValue> meta = p.getMetadata("FacPlayer");
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
		Main.sql.fastUpdateUnsafe("INSERT INTO fac_players (uuid) VALUES (?)", p.getUniqueId());
		return sp;
	}

	public static Faction registerFaction(FacPlayer fp) throws Exception {
		if(fp.faction!=null)return null;
		Faction fac = new Faction(fp);

		int i=0;
		loops:
		while(true){
			i++;
			if(i==20)throw new FacException("Internal error while generating ID : too much rounds needed ! (Too much factions ?)");
			fac.id = Main.r.nextInt();
			for(Faction lf : Utils.factionCache){
				if(lf.id==fac.id)continue loops;
			}
			break;
		}

		Main.sql.fastUpdate("INSERT INTO factions (owner) VALUES (?)", fp.uuid);
		Main.sql.fastUpdate("UPDATE fac_players SET faction=?, rank=? WHERE uuid=?", MemberRank.CHEF.id, fp.uuid);

		Utils.factionCache.add(fac);
		return fac;
	}
	// DELETE

	public static void deleteFaction(Faction fac) throws Exception {
		ServerUtils.wantMainThread();

		for (FacPlayer fp : fac.getMembers()) {
			fp.rank = MemberRank.DEFAULT;
			fp.faction = null;
		}
		fac.members.clear();

		fac.delHolos();
		fac.sendTeamMsg("\nTa faction vient d'être supprimée !\n");

		Main.sql.fastUpdateUnsafe("DELETE FROM factions WHERE faction=?", fac.id);
		Main.sql.fastUpdate("UPDATE sky_players SET factionnull, rank=null WHERE faction=?", fac.id);
	}

	public static boolean deleteSkyPlayer(FacPlayer sp) {

		int a = Main.sql.fastUpdate("DELETE FROM fac_players WHERE uuid=?", sp.uuid);
		if(a==-1)return false;
		a = Main.sql.fastUpdate("DELETE FROM sky_pis WHERE uuid=?", sp.uuid.toString());
		if(a==-1)return false;

		playerCache.remove(sp);
		if(sp.isOnline())sp.p.kickPlayer("§cTon compte Skyblock à été supprimé. Merci de te reconnecter pour procéder à la regénération d'un compte");
		return true;
	}
}
