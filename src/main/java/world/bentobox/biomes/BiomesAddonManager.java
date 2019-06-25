package world.bentobox.biomes;


import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.Database;
import world.bentobox.bentobox.util.ItemParser;
import world.bentobox.bentobox.util.Util;
import world.bentobox.biomes.database.objects.BiomesObject;
import world.bentobox.biomes.config.Settings.VisibilityMode;
import world.bentobox.biomes.panels.GuiUtils;


/**
 * This class contains all necessary thinks for Biomes Addon. It tries to manage and hold all necessary
 * information.
 */
public class BiomesAddonManager
{
	/**
	 * This is default constructor for Addon Manager.
	 * @param addon Inits addon manager.
	 */
	protected BiomesAddonManager(BiomesAddon addon)
	{
		this.addon = addon;

		this.biomesDatabase = new Database<>(addon, BiomesObject.class);
		this.biomesCacheData = new HashMap<>(Biome.values().length * 2);

		this.biomesFile = new File(this.addon.getDataFolder(), "biomes.yml");

		if (!this.biomesFile.exists())
		{
			this.addon.saveResource("biomes.yml", false);
		}

		this.load();

		// TODO: Remove this code after some time, as this is just a protective code against invalid world names.
		if (Bukkit.getBukkitVersion().startsWith("1.14"))
		{
			this.biomesCacheData.values().forEach(biomesObject -> {
				if (biomesObject.getWorld().matches(".*[A-Z]+.*"))
				{
					biomesObject.setWorld(biomesObject.getWorld().toLowerCase());
					biomesObject.setUniqueId(biomesObject.getUniqueId().toLowerCase());

					this.addon.logWarning("Biomes addon fixed your data for biome " + biomesObject.getUniqueId() + ". 1.14 does not allow to use capital letters in world names.");
				}
			});
		}
	}


// ---------------------------------------------------------------------
// Section: Loading
// ---------------------------------------------------------------------


	/**
	 * Creates biomes cache.
	 */
	private void load()
	{
		this.biomesCacheData.clear();
		this.addon.getLogger().info("Loading biomes...");

		this.biomesDatabase.loadObjects().forEach(this::loadBiomes);
	}


	/**
	 * This class reload
	 */
	public void reload()
	{
		this.addon.getLogger().info("Reloading biomes...");

		this.biomesDatabase = new Database<>(this.addon, BiomesObject.class);
		this.biomesDatabase.loadObjects().forEach(this::loadBiomes);
	}


// ---------------------------------------------------------------------
// Section: Storing
// ---------------------------------------------------------------------


	/**
	 * This method allows to store single biome object.
	 * @param biome Biome that must be saved in database.
	 */
	public void saveBiome(BiomesObject biome)
	{
		this.biomesDatabase.saveObject(biome);
	}


	/**
	 * Save biomes from cache into database
	 */
	public void save()
	{
		this.biomesCacheData.values().forEach(this.biomesDatabase::saveObject);
	}

	/**
	 * Loads biomes in cache silently. Used when loading.
	 * @param biome that must be stored.
	 * @return true if successful
	 */
	private boolean loadBiomes(BiomesObject biome)
	{
		return this.loadBiomes(biome, true, null, true);
	}


