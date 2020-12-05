package fr.entasia.faccore.apis;

import fr.entasia.faccore.objs.FacException;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;

import javax.swing.plaf.SeparatorUI;
import java.util.Random;

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


	public boolean equals(ChunkID cid){
		return dim==cid.dim&&x==cid.x&&z==cid.z;
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
		return getKey(x, z);
	}

	public static void mainab(String[] za){
		for(int i=0;i<64-27;i++)System.out.print(" ");
		for(int i=0;i<27;i++)System.out.print("a");
		System.out.println();

		long key = Block.getBlockKey(-1, 0, 0);
		print(key);
		key = key << 37;
		key &= ~((long) 0b1 << 63); // zerofill injection
		print(key);
		key = key >> 37;
		print(key);

		System.out.println(Block.getBlockKeyX(key));
	}

	public static void main(String[] za) {
		Random r = new Random();
		for(int i=0;i<Integer.MAX_VALUE;i++){
			if(!test(r.nextInt(10000)-5000, r.nextInt(10000)-5000))throw new RuntimeException();
		}
	}

	public static void maina(String[] za) {
		test(-2484, 4008);
	}

	public static boolean test(int x, int z){
		long key = getChunkKey(x, z);
//		print(key);

		int inj = 2;
		key &= ~((long) 0b11 << 62); // removed
		key |= ((long) inj << 62); // injected

		inj = (int) (key>>>62);
		key &= ~((long) 0b11 << 62); // zerofill injection
		int nx = (int) (key << 33 >> 33);


		int nz = (int) (key << 2 >> 33);
//		print(key);

//		System.out.println(" ");
//		System.out.println(x+" "+nx);
//		System.out.println(z+" "+nz);
		return x==nx&&z==nz;
	}

	static void print(long key) {
		String s = Long.toBinaryString(key);
		while(s.length()<64)s="0"+s;
		System.out.println(s);
	}

	private static long getVanillaKey(int x, int z) {
		return
				((long)x & Integer.MAX_VALUE)
				|
						(((long)z & Integer.MAX_VALUE) << 31);
	}

	private static long getKey(int x, int z){
		long key = getVanillaKey(x, z);
		int inj = 2;
		key &= ~((long) 0b11 << 62); // removed
		key |= ((long) inj << 62); // injected
		return key;
	}


}
