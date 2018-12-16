package world.bentobox.addons.biomes.tasks;


import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.scheduler.BukkitRunnable;

import world.bentobox.addons.biomes.objects.BiomesObject;
import world.bentobox.addons.biomes.utils.Utils;
import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.user.User;


/**
 * This class updates biome from min coordinate till max coordinate.
 */
public class BiomeUpdateTask extends BukkitRunnable
{
	public BiomeUpdateTask(User user, World world, int minX, int maxX, int minZ, int maxZ, BiomesObject biome)
	{
		this.user = user;
		this.world = world;
		this.minX = minX > maxX ? maxX : minX;
		this.maxX = minX < maxX ? maxX : minX;
		this.minZ = minZ > maxZ ? maxZ : minZ;
		this.maxZ = minZ < maxZ ? maxZ : minZ;

		this.biome = biome;
	}


	@Override
	public void run()
	{
		this.user.sendMessage("biomes.update.start");

		// Update world coordinates with new biomes.

		Biome newBiome = Utils.parseBiome(this.biome);

		for (int x = this.minX; x <= this.maxX; x++)
		{
			for (int z = this.minZ; z <= this.maxZ; z++)
			{
				this.world.setBiome(x, z, newBiome);
			}
		}

		this.user.sendMessage("biomes.update.done",
			TextVariables.LABEL,
			this.biome.getFriendlyName());
	}


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------

	private User user;

	private World world;

	private int minX;

	private int maxX;

	private int minZ;

	private int maxZ;

	private BiomesObject biome;
}
