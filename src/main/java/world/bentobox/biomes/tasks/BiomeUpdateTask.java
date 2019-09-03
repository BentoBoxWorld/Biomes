package world.bentobox.biomes.tasks;


import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

import world.bentobox.bentobox.api.user.User;
import world.bentobox.biomes.BiomesAddon;
import world.bentobox.biomes.database.objects.BiomesObject;
import world.bentobox.biomes.database.objects.BiomeChunkUpdateObject;
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
     * @param world World where biome update will happen.
     * @param biome BiomeObject that will changed.
     */
    public BiomeUpdateTask(BiomesAddon addon, User user, World world, BiomesObject biome)
    {
        this.addon = addon;
        this.user = user;
        this.world = world;
        this.biome = biome;
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

        Biome newBiome = this.biome.getBiome();

        // After everything is created update biome where it is possible.
        // Use iterator as it will allow to remove element from list.
        Iterator<BiomeChunkUpdateObject> iterator = chunkUpdateObjectList.iterator();

        while (iterator.hasNext())
        {
            BiomeChunkUpdateObject updateObject = iterator.next();

            boolean completlyChanged = true;

            for (int x = updateObject.getMinX();
                completlyChanged && x <= updateObject.getMaxX();
                x++)
            {
                for (int z = updateObject.getMinZ();
                    completlyChanged && z <= updateObject.getMaxZ();
                    z++)
                {
                    if (!this.world.isChunkLoaded(updateObject.getChunkX(), updateObject.getChunkZ()))
                    {
                        // If chunk is unloaded then skip change.
                        completlyChanged = false;
                    }
                    else
                    {
                        // Change Biome
                        updateObject.getWorld().setBiome(x, z, newBiome);
                    }
                }
            }

            if (completlyChanged)
            {
                // If biome is changed completely, then remove object.
                iterator.remove();
            }
            else
            {
                // Otherwise add it to manager.
                this.addon.getAddonManager().addChunkUpdateObject(updateObject);
            }
        }

        if (this.user.isPlayer())
        {
            this.user.sendMessage("biomes.messages.update-done",
                "[biome]",
                this.biome.getFriendlyName());

            this.addon.log(this.user.getName() + " change biome in loaded chunks to " +
                this.biome.getBiome() + " from x=" + this.minX + ":" + this.maxX + " z=" + this.minZ + ":" + this.maxZ +
                " while standing on x=" + this.user.getLocation().getBlockX() + " z=" + this.user.getLocation().getBlockZ());
        }
        else
        {
            this.addon.log("Console changed biome in loaded chunks to " +
                this.biome.getBiome() + " from x=" + this.minX + ":" + this.maxX + " z=" + this.minZ + ":" + this.maxZ);
        }

        // Log information about unloaded chunk cound.
        if (!chunkUpdateObjectList.isEmpty())
        {
            this.addon.log("Populated offline updater with " + chunkUpdateObjectList.size() + " chunks.");
        }

        // Fire event that biome is changed.
        Bukkit.getPluginManager().callEvent(
            new BiomeChangedEvent(this.biome.getUniqueId(),
                this.biome.getBiome(),
                this.user.getUniqueId(),
                this.minX,
                this.minZ,
                this.maxX,
                this.maxZ));
    }


    /**
     * This method create object that contains information where biome must be changed.
     * @param chunkX Chunk X coordinate.
     * @param chunkZ Chunk Z coordinate.
     * @return ChunkUpdateObject that contains all necessary information about pending
     * biome updating.
     */
    private BiomeChunkUpdateObject createChunkUpdateObject(int chunkX, int chunkZ)
    {
        BiomeChunkUpdateObject chunkUpdateObject = new BiomeChunkUpdateObject();
        chunkUpdateObject.setUniqueId(Utils.getGameMode(this.world) + "_" + chunkX + "-" + chunkZ);
        chunkUpdateObject.setWorld(this.world);
        chunkUpdateObject.setBiome(this.biome.getBiome());

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

        return chunkUpdateObject;
    }


    // ---------------------------------------------------------------------
    // Section: Setters
    // ---------------------------------------------------------------------


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


    // ---------------------------------------------------------------------
    // Section: Variables
    // ---------------------------------------------------------------------

    private BiomesAddon addon;

    private User user;

    private World world;

    private int minX;

    private int maxX;

    private int minZ;

    private int maxZ;

    private BiomesObject biome;
}
