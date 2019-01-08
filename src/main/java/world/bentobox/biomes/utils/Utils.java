package world.bentobox.biomes.utils;


import org.apache.commons.lang.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.inventory.ItemStack;
import java.util.*;

import world.bentobox.biomes.BiomesAddon;
import world.bentobox.biomes.objects.BiomesObject;
import world.bentobox.biomes.objects.Settings.UpdateMode;


/**
 * Utils class that contains useful methods.
 */
public class Utils
{
	/**
	 * Thus method parses input string to ItemStack.
	 * @param inputString Splitted string.
	 * @return ItemStack that represents input string.
	 */
	private static ItemStack parse2ArrayString(String[] inputString)
	{
		int reqAmount;

		try
		{
			reqAmount = Integer.parseInt(inputString[1]);
		}
		catch (Exception e)
		{
			return null;
		}

		Material reqItem = Material.getMaterial(inputString[0].toUpperCase() + "_ITEM");

		if (reqItem == null)
		{
			reqItem = Material.getMaterial(inputString[0].toUpperCase());
		}

		if (reqItem == null)
		{
			return null;
		}

		return new ItemStack(reqItem, reqAmount);
	}


	/**
	 * Create ItemStack from 3 string parts.
	 * @param inputString Splitted string.
	 * @return ItemStack that is created from input string.
	 */
	private static ItemStack parse3ArrayString(String[] inputString)
	{
		String[] twoArrayString = {inputString[0], inputString[2]};

		return Utils.parse2ArrayString(twoArrayString);
	}

	/**
	 * This method parse given string to ItemStack element.
	 * @return the parsed ItemStack element.
	 */
	public static ItemStack parseItem(BiomesAddon addon, String inputString)
	{
		String[] part = inputString.split(":");

		ItemStack itemStack;

		if (part.length == 2)
		{
			itemStack = Utils.parse2ArrayString(part);
		}
		else if (part.length == 3)
		{
			itemStack = Utils.parse3ArrayString(part);
		}
		else
		{
			itemStack = null;
		}

		if (itemStack == null)
		{
			addon.getLogger().severe(() -> "Problem with " + inputString + " in config.yml!");
		}

		return itemStack;
	}


	/**
	 * This method splits input string in multiple string lists.
	 * @param string String that must be splitted.
	 * @return List of splited strings.
	 */
	public static List<String> splitString(String string)
	{
		string = ChatColor.translateAlternateColorCodes('&', string);

		List<String> result = new ArrayList<>();
		Arrays.asList(string.split("\\|")).forEach(
			line -> result.addAll(Arrays.asList(WordUtils.wrap(line, 25).split("\\n"))));

		return result;
	}


	/**
	 * This method combines input string array in single string.
	 * @param args String list that must be combined.
	 * @return Combined string.
	 */
	public static String mergeStringList(List<String> args)
	{
		if (args.isEmpty())
		{
			return "";
		}

		Iterator<String> iterator = args.iterator();

		StringBuilder builder = new StringBuilder();
		builder.append(iterator.next());

		while (iterator.hasNext())
		{
			builder.append(" ");
			builder.append(iterator.next());
		}

		return builder.toString();
	}


	/**
	 * This method parses BiomesObject to necessary Biome.
	 * @param biomesObject BiomesObject.
	 * @return Biome that is represented by BiomesObject.
	 */
	public static Biome parseBiome(BiomesObject biomesObject)
	{
		int id = biomesObject.getBiomeID();

		if (id < 0 || Biome.values().length < id)
		{
			return null;
		}

		return Biome.values()[id];
	}


	/**
	 * This method parses input string to valid list of environments.
	 * @param environment Input string.
	 * @return List with valid world environments.
	 */
	public static List<World.Environment> parseEnvironments(String environment)
	{
		List<World.Environment> returnList = new ArrayList<>(3);

		String[] split = environment.split(":");

		for (String s : split)
		{
			if (s.toUpperCase().equals(World.Environment.NORMAL.name()))
			{
				returnList.add(World.Environment.NORMAL);
			}
			else if (s.toUpperCase().equals(World.Environment.NETHER.name()))
			{
				returnList.add(World.Environment.NETHER);
			}
			else if (s.toUpperCase().equals(World.Environment.THE_END.name()))
			{
				returnList.add(World.Environment.THE_END);
			}
		}

		return returnList;
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
	 * This method parse default input string type to corresponding update mode.
	 * @return Default Update mode.
	 */
	public static UpdateMode parseStringToUpdateMode(String type)
	{
		switch (type.toUpperCase())
		{
			case "ISLAND":
				return UpdateMode.ISLAND;
			case "CHUNK":
				return UpdateMode.CHUNK;
			case "SQUARE":
				return UpdateMode.SQUARE;
			default:
				return UpdateMode.ISLAND;
		}
	}


	/**
	 * This method parse default input string type to corresponding update mode.
	 * @return Update mode.
	 */
	public static UpdateMode parseStrictToUpdateMode(String type)
	{
		switch (type.toUpperCase())
		{
			case "ISLAND":
				return UpdateMode.ISLAND;
			case "CHUNK":
				return UpdateMode.CHUNK;
			case "SQUARE":
				return UpdateMode.SQUARE;
			default:
				return null;
		}
	}
}
