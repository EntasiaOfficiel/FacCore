package fr.entasia.faccore.apis;

import org.bukkit.World;

public enum Dimension {
	OVERWORLD(0),
	NETHER(1),
	END(2);

	public int id;
	public World world;

	Dimension(int id){
		this.id = id;
	}
	
	public static Dimension getDimension(World w){
		for(Dimension d : Dimension.values()){
			if(d.world==w)return d;
		}
		return null;
	}

	public static boolean isGameWorld(World w){
		for(Dimension d : Dimension.values()){
			if(d.world!=null&&d.world==w)return true;
		}
		return false;
	}

}
