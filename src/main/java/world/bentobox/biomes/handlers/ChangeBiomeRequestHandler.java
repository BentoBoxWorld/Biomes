package world.bentobox.biomes.handlers;


import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.World;

import world.bentobox.bentobox.api.addons.request.AddonRequestHandler;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.biomes.BiomesAddon;
import world.bentobox.biomes.config.Settings;
import world.bentobox.biomes.database.objects.BiomesObject;
import world.bentobox.biomes.tasks.BiomeUpdateHelper;


/**
 * This Request Handler returns if requested inputdata can be enough to change biome.
 */
public class ChangeBiomeRequestHandler extends AddonRequestHandler
{
	/**
	 * Constructor creates a new ChangeBiomeRequestHandler instance.
	 *
	 * @param addon of type ChallengesAddon
	 */
	public ChangeBiomeRequestHandler(BiomesAddon addon)
	{
		super("biome-request-change");
		this.addon = addon;
	}


	/**
	 * @param metaData Required meta data.
	 * @return Set of strings that contains completed challenges.
	 * @see AddonRequestHandler#handle(Map<String, Object>)
	 */
	@Override
	public Object handle(Map<String, Object> metaData)
	{
		/*
            What we need in the metaData:
            	0. "player" -> UUID that represents targeted player UUID.
				1. "world-name" -> String that represents world name where biome must be changed
				2. "biomeId" -> String that represents biome unique ID.
			What you can specify more in metaData:
				0. "updateMode" -> String that represents how biome is necessary to be changed.
				1. "range" -> Integer that represents range of biome update change.
				2. "checkRequirements" -> Boolean that represents if requirements must be checked or not. By default it is true.
				3. "withdraw" -> Boolean that indicates that money will be withdraw from players account. By default it is true.
            What we will return:
            	- Map that contains:
            	  1. key "status" which is boolean that indicate if biome change was successful.
            	  2. key "reason" which is string that returns errror message.
         */


		Map<String, Object> returnMap = new HashMap<>(2);

		if (metaData == null || metaData.isEmpty())
		{
			returnMap.put("status", false);
			returnMap.put("reason", "Given MetaData map is not defined!");
		}
		else if (!metaData.containsKey("world-name") ||
			!(metaData.get("world-name") instanceof String) ||
			Bukkit.getWorld((String) metaData.get("world-name")) == null)
		{
			returnMap.put("status", false);
			returnMap.put("reason", "Missing 'world-name' or it is not valid!");
		}
		else if (!metaData.containsKey("player") ||
			!(metaData.get("player") instanceof UUID))
		{
			returnMap.put("status", false);
			returnMap.put("reason", "Missing 'player' or it is not valid!");
		}
		else if (!metaData.containsKey("biomeId") ||
			!(metaData.get("biomeId") instanceof String) ||
			this.addon.getAddonManager().getBiomeFromString((String) metaData.get("biomeId")) == null)
		{
			returnMap.put("status", false);
			returnMap.put("reason", "Missing 'biomeId' or it is not valid!");
		}
		else
		{
			World world = Bukkit.getWorld((String) metaData.get("world-name"));
			UUID player = (UUID) metaData.get("player");
			BiomesObject biome = this.addon.getAddonManager().
				getBiomeFromString((String) metaData.get("biomeId"));

			// Get Update Mode.

			Settings.UpdateMode mode = metaData.containsKey("updateMode") &&
				metaData.get("updateMode") instanceof String &&
				Settings.UpdateMode.getMode((String) metaData.get("updateMode")) != null ?
				Settings.UpdateMode.getMode((String) metaData.get("updateMode")) :
				this.addon.getSettings().getDefaultMode();

			// Get Update Range.

			int range = metaData.containsKey("range") &&
				metaData.get("range") instanceof Integer ? (int) metaData.get("range") :
				this.addon.getSettings().getDefaultSize();

			// Get Requirement Checking

			boolean checkRequirements = !metaData.containsKey("checkRequirements") ||
				!(metaData.get("checkRequirements") instanceof Boolean) ||
				(boolean) metaData.get("checkRequirements");

			// Get Withdraw value

			boolean withdraw = !metaData.containsKey("withdraw") ||
				!(metaData.get("withdraw") instanceof Boolean) ||
				(boolean) metaData.get("withdraw");

			BiomeUpdateHelper helper = new BiomeUpdateHelper(this.addon,
				User.getInstance(player),
				User.getInstance(player),
				biome,
				world,
				mode,
				range,
				withdraw);

			if (checkRequirements)
			{
				if (helper.canChangeBiome())
				{
					helper.updateIslandBiome();

					returnMap.put("status", true);
					returnMap.put("reason", "Biome is updated by checking all requirements!");
				}
				else
				{
					returnMap.put("status", false);
					returnMap.put("reason", "Player does not met requirements for biome changing!");
				}
			}
			else
			{
				helper.updateIslandBiome();

				returnMap.put("status", true);
				returnMap.put("reason", "Biome is updated by skipping all requirements!");
			}
		}

		return returnMap;
	}


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------


	/**
	 * Variable stores biomes addon.
	 */
	private BiomesAddon addon;
}
