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


	/**
	 * This method normalizes given coordinate to factor 4.
	 * In Minecraft 1.15 and 1.16, biomes are stored in 4x4x4 blocks.
	 * @param coordinate Input coordinate.
	 * @return Normalized coordinate.
	 */
	public static int normalizeBy4(int coordinate)
	{
		return (coordinate >> 2) << 2;
	}
}
