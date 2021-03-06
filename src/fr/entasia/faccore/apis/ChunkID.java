package fr.entasia.faccore.apis;

import fr.entasia.faccore.objs.FacException;
import org.bukkit.Chunk;

public class ChunkID {
	public final Dimension dim;
	public final int x, z;
	protected Chunk chunk;

	public ChunkID(Chunk c){
		dim = Dimension.get(c.getWorld());
		if(dim==null)throw new FacException(c.getWorld()+" is not a game world");
		this.x = c.getX();
		this.z = c.getZ();
	}

	public ChunkID(Dimension dim, int x, int z){
		this.dim = dim;
		this.x = x>>4;
		this.z = z>>4;
	}

	public ChunkID(long key){
		// only 27-4=23 bits per coordinate

		dim = Dimension.get((int) (key>>>62));
		if(dim==null)throw new FacException("World with id "+(key>>>62)+" is not a game world");

		key &= ~((long) 0b11 << 62); // zerofill injection
		x = (int) (key << 33 >> 33);
		z = (int) (key << 2 >> 33);
	}


	// FONCTIONS A AVOIR


//	public int hashCode(){
//		System.out.println("hashCode");
//		return super.hashCode();
//	}

	public boolean equals(Object obj){
		if(obj instanceof ChunkID){
			ChunkID cid = (ChunkID)obj;
			return dim==cid.dim&&x==cid.x&&z==cid.z;
		}else return false;
	}

	public String toString(){
		return "ChunkID["+dim+";"+x+";"+z+"]";
	}


	// FONCTIONS RANDOM


	public Chunk getChunk(){
		if(chunk==null) chunk = dim.world.getChunkAt(x, z);
		return chunk;
	}

	public long getKey(){
		long key = getVanillaKey(x, z);
		key &= ~((long) 0b11 << 62); // zerofill injection
		key |= ((long) dim.id << 62); // injected
		return key;
	}

	private static long getVanillaKey(int x, int z) {
		return
				((long)x & Integer.MAX_VALUE)
				|
						(((long)z & Integer.MAX_VALUE) << 31);
	}

}
