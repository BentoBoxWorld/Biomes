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
	 * Chunk load event that will force biome change and unload chunk if current chunk is
	 * loaded by manual process.
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

			// Update biome from minX till maxX and minZ till maxZ.
			for (int x = updateObject.getMinX(); x <= updateObject.getMaxX(); x++)
			{
				for (int z = updateObject.getMinZ(); z <= updateObject.getMaxZ(); z++)
				{
					// Change Biome
					updateObject.getWorld().setBiome(x, z, updateObject.getBiome());
				}
			}

			// If process is completed, then remove force-load status and unload
			// chunk.
			chunk.unload(true);

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
	private BiomesAddon addon;
}
