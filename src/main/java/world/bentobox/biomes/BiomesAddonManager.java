package world.bentobox.biomes;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.Database;
import world.bentobox.bentobox.util.ItemParser;
import world.bentobox.bentobox.util.Util;
import world.bentobox.biomes.config.Settings.VisibilityMode;
import world.bentobox.biomes.database.objects.BiomeChunkUpdateObject;
import world.bentobox.biomes.database.objects.BiomesObject;
import world.bentobox.biomes.panels.GuiUtils;
import world.bentobox.biomes.utils.Utils;


/**
 * This class contains all necessary thinks for Biomes Addon. It tries to manage and hold all necessary
 * information.
 */
public class BiomesAddonManager
{
    private static final String BIOME = "[biome]";

    /**
     * This is default constructor for Addon Manager.
     * @param addon Inits addon manager.
     */
    protected BiomesAddonManager(BiomesAddon addon)
    {
        this.addon = addon;

        this.biomesDatabase = new Database<>(addon, BiomesObject.class);
        this.biomesCacheData = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

        this.biomesFile = new File(this.addon.getDataFolder(), "biomes.yml");

        if (!this.biomesFile.exists())
        {
            this.addon.saveResource("biomes.yml", false);
        }

        this.biomePendingChunkUpdateDatabase = new Database<>(addon, BiomeChunkUpdateObject.class);
        this.biomePendingChunkUpdateMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

        this.load();
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
        this.biomePendingChunkUpdateMap.clear();

        this.addon.getLogger().info("Loading biomes...");

        this.biomesDatabase.loadObjects().forEach(this::loadBiomes);
        this.biomePendingChunkUpdateDatabase.loadObjects().forEach(this::addChunkUpdateObject);
    }


    /**
     * This class reload
     */
    public void reload()
    {
        this.addon.getLogger().info("Reloading biomes...");

        this.biomesDatabase = new Database<>(this.addon, BiomesObject.class);
        this.biomesDatabase.loadObjects().forEach(this::loadBiomes);

        this.biomePendingChunkUpdateDatabase = new Database<>(this.addon, BiomeChunkUpdateObject.class);
        this.biomePendingChunkUpdateDatabase.loadObjects().forEach(this::addChunkUpdateObject);
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
        this.biomesDatabase.saveObjectAsync(biome);
    }


