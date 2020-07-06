package world.bentobox.biomes.utils;


import org.bukkit.World;
import org.bukkit.block.Biome;

import java.util.*;

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


	/**
	 * This method allows to get next value from array list after given value.
	 * @param values Array that should be searched for given value.
	 * @param currentValue Value which next element should be found.
	 * @param <T> Instance of given object.
	 * @return Next value after currentValue in values array.
	 */
	public static <T extends Object> T getNextValue(T[] values, T currentValue)
	{
		for (int i = 0; i < values.length; i++)
		{
			if (values[i].equals(currentValue))
			{
				if (i + 1 == values.length)
				{
					return values[0];
				}
				else
				{
					return values[i + 1];
				}
			}
		}

		return currentValue;
	}


	/**
	 * This method allows to get previous value from array list after given value.
	 * @param values Array that should be searched for given value.
	 * @param currentValue Value which previous element should be found.
	 * @param <T> Instance of given object.
	 * @return Previous value before currentValue in values array.
	 */
	public static <T extends Object> T getPreviousValue(T[] values, T currentValue)
	{
		for (int i = 0; i < values.length; i++)
		{
			if (values[i].equals(currentValue))
			{
				if (i > 0)
				{
					return values[i - 1];
				}
				else
				{
					return values[values.length - 1];
				}
			}
		}

		return currentValue;
	}


	/**
	 * This method returns map that contains biomes name as key and biome as value.
	 * @return Map that contains relation from biome name to biome.
	 */
	public static Map<String, Biome> getBiomeNameMap()
	{
		Biome[] biomes = Biome.values();

		Map<String, Biome> returnMap = new HashMap<>(biomes.length);

		for (Biome biome : biomes)
		{
			returnMap.put(biome.name(), biome);
		}

		return returnMap;
	}


	/**
	 * This method returns if current biome is locally detected as snowy biome.
	 * @param biome Biome that must be checked.
	 * @return {@code true} if I think it is snowy biome, {@code false} otherwise.
	 */
	public static boolean isSnowyBiome(Biome biome)
	{
		switch (biome)
		{
			case SNOWY_TUNDRA:
			case ICE_SPIKES:
			case SNOWY_TAIGA:
			case SNOWY_TAIGA_MOUNTAINS:
			case FROZEN_RIVER:
			case SNOWY_BEACH:
				return true;
			default:
				return false;
		}
	}


	/**
	 * This method returns if current biome is locally detected as cold biome.
	 * @param biome Biome that must be checked.
	 * @return {@code true} if I think it is cold biome, {@code false} otherwise.
	 */
	public static boolean isColdBiome(Biome biome)
	{
		switch (biome)
		{
			case MOUNTAINS:
			case GRAVELLY_MOUNTAINS:
			case WOODED_MOUNTAINS:
			case MODIFIED_GRAVELLY_MOUNTAINS:
			case TAIGA:
			case TAIGA_MOUNTAINS:
			case GIANT_TREE_TAIGA:
			case GIANT_SPRUCE_TAIGA:
			case STONE_SHORE:
				return true;
			default:
				return false;
		}
	}


	/**
	 * This method returns if current biome is locally detected as lush biome.
	 * @param biome Biome that must be checked.
	 * @return {@code true} if I think it is lush biome, {@code false} otherwise.
	 */
	public static boolean isLushBiome(Biome biome)
	{
		switch (biome)
		{
			case PLAINS:
			case SUNFLOWER_PLAINS:
			case FOREST:
			case FLOWER_FOREST:
			case BIRCH_FOREST:
			case TALL_BIRCH_FOREST:
			case DARK_FOREST:
			case DARK_FOREST_HILLS:
			case SWAMP:
			case SWAMP_HILLS:
			case JUNGLE:
			case MODIFIED_JUNGLE:
			case JUNGLE_EDGE:
			case MODIFIED_JUNGLE_EDGE:
			case BAMBOO_JUNGLE:
			case RIVER:
			case BEACH:
			case MUSHROOM_FIELDS:
			case MUSHROOM_FIELD_SHORE:
				return true;
			default:
				return false;
		}
	}


	/**
	 * This method returns if current biome is locally detected as dry biome.
	 * @param biome Biome that must be checked.
	 * @return {@code true} if I think it is dry biome, {@code false} otherwise.
	 */
	public static boolean isDryBiome(Biome biome)
	{
		switch (biome)
		{
			case DESERT:
			case DESERT_LAKES:
			case SAVANNA:
			case SHATTERED_SAVANNA:
			case BADLANDS:
			case ERODED_BADLANDS:
			case WOODED_BADLANDS_PLATEAU:
			case MODIFIED_WOODED_BADLANDS_PLATEAU:
			case BADLANDS_PLATEAU:
			case SAVANNA_PLATEAU:
			case MODIFIED_BADLANDS_PLATEAU:
			case SHATTERED_SAVANNA_PLATEAU:
				return true;
			default:
				return false;
		}
	}


	/**
	 * This method returns if current biome is locally detected as ocean biome.
	 * @param biome Biome that must be checked.
	 * @return {@code true} if I think it is ocean biome, {@code false} otherwise.
	 */
	public static boolean isOceanBiome(Biome biome)
	{
		switch (biome)
		{
			case WARM_OCEAN:
			case LUKEWARM_OCEAN:
			case DEEP_LUKEWARM_OCEAN:
			case OCEAN:
			case DEEP_OCEAN:
			case COLD_OCEAN:
			case DEEP_COLD_OCEAN:
			case FROZEN_OCEAN:
			case DEEP_FROZEN_OCEAN:
				return true;
			default:
				return false;
		}
	}


	/**
	 * This method returns if current biome is locally detected as neutral biome.
	 * @param biome Biome that must be checked.
	 * @return {@code true} if I think it is neutral biome, {@code false} otherwise.
	 */
	public static boolean isNeutralBiome(Biome biome)
	{
		switch (biome)
		{
			case THE_VOID:
			case WOODED_HILLS:
			case TAIGA_HILLS:
			case SNOWY_TAIGA_HILLS:
			case JUNGLE_HILLS:
			case DESERT_HILLS:
			case BIRCH_FOREST_HILLS:
			case TALL_BIRCH_HILLS:
			case GIANT_TREE_TAIGA_HILLS:
			case GIANT_SPRUCE_TAIGA_HILLS:
			case SNOWY_MOUNTAINS:
				return true;
			default:
				return false;
		}
	}


	/**
	 * This method returns if current biome is locally detected as unused biome.
	 * @param biome Biome that must be checked.
	 * @return {@code true} if I think it is unused biome, {@code false} otherwise.
	 */
	public static boolean isUnusedBiome(Biome biome)
	{
		switch (biome)
		{
			case MOUNTAIN_EDGE:
			case DEEP_WARM_OCEAN:
				return true;
			default:
				return false;
		}
	}


	/**
	 * This method returns if current biome is locally detected as nether biome.
	 * @param biome Biome that must be checked.
	 * @return {@code true} if I think it is nether biome, {@code false} otherwise.
	 */
	public static boolean isNetherBiome(Biome biome)
	{
		// TODO: 1.15.2 support.
		switch (biome.name())
		{
			case "NETHER":
			case "NETHER_WASTES":
			case "SOUL_SAND_VALLEY":
			case "CRIMSON_FOREST":
			case "WARPED_FOREST":
			case "BASALT_DELTAS":
				return true;
			default:
				return false;
		}
	}


	/**
	 * This method returns if current biome is locally detected as the end biome.
	 * @param biome Biome that must be checked.
	 * @return {@code true} if I think it is the end biome, {@code false} otherwise.
	 */
	public static boolean isTheEndBiome(Biome biome)
	{
		switch (biome)
		{
			case THE_END:
			case SMALL_END_ISLANDS:
			case END_MIDLANDS:
			case END_HIGHLANDS:
			case END_BARRENS:
				return true;
			default:
				return false;
		}
	}
}
