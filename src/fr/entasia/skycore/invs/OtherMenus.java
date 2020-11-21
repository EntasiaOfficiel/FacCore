package fr.entasia.skycore.invs;

import fr.entasia.apis.menus.MenuCreator;
import fr.entasia.apis.other.ItemBuilder;
import fr.entasia.apis.utils.ItemUtils;
import fr.entasia.skycore.objs.tasks.RankTask;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class OtherMenus {

	public static MenuCreator topRankMenu = new MenuCreator();
	public static void topRankOpen(Player p){

		Inventory inv = topRankMenu.createInv(6, "§6Top 10 des niveau d'îles :");


		ItemStack item;
		RankTask.RankEntry re;
		SlotEntry se;
		for(int i=0;i<entries.length;i++){
			re = RankTask.list[i];
			if(re.is==null||re.lvl<=0)break;
			se = entries[i];
			item = new ItemBuilder(Material.PLAYER_HEAD).damage(3).name(se.color+"Top "+(i+1)).lore(re.is.getName(), "§aNiveau : "+
					re.lvl, "§cChef §6: "+re.is.getOwner().sp.name).build();
			ItemUtils.placeSkullAsync(inv, se.slot, item, re.is.getOwner().sp.name);
		}

		p.openInventory(inv);
	}

	public static SlotEntry[] entries = new SlotEntry[RankTask.list.length];

	public static class SlotEntry {
		public int slot;
		public ChatColor color;

		public SlotEntry(int slot, ChatColor color){
			this.slot = slot;
			this.color = color;
		}
	}

	static{
		entries[0] = new SlotEntry(4 , ChatColor.GOLD); // 1
		entries[1] = new SlotEntry(11, ChatColor.YELLOW);
		entries[2] = new SlotEntry(15, ChatColor.YELLOW); // 3
		entries[3] = new SlotEntry(22, ChatColor.AQUA);
		entries[4] = new SlotEntry(28, ChatColor.AQUA); // 5
		entries[5] = new SlotEntry(34, ChatColor.BLUE);
		entries[6] = new SlotEntry(39, ChatColor.BLUE); // 7
		entries[7] = new SlotEntry(41, ChatColor.BLUE);
		entries[8] = new SlotEntry(45, ChatColor.BLUE); // 9
		entries[9] = new SlotEntry(53, ChatColor.BLUE);
	}


}
