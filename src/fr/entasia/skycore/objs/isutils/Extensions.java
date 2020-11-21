package fr.entasia.skycore.objs.isutils;

import fr.entasia.skycore.Utils;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.Arrays;

public enum Extensions {
	X100(20, "§7100x100 (par défaut)", Material.IRON_BLOCK, 0, 0, "§7La taille par défaut de ton île.", "§7C'est quand même assez grand pour commencer !"),
	X200(21, "§3200x200", Material.LAPIS_BLOCK, 200000, 700, "§3Tu devrais pouvoir faire des grands", "§3champs et de bonnes récoltes avec ca !"),
	X300(23, "§6300x300", Material.GOLD_BLOCK, 500000, 2000, "§6Une bonne taille pour faire", "§6de belles villes flottantes !"),
	X400(24, "§b400x400", Material.DIAMOND_BLOCK, 1000000, 3500, "§bJe vois pas à quoi ca pourrait te servir", "§bmais bon, tu pourra te la péter !");

	public int slot;
	public String name;
	public Material type;
	public int minlvl;
	public int price;
	public ArrayList<String> lore;

	Extensions(int slot, String name, Material type, int price, int minlvl, String... lore) {
		this.slot = slot;
		this.name = name;
		this.type = type;
		this.minlvl = minlvl;
		this.price = price;
		this.lore = new ArrayList<>(Arrays.asList(lore));
		this.lore.add("");
		this.lore.add("§ePrix : " + Utils.formatMoney(price));
		this.lore.add("§eNiveau requis : " + minlvl);
	}
}
