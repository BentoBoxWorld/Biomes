package world.bentobox.biomes.tasks;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.scheduler.BukkitRunnable;

import world.bentobox.bentobox.api.user.User;
import world.bentobox.biomes.BiomesAddon;
import world.bentobox.biomes.database.objects.BiomeChunkUpdateObject;
import world.bentobox.biomes.database.objects.BiomesObject;
import world.bentobox.biomes.events.BiomeChangedEvent;
import world.bentobox.biomes.utils.Utils;


/**
 * This class updates biome from min coordinate till max coordinate.
 */
public class BiomeUpdateTask extends BukkitRunnable
{
    /**
     * Default Update task constructor.
     * @param addon BiomeAddon object.
     * @param user Player who calls biome update.
     * @param location Location for biome location object.
     * @param biome BiomeObject that will changed.
     */
    public BiomeUpdateTask(BiomesAddon addon,
        User user,
        Location location,
        BiomesObject biome)
    {
        this.addon = addon;
        this.user = user;
        this.location = location;
        this.biome = biome.getBiome();
        this.objectID = biome.getUniqueId();
        this.objectName = biome.getFriendlyName();
    }


    /**
     * Default RunTask.
     */
    @Override
    public void run()
    {
        if (this.user.isPlayer())
        {
            this.user.sendMessage("biomes.messages.update-start");
        }

        // Create and populate ChunkObjectObjects where biome change should occur.
        List<BiomeChunkUpdateObject> chunkUpdateObjectList = new ArrayList<>();

        for (int x = this.minX >> 4, maxX = this.maxX >> 4; x <= maxX; x++)
        {
            for (int z = this.minZ >> 4, maxZ = this.maxZ >> 4; z <= maxZ; z++)
            {
                chunkUpdateObjectList.add(this.createChunkUpdateObject(x, z));
            }
        }

        // After everything is created update biome where it is possible.
        // Use iterator as it will allow to remove element from list.
        Iterator<BiomeChunkUpdateObject> iterator = chunkUpdateObjectList.iterator();

        while (iterator.hasNext())
        {
            BiomeChunkUpdateObject updateObject = iterator.next();

            // Since Minecraft 1.15 biome is stored in 4x4x4 blocks.
            // There is no point to update biome in each block position, only in every 4.

            if (!this.world.isChunkLoaded(updateObject.getChunkX(), updateObject.getChunkZ()))
            {
                // Temporarily load the chunk
                this.world.getChunkAt(updateObject.getChunkX(), updateObject.getChunkZ());
            }

            for (int x = updateObject.getMinX();
                x <= updateObject.getMaxX();
                x += 4)
            {
                for (int z = updateObject.getMinZ();
                    z <= updateObject.getMaxZ();
                    z += 4)
                {
                    for (int y = updateObject.getMinY();
                        y <= updateObject.getMaxY();
                        y += 4)
                    {
                        // Biome should not be changed in Greenhouses.
                        if (!this.addon.getAddonManager().hasGreenhouseInLocation(updateObject.getWorld(), x, y, z))
                        {
                            // Change Biome
                            updateObject.getWorld().setBiome(x, y, z, this.biome);
                        }
                    }
                }
            }

            // If biome is changed completely, then remove object.
            iterator.remove();
        }

        if (this.user.isPlayer())
        {
            this.user.sendMessage("biomes.messages.update-done",
                "[biome]",
                this.objectName);

            this.addon.log(this.user.getName() + " change biome in loaded chunks to " +
                this.biome + " from" +
                " x=" + this.minX + ":" + this.maxX +
                " y=" + this.minY + ":" + this.maxY +
                " z=" + this.minZ + ":" + this.maxZ +
                " while standing on" +
                " x=" + this.location.getBlockX() +
                " y=" + this.location.getBlockY() +
                " z=" + this.location.getBlockZ());
        }
        else
        {
            this.addon.log("Console changed biome in loaded chunks to " +
                this.biome + " from" +
                " x=" + this.minX + ":" + this.maxX +
                " y=" + this.minY + ":" + this.maxY +
                " z=" + this.minZ + ":" + this.maxZ);
        }

        // Fire event that biome is changed.
        Bukkit.getPluginManager().callEvent(
            new BiomeChangedEvent(this.objectID,
                this.biome,
                this.user.getUniqueId(),
                this.minX,
                this.minY,
                this.minZ,
                this.maxX,
                this.maxY,
                this.maxZ));
    }


