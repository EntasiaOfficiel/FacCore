package fr.entasia.faccore.invs;

import fr.entasia.apis.menus.MenuClickEvent;
import fr.entasia.apis.menus.MenuCreator;
import fr.entasia.apis.other.InstantFirework;
import fr.entasia.apis.other.ItemBuilder;
import fr.entasia.apis.utils.ItemUtils;
import fr.entasia.apis.utils.TextUtils;
import fr.entasia.faccore.Utils;
import fr.entasia.faccore.apis.FacPlayer;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class IsMenus {

	// MENU DE BASE DE LA FACTION

	private static final MenuCreator baseIslandMenu = new MenuCreator() {

		@Override
		public void onMenuClick(MenuClickEvent e) {
			FacPlayer fp  = (FacPlayer)e.data;
			e.player.closeInventory();
			switch(e.item.getType()){
				case PLAYER_HEAD:{
					manageTeamOpen(fp);
					break;
				}
				case OAK_DOOR:{
					fp.getFaction().teleportHome(e.player);
					break;
				}
				default:{
					e.player.sendMessage("§cCette option n'est pas encore prête !");
					break;
				}
			}
		}
	};


	public static void baseIslandOpen(FacPlayer link) {

		Inventory inv = baseIslandMenu.createInv(5, "§6Menu principal de la faction :", link);

		ItemStack item = new ItemStack(Material.BLUE_STAINED_GLASS_PANE);
		for (int i = 0; i < 45; i += 9) {
			inv.setItem(i, item);
			inv.setItem(i + 8, item);
		}
		for (int i = 36; i < 45; i++) inv.setItem(i, item);

		item = new ItemStack(Material.WHITE_STAINED_GLASS_PANE);
		for (int i = 1; i < 8; i++) inv.setItem(i, item);


		item = new ItemStack(Material.PAPER);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName("§6Informations");
		ArrayList<String> a = new ArrayList<>();
		a.add("§eID : §6" + link.getFaction().id);
		if(link.getFaction().getName()!=null)a.add("§eNom : "+link.getFaction().getName());
		a.add("§eBanque de la faction : §6" + Utils.formatMoney(link.getFaction().getBank()));
		ArrayList<FacPlayer> members = link.getFaction().getMembers();
		if(members.size()==1)a.add("§eAucune équipe !");
		else a.add("§eÉquipe : §6"+members.size()+"§e membres");
		a.add("§eRôle : §6"+TextUtils.firstLetterUpper(link.getRank().name));
		meta.setLore(a);
		item.setItemMeta(meta);
		inv.setItem(4, item);

		item = new ItemStack(Material.OAK_DOOR);
		meta = item.getItemMeta();
		meta.setDisplayName("§aSe téléporter au home");
		item.setItemMeta(meta);
		inv.setItem(19, item);

		ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
		SkullMeta smeta = (SkullMeta) skull.getItemMeta();
		smeta.setDisplayName("§eVoir l'équipe de ta faction");
		skull.setItemMeta(smeta);
		ItemUtils.placeSkullAsync(inv, 20, skull, link.p);


		link.p.openInventory(inv);
	}


	// MENU DE TEAM


	private static final MenuCreator manageTeamMenu = new MenuCreator() {

		@Override
		public void onMenuClick(MenuClickEvent e) {
			FacPlayer link = (FacPlayer)e.data;
			if(e.item.getType()==Material.WRITABLE_BOOK) baseIslandOpen(link);
		}
	};

	public static void manageTeamOpen(FacPlayer link){
		Inventory inv = manageTeamMenu.createInv(3, "§6Ton équipe :", link);

		int i = 0;
		for(FacPlayer ll : link.getFaction().getMembers()){

			ItemStack item = new ItemStack(Material.PLAYER_HEAD);
			SkullMeta smeta = (SkullMeta) item.getItemMeta();

			smeta.setDisplayName(ll.getName());
			if(ll.equals(link))smeta.setLore(Collections.singletonList("§aC'est toi !"));
			item.setItemMeta(smeta);
			ItemUtils.placeSkullAsync(inv, i, item, ll.name);
			i++;
		}

		ItemBuilder item = new ItemBuilder(Material.WRITABLE_BOOK).name("§cRetour au menu précédent");
		inv.setItem(26, item.build());

		link.p.openInventory(inv);
	}


}
