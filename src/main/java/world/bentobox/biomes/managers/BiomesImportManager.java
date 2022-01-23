//
// Created by BONNe
// Copyright - 2020
//


package world.bentobox.biomes.managers;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.Nullable;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.json.BentoboxTypeAdapterFactory;
import world.bentobox.bentobox.database.objects.DataObject;
import world.bentobox.bentobox.util.ItemParser;
import world.bentobox.biomes.BiomesAddon;
import world.bentobox.biomes.database.objects.BiomesBundleObject;
import world.bentobox.biomes.database.objects.BiomesObject;
import world.bentobox.biomes.utils.Constants;
import world.bentobox.biomes.utils.Utils;


/**
 * This class will manage importing generators from template into database.
 */
public class BiomesImportManager
{

    /**
     * Default constructor.
     *
     * @param addon Instance of addon object.
     */
    public BiomesImportManager(BiomesAddon addon)
    {
        this.addon = addon;

        this.biomesFile = new File(this.addon.getDataFolder(), "biomesTemplate.yml");

        if (!this.biomesFile.exists())
        {
            this.addon.saveResource("biomesTemplate.yml", false);
        }
    }

    // ---------------------------------------------------------------------
    // Section: Template Methods
    // ---------------------------------------------------------------------


    /**
     * This method imports generator tiers from template
     *
     * @param user - user
     * @param world - world to import into
     * @return true if successful
     */
    public boolean importFile(@Nullable User user, World world)
    {
        if (!this.biomesFile.exists())
        {
            if (user != null)
            {
                Utils.sendMessage(user,
                    user.getTranslation(Constants.ERRORS + "no-file",
                        Constants.PARAMETER_FILE, this.biomesFile.getName()));
            }

            return false;
        }

        return this.importFile(user, world, this.biomesFile.getName());
    }


    /**
     * This method imports generator tiers from template
     *
     * @param user - user
     * @param world - world to import into
     * @param file - file that must be imported
     * @return true if successful
     */
    public boolean importFile(@Nullable User user, World world, String file)
    {
        File generatorFile = new File(this.addon.getDataFolder(), file.endsWith(".yml") ? file : file + ".yml");

        if (!generatorFile.exists())
        {
            if (user != null)
            {
                Utils.sendMessage(user,
                    user.getTranslation(Constants.ERRORS + "no-file",
                        Constants.PARAMETER_FILE, file));
            }

            return false;
        }

        YamlConfiguration config = new YamlConfiguration();

        try
        {
            config.load(generatorFile);
        }
        catch (IOException | InvalidConfigurationException e)
        {
            if (user != null)
            {
                Utils.sendMessage(user,
                    user.getTranslation(Constants.ERRORS + "no-load",
                        Constants.PARAMETER_FILE, file,
                        TextVariables.DESCRIPTION, e.getMessage()));
            }
            else
            {
                this.addon.logError("Exception when loading file. " + e.getMessage());
            }

            return false;
        }

        Optional<GameModeAddon> optional = this.addon.getPlugin().getIWM().getAddon(world);

        if (optional.isEmpty())
        {
            if (user != null)
            {
                Utils.sendMessage(user,
                    user.getTranslation(Constants.ERRORS + "not-a-gamemode-world",
                        Constants.PARAMETER_WORLD, world.getName()));
            }
            else
            {
                this.addon.logWarning("Given world is not a gamemode world.");
            }

            return false;
        }

        this.addon.getAddonManager().wipeGameModeBiomes(optional);
        this.createBiomes(config, user, optional.get());

        return true;
    }


    /**
     * This method creates generator tier object from config file.
     *
     * @param config YamlConfiguration that contains all generators.
     * @param user User who calls reading.
     * @param gameMode GameMode in which generator tiers must be imported
     */
    private void createBiomes(YamlConfiguration config, @Nullable User user, GameModeAddon gameMode)
    {
        int biomeCount;
        int bundleCount;

        if (config.isConfigurationSection("biomes"))
        {
            biomeCount = this.importBiomes(config.getConfigurationSection("biomes"),
                gameMode.getOverWorld());
        }
        else
        {
            biomeCount = 0;
        }

        if (config.isConfigurationSection("bundles"))
        {
            bundleCount = this.importBundles(config.getConfigurationSection("bundles"),
                gameMode.getDescription().getName() + "_");
        }
        else
        {
            bundleCount = 0;
        }

        if (user != null)
        {
            Utils.sendMessage(user, user.getTranslation(Constants.MESSAGES + "import-count",
                "[biomes]", String.valueOf(biomeCount),
                "[bundles]", String.valueOf(bundleCount)));
        }
    }


