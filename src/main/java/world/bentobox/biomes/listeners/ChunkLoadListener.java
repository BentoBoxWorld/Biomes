package world.bentobox.biomes.listeners;


import org.bukkit.Chunk;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;

import world.bentobox.biomes.BiomesAddon;
import world.bentobox.biomes.database.objects.BiomeChunkUpdateObject;


/**
 * This listener fires on chunk loading. If given chunk has pending biome updating, then
 * this process will change biome in it.
 */
public class ChunkLoadListener implements Listener
{
    /**
     * Default constructor that inits listener.
     * @param addon BiomeAddon object.
     */
    public ChunkLoadListener(BiomesAddon addon)
    {
        this.addon = addon;
    }


    /**
     * Chunk load event that will change the biome if required when a chunk is
     * loaded.
     * @param event ChunkLoadEvent object.
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onChunkLoad(ChunkLoadEvent event)
    {
        Chunk chunk = event.getChunk();

        BiomeChunkUpdateObject updateObject = this.addon.getAddonManager().
            getPendingChunkUpdateObject(chunk.getWorld(), chunk.getX(), chunk.getZ());

        if (updateObject != null)
        {
            if (updateObject.getMaxX() - updateObject.getMinX() > 15 ||
                (updateObject.getMinX() >> 4) != chunk.getX() ||
                (updateObject.getMaxX() >> 4) != chunk.getX())
            {
                // Something is wrong with X coordinates. Change to whole chunk.
                updateObject.setMinX(updateObject.getChunkX() << 4);
                updateObject.setMaxX(updateObject.getChunkX() << 4 + 15);
            }

            if (updateObject.getMaxZ() - updateObject.getMinZ() > 15 ||
                (updateObject.getMinZ() >> 4) != chunk.getZ() ||
                (updateObject.getMaxZ() >> 4) != chunk.getZ())
            {
                // Something is wrong with Z coordinates. Change to whole chunk.
                updateObject.setMinZ(updateObject.getChunkZ() << 4);
                updateObject.setMaxZ(updateObject.getChunkZ() << 4 + 15);
            }

            // Update biome from minX till maxX and minZ till maxZ, and minY till maxY.
            // Every 4th block must be updated as biome are stored in 4x4x4 cubes.

            for (int x = updateObject.getMinX(); x <= updateObject.getMaxX(); x += 4)
            {
                for (int z = updateObject.getMinZ(); z <= updateObject.getMaxZ(); z += 4)
                {
                    for (int y = updateObject.getMinY(); y <= updateObject.getMaxY(); y += 4)
                    {
                        // Biome should not be changed in Greenhouses.
                        if (!this.addon.getAddonManager().
                            hasGreenhouseInLocation(updateObject.getWorld(), x, y, z))
                        {
                            // Change Biome
                            updateObject.getWorld().setBiome(x, y, z, updateObject.getBiome());
                        }
                    }
                }
            }

            // Remove ChunkUpdateObject.
            this.addon.getAddonManager().removeUpdateObject(updateObject);
        }
    }


    // ---------------------------------------------------------------------
    // Section: Variables
    // ---------------------------------------------------------------------


    /**
     * BiomeAddon variable.
     */
    private final BiomesAddon addon;
}
