package fr.entasia.skycore.apis;

import fr.entasia.skycore.Utils;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

public class OthersAPI {
	public static boolean isMasterEdit(Player p){
		return p.getGameMode()== GameMode.CREATIVE&& Utils.masterEditors.contains(p);
	}
}
