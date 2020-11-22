package fr.entasia.faccore.objs;

import fr.entasia.faccore.Utils;
import fr.entasia.faccore.apis.Faction;
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
			for (Faction is : Utils.factionCache) {
				if (rankEntry.bank < is.getBank()) {
					for (RankEntry is2 : list) {
						if (is2.fac == is) continue check;
					}
					rankEntry.fac = is;
					rankEntry.bank = is.getBank();
				}
			}
		}
		RankTask.list = list;
	}

	public static class RankEntry{
		public Faction fac;
		public long bank;

	}
}