    /**
     * Import biomes from the given config.
     *
     * @param reader the reader
     * @param world the world
     * @return the int
     */
    private int importBiomes(ConfigurationSection reader, World world)
    {
        int size = 0;

        Map<String, Biome> biomeNameMap = Utils.getBiomeNameMap();

        for (String biome : reader.getKeys(false))
        {
            if (biomeNameMap.containsKey(biome.toUpperCase()))
            {
                BiomesObject newBiomeObject = new BiomesObject(Biome.valueOf(biome.toUpperCase()), world);
                newBiomeObject.setDeployed(true);

                ConfigurationSection details = reader.getConfigurationSection(biome);

                newBiomeObject.setFriendlyName(details.getString("friendlyName", biome));

                newBiomeObject.setDescription(Arrays.stream(details.getString("description", "").split("\n")).toList());
                newBiomeObject.setIcon(ItemParser.parse(details.getString("icon")));

                newBiomeObject.setUnlockLevel(details.getLong("islandLevel", 0));
                newBiomeObject.setUnlockCost(details.getDouble("cost", 0.0));

                String environmentValue = details.getString("environment", "normal").toUpperCase();

                switch (environmentValue)
                {
                    case "NETHER" -> newBiomeObject.setEnvironment(World.Environment.NETHER);
                    case "THE_END" -> newBiomeObject.setEnvironment(World.Environment.THE_END);
                    default -> newBiomeObject.setEnvironment(World.Environment.NORMAL);
                }

                newBiomeObject.setOrder(details.getInt("order", 0));

                List<String> permissions = details.getStringList("permission");

                if (permissions.isEmpty())
                {
                    newBiomeObject.setUnlockPermissions(Collections.emptySet());
                }
                else
                {
                    newBiomeObject.setUnlockPermissions(new HashSet<>(permissions));
                }

                if (this.addon.getAddonManager().loadBiomes(newBiomeObject, false, null, true))
                {
                    this.addon.getAddonManager().saveBiome(newBiomeObject);
                    size++;
                }
            }
        }

        return size;
    }


    /**
     * Import bundles from the given config.
     *
     * @param reader the reader
     * @param prefix the prefix
     * @return the int
     */
    private int importBundles(ConfigurationSection reader, String prefix)
    {
        int bundleSize = 0;

        for (String bundleId : reader.getKeys(false))
        {
            BiomesBundleObject generatorBundle = new BiomesBundleObject();
            generatorBundle.setUniqueId(prefix + bundleId.toLowerCase());

            ConfigurationSection details = reader.getConfigurationSection(bundleId);

            if (details != null)
            {
                // Read prefix
                generatorBundle.setFriendlyName(details.getString("name",
                    bundleId.replaceAll("_", " ")));

                // Read description
                if (details.isList("description"))
                {
                    generatorBundle.setDescription(details.getStringList("description"));
                }
                else if (details.isString("description"))
                {
                    String description = details.getString("description");

                    if (description != null)
                    {
                        // Define as list.
                        generatorBundle.setDescription(Arrays.asList(
                            description.replaceAll("\\|", "\n").
                                split("\n").
                                clone()));
                    }
                }

                // Read icon
                ItemStack icon = ItemParser.parse(details.getString("icon"));
                generatorBundle.setBundleIcon(icon == null ? new ItemStack(Material.PAPER) : icon);
                // Read generators
                generatorBundle.setBiomeObjects(
                    details.getStringList("biomes").stream().
                        map(id -> prefix + id).
                        collect(Collectors.toSet()));
            }

            // Save object in database.
            this.addon.getAddonManager().saveBundle(generatorBundle);
            this.addon.getAddonManager().loadBundle(generatorBundle, false, null);
            bundleSize++;
        }

        return bundleSize;
    }


    // ---------------------------------------------------------------------
    // Section: Database Methods
    // ---------------------------------------------------------------------


