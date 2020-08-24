package world.bentobox.biomes.database.objects;


import org.bukkit.World;
import org.bukkit.block.Biome;

import com.google.gson.annotations.Expose;

import world.bentobox.bentobox.database.objects.DataObject;
import world.bentobox.bentobox.database.objects.Table;


/**
 * Database object ChunkUpdateObject stores all chunks where biome should be changed but
 * was not done due they were unloaded at that moment.
 *
 * @author BONNe
 * Created on 2019-07-03
 */
@Table(name = "BiomeUpdateChunks")
public class BiomeChunkUpdateObject implements DataObject
{
    /**
     * Constructor ChunkUpdateObject creates a new ChunkUpdateObject instance.
     */
    public BiomeChunkUpdateObject()
    {
    }


    // ---------------------------------------------------------------------
    // Section: Getters and setters
    // ---------------------------------------------------------------------


    /**
     * This method returns the uniqueId value.
     * @return the value of uniqueId.
     */
    @Override
    public String getUniqueId()
    {
        return this.uniqueId;
    }


    /**
     * This method sets the uniqueId value.
     * @param uniqueId the uniqueId new value.
     *
     */
    @Override
    public void setUniqueId(String uniqueId)
    {
        this.uniqueId = uniqueId;
    }


    /**
     * This method returns the minX value.
     * @return the value of minX.
     */
    public int getMinX()
    {
        return minX;
    }


    /**
     * This method sets the minX value.
     * @param minX the minX new value.
     *
     */
    public void setMinX(int minX)
    {
        this.minX = minX;
    }


    /**
     * This method returns the maxX value.
     * @return the value of maxX.
     */
    public int getMaxX()
    {
        return maxX;
    }


    /**
     * This method sets the maxX value.
     * @param maxX the maxX new value.
     *
     */
    public void setMaxX(int maxX)
    {
        this.maxX = maxX;
    }


    /**
     * This method returns the minZ value.
     * @return the value of minZ.
     */
    public int getMinZ()
    {
        return minZ;
    }


    /**
     * This method sets the minZ value.
     * @param minZ the minZ new value.
     *
     */
    public void setMinZ(int minZ)
    {
        this.minZ = minZ;
    }


    /**
     * This method returns the maxZ value.
     * @return the value of maxZ.
     */
    public int getMaxZ()
    {
        return maxZ;
    }


    /**
     * This method sets the maxZ value.
     * @param maxZ the maxZ new value.
     *
     */
    public void setMaxZ(int maxZ)
    {
        this.maxZ = maxZ;
    }


    /**
     * This method returns the minY value.
     * @return the value of minY.
     */
    public int getMinY()
    {
        return minY;
    }


    /**
     * This method sets the minY value.
     * @param minY the minY new value.
     *
     */
    public void setMinY(int minY)
    {
        this.minY = minY;
    }


    /**
     * This method returns the maxY value.
     * @return the value of maxY.
     */
    public int getMaxY()
    {
        return maxY;
    }


    /**
     * This method sets the maxY value.
     * @param maxY the maxY new value.
     *
     */
    public void setMaxY(int maxY)
    {
        this.maxY = maxY;
    }


    /**
     * This method returns the chunkX value.
     * @return the value of chunkX.
     */
    public int getChunkX()
    {
        return chunkX;
    }


    /**
     * This method sets the chunkX value.
     * @param chunkX the chunkX new value.
     *
     */
    public void setChunkX(int chunkX)
    {
        this.chunkX = chunkX;
    }


    /**
     * This method returns the chunkZ value.
     * @return the value of chunkZ.
     */
    public int getChunkZ()
    {
        return chunkZ;
    }


    /**
     * This method sets the chunkZ value.
     * @param chunkZ the chunkZ new value.
     *
     */
    public void setChunkZ(int chunkZ)
    {
        this.chunkZ = chunkZ;
    }


    /**
     * This method returns the biome value.
     * @return the value of biome.
     */
    public Biome getBiome()
    {
        return biome;
    }


    /**
     * This method sets the biome value.
     * @param biome the biome new value.
     *
     */
    public void setBiome(Biome biome)
    {
        this.biome = biome;
    }


    /**
     * This method returns the world value.
     * @return the value of world.
     */
    public World getWorld()
    {
        return world;
    }


    /**
     * This method sets the world value.
     * @param world the world new value.
     *
     */
    public void setWorld(World world)
    {
        this.world = world;
    }


    /**
     * This method returns the forceLoaded value.
     * @return the value of forceLoaded.
     */
    public boolean isForceLoaded()
    {
        return forceLoaded;
    }


    /**
     * This method sets the forceLoaded value.
     * @param forceLoaded the forceLoaded new value.
     *
     */
    public void setForceLoaded(boolean forceLoaded)
    {
        this.forceLoaded = forceLoaded;
    }


    // ---------------------------------------------------------------------
    // Section: Variables
    // ---------------------------------------------------------------------


    /**
     * Unique ID of current object.
     */
    @Expose
    private String uniqueId;

    /**
     * Minimal X coordinate of biome update.
     */
    @Expose
    private int minX;

    /**
     * Maximal X coordinate of biome update.
     */
    @Expose
    private int maxX;

    /**
     * Minimal Z coordinate of biome update.
     */
    @Expose
    private int minZ;

    /**
     * Maximal Z coordinate of biome update.
     */
    @Expose
    private int maxZ;

    /**
     * Minimal Y coordinate of biome update.
     */
    @Expose
    private int minY;

    /**
     * Maximal Y coordinate of biome update.
     */
    @Expose
    private int maxY;

    /**
     * Chunk X coordinate.
     */
    @Expose
    private int chunkX;

    /**
     * Chunk Z coordinate.
     */
    @Expose
    private int chunkZ;

    /**
     * Biome that will update in current update process.
     */
    @Expose
    private Biome biome;

    /**
     * World where current object should work.
     */
    @Expose
    private World world;

    /**
     * Boolean that indicate that chunk is already force-loaded.
     */
    @Expose
    private boolean forceLoaded;
}
