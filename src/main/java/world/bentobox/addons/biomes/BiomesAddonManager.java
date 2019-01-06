package world.bentobox.addons.biomes;


import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.IOException;
import java.util.*;

import world.bentobox.addons.biomes.objects.BiomesObject;
import world.bentobox.addons.biomes.utils.Utils;
import world.bentobox.addons.biomes.utils.Utils.VisibilityMode;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.Database;
import world.bentobox.bentobox.util.Util;


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

		// Currently only 2 game modes.
		this.worldBiomeList = new HashMap<>(2);
		this.biomesMap = new HashMap<>(Biome.values().length * 2);

		this.biomesFile = new File(this.addon.getDataFolder(), "biomes.yml");

		if (!this.biomesFile.exists())
		{
			this.addon.saveResource("biomes.yml", false);
		}

		this.initBiomes();
	}


// ---------------------------------------------------------------------
// Section: Loading
// ---------------------------------------------------------------------


	/**
	 * Creates biomes cache.
	 */
	private void initBiomes()
	{
		this.worldBiomeList.clear();
		this.addon.getLogger().info("Loading biomes...");

		this.biomesDatabase.loadObjects().forEach(this::storeBiome);

		for (Map.Entry<String, List<BiomesObject>> entry : this.worldBiomeList.entrySet())
		{
			entry.getValue().sort(Comparator.comparingInt(BiomesObject::getBiomeID));
		}
	}


	/**
	 * This class reload
	 */
	public void reloadManager()
	{
		this.addon.getLogger().info("Reloading biomes...");

		this.biomesDatabase = new Database<>(this.addon, BiomesObject.class);
		this.biomesDatabase.loadObjects().forEach(this::storeBiome);

		for (Map.Entry<String, List<BiomesObject>> entry : this.worldBiomeList.entrySet())
		{
			entry.getValue().sort(Comparator.comparingInt(BiomesObject::getBiomeID));
		}
	}


