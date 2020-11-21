package fr.entasia.faccore.apis;


import fr.entasia.apis.other.ChatComponent;
import fr.entasia.apis.other.CodePasser;
import fr.entasia.apis.utils.Serialization;
import fr.entasia.apis.utils.TextUtils;
import fr.entasia.faccore.Main;
import fr.entasia.faccore.Utils;
import fr.entasia.faccore.apis.mini.Dimensions;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class Faction {

	protected int id;
	protected String name=null;
	protected Location home;
	protected FacPlayer owner;
	protected ArrayList<FacPlayer> members = new ArrayList<>();
	protected ArrayList<FacPlayer> invites = new ArrayList<>();

	protected long bank=0;

	// online stuff
	protected ArmorStand[] holo;


	// CONSTRUCTEURS

	public Faction(FacPlayer fp){
		fp.faction = this;
		owner = fp;
		fp.rank = MemberRank.CHEF;
		members.add(fp);
	}


	// FONCTIONS A AVOIR

	public String toString(){
		return "Faction["+id+"]";
	}

	public int hashCode(){
		return id;
	}


	// FONCTIONS RANDOM


	public String getName(){
		return name;
	}

	public boolean setName(String name){
		if(name.length()>20)return false;
		this.name = name;
		setHoloName();
		Main.sql.fastUpdate("UPDATE factions SET name=? WHERE faction=?", name, id);
		return true;
	}



	public Location getHome(){
		return home;
	}

	public void teleportHome(Player p){
		p.setFallDistance(0);
		p.teleport(home);
		trySetHolos();
	}

	public void setHome(Location home){
		this.home = home;
			Main.sql.fastUpdate("UPDATE factions SET home_w=?, home_x=?, home_y=?, home_z=? where faction=?",
					Dimensions.getDimension(home.getWorld()).id, home.getBlockX(), home.getBlockY(), home.getBlockZ(), id);
	}



	public ArrayList<FacPlayer> getMembers(){
		return new ArrayList<>(members);
	}

	private static final Comparator<FacPlayer> memberComparator = Comparator.comparingInt(o -> o.getRank().id);

	public ArrayList<FacPlayer> getSortedMembers(){
		ArrayList<FacPlayer> a = getMembers();
		a.sort(memberComparator);
		Collections.reverse(a);
		return a;
	}

	public FacPlayer getMember(UUID uuid){
		for(FacPlayer fp : members){
			if(fp.getRank()!=MemberRank.DEFAULT&&fp.uuid==uuid)return fac;
		}
		return null;
	}

	/*
	if(fp.faction!=null)return false;

		fp.faction = fac;
		fp.rank = rank;
		fac.members.add(fp);


	 */

	public boolean addMember(FacPlayer fp) {
		if (fp.faction != null) return false;
		if (getMember(fp.uuid) != null) return false;
		members.add(fp);
		fp.faction = this;
		Main.sql.fastUpdate("UPDATE fac_players SET faction=?, rank=? WHERE uuid=?", MemberRank.RECRUE.id, id, MemberRank.RECRUE.id, fp.uuid);
		return true;
	}

//	public boolean reRankMember(ISPLink link, MemberRank rank){
//		if(rank==MemberRank.DEFAULT) InternalAPI.warn("Utilise removeMember() pour supprimer un joueur de l'île !", true);
//		else if(link.is.equals(this)){
//			link.setRank(rank);
//			if(rank==MemberRank.CHEF) {
//				owner.setRank(MemberRank.ADJOINT);
//				owner = link;
//			}
//			if(InternalAPI.SQLEnabled())Main.sql.fastUpdate("UPDATE sky_pis SET rank = ? WHERE uuid=? and x=? and z=?", rank.id, link.sp.uuid, link.is.isid.x, link.is.isid.z);
//			return true;
//		} else InternalAPI.warn("L'île fournie ne correspond pas", true);
//		return false;
//	}


	public ArrayList<FacPlayer> getInvites(){
		return new ArrayList<>(invites);
	}

	public boolean invitePlayer(FacPlayer sp){
		if(isInvited(sp))return false;
		else{
			invites.add(sp);
			sp.invites.add(this);
			if(sp.p!=null)sp.p.sendMessage();
			return true;
		}
	}

	public boolean isInvited(FacPlayer sp){
		for(FacPlayer lsp : invites){
			if(lsp.equals(sp))return true;
		}
		return false;
	}

	public boolean cancelInvite(FacPlayer sp){
		sp.invites.remove(this);
		return invites.remove(sp);
	}


	private static final BaseComponent[] b1 = ChatComponent.create("§3Is-Chat§b>> ");
	private static final BaseComponent[] b2 = ChatComponent.create(" §8| §7");

	public void islandChat(ISPLink link, String msg){
		islandChat(link, ChatComponent.create(msg));
	}

	public void islandChat(ISPLink link, BaseComponent... msg){
		sendTeamMsg(new ChatComponent().append(b1).append(link.getName()).append(b2).append(msg).create());
	}

	public void sendTeamMsg(String msg){
		sendTeamMsg(ChatComponent.create(msg));
	}

	public void sendTeamMsg(BaseComponent... msg){
		for(ISPLink link : members){
			if(link.sp.p!=null){
				link.sp.p.sendMessage(msg);
			}
		}
	}


	public ISPLink getOwner(){
		return owner;
	}


	public ArrayList<FacPlayer> getBanneds(){
		return new ArrayList<>(banneds);
	}

	public boolean addBanned(FacPlayer sp){
		if(banneds.contains(sp))return false;
		banneds.add(sp);
		ISPLink link = getMember(sp.uuid);
		if(link==null){
			if(InternalAPI.SQLEnabled())Main.sql.fastUpdate("INSERT INTO sky_pis (rank, x, z, uuid) VALUES (?, ?, ?, ?)", 0, facID.x, facID.z, sp.uuid);
		}else{
			link.removeMember();
			if(InternalAPI.SQLEnabled())Main.sql.fastUpdate("UPDATE sky_pis SET rank=? where x=? and z=? and uuid=?)", 0, facID.x, facID.z, sp.uuid);
		}
		return true;
	}

	public boolean removeBanned(FacPlayer sp){
		if(banneds.contains(sp)){
			this.banneds.remove(sp);
			if(InternalAPI.SQLEnabled())Main.sql.fastUpdate("DELETE FROM sky_pis WHERE x=? and z=? and uuid=?", facID.x, facID.z, sp.uuid);
			return true;
		}else return false;
	}

	public boolean isBanned(FacPlayer sp){
		return this.banneds.contains(sp);
	}


	public byte getExtension(){
		return extension;
	}

	public void setExtension(byte extension){
		this.extension = extension;
		if(InternalAPI.SQLEnabled())Main.sql.fastUpdate("UPDATE factions SET extension=? WHERE x=? and z=?", extension, facID.x, facID.z);
	}


	public long getBank(){
		return bank;
	}

	public void addBank(long m){
		setBank(bank+m);
	}

	public boolean withdrawBank(long m){
		return setBank(bank-m);
	}

	public boolean setBank(long m){
		if(m<0)return false;
		bank = m;
		setHoloBank();
		if(InternalAPI.SQLEnabled())Main.sql.fastUpdate("UPDATE factions SET bank=? WHERE x=? and z=?", m, facID.x, facID.z);
		return true;

	}

	protected void delHolos(){
		for(Entity ent : home.clone().add(0, 3, 0).getNearbyEntities(1, 4, 1)){
			if(ent instanceof ArmorStand){
				if(!ent.getScoreboardTags().contains("isholo"))return;
				ent.remove();
			}
		}
	}

	public void trySetHolos(){
		if(holo==null){
			delHolos();
			holo = new ArmorStand[4];
			if(name!=null)setHoloName();
			setHoloLevel();
			setHoloBank();
			if(holo[3]==null) holo[3] = createAM(3);
			holo[3].setCustomName("§eID : §6"+ facID.str());

		}
	}

	protected void setHoloName(){
		if(holo[0]==null) holo[0] = createAM(0);
		holo[0].setCustomName("§a"+name);
	}
	protected void setHoloLevel(){
		if(holo[1]==null) holo[1] = createAM(1);
		holo[1].setCustomName("§eNiveau de l'île : §6"+level);
	}
	protected void setHoloBank(){
		if(holo[2]==null) holo[2] = createAM(2);
		holo[2].setCustomName("§eBanque : §6"+ Utils.formatMoney(bank));
	}

	private ArmorStand createAM(int n){
		ArmorStand temp  = (ArmorStand) home.getWorld().spawnEntity(home.clone().add(0, 3-0.3*n, 0), EntityType.ARMOR_STAND);
		temp.setVisible(false);
		temp.setInvulnerable(true);
		temp.setCustomNameVisible(true);
		temp.setGravity(false);
		temp.setMarker(true);
		temp.addScoreboardTag("isholo");
		return temp;
	}



	public void tryLoad(){
		if(!loaded){
			loaded = true;
			autominers = Collections.synchronizedList(new ArrayList<>());

			try{
				ResultSet rs = Main.sqlite.fastSelectUnsafe("SELECT * FROM autominers WHERE is_x=? and is_z=? ", facID.x, facID.z);
				Block b;
				World w;
				ItemStack item;
				while(rs.next()){
					item = Serialization.deserialiseItem(rs.getString("item"));
					w = Bukkit.getWorld(rs.getString("world"));
					if(w!=null){
						b = w.getBlockAt(rs.getInt("x"), rs.getInt("y"), rs.getInt("z"));
						if(b.getType()!=Material.AIR){
							AutoMiner am = new AutoMiner();
							int i = 0;
							for(Entity ent : b.getLocation().add(AutoMiner.normaliser).getNearbyEntitiesByType(ArmorStand.class, 0.4)){
								if("AMPickaxe".equals(ent.getCustomName())){
									if(i==4){
										i = 5;
										break;
									}
									am.armorStands[i] = (ArmorStand) ent;
									i++;
								}
							}
							if(i==4) {
								am.init(this, b, item);
								am.deleteAM();
								am.spawn();
								continue;
							}
						}
						AutoMiner.deleteAMByBlock(b);
						w.dropItem(b.getLocation(), item);
					}
					Main.sqlite.fastUpdate("DELETE FROM autominers WHERE x=? and y=? and z=?", rs.getInt("x"), rs.getInt("y"), rs.getInt("z"));

				}
			}catch(SQLException e){
				e.printStackTrace();
				Main.sqlite.broadcastError();
				InternalAPI.warn("Erreur lors du chargement des autominers de l'île "+ facID.str()+" !", false);
			}
		}
	}


}
