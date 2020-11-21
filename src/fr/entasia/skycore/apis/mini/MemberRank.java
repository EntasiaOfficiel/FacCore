package fr.entasia.skycore.apis.mini;

// rien/ban = 0 (plugin/Base SQL)
// recrue = 1
// membre = 2
// adjoint = 4
// chef = 5

import org.bukkit.ChatColor;

public enum MemberRank {
	DEFAULT(0, ChatColor.GRAY, "Visiteur"),
	RECRUE(1, ChatColor.LIGHT_PURPLE, "Recrue"),
	MEMBRE(2, ChatColor.GREEN, "Membre"),
	ADJOINT(3, ChatColor.DARK_AQUA, "Adjoint"),
	CHEF(4, ChatColor.RED, "Chef");

	public int id;
	public ChatColor color;
	public String name;

	public static MemberRank getType(int i){
		for(MemberRank m : MemberRank.values()){
			if(m.id==i)return m;
		}
		return null;
	}

	MemberRank(int id, ChatColor color, String name){
		this.id = id;
		this.color = color;
		this.name = name;
	}

	public String getName(){
		return color+name;
	}
}