	/**
	 * Load biomes in the cache.
	 * @param biome - biome that must be stored.
	 * @param overwrite - true if previous biomes should be overwritten
	 * @param user - user making the request
	 * @param silent - if true, no messages are sent to user
	 * @return - true if imported
	 */
	public boolean loadBiomes(BiomesObject biome, boolean overwrite, User user, boolean silent)
	{
		// If biome is null, the skip this biome!
		if (biome.getBiome() == null)
		{
			if (!silent)
			{
				user.sendMessage("biomes.messages.skipping",
					"[biome]",
					biome.toString());
			}

			return false;
		}

		// Contains in array list is not fast.. but list is not so large, so it is ok there.

		if (this.biomesCacheData.containsKey(biome.getUniqueId()))
		{
			if (!overwrite)
			{
				if (!silent)
				{
					user.sendMessage("biomes.messages.skipping",
						"[biome]",
						biome.getFriendlyName());
				}

				return false;
			}
			else
			{
				if (!silent)
				{
					user.sendMessage("biomes.messages.overwriting",
						"[biome]",
						biome.getFriendlyName());
				}

				this.biomesCacheData.replace(biome.getUniqueId(), biome);
				return true;
			}
		}

		if (!silent)
		{
			user.sendMessage("biomes.messages.imported",
				"[biome]",
				biome.getFriendlyName());
		}

		this.biomesCacheData.put(biome.getUniqueId(), biome);
		return true;
	}


// ---------------------------------------------------------------------
// Section: Importing
// ---------------------------------------------------------------------


	/**
	 * This method imports biomes on first run.
	 * @param world World name in which biome must be imported.
	 */
	public void importBiomes(String world)
	{
		if (!this.biomesFile.exists())
		{
			this.addon.logError("Missing biomes.yml file!");
			return;
		}

		YamlConfiguration config = new YamlConfiguration();

		try
		{
			config.load(this.biomesFile);
		}
		catch (IOException | InvalidConfigurationException e)
		{
			this.addon.logError("Error on parsing biomes.yml file!");
			return;
		}

		ConfigurationSection reader = config.getConfigurationSection("biomes.biomesList");

		Map<String, Biome> biomeNameMap = BiomesAddonManager.getBiomeNameMap();

		int counter = 0;

		for (String biome : reader.getKeys(false))
		{
			if (biomeNameMap.containsKey(biome.toUpperCase()))
			{
				BiomesObject newBiomeObject = new BiomesObject(biomeNameMap.get(biome.toUpperCase()), world);
				newBiomeObject.setDeployed(true);

				ConfigurationSection details = reader.getConfigurationSection(biome);

				newBiomeObject.setFriendlyName(details.getString("friendlyName", biome));

				newBiomeObject.setDescription(
					GuiUtils.stringSplit(details.getString("description", ""),
						this.addon.getSettings().getLoreLineLength()));
				newBiomeObject.setIcon(ItemParser.parse(details.getString("icon") + ":1"));

				newBiomeObject.setRequiredLevel(details.getInt("islandLevel", 0));
				newBiomeObject.setRequiredCost(details.getInt("cost", 0));

				List<String> permissions = details.getStringList("permission");

				if (permissions == null || permissions.isEmpty())
				{
					newBiomeObject.setRequiredPermissions(Collections.emptySet());
				}
				else
				{
					newBiomeObject.setRequiredPermissions(new HashSet<>(permissions));
				}

				this.biomesCacheData.put(newBiomeObject.getUniqueId(), newBiomeObject);
				counter++;
			}
		}

		this.addon.log("Imported " + counter + " biomes into " + world);
		this.save();
	}


	/**
	 * This method imports biomes
	 *
	 * @param user - user
	 * @param world - world to import into
	 * @param overwrite - true if previous ones should be overwritten
	 * @return true if successful
	 */
	public boolean importBiomes(User user, World world, boolean overwrite)
	{
		return this.importBiomes(user, Util.getWorld(world).getName(), overwrite);
	}


	/**
	 * This method imports biomes
	 *
	 * @param user - user
	 * @param world - world to import into
	 * @param overwrite - true if previous ones should be overwritten
	 * @return true if successful
	 */
	public boolean importBiomes(User user, String world, boolean overwrite)
	{
		if (!this.biomesFile.exists())
		{
			user.sendMessage("biomes.errors.no-file");
			return false;
		}

		YamlConfiguration config = new YamlConfiguration();

		try
		{
			config.load(this.biomesFile);
		}
		catch (IOException | InvalidConfigurationException e)
		{
			user.sendMessage("biomes.errors.no-load",
				"[message]",
				e.getMessage());
			return false;
		}

		this.readBiomes(config, user, world, overwrite);

		// Update biome order.
		this.addon.getAddonManager().save();
		return true;
	}


