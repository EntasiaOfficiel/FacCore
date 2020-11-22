package fr.entasia.faccore.apis;


import fr.entasia.apis.other.ChatComponent;
import fr.entasia.faccore.Main;
import fr.entasia.faccore.Utils;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.*;

public class Faction {

	public final int id;
	protected String name=null;
	protected Location home;
	protected FacPlayer owner;
	protected ArrayList<FacPlayer> members = new ArrayList<>();
	protected ArrayList<FacPlayer> invites = new ArrayList<>();
	protected ArrayList<ChunkID> chunks = new ArrayList<>();
	protected HashMap<Faction, FactionRelation> sideRelations = new HashMap<>();



	protected float power=0;
	protected long bank=0;

	// online stuff
	protected ArmorStand[] holo;


	// CONSTRUCTEURS

	public Faction(int id) {
		this.id = id;
	}

	public Faction(int id, FacPlayer fp){
		this.id = id;
		fp.faction = this;
		owner = fp;
		fp.rank = MemberRank.CHEF;
		members.add(fp);
	}


	// FONCTIONS A AVOIR


	public boolean equals(Faction fac){
		return id==fac.id;
	}

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
					Dimension.getDimension(home.getWorld()).id, home.getBlockX(), home.getBlockY(), home.getBlockZ(), id);
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
			if(fp.getRank()!=MemberRank.DEFAULT&&fp.uuid==uuid)return fp;
		}
		return null;
	}

	public boolean addMember(FacPlayer fp) {
		if (fp.faction != null) return false;
		if (getMember(fp.uuid) != null) return false;
		members.add(fp);
		fp.faction = this;
		Main.sql.fastUpdate("UPDATE fac_players SET faction=?, rank=? WHERE uuid=?", MemberRank.RECRUE.id, id, MemberRank.RECRUE.id, fp.uuid);
		return true;
	}


	public byte claim(ChunkID cid){
		// TODO POWER CHECK
		if(chunks.contains(cid))return 1;

		chunks.add(cid);
		return 0;
	}

	public byte unClaim(ChunkID cid){
		// TODO POWER
		if(chunks.remove(cid))return 0;
		else return 1;

	}


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


	private static final BaseComponent[] b1 = ChatComponent.create("§3Chat de faction§b>> ");
	private static final BaseComponent[] b2 = ChatComponent.create(" §8| §7");

	public void islandChat(FacPlayer fp, String msg){
		islandChat(fp, ChatComponent.create(msg));
	}

	public void islandChat(FacPlayer fp, BaseComponent... msg){
		sendTeamMsg(new ChatComponent().append(b1).append(fp.getName()).append(b2).append(msg).create());
	}

	public void sendTeamMsg(String msg){
		sendTeamMsg(ChatComponent.create(msg));
	}

	public void sendTeamMsg(BaseComponent... msg){
		for(FacPlayer fp : members){
			if(fp.p!=null){
				fp.p.sendMessage(msg);
			}
		}
	}


	public FacPlayer getOwner(){
		return owner;
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
		Main.sql.fastUpdate("UPDATE factions SET bank=? WHERE id=?", m, id);
		return true;

	}

	protected void delHolos(){
		for(Entity ent : home.clone().add(0, 3, 0).getNearbyEntities(1, 4, 1)){
			if(ent instanceof ArmorStand){
				if(ent.getScoreboardTags().contains("isholo")){
					ent.remove();
				}
			}
		}
	}

	public void trySetHolos(){
		if(holo==null){
			delHolos();
			holo = new ArmorStand[2];
			setHoloName();
			setHoloBank();

		}
	}

	protected void setHoloName(){
		if(holo[0]==null) holo[0] = createAM(0);
		String n;
		if(name==null)n = "§2Faction de §a"+owner.name;
		else n = "§a"+name;
		holo[0].setCustomName(n);
	}

	protected void setHoloBank(){
		if(holo[1]==null) holo[1] = createAM(1);
		holo[1].setCustomName("§eBanque : §6"+ Utils.formatMoney(bank));
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

}
