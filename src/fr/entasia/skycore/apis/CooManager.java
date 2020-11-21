package fr.entasia.skycore.apis;

import fr.entasia.skycore.Utils;
import org.bukkit.Location;


public class CooManager {

	private static int applyTr(int a){
		return (int) Math.floor(a/(double) Utils.ISSIZE);
	}

	public static ISID getIslandID(Location loc){
		return getIslandID(loc.getBlockX(), loc.getBlockZ());
	}

	public static ISID getIslandID(int x, int z){
		return new ISID(applyTr(x), applyTr(z));
	}

//	public static boolean areSameIsland(int xa, int za, int xb, int zb){ // ne sert nulle part ?
//		return new ISID(applyTr(xa), applyTr(za)).equals(new ISID(applyTr(xb), applyTr(zb)));
//	}

	public static ISID findFreeSpot(){
		int x = 0;
		int z = 0;
		int sequ = 1;
		byte dir = 0;
		while(true){
			for(int i=0;i<sequ;i++){
				if(BaseAPI.getIsland(new ISID(x, z))==null)return new ISID(x, z);
				switch (dir){
					case 0:
						x++;
						break;
					case 1:
						z++;
						break;
					case 2:
						x--;
						break;
					case 3:
						z--;
						break;
				}
			}
			if(dir==1||dir==3)sequ++;
			if(dir==3)dir=0;
			else dir++;
		}
	}
}