// ---------------------------------------------------------------------
// Section: Storing
// ---------------------------------------------------------------------


	/**
	 * This method allows to store single biome object.
	 * @param biome Biome that must be stored.
	 */
	public void saveBiome(BiomesObject biome)
	{
		this.biomesDatabase.saveObject(biome);
	}


	/**
	 * Save configs and biomes data
	 */
	private void save()
	{
		for (Map.Entry<String, List<BiomesObject>> entry : this.worldBiomeList.entrySet())
		{
			entry.getValue().forEach(this.biomesDatabase::saveObject);
		}
	}


	/**
	 * Save to the database
	 * @param async - if true, saving will be done async
	 */
	public void save(boolean async)
	{
		if (async)
		{
			this.addon.getServer().getScheduler().
				runTaskAsynchronously(this.addon.getPlugin(), bukkitTask -> BiomesAddonManager.this.save());
		}
		else
		{
			this.save();
		}
	}


	/**
	 * Store biomes silently. Used when loading.
	 * @param biome that must be stored.
	 * @return true if successful
	 */
	private boolean storeBiome(BiomesObject biome)
	{
		return this.storeBiome(biome, true, null, true);
	}


	/**
	 * Stores the biomes.
	 * @param biome - biome that must be stored.
	 * @param overwrite - true if previous biomes should be overwritten
	 * @param user - user making the request
	 * @param silent - if true, no messages are sent to user
	 * @return - true if imported
	 */
	public boolean storeBiome(BiomesObject biome, boolean overwrite, User user, boolean silent)
	{
		// Contains in array list is not fast.. but list is not so large, so it is ok there.

		if (this.biomesMap.containsKey(biome.getUniqueId()))
		{
			if (!overwrite)
			{
				if (!silent)
				{
					user.sendMessage("biomes.messages.warnings.skipping",
						"[biome]",
						biome.getFriendlyName());
				}

				return false;
			}
			else
			{
				if (!silent)
				{
					user.sendMessage("biomes.messages.warnings.overwriting",
						"[biome]",
						biome.getFriendlyName());
				}

				this.biomesMap.replace(biome.getUniqueId(), biome);
				List<BiomesObject> biomesList = this.worldBiomeList.get(biome.getWorld());
				// Assert INDEX out of bounds. If happens then issue is in biome loading not here!
				biomesList.set(biomesList.indexOf(biome), biome);

				return true;
			}
		}

		if (!silent)
		{
			user.sendMessage("biomes.messages.information.imported",
				"[biome]",
				biome.getFriendlyName());
		}

		this.biomesMap.put(biome.getUniqueId(), biome);
		this.worldBiomeList.computeIfAbsent(biome.getWorld(),
			s -> new ArrayList<>(Biome.values().length)).add(biome);

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

		Map<String, Biome> biomeNameMap = Utils.getBiomeNameMap();

		for (String biome : reader.getKeys(false))
		{
			if (biomeNameMap.containsKey(biome.toUpperCase()))
			{
				BiomesObject newBiomeObject = new BiomesObject(biomeNameMap.get(biome.toUpperCase()), world);
				newBiomeObject.setDeployed(true);

				ConfigurationSection details = reader.getConfigurationSection(biome);

				newBiomeObject.setFriendlyName(details.getString("friendlyName", biome));

				newBiomeObject.setDescription(
					Utils.splitString(details.getString("description", "")));
				newBiomeObject.setIcon(
					Utils.parseItem(this.addon, details.getString("icon") + ":1"));

				newBiomeObject.setRequiredLevel(details.getInt("islandLevel", 0));
				newBiomeObject.setRequiredCost(details.getInt("cost", 0));
				newBiomeObject.setPermission(details.getString("permission", ""));

				this.biomesMap.put(newBiomeObject.getUniqueId(), newBiomeObject);
				this.worldBiomeList.computeIfAbsent(newBiomeObject.getWorld(),
					s -> new ArrayList<>(Biome.values().length)).add(newBiomeObject);
			}
		}

		this.addon.log("Imported " + this.worldBiomeList.get(world).size() + " biomes into " + world);

		this.worldBiomeList.get(world).sort(Comparator.comparingInt(BiomesObject::getBiomeID));
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
			user.sendMessage("biomes.messages.errors.no-file");
			return false;
		}

		YamlConfiguration config = new YamlConfiguration();

		try
		{
			config.load(this.biomesFile);
		}
		catch (IOException | InvalidConfigurationException e)
		{
			user.sendMessage("biomes.messages.errors.no-load",
				"[message]",
				e.getMessage());
			return false;
		}

		this.readBiomes(config, user, world, overwrite);

		// Update biome order.
		this.worldBiomeList.get(world).sort(Comparator.comparingInt(BiomesObject::getBiomeID));
		this.addon.getAddonManager().save(false);
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

		Map<String, Biome> biomeNameMap = Utils.getBiomeNameMap();

		for (String biome : reader.getKeys(false))
		{
			if (biomeNameMap.containsKey(biome.toUpperCase()))
			{
				BiomesObject newBiomeObject = new BiomesObject(biomeNameMap.get(biome.toUpperCase()), world);
				newBiomeObject.setDeployed(true);

				ConfigurationSection details = reader.getConfigurationSection(biome);

				newBiomeObject.setFriendlyName(details.getString("friendlyName", biome));

				newBiomeObject.setDescription(
					Utils.splitString(details.getString("description", "")));
				newBiomeObject.setIcon(
					Utils.parseItem(this.addon, details.getString("icon") + ":1"));

				newBiomeObject.setRequiredLevel(details.getInt("islandLevel", 0));
				newBiomeObject.setRequiredCost(details.getInt("cost", 0));
				newBiomeObject.setPermission(details.getString("permission", ""));

				if (this.addon.getAddonManager().storeBiome(
					newBiomeObject, overwrite, user, false))
				{
					size++;
				}
			}
			else
			{
				user.sendMessage("biomes.messages.errors.load-biome",
					"[biome]",
					biome);
			}
		}

		user.sendMessage("biomes.messages.information.import-count",
			"[number]",
			String.valueOf(size));
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
					user.hasPermission(biomesObject.getPermission())))
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
		return this.getBiomes(Util.getWorld(world).getName());
	}


	/**
	 * This method returns list with loaded biomes for given world.
	 * @param worldName Name of world where biome operates.
	 * @return list with loaded biomes.
	 */
	public List<BiomesObject> getBiomes(String worldName)
	{
		return this.worldBiomeList.getOrDefault(worldName, Collections.emptyList());
	}


	/**
	 * This method returns biome object that hides behind biome name or null, if biome
	 * with name does not exist.
	 * @param biomeUniqueID Biome's name.
	 * @return BiomesObject that is represented by biome string.
	 */
	public BiomesObject getBiomeFromString(String biomeUniqueID)
	{
		return this.biomesMap.getOrDefault(biomeUniqueID, null);
	}


	/**
	 * Given method removes biome from database and cache.
	 * @param biome Biome that must be removed.
	 */
	public void removeBiome(BiomesObject biome)
	{
		if (this.biomesMap.containsKey(biome.getUniqueId()))
		{
			this.biomesMap.remove(biome.getUniqueId());
			this.worldBiomeList.get(biome.getWorld()).remove(biome);
			this.biomesDatabase.deleteObject(biome);
		}
	}


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------

	/**
	 * Variable current addon.
	 */
	private BiomesAddon addon;

	/**
	 * Variable stores map that links worlds to their biomes.
	 */
	private Map<String, List<BiomesObject>> worldBiomeList;

	/**
	 * Variable stores map that links String to loaded biomes object.
	 */
	private Map<String, BiomesObject> biomesMap;

	/**
	 * Variable stores database of biomes objects.
	 */
	private Database<BiomesObject> biomesDatabase;

	/**
	 * Variable stores biomes.yml location
	 */
	private File biomesFile;
}
