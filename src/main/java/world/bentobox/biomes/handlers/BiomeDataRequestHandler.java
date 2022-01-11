package world.bentobox.biomes.handlers;


import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import world.bentobox.bentobox.api.addons.request.AddonRequestHandler;
import world.bentobox.biomes.BiomesAddon;
import world.bentobox.biomes.database.objects.BiomesObject;


/**
 * This handler returns data for requested challenge.
 */
public class BiomeDataRequestHandler extends AddonRequestHandler
{

	private static final String BIOME_ID = "biomeId";


    /**
	 * Constructor creates a new BiomeDataRequestHandler instance.
	 *
	 * @param addon of type BiomesAddon
	 */
	public BiomeDataRequestHandler(BiomesAddon addon)
	{
		super("biome-data");
		this.addon = addon;
	}


	/**
	 * @param metaData Required meta data.
	 * @return Map that returns information about biome
	 * @see AddonRequestHandler#handle(Map &lt;String, Object&gt;)
	 */
	@Override
	public Object handle(Map<String, Object> metaData)
	{
		/*
            What we need in the metaData:
				0. "biomeId" -> String
            What we will return:
				- Empty Map if biome is not given or not found
				- Map that contains information about given biome:
					- uniqueId: the same id that was passed to this handler.
					- world: string that represents world name where biome operates.
					- biome: string that represents Minecraft Biome name.

			 		- name: String object of display name for biome.
					- deployed: boolean object of deployment status.
			 		- description: List of strings that represents biomes description.

			 		- icon: ItemStack object that represents biome.
			 		- order: Integer object of order number for given biome.

					- cost: Integer that represents biomes change cost.
			 		- level: Long that represents minimal island level for this biome to work.
			 		- permissions: Set of strings that represents required permissions.
         */

		if (metaData == null ||
			metaData.isEmpty() ||
			metaData.get(BIOME_ID) == null ||
			!(metaData.get(BIOME_ID) instanceof String))
		{
			return Collections.emptyMap();
		}

		BiomesObject biome =
			this.addon.getAddonManager().getBiomeByID((String) metaData.get(BIOME_ID));

		Map<String, Object> biomesDataMap;

		if (biome == null)
		{
			biomesDataMap = Collections.emptyMap();
		}
		else
		{
			biomesDataMap = new HashMap<>();

			biomesDataMap.put("uniqueId", biome.getUniqueId());
			biomesDataMap.put("world", biome.getWorld());
			biomesDataMap.put("biome", biome.getBiome().name());

			biomesDataMap.put("name", biome.getFriendlyName());
			biomesDataMap.put("description", biome.getDescription());
			biomesDataMap.put("deployed", biome.isDeployed());

			biomesDataMap.put("icon", biome.getIcon());
			biomesDataMap.put("order", biome.getOrder());

			biomesDataMap.put("cost", biome.getUnlockCost());
			biomesDataMap.put("level", biome.getUnlockLevel());
			biomesDataMap.put("permissions", biome.getUnlockPermissions());
		}

		return biomesDataMap;
	}


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------


	/**
	 * Variable stores biomes addon.
	 */
	private BiomesAddon addon;
}