	/**
	 * This method creates biomes object from config file.
	 * @param config YamlConfiguration that contains all biomes.
	 * @param user User who calls reading.
	 * @param world World in which biomes must be imported
	 * @param overwrite Boolean that indicate if biomes should be overwritted.
	 */
	private void readBiomes(YamlConfiguration config, User user, String world, boolean overwrite)
	{
		int size = 0;

		ConfigurationSection reader = config.getConfigurationSection("biomes.biomesList");

		Map<String, Biome> biomeNameMap = BiomesAddonManager.getBiomeNameMap();

		for (String biome : reader.getKeys(false))
		{
			if (biomeNameMap.containsKey(biome.toUpperCase()))
			{
				BiomesObject newBiomeObject = new BiomesObject(Biome.valueOf(biome.toUpperCase()), world);
				newBiomeObject.setDeployed(true);

				ConfigurationSection details = reader.getConfigurationSection(biome);

				newBiomeObject.setFriendlyName(details.getString("friendlyName", biome));

				newBiomeObject.setDescription(
					GuiUtils.stringSplit(details.getString("description", ""),
						this.addon.getSettings().getLoreLineLength()));
				newBiomeObject.setIcon(ItemParser.parse(details.getString("icon") + ":1"));

				newBiomeObject.setRequiredLevel(details.getInt("islandLevel", 0));
				newBiomeObject.setRequiredCost(details.getInt("cost", 0));

				List<String> permissions = details.getStringList("permission");

				if (permissions == null || permissions.isEmpty())
				{
					newBiomeObject.setRequiredPermissions(Collections.emptySet());
				}
				else
				{
					newBiomeObject.setRequiredPermissions(new HashSet<>(permissions));
				}

				if (this.addon.getAddonManager().loadBiomes(
					newBiomeObject, overwrite, user, false))
				{
					size++;
				}
			}
			else
			{
				user.sendMessage("biomes.errors.load-biome",
					"[biome]",
					biome);
			}
		}

		user.sendMessage("biomes.messages.import-count",
			"[number]",
			String.valueOf(size));
	}


// ---------------------------------------------------------------------
// Section: Creating
// ---------------------------------------------------------------------


	/**
	 * This method creates and returns new biome with given uniqueID.
	 * @param uniqueID - new ID for challenge.
	 * @param worldName - world name where biome operates.
	 * @return biome that is currently created.
	 */
	public BiomesObject createBiome(String uniqueID, String worldName)
	{
		if (!this.containsBiome(uniqueID))
		{
			BiomesObject biome = new BiomesObject();
			biome.setUniqueId(uniqueID);

			// Sets default biome as VOID.
			biome.setBiome(Biome.THE_VOID);
			biome.setWorld(worldName);

			this.saveBiome(biome);
			this.loadBiomes(biome);

			return biome;
		}
		else
		{
			return null;
		}
	}


// ---------------------------------------------------------------------
// Section: Getters / Setters
// ---------------------------------------------------------------------


	/**
	 * This method returns biomes that is visible for user by using default visibility mode.
	 * @param world World in which biomes must be returned.
	 * @param user User who will see biomes.
	 * @return Visible biome list.
	 */
	public List<BiomesObject> getBiomes(World world, User user)
	{
		return this.getBiomes(world, user, this.addon.getSettings().getVisibilityMode());
	}


