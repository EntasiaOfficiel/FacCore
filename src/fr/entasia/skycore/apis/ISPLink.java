package fr.entasia.skycore.apis;

import fr.entasia.skycore.Main;
import fr.entasia.skycore.apis.mini.MemberRank;

public class ISPLink {

	public final BaseIsland is;
	public final SkyPlayer sp;
	protected MemberRank rank;

	public ISPLink(BaseIsland is, SkyPlayer sp, MemberRank rank){
		this.is = is;
		this.sp = sp;
		this.rank = rank;
	}

	/*
	0 succès
	1 joueur est déja chef d'une île
	2 autre erreur
	 */

	public byte setRank(MemberRank rank){
		if(rank==MemberRank.DEFAULT){
			InternalAPI.warn("Utilise remove() pour supprimer un joueur de l'île !", true);
			return 2;
		}
		this.rank = rank;
		if(rank==MemberRank.CHEF) {
			if(sp.ownerIsland!=null)return 1;
			is.owner.setRank(MemberRank.ADJOINT);
			is.owner = this;
			sp.ownerIsland = this;
		}
		if(InternalAPI.SQLEnabled()) Main.sql.fastUpdate("UPDATE sky_pis SET rank = ? WHERE uuid=? and x=? and z=?", rank.id, sp.uuid, is.isid.x, is.isid.z);
		return 0;
	}

	public boolean removeMember(){
		rank = MemberRank.DEFAULT;
		if(is.members.remove(this) && sp.islands.remove(this)){
			if(InternalAPI.SQLEnabled())Main.sql.fastUpdate("DELETE FROM sky_pis WHERE x=? and z=? and uuid=?", is.isid.x, is.isid.z, sp.uuid);
			return true;
		}
		return false;
	}


	public MemberRank getRank(){
		return rank;
	}

	public String getName(){
		return rank.getName()+"§7 "+sp.name;
	}

	public boolean equals(ISPLink l){
		return l.is.equals(is)&&l.sp.equals(sp);
	}
}
