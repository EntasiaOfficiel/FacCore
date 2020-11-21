package fr.entasia.skycore.objs;

import org.bukkit.Material;
import org.bukkit.block.Biome;

public enum IslandType {
	DEFAULT(0, Biome.PLAINS, Material.OAK_DOOR),
	ICE(1, Biome.SNOWY_TUNDRA, Material.SPRUCE_DOOR),
	DESERT(2, Biome.DESERT, Material.BIRCH_DOOR),
	JUNGLE(3, Biome.JUNGLE, Material.JUNGLE_DOOR),
	MESA(4, Biome.BADLANDS, Material.ACACIA_DOOR),
	SWAMP(5, Biome.SWAMP, Material.DARK_OAK_DOOR);

	public int id;
	public IslandShematics schems;
	public Biome biome;
	public Material door;

	IslandType(int id, Biome biome, Material door){
		this.id = id;
		this.biome = biome;
		this.door = door;
	}

	public static IslandType getType(int i){
		return IslandType.values()[i];
	}

}
