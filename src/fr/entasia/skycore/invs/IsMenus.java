package fr.entasia.skycore.invs;

import fr.entasia.apis.menus.MenuClickEvent;
import fr.entasia.apis.menus.MenuCreator;
import fr.entasia.apis.other.InstantFirework;
import fr.entasia.apis.other.ItemBuilder;
import fr.entasia.apis.utils.ItemUtils;
import fr.entasia.apis.utils.TextUtils;
import fr.entasia.skycore.Utils;
import fr.entasia.skycore.apis.ISPLink;
import fr.entasia.skycore.apis.SkyPlayer;
import fr.entasia.skycore.apis.TerrainManager;
import fr.entasia.skycore.objs.IslandType;
import fr.entasia.skycore.objs.isutils.Extensions;
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

	// MENU CHOISIR UNE ILE DE DEPART

	private static final MenuCreator startIslandChooseMenu = new MenuCreator() {

		@Override
		public void onMenuClick(MenuClickEvent e) {
			SkyPlayer sp = (SkyPlayer)e.data;
			e.player.closeInventory();
			switch(e.item.getType()){
				case GRASS:
					TerrainManager.tryGenerateIsland(sp, IslandType.DEFAULT);
					break;
				case ICE:
					TerrainManager.tryGenerateIsland(sp, IslandType.ICE);
					break;
				case SAND:
					TerrainManager.tryGenerateIsland(sp, IslandType.DESERT);
					break;
				case VINE:
					TerrainManager.tryGenerateIsland(sp, IslandType.JUNGLE);
					break;
				case TERRACOTTA:
					TerrainManager.tryGenerateIsland(sp, IslandType.MESA);
					break;
				case LILY_PAD:
					TerrainManager.tryGenerateIsland(sp, IslandType.SWAMP);
					break;
				default:
					e.player.sendMessage("§cErreur ! Cette ile n'a pas été correctement configurée ! Préviens un Membre du Staff");
			}
			e.player.closeInventory();
		}
	};


	public static void startIslandChooseOpen(SkyPlayer sp){
		Inventory inv = startIslandChooseMenu.createInv(3, "§6Quelle île veut-tu ?", sp);

		ItemStack item = new ItemStack(Material.GRASS);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName("§aIle Basique");
		item.setItemMeta(meta);
		inv.setItem(10, item);

		item = new ItemStack(Material.ICE);
		meta = item.getItemMeta();
		meta.setDisplayName("§bIle des Neiges");
		item.setItemMeta(meta);
		inv.setItem(20, item);

		item = new ItemStack(Material.SAND);
		meta = item.getItemMeta();
		meta.setDisplayName("§aIle de Sable");
		item.setItemMeta(meta);
		inv.setItem(12, item);

		item = new ItemStack(Material.VINE);
		meta = item.getItemMeta();
		meta.setDisplayName("§2Ile Jungle");
		item.setItemMeta(meta);
		inv.setItem(14, item);

		item = new ItemStack(Material.TERRACOTTA);
		meta = item.getItemMeta();
		meta.setDisplayName("§6Ile Mesa");
		item.setItemMeta(meta);
		inv.setItem(24, item);

		item = new ItemStack(Material.LILY_PAD);
		meta = item.getItemMeta();
		meta.setDisplayName("§2Ile des Marais");
		item.setItemMeta(meta);
		inv.setItem(16, item);

		sp.p.openInventory(inv);
	}

	// MENU CHOISIR IS PAR DEFAUT


	private static final MenuCreator islandsListMenu = new MenuCreator() {

		@Override
		public void onMenuClick(MenuClickEvent e) {
			A a = ((A)e.data); // laisse
			ISPLink chosen = a.tracker.get(e.slot);
			if(chosen==null)e.player.sendMessage("§cUne erreur est survenue lors du choix de l'île !");
			else{
				if(!a.force&&e.click==MenuClickEvent.ClickType.LEFT){
					baseIslandOpen(chosen);
				}else{
					chosen.sp.setDefaultIS(chosen.is.isid);
					e.player.closeInventory();
					e.player.sendMessage("§aîle par défaut choisie avec succès !");
				}
			}
		}
	};

	private static class A{
		public HashMap<Integer, ISPLink> tracker = new HashMap<>();
		public boolean force;
	}

	public static void islandsListOpen(SkyPlayer sp, boolean force){
		A a = new A();
		a.force = force;
		String name;
		if(force){
			name = "§6Quelle île ?";
			a.tracker.put(-1, null);
		}
		else name = "§6Liste de tes îles :";
		Inventory inv = islandsListMenu.createInv(3, name, a);

		int j = 10;
		ArrayList<String> list;
		for(ISPLink link : sp.getIslands()){
			ItemStack item = new ItemStack(Material.OAK_SAPLING);
			ItemMeta meta = item.getItemMeta();
			meta.setDisplayName("§aIle ");
			list = new ArrayList<>();
			list.add("§eID: "+link.is.isid.str());
			if(link.is.getName()!=null)list.add("§eNom : "+link.is.getName());
			list.add("§eRang: "+link.getName());
			list.add("");
			if(!force)list.add("§aClick gauche pour voir les informations de l'île !");
			if(link.sp.getDefaultIS()==link){
				list.add("§2île par défaut actuelle !");
			}else{
				if(force)list.add("§aClique pour définir en île par défaut !");
				else list.add("§aClick droit pour définir en île par défaut !");
			}
			meta.setLore(list);
			item.setItemMeta(meta);
			inv.setItem(j, item);
			a.tracker.put(j, link);
			j+=2;
		}

		sp.p.openInventory(inv);
	}

	// MENU IS DE BASE DE L'ILE

	private static final MenuCreator baseIslandMenu = new MenuCreator() {

		@Override
		public void onMenuClick(MenuClickEvent e) {
			ISPLink link  = (ISPLink)e.data;
			e.player.closeInventory();
			switch(e.item.getType()){
				case PLAYER_HEAD:{
					manageTeamOpen(link);
					break;
				}
				case REDSTONE_BLOCK:{
					upgradeOpen(link);
					// A FAIRE
					break;
				}
				default:{
					if(e.item.getType()==link.is.type.door){
						link.is.teleportHome(e.player);
						e.player.sendMessage("§eTu as été téléporté à ton île !");
					}else e.player.sendMessage("§cCette option n'est pas encore prête !");
					break;
				}
			}
		}
	};


	public static void baseIslandOpen(ISPLink link) {

		Inventory inv = baseIslandMenu.createInv(5, "§6Menu principal de l'île :", link);

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
		a.add("§eID : §6" + link.is.isid.x + ";" + link.is.isid.z);
		if(link.is.getName()!=null)a.add("§eNom : "+link.is.getName());
		a.add("§eNiveau : §6" + link.is.getLevel());
		a.add("§eBanque d'île : §6" + Utils.formatMoney(link.is.getBank()));
		ArrayList<ISPLink> members = link.is.getMembers();
		if(members.size()==1)a.add("§eAucune équipe !");
		else a.add("§eÉquipe : §6"+members.size()+"§e membres");
		a.add("§eRôle : §6"+TextUtils.firstLetterUpper(link.getRank().name));
		a.add("§eNuméro personnel : §6" + (link.sp.getIslands().indexOf(link)+1));
		meta.setLore(a);
		item.setItemMeta(meta);
		inv.setItem(4, item);

		item = new ItemStack(link.is.type.door);
		meta = item.getItemMeta();
		meta.setDisplayName("§aSe téléporter à l'île");
		item.setItemMeta(meta);
		inv.setItem(19, item);

		ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
		SkullMeta smeta = (SkullMeta) skull.getItemMeta();
		smeta.setDisplayName("§eVoir l'équipe de ton île");
		skull.setItemMeta(smeta);
		ItemUtils.placeSkullAsync(inv, 20, skull, link.sp.p);

		item = new ItemStack(Material.BOOK);
		meta = item.getItemMeta();
		meta.setDisplayName("§6Challenges");
		item.setItemMeta(meta);
		inv.setItem(33, item);

		item = new ItemStack(Material.REDSTONE_BLOCK);
		meta = item.getItemMeta();
		meta.setDisplayName("§6Améliorations de l'île");
		item.setItemMeta(meta);
		inv.setItem(34, item);

		link.sp.p.openInventory(inv);
	}


	// MENU DE TEAM


	private static final MenuCreator manageTeamMenu = new MenuCreator() {

		@Override
		public void onMenuClick(MenuClickEvent e) {
			ISPLink link = (ISPLink)e.data;
			if(e.item.getType()==Material.WRITABLE_BOOK) baseIslandOpen(link);
		}
	};

	public static void manageTeamOpen(ISPLink link){
		Inventory inv = manageTeamMenu.createInv(3, "§6Ton équipe :", link);

		int i = 0;
		for(ISPLink ll : link.is.getSortedMembers()){

			ItemStack item = new ItemStack(Material.PLAYER_HEAD);
			SkullMeta smeta = (SkullMeta) item.getItemMeta();

			smeta.setDisplayName(ll.getName());
			if(ll.equals(link))smeta.setLore(Collections.singletonList("§aC'est toi !"));
			item.setItemMeta(smeta);
			ItemUtils.placeSkullAsync(inv, i, item, ll.sp.name);
			i++;
		}

		ItemBuilder item = new ItemBuilder(Material.WRITABLE_BOOK).name("§cRetour au menu précédent");
		inv.setItem(26, item.build());

		link.sp.p.openInventory(inv);
	}

	private static final MenuCreator upgradeMenu = new MenuCreator() {

		@Override
		public void onMenuClick(MenuClickEvent e) {
			ISPLink link = (ISPLink)e.data;
			if(e.item.getType()==Material.WRITABLE_BOOK) baseIslandOpen(link);
			else{
				e.player.closeInventory();
				Extensions[] list = Extensions.values();
				Extensions u;
				for(int i=0;i<list.length;i++){
					u = list[i];
					if(e.item.getType() == u.type){
						if(link.is.getExtension()>=i){
							e.player.sendMessage("§cTu as déja acquis cette extension !");
							return;
						}else if((link.is.getExtension()+1)!=i) {
							e.player.sendMessage("§cTu dois d'abord acheter les autres extensions !");
							return;
						}else if(link.is.getLevel()<u.minlvl){
							e.player.sendMessage("§cLe niveau de ton île est insuffisant ! Niveau demandé : §4"+u.minlvl+"§c. Niveau de ton île : §4"+link.is.getLevel());
							return;
						}
						if(link.sp.getMoney()>=u.price)link.sp.withdrawMoney(u.price);
						else if(link.is.getBank()>=u.price)link.is.withdrawBank(u.price);
						else{
							e.player.sendMessage("§cTu n'as pas assez de monnaie ! Monnaie demandée pour l'amélioration : §4"+u.price+"§c$");
							return;
						}
						link.is.setExtension((byte) (link.is.getExtension()+1));
						InstantFirework.explode(e.player.getLocation(), FireworkEffect.builder().withColor(Color.BLUE, Color.GREEN).withFade(Color.MAROON, Color.RED).flicker(true).build());
						e.player.sendMessage("§aTon île à été améliorée avec succès ! :D");
						return;
					}
				}
			}
		}
	};

	public static void upgradeOpen(ISPLink link){
		Inventory inv = upgradeMenu.createInv(4, "§6Amélioration de l'île :", link);


		ItemBuilder item = new ItemBuilder(Material.PAPER).name("§cKeskecé ?").lore("§3Ceci te sert à améliorer la zone de construction de ton île.",
				"§3Regarde les descriptions des 4 blocks ci dessous pour avoir les prix/prérequis pour les améliorations :)");
		inv.setItem(13, item.build());

		Extensions[] list = Extensions.values();
		for(int i=0;i<list.length;i++){
			item = new ItemBuilder(list[i].type).name(list[i].name).lore(list[i].lore);
			if(link.is.getExtension()>=i){
				item.fakeEnchant();
			}
			inv.setItem(list[i].slot, item.build());
		}

		item = new ItemBuilder(Material.WRITABLE_BOOK).name("§cRetour au menu précédent");
		inv.setItem(35, item.build());

		link.sp.p.openInventory(inv);
	}
}
