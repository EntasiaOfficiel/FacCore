package fr.entasia.faccore.apis;

import fr.entasia.faccore.Main;
import fr.entasia.faccore.apis.mini.Dimensions;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.UUID;

public class FacPlayer {

	public final UUID uuid;
	public final String name;
	public Player p;

	protected Faction fac;
	protected long money;

	// online stuff
	public ArrayList<Faction> invites = new ArrayList<>(); // TODO A VOIR POUR PAS INIT
	public boolean internalChat = false;



	// CONSTRUCTEURS


	public FacPlayer(UUID uuid, String name){
		this.uuid = uuid;
		this.name = name;
	}

	public FacPlayer(Player p){
		this.uuid = p.getUniqueId();
		this.name = p.getDisplayName();
		this.money = 100;
	}


	// FONCTIONS UTILES


	public boolean isOnline(){
		return p!=null&&p.isOnline();
	}

	// FONCTIONS A AVOIR


	public boolean equals(FacPlayer sp){
		return sp.uuid.equals(uuid);
	}

	public int hashCode(){
		return uuid.hashCode();
	}

	@Override
	public String toString() {
		return "SkyPlayer["+name+"]";
	}


	// FONCTIONS RANDOM

	public Faction getFaction() {
		return fac;
	}


	public long getMoney(){
		return money;
	}

	public boolean setMoney(long m){
		if(m<0) return false;
		money=m;
		if(InternalAPI.SQLEnabled())Main.sql.fastUpdate("UPDATE sky_players SET money=? WHERE uuid=?", money, uuid);
		return true;
	}

	public void addMoney(long m){
		setMoney(money+m);
	}

	public boolean withdrawMoney(long m){
		return setMoney(money-m);
	}

}
