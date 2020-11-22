package fr.entasia.faccore.apis;

import fr.entasia.faccore.objs.FacException;
import org.bukkit.Chunk;
import org.bukkit.World;

public class ChunkID {
    public final Dimension dim;
    public final int x, z;
    protected Chunk chunk;

    public ChunkID(Chunk c){
        dim = Dimension.getDimension(c.getWorld());
        if(dim==null)throw new FacException("World is not a game world");
        this.x = c.getX();
        this.z = c.getZ();
    }

    public ChunkID(Dimension dim, int x, int z){
        this.dim = dim;
        this.x = x>>4;
        this.z = z>>4;
    }

    public ChunkID(World w, int x, int z){
        this.dim = Dimension.getDimension(w);
        if(this.dim==null)throw new FacException("World is not a game world");
        this.x = x>>4;
        this.z = z>>4;
    }


    public Chunk getChunk(){
        if(chunk==null){
            chunk = dim.world.getChunkAt(x, z);
        }
        return chunk;
    }
}
