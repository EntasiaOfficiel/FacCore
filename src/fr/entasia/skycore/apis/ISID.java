package fr.entasia.skycore.apis;

import fr.entasia.skycore.Utils;
import fr.entasia.skycore.apis.mini.Dimensions;
import org.bukkit.Location;

public class ISID {

	public final int x,z;

	public ISID(int x, int z){
		this.x = x;
		this.z = z;
	}

	public boolean equals(ISID isid){
		return (x==isid.x&&z==isid.z);
	}

	public String toString(){
		return "ISID["+x+";"+z+"]";
	}
	public String str(){
		return x+";"+z;
	}

	public static ISID parse(String text){
		String[] id = text.split(";");
		try {
			if (id.length == 2) {
				return new ISID(Integer.parseInt(id[0]), Integer.parseInt(id[1]));
			} else throw new NumberFormatException();
		} catch (NumberFormatException e) {
			return null;
		}
	}



	public int getMinXTotal(){
		return x* Utils.ISSIZE;
	}
	public int getMinZTotal(){
		return z*Utils.ISSIZE;
	}
	public int getMaxXTotal(){
		return (x+1)*Utils.ISSIZE-1;
	}
	public int getMaxZTotal(){
		return (z+1)*Utils.ISSIZE-1;
	}

	public int getMiddleX(){
		return x*Utils.ISSIZE+Utils.ISSIZE/2;
	}

	public int getMiddleZ() {
		return z*Utils.ISSIZE+Utils.ISSIZE/2;
	}

	public int distanceFromIS(Location loc){
		return distanceFromIS(loc.getBlockX(), loc.getBlockZ());
	}

	public int distanceFromIS(int x, int z){
		return Math.max(Math.abs(getMiddleX()-x), Math.abs(getMiddleZ()-z));
	}

	public Location getMiddleLoc(Dimensions d){
		return new Location(d.world, getMiddleX(), 70.5, getMiddleZ());
	}
}
