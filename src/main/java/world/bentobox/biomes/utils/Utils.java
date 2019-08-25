package world.bentobox.biomes.utils;


import org.bukkit.World;

import world.bentobox.bentobox.BentoBox;


/**
 * Util methods used in different situations.
 */
public class Utils
{
	/**
	 * This method transforms given World into GameMode name. If world is not a GameMode
	 * world then it returns null.
	 * @param world World which gameMode name must be found out.
	 * @return GameMode name or null.
	 */
	public static String getGameMode(World world)
	{
		return BentoBox.getInstance().getIWM().getAddon(world).
			map(gameModeAddon -> gameModeAddon.getDescription().getName()).
			orElse(null);
	}
}
