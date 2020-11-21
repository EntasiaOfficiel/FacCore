package fr.entasia.skycore.objs.tasks;

import fr.entasia.skycore.Utils;
import fr.entasia.skycore.apis.BaseIsland;
import org.bukkit.scheduler.BukkitRunnable;

public class RankTask extends BukkitRunnable {

	public static RankEntry[] list;

	@Override
	public void run() {
		RankEntry[] list = new RankEntry[10];
		for(int i=0;i<list.length;i++) {
			list[i] = new RankEntry(); // fake
		}

		for (RankEntry rankEntry : list) {
			check:
			for (BaseIsland is : Utils.islandCache) {
				if (rankEntry.lvl < is.getLevel()) {
					for (RankEntry is2 : list) {
						if (is2.is == is) continue check;
					}
					rankEntry.is = is;
					rankEntry.lvl = is.getLevel();
				}
			}
		}
		RankTask.list = list;
	}

	public static class RankEntry{
		public BaseIsland is;
		public int lvl;

	}
}
