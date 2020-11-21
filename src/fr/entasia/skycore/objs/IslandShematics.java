package fr.entasia.skycore.objs;


import com.sk89q.worldedit.extent.clipboard.Clipboard;

public class IslandShematics {

	public String name;
	public Clipboard island;
	public String[] plans;
	public Clipboard[] structures;
	public Clipboard[] miniIslands;


	public int indexOf(String a){
		for(int i=0;i<plans.length;i++){
			if(plans[i].equals(a))return i;
		}
		return -1;
	}

}