    /**
     * Save biomes from cache into database
     */
    public void save()
    {
        this.biomesCacheData.values().forEach(this.biomesDatabase::saveObjectAsync);

        // Clear Database.
        List<BiomeChunkUpdateObject> objectList =
                this.biomePendingChunkUpdateDatabase.loadObjects();
        objectList.forEach(object -> this.biomePendingChunkUpdateDatabase.
                deleteID(object.getUniqueId()));

        // Save cache into database.
        if (!this.biomePendingChunkUpdateMap.isEmpty())
        {
            this.biomePendingChunkUpdateMap.values().forEach(
                    this.biomePendingChunkUpdateDatabase::saveObjectAsync);
        }
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
                        BIOME,
                        biome.toString());
            }

            return false;
        }

        // Compatibility fix from older versions.
        if (biome.getEnvironment() == null)
        {
            // If not specified, use over world.
            biome.setEnvironment(World.Environment.NORMAL);
        }

        // Contains in array list is not fast.. but list is not so large, so it is ok there.

        if (this.biomesCacheData.containsKey(biome.getUniqueId()))
        {
            if (!overwrite)
            {
                if (!silent)
                {
                    user.sendMessage("biomes.messages.skipping",
                            BIOME,
                            biome.getFriendlyName());
                }

                return false;
            }
            else
            {
                if (!silent)
                {
                    user.sendMessage("biomes.messages.overwriting",
                            BIOME,
                            biome.getFriendlyName());
                }

                this.biomesCacheData.replace(biome.getUniqueId(), biome);
                return true;
            }
        }

        if (!silent)
        {
            user.sendMessage("biomes.messages.imported",
                    BIOME,
                    biome.getFriendlyName());
        }

        this.biomesCacheData.put(biome.getUniqueId(), biome);
        return true;
    }


    // ---------------------------------------------------------------------
    // Section: Importing
    // ---------------------------------------------------------------------


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
        world = Util.getWorld(world);

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
    private void readBiomes(YamlConfiguration config, User user, World world, boolean overwrite)
    {
        int size = 0;

        ConfigurationSection reader = config.getConfigurationSection("biomes.biomesList");

        Map<String, Biome> biomeNameMap = Utils.getBiomeNameMap();

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
                newBiomeObject.setRequiredCost(details.getDouble("cost", 0.0));

                String environmentValue = details.getString("environment", "normal").toUpperCase();

                switch (environmentValue)
                {
                case "NETHER":
                    newBiomeObject.setEnvironment(World.Environment.NETHER);
                    break;
                case "THE_END":
                    newBiomeObject.setEnvironment(World.Environment.THE_END);
                    break;
                default:
                    newBiomeObject.setEnvironment(World.Environment.NORMAL);
                    break;
                }

                newBiomeObject.setOrder(details.getInt("order", 0));

                List<String> permissions = details.getStringList("permission");

                if (permissions.isEmpty())
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
                        BIOME,
                        biome);
            }
        }

        user.sendMessage("biomes.messages.import-count",
                TextVariables.NUMBER,
                String.valueOf(size));
    }


    // ---------------------------------------------------------------------
    // Section: Migrate
    // ---------------------------------------------------------------------


    /**
     * This method migrated all biomes addon data from worldName to addonID format.
     */
    public void migrateDatabase(User user, World world)
    {
        world = Util.getWorld(world);

        if (user.isPlayer())
        {
            user.sendMessage("biomes.messages.admin.migrate-start");
        }
        else
        {
            this.addon.log("Starting migration to new data format.");
        }

        boolean biomes = this.migrateBiomes(world);

        if (biomes)
        {
            if (user.isPlayer())
            {
                user.sendMessage("biomes.messages.admin.migrate-end");
            }
            else
            {
                this.addon.log("Migration to new data format completed.");
            }
        }
        else
        {
            if (user.isPlayer())
            {
                user.sendMessage("biomes.messages.admin.migrate-not");
            }
            else
            {
                this.addon.log("All data is valid. Migration is not necessary.");
            }
        }
    }


    /**
     * This method migrates biomes object to new id format.
     * @param world World which biomes must be updated.
     * @return {@code true} if any biome is updated,
     * 			{@code false} otherwise.
     */
    private boolean migrateBiomes(World world)
    {
        String addonName = Utils.getGameMode(world);

        if (addonName == null || addonName.equalsIgnoreCase(world.getName()))
        {
            return false;
        }

        boolean updated = false;
        List<BiomesObject> objectList = this.biomesDatabase.loadObjects();

        for (BiomesObject biomesObject : objectList)
        {
            if (biomesObject.getUniqueId().regionMatches(true, 0, world.getName() + "-", 0, world.getName().length() + 1))
            {
                this.biomesDatabase.deleteID(biomesObject.getUniqueId());
                this.biomesCacheData.remove(biomesObject.getUniqueId());

                biomesObject.setUniqueId(
                        addonName + "_" + biomesObject.getUniqueId().substring(world.getName().length() + 1));

                // Update world, as in some situations it was not set.
                biomesObject.setWorld(Util.getWorld(world).getName());

                this.biomesDatabase.saveObjectAsync(biomesObject);
                this.biomesCacheData.put(biomesObject.getUniqueId(), biomesObject);

                updated = true;
            }
        }

        return updated;
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
            biome.setFriendlyName(uniqueID);

            // Sets default biome as VOID.
            biome.setBiome(Biome.THE_VOID);
            biome.setWorld(worldName);
            biome.setEnvironment(World.Environment.NORMAL);

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

        allBiomeList.stream().
        // Filter out all biomes that has a different environment then players world.
        filter(biomeObject -> user.getWorld().getEnvironment().equals(biomeObject.getEnvironment())).
        // Filter out undeployed biomes if visibility mode is set to only deployed
        filter(biomesObject -> visibilityMode.equals(VisibilityMode.ALL) ||
                biomesObject.isDeployed() &&
                (visibilityMode.equals(VisibilityMode.DEPLOYED) ||
                        visibilityMode.equals(VisibilityMode.ACCESSIBLE))).
        // Filter out biomes which does user not have permissions
        filter(biomesObject ->
        biomesObject.getRequiredPermissions().isEmpty() ||
        biomesObject.getRequiredPermissions().stream().allMatch(user::hasPermission)).
        forEach(returnBiomesList::add);

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
                filter(biome -> biome.getWorld().equalsIgnoreCase(worldName)).
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
     * This method returns if in given world biomes are setup.
     * @param world World that must be checked.
     * @return True if in given world exist biomes.
     */
    public boolean hasAnyBiome(World world)
    {
        String worldName = Util.getWorld(world) == null ? "" : Util.getWorld(world).getName();

        return !worldName.isEmpty() &&
                this.biomesCacheData.values().stream().anyMatch(biome -> biome.getWorld().equalsIgnoreCase(worldName));
    }


    /**
     * This method returns true if in given location exit a greenhouse.
     * @param world World where greenhouse must be searched.
     * @param x X location.
     * @param y Y location.
     * @param z Z location.
     * @return {@code true} if in given location exist a greenhouse, {@code false} otherwise.
     */
    public boolean hasGreenhouseInLocation(World world, int x, int y, int z)
    {
        return this.addon.isGreenhousesProvided() &&
            this.addon.getGreenhouses().getManager().getMap().
                inGreenhouse(new Location(world, x, y, z));
    }


    // ---------------------------------------------------------------------
    // Section: Later Biome Updater
    // ---------------------------------------------------------------------


    /**
     * This method finds and returns BiomeChunkUpdaterObject in given world with given
     * chunk X and Z coordinates.
     * @param world World where process will happen.
     * @param x Chunk X coordinate.
     * @param z Chunk Z coordinate.
     * @return BiomeChunkUpdateObject where update is pending or null.
     */
    public BiomeChunkUpdateObject getPendingChunkUpdateObject(World world, int x, int z)
    {
        return this.biomePendingChunkUpdateMap.get(Utils.getGameMode(world) + "_" + x + "-" + z);
    }


    /**
     * This method returns collection with all objects that contains information about
     * chunks where biome update is still not completed.
     * @return Collection of BiomeCHunkUpdateObjects.
     */
    public Collection<BiomeChunkUpdateObject> getBiomeUpdaterCollection()
    {
        return this.biomePendingChunkUpdateMap.values();
    }


    /**
     * This method adds BiomeChunkUpdateObject to cache.
     * @param updateObject Object that must be added to cache.
     */
    public void addChunkUpdateObject(BiomeChunkUpdateObject updateObject)
    {
        this.biomePendingChunkUpdateMap.put(updateObject.getUniqueId(), updateObject);
    }


    /**
     * This method removes given element form cache and database.
     * @param element Element that should be removed.
     */
    public void removeUpdateObject(BiomeChunkUpdateObject element)
    {
        if (this.biomePendingChunkUpdateMap.containsKey(element.getUniqueId()))
        {
            this.biomePendingChunkUpdateMap.remove(element.getUniqueId());
            this.biomePendingChunkUpdateDatabase.deleteObject(element);
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

    /**
     * Variable stores BiomeChunkUpdateObject objects that contains information about
     * chunk that is not updated yet.
     */
    private Map<String, BiomeChunkUpdateObject> biomePendingChunkUpdateMap;

    /**
     * Variable stores database of BiomeChunkUpdateObject.
     */
    private Database<BiomeChunkUpdateObject> biomePendingChunkUpdateDatabase;
}
