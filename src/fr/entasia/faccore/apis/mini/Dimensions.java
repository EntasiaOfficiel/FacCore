package fr.entasia.faccore.apis.mini;

import org.bukkit.World;

public enum Dimensions {
	OVERWORLD(0),
	NETHER(1),
	END(2);
//	CLOUDS(3);

	public int id;
	public World world;

	Dimensions(int id){
		this.id = id;
	}
	
	public static Dimensions getDimension(World w){
		for(Dimensions d : Dimensions.values()){
			if(d.world==w)return d;
		}
		return null;
	}

	public static boolean isGameWorld(World w){
		for(Dimensions d : Dimensions.values()){
			if(d.world!=null&&d.world==w)return true;
		}
		return false;
	}

}
