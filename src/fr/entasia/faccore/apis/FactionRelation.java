package fr.entasia.faccore.apis;

import org.bukkit.ChatColor;

public enum FactionRelation {
	ALLY(4, ChatColor.GREEN, "Allié"),
	TRUCE(3, ChatColor.BLUE, "En paix"), // TODO A CHANGER
	NEUTRAL(2, ChatColor.GRAY, "Neutre"),
	ENEMY(1, ChatColor.RED, "Ennemi"),

	;

	public int id;
	public ChatColor color;
	public String name;

	public static FactionRelation getType(int i){
		for(FactionRelation m : FactionRelation.values()){
			if(m.id==i)return m;
		}
		return null;
	}

	FactionRelation(int id, ChatColor color, String name){
		this.id = id;
		this.color = color;
		this.name = name;
	}

	public String getName(){
		return color+name;
	}
}
