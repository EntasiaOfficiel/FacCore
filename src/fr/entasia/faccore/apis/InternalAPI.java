package fr.entasia.faccore.apis;

import fr.entasia.apis.utils.PlayerUtils;
import fr.entasia.apis.utils.ServerUtils;
import fr.entasia.errors.EntasiaException;
import fr.entasia.faccore.Main;
import fr.entasia.faccore.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;

import java.sql.ResultSet;
import java.util.UUID;

public class InternalAPI {

	public static void warn(String msg, boolean stack) {
		if(stack)new EntasiaException("Warning FacCore").printStackTrace();
		Main.main.getLogger().warning(msg);
		ServerUtils.permMsg("logs.warn", "§6Warning FacCore : §c"+msg);
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

			long key = rs.getLong("home");
			if(key!=0){
				Dimension dim = Dimension.get((int) (key>>>62));
				if(dim==null)return;
				key &= ~((long) 0b11 << 62); // delete injection
				fac.home = new Location(dim.world, Block.getBlockKeyX(key), Block.getBlockKeyY(key), Block.getBlockKeyZ(key));
			}

			Utils.factionCache.add(fac);
		}

		rs = Main.sql.fastSelectUnsafe("SELECT * FROM fac_claims");
		while(rs.next()) { // CLAIMS
			facID = rs.getInt("faction");
			assert fac != null; // tkt
			if(facID!=fac.id)fac = BaseAPI.getFaction(facID);

			fac.claims.add(new ChunkID(rs.getLong("loc")));
		}

		rs = Main.sql.connection.prepareStatement("SELECT playerdata.global.name, fac_players.* from fac_players INNER JOIN playerdata.global ON fac_players.uuid = playerdata.global.uuid").executeQuery();

		while(rs.next()){ // FACPLAYER
			fp = new FacPlayer(UUID.fromString(rs.getString("uuid")), rs.getString("name"));
			fp.money = rs.getLong("money");


			facID = rs.getInt("faction");
			fp.rank = MemberRank.getType(rs.getInt("rank"));

			if(fac==null||fac.id!=facID)fac = BaseAPI.getFaction(facID);

			if(fac==null){
				Main.main.getLogger().severe("Tentative de récupération d'une faction non existante ! (par membre)");
				Main.main.getLogger().severe("UUID="+rs.getString("uuid"));
				Main.main.getLogger().severe("ISID="+ facID);
				Main.main.getLogger().severe("RANK="+fp.rank);
				continue;
			}

			fac.members.add(fp);
			fp.faction = fac;
			if(fp.rank==MemberRank.CHEF) fac.owner = fp;


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