    /**
     * This method create object that contains information where biome must be changed.
     * @param chunkX Chunk X coordinate.
     * @param chunkZ Chunk Z coordinate.
     * @return ChunkUpdateObject that contains all necessary information about pending
     * biome updating.
     * Note!
     * If update is queued in different Y values, then newest object will overwrite value.
     * Older object will not be saved.
     * It is so, because fo uniqueId generation.
     */
    private BiomeChunkUpdateObject createChunkUpdateObject(int chunkX, int chunkZ)
    {
        BiomeChunkUpdateObject chunkUpdateObject = new BiomeChunkUpdateObject();
        chunkUpdateObject.setUniqueId(Utils.getGameMode(this.world) + "_" + chunkX + "-" + chunkZ);
        chunkUpdateObject.setWorld(this.world);
        chunkUpdateObject.setBiome(this.biome);

        chunkUpdateObject.setChunkX(chunkX);
        chunkUpdateObject.setChunkZ(chunkZ);

        // transform to chunk coordinates
        if (this.minX >> 4 == chunkX)
        {
            chunkUpdateObject.setMinX(this.minX);
        }
        else
        {
            chunkUpdateObject.setMinX(chunkX << 4);
        }

        if (this.maxX >> 4 == chunkX)
        {
            chunkUpdateObject.setMaxX(this.maxX);

        }
        else
        {
            chunkUpdateObject.setMaxX((chunkX << 4) + 15);
        }

        if (this.minZ >> 4 == chunkZ)
        {
            chunkUpdateObject.setMinZ(this.minZ);
        }
        else
        {
            chunkUpdateObject.setMinZ(chunkZ << 4);
        }

        if (this.maxZ >> 4 == chunkZ)
        {
            chunkUpdateObject.setMaxZ(this.maxZ);

        }
        else
        {
            chunkUpdateObject.setMaxZ((chunkZ << 4) + 15);
        }

        chunkUpdateObject.setMinY(this.minY);
        chunkUpdateObject.setMaxY(this.maxY);

        return chunkUpdateObject;
    }


    // ---------------------------------------------------------------------
    // Section: Setters
    // ---------------------------------------------------------------------

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
     * Default Setter.
     * @param minX Integer.
     */
    public void setMinX(int minX)
    {
        this.minX = minX;
    }


    /**
     * Default Setter.
     * @param maxX Integer.
     */
    public void setMaxX(int maxX)
    {
        this.maxX = maxX;
    }


    /**
     * Default Setter.
     * @param minZ Integer.
     */
    public void setMinZ(int minZ)
    {
        this.minZ = minZ;
    }


    /**
     * Default Setter.
     * @param maxZ Integer.
     */
    public void setMaxZ(int maxZ)
    {
        this.maxZ = maxZ;
    }


    /**
     * This method sets the minY value.
     * @param minY the minY new value.
     */
    public void setMinY(int minY)
    {
        this.minY = minY;
    }


    /**
     * This method sets the maxY value.
     * @param maxY the maxY new value.
     */
    public void setMaxY(int maxY)
    {
        this.maxY = maxY;
    }


    // ---------------------------------------------------------------------
    // Section: Variables
    // ---------------------------------------------------------------------

    /**
     * Instance of biomes addon.
     */
    private final BiomesAddon addon;

    /**
     * Instance of user who will be informed about update task.
     */
    private final User user;

    /**
     * Instance of location from which update was called.
     */
    private final Location location;

    /**
     * Instance of biome that is required to be changed.
     */
    private final Biome biome;

    /**
     * Original biome object name.
     */
    private final String objectName;

    /**
     * Original biome object id.
     */
    private final String objectID;

    /**
     * Instance of world where update will be processed.
     */
    private World world;

    /**
     * Minimal X Coordinate.
     */
    private int minX;

    /**
     * Maximal X Coordinate.
     */
    private int maxX;

    /**
     * Minimal Z Coordinate.
     */
    private int minZ;

    /**
     * Maximal Z Coordinate.
     */
    private int maxZ;

    /**
     * Minimal Y Coordinate.
     */
    private int minY;

    /**
     * Maximal Y Coordinate.
     */
    private int maxY;
}