    /**
     * This method generates file with a given name that contains everything from database for given world. It creates
     * exact copy of data from database.
     *
     * @param user User who triggers this method.
     * @param world World which generators must be exported.
     * @param fileName FileName that will be used.
     * @return {@code true} if export was successful, {@code false} otherwise.
     */
    public boolean generateDatabaseFile(User user, World world, String fileName)
    {
        File defaultFile = new File(this.addon.getDataFolder(),
            fileName.endsWith(".json") ? fileName : fileName + ".json");

        if (defaultFile.exists())
        {
            if (user.isPlayer())
            {
                Utils.sendMessage(user,
                    user.getTranslation(Constants.ERRORS + "file-exist",
                        Constants.PARAMETER_FILE, fileName));
            }
            else
            {
                this.addon.logWarning(Constants.ERRORS + "file-exist");
            }

            return false;
        }

        try
        {
            if (defaultFile.createNewFile())
            {
                String replacementString = Utils.getGameMode(world).toLowerCase() + "_";
                BiomesAddonManager manager = this.addon.getAddonManager();

                List<BiomesObject> generatorTierList = manager.getBiomes(world).
                    stream().
                    map(biomesObject ->
                    {
                        // Use clone to avoid any changes in existing challenges.
                        BiomesObject clone = biomesObject.copy();
                        // Remove gamemode from biomesObject id.
                        clone.setUniqueId(biomesObject.getUniqueId().replaceFirst(replacementString, ""));
                        return clone;
                    }).
                    collect(Collectors.toList());

                List<BiomesBundleObject> levelList = manager.getBundles(world).
                    stream().
                    map(biomesBundle ->
                    {
                        // Use clone to avoid any changes in existing levels.
                        BiomesBundleObject clone = biomesBundle.copy();
                        // Remove gamemode from bundle ID.
                        clone.setUniqueId(biomesBundle.getUniqueId().replaceFirst(replacementString, ""));
                        // Remove gamemode form generators.
                        clone.setBiomeObjects(biomesBundle.getBiomeObjects().stream().
                            map(id -> id.replaceFirst(replacementString, "")).
                            collect(Collectors.toSet()));

                        return clone;
                    }).
                    collect(Collectors.toList());

                DefaultDataHolder exportedGeneratorData = new DefaultDataHolder();
                exportedGeneratorData.setUniqueId(fileName.endsWith(".json") ?
                    fileName.substring(0, fileName.length() - 5) : fileName);
                exportedGeneratorData.setBiomesObjectList(generatorTierList);
                exportedGeneratorData.setBiomesBundleList(levelList);
                exportedGeneratorData.setVersion(this.addon.getDescription().getVersion());
                exportedGeneratorData.setAuthor(user.getName());

                try (BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(defaultFile), StandardCharsets.UTF_8)))
                {
                    writer.write(Objects.requireNonNull(
                        new DefaultJSONHandler(this.addon).toJsonString(exportedGeneratorData)));
                }
            }
        }
        catch (IOException e)
        {
            if (user.isPlayer())
            {
                Utils.sendMessage(user,
                    user.getTranslation(Constants.ERRORS + "no-load",
                        Constants.PARAMETER_FILE, fileName,
                        TextVariables.DESCRIPTION, e.getMessage()));
            }

            this.addon.logError("Could not save json file: " + e.getMessage());
            return false;
        }
        finally
        {
            if (user.isPlayer())
            {
                Utils.sendMessage(user,
                    user.getTranslation(Constants.CONVERSATIONS + "database-export-completed",
                        Constants.PARAMETER_WORLD, world.getName(),
                        Constants.PARAMETER_FILE, fileName));
            }
            else
            {
                this.addon.logWarning("Database Export Completed");
            }
        }

        return true;
    }


    /**
     * This method imports everything from database file into database.
     *
     * @param user User who triggers this method.
     * @param world World which generators must be exported.
     * @param fileName FileName that will be used.
     * @return {@code true} if export was successful, {@code false} otherwise.
     */
    public boolean importDatabaseFile(User user, World world, String fileName)
    {
        BiomesAddonManager manager = this.addon.getAddonManager();

        // If exist any generator that is bound to current world, then do not load generators.
        if (!manager.getBiomes(world).isEmpty())
        {
            manager.wipeGameModeBiomes(this.addon.getPlugin().getIWM().getAddon(world));
        }

        try
        {
            // This prefix will be used to all generators. That is a unique way how to separate generators for
            // each game mode.
            String uniqueIDPrefix = Utils.getGameMode(world).toLowerCase() + "_";
            DefaultDataHolder downloadedBiomes = new DefaultJSONHandler(this.addon).loadObject(fileName);

            if (downloadedBiomes == null)
            {
                return false;
            }

            // All new generators should get correct ID. So we need to map it to loaded generators.
            downloadedBiomes.getBiomesObjectList().forEach(biomesObject ->
            {
                // Set correct biomesObject ID
                biomesObject.setUniqueId(uniqueIDPrefix + biomesObject.getUniqueId());
                // Load generator in memory
                manager.saveBiome(biomesObject);
                manager.loadBiomes(biomesObject, false, user, true);
            });

            downloadedBiomes.getBiomesBundleList().forEach(generatorBundle ->
            {
                // Set correct bundle ID
                generatorBundle.setUniqueId(uniqueIDPrefix + generatorBundle.getUniqueId());
                // Reset names for all generators.
                generatorBundle.setBiomeObjects(generatorBundle.getBiomeObjects().stream().
                    map(generatorTier -> uniqueIDPrefix + generatorTier).
                    collect(Collectors.toSet()));
                // Load level in memory
                manager.saveBundle(generatorBundle);
                manager.loadBundle(generatorBundle, false, user);
            });
        }
        catch (Exception e)
        {
            addon.getPlugin().logStacktrace(e);
            return false;
        }

        return true;
    }


    /**
     * This method saves and imports given string as generators.
     *
     * @param user User who called method.
     * @param world World which will be targeted.
     * @param stoneGeneratorLibrary String that contains all data for generators.
     */
    public void processDownloadedFile(User user, World world, String stoneGeneratorLibrary)
    {
        DefaultDataHolder downloadedGenerators =
            new DefaultJSONHandler(this.addon).loadWebObject(stoneGeneratorLibrary);

        File downloadFile = new File(this.addon.getDataFolder(), downloadedGenerators.getUniqueId() + ".json");
        int i = 1;

        while (downloadFile.exists())
        {
            downloadFile = new File(this.addon.getDataFolder(),
                downloadedGenerators.getUniqueId() + "-" + i++ + ".json");
        }

        try (BufferedWriter writer = new BufferedWriter(
            new OutputStreamWriter(new FileOutputStream(downloadFile), StandardCharsets.UTF_8)))
        {
            writer.write(Objects.requireNonNull(
                new DefaultJSONHandler(this.addon).toJsonString(downloadedGenerators)));
        }
        catch (Exception e)
        {
            if (user.isPlayer())
            {
                Utils.sendMessage(user,
                    user.getTranslation(Constants.ERRORS + "no-load",
                        Constants.PARAMETER_FILE, downloadFile.getName(),
                        TextVariables.DESCRIPTION, e.getMessage()));
            }

            this.addon.logError("Could not save json file: " + e.getMessage());
        }

        this.importDatabaseFile(user, world, downloadFile.getName());
    }


    // ---------------------------------------------------------------------
    // Section: Class instances
    // ---------------------------------------------------------------------


    /**
     * This Class allows to load  and their levels as objects much easier.
     */
    private static final class DefaultJSONHandler
    {
        /**
         * This constructor inits JSON builder that will be used to parse challenges.
         *
         * @param addon Challenges Adddon
         */
        DefaultJSONHandler(BiomesAddon addon)
        {
            GsonBuilder builder =
                new GsonBuilder().excludeFieldsWithoutExposeAnnotation().enableComplexMapKeySerialization();
            // Register adapters
            builder.registerTypeAdapterFactory(new BentoboxTypeAdapterFactory(addon.getPlugin()));

            // Keep null in the database
            builder.serializeNulls();
            // Allow characters like < or > without escaping them
            builder.disableHtmlEscaping();

            this.addon = addon;
            this.gson = builder.setPrettyPrinting().create();
        }


        /**
         * This method returns json object that is parsed to string. Json object is made from given instance.
         *
         * @param instance Instance that must be parsed to json string.
         * @return String that contains JSON information from instance object.
         */
        String toJsonString(DefaultDataHolder instance)
        {
            // Null check
            if (instance == null)
            {
                this.addon.logError("JSON database request to store a null. ");
                return null;
            }

            return this.gson.toJson(instance);
        }


        /**
         * This method creates and adds to list all objects from default.json file.
         *
         * @param fileName Name of the file.
         * @return List of all objects from default.json that is with T instance.
         */
        DefaultDataHolder loadObject(String fileName)
        {
            if (!fileName.endsWith(".json"))
            {
                fileName = fileName + ".json";
            }

            File defaultFile = new File(this.addon.getDataFolder(), fileName);

            try (InputStreamReader reader = new InputStreamReader(new FileInputStream(defaultFile),
                StandardCharsets.UTF_8))
            {
                DefaultDataHolder object = this.gson.fromJson(reader, DefaultDataHolder.class);
                object.setUniqueId(fileName);

                reader.close(); // NOSONAR Required to keep OS file handlers low and not rely on GC

                return object;
            }
            catch (FileNotFoundException e)
            {
                this.addon.logError("Could not load file '" + defaultFile.getName() + "': File not found.");
            }
            catch (Exception e)
            {
                this.addon.logError("Could not load objects " + defaultFile.getName() + " " + e.getMessage());
            }

            return null;
        }


        /**
         * This method creates and adds to list all objects from default.json file.
         *
         * @param downloadedObject Object that is downloaded.
         * @return List of all objects from default.json that is with T instance.
         */
        DefaultDataHolder loadWebObject(String downloadedObject)
        {
            return this.gson.fromJson(downloadedObject, DefaultDataHolder.class);
        }


        // ---------------------------------------------------------------------
        // Section: Variables
        // ---------------------------------------------------------------------

        /**
         * Holds JSON builder object.
         */
        private final Gson gson;

        /**
         * Holds StoneGeneratorAddon object.
         */
        private final BiomesAddon addon;
    }


    /**
     * This is simple object that will allow to store all current generators and bundles in single file.
     */
    private static final class DefaultDataHolder implements DataObject
    {
        /**
         * Default constructor. Creates object with empty lists.
         */
        DefaultDataHolder()
        {
            this.biomesObjectList = Collections.emptyList();
            this.biomesBundleList = Collections.emptyList();
            this.version = "";
            this.author = null;
        }


        /**
         * This method returns stored challenge list.
         *
         * @return list that contains default challenges.
         */
        List<BiomesObject> getBiomesObjectList()
        {
            return biomesObjectList;
        }


        /**
         * This method sets given list as generator tiers.
         *
         * @param biomesObjectList new generator tiers.
         */
        void setBiomesObjectList(List<BiomesObject> biomesObjectList)
        {
            this.biomesObjectList = biomesObjectList;
        }


        /**
         * This method returns list of generator bundles.
         *
         * @return List that contains generator bundles.
         */
        List<BiomesBundleObject> getBiomesBundleList()
        {
            return biomesBundleList;
        }


        /**
         * This method sets given list as generator bundle list.
         *
         * @param biomesBundleList new generator bundle list.
         */
        void setBiomesBundleList(List<BiomesBundleObject> biomesBundleList)
        {
            this.biomesBundleList = biomesBundleList;
        }


        /**
         * This method returns the version value.
         *
         * @return the value of version.
         */
        public String getVersion()
        {
            return version;
        }


        /**
         * This method sets the version value.
         *
         * @param version the version new value.
         */
        public void setVersion(String version)
        {
            this.version = version;
        }


        /**
         * @return unqinue Id;
         */
        @Override
        public String getUniqueId()
        {
            return this.uniqueId;
        }


        /**
         * @param uniqueId - unique ID the uniqueId to set
         */
        @Override
        public void setUniqueId(String uniqueId)
        {
            this.uniqueId = uniqueId;
        }


        /**
         * Gets author.
         *
         * @return the author
         */
        public String getAuthor()
        {
            return author;
        }


        /**
         * Sets author.
         *
         * @param author the author
         */
        public void setAuthor(String author)
        {
            this.author = author;
        }


        // ---------------------------------------------------------------------
        // Section: Variables
        // ---------------------------------------------------------------------

        /**
         * Holds a list with biomes objects.
         */
        @Expose
        private List<BiomesObject> biomesObjectList;

        /**
         * Holds a list with biomes bundles.
         */
        @Expose
        private List<BiomesBundleObject> biomesBundleList;

        /**
         * Holds a variable that stores in which addon version file was made.
         */
        @Expose
        private String version;

        /**
         * Holds an author for export file.
         */
        @Expose
        private String author;

        /**
         * The Unique id.
         */
        @Expose
        private String uniqueId;
    }


    // ---------------------------------------------------------------------
    // Section: Variables
    // ---------------------------------------------------------------------

    /**
     * Addon class instance.
     */
    private final BiomesAddon addon;

    /**
     * Variable stores biomesTemplate.yml location
     */
    private final File biomesFile;
}