	/**
	 * This method returns biomes that is visible for user in given world.
	 * @param world World in which biomes must be returned.
	 * @param user User who will see biomes.
	 * @param visibilityMode active visibilityMode. Only ALL will return all biomes.
	 * DEPLOYED will show biomes that has deployedFlag enabled.
	 * ACCESSIBLE will show biomes that are deployed and user has permission that unlocks biome.
	 * TOGGLEABLE will work as ACCESSIBLE.
	 * @return Visible biome list.
	 */
	public List<BiomesObject> getBiomes(World world, User user, VisibilityMode visibilityMode)
	{
		List<BiomesObject> allBiomeList = this.getBiomes(world);

		if (visibilityMode.equals(VisibilityMode.ALL))
		{
			return allBiomeList;
		}

		List<BiomesObject> returnBiomesList = new ArrayList<>(allBiomeList.size());

		allBiomeList.forEach(biomesObject -> {
			if (biomesObject.isDeployed() &&
				(visibilityMode.equals(VisibilityMode.DEPLOYED) ||
					biomesObject.getRequiredPermissions().isEmpty() ||
					biomesObject.getRequiredPermissions().stream().allMatch(user::hasPermission)))
			{
				returnBiomesList.add(biomesObject);
			}
		});

		return returnBiomesList;
	}


	/**
	 * This method returns list with loaded biomes for given world.
	 * @param world World where biome operates.
	 * @return list with loaded biomes.
	 */
	public List<BiomesObject> getBiomes(World world)
	{
		world = Util.getWorld(world);

		return world == null ? Collections.emptyList() : this.getBiomes(world.getName());
	}


	/**
	 * This method returns list with loaded biomes for given world.
	 * @param worldName Name of world where biome operates.
	 * @return list with loaded biomes.
	 */
	public List<BiomesObject> getBiomes(String worldName)
	{
		return this.biomesCacheData.values().stream().
			sorted(BiomesObject::compareTo).
			filter(biome -> biome.getWorld().equals(worldName)).
			collect(Collectors.toList());
	}


	/**
	 * This method returns biome object that hides behind biome name or null, if biome
	 * with name does not exist.
	 * @param biomeUniqueID Biome's name.
	 * @return BiomesObject that is represented by biome string.
	 */
	public BiomesObject getBiomeFromString(String biomeUniqueID)
	{
		return this.biomesCacheData.getOrDefault(biomeUniqueID, null);
	}


	/**
	 * Check if a biome exists - case insensitive
	 *
	 * @param name - name of biome
	 * @return true if it exists, otherwise false
	 */
	public boolean containsBiome(String name)
	{
		if (this.biomesCacheData.containsKey(name))
		{
			return true;
		}
		else
		{
			// check database.
			if (this.biomesDatabase.objectExists(name))
			{
				BiomesObject biome = this.biomesDatabase.loadObject(name);
				this.biomesCacheData.put(name, biome);
				return true;
			}
		}

		return false;
	}


	/**
	 * Given method removes biome from database and cache.
	 * @param biome Biome that must be removed.
	 */
	public void removeBiome(BiomesObject biome)
	{
		if (this.biomesCacheData.containsKey(biome.getUniqueId()))
		{
			this.biomesCacheData.remove(biome.getUniqueId());
			this.biomesDatabase.deleteObject(biome);
		}
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
	 * This method returns if in given world biomes are setup.
	 * @param world World that must be checked.
	 * @return True if in given world exist biomes.
	 */
	public boolean hasAnyBiome(World world)
	{
		String worldName = Util.getWorld(world) == null ? "" : Util.getWorld(world).getName();

		return !worldName.isEmpty() &&
			this.biomesCacheData.values().stream().anyMatch(biome -> biome.getWorld().equals(worldName));
	}


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------

	/**
	 * Variable current addon.
	 */
	private BiomesAddon addon;

	/**
	 * Variable stores map that links String to loaded biomes object.
	 */
	private Map<String, BiomesObject> biomesCacheData;

	/**
	 * Variable stores database of biomes objects.
	 */
	private Database<BiomesObject> biomesDatabase;

	/**
	 * Variable stores biomes.yml location
	 */
	private File biomesFile;
}
