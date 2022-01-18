///
// Created by BONNe
// Copyright - 2021
///

package world.bentobox.biomes.managers;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.Database;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.util.Util;
import world.bentobox.biomes.BiomesAddon;
import world.bentobox.biomes.database.objects.BiomesBundleObject;
import world.bentobox.biomes.database.objects.BiomesIslandDataObject;
import world.bentobox.biomes.database.objects.BiomesObject;
import world.bentobox.biomes.events.BiomeUnlockEvent;
import world.bentobox.biomes.events.BiomePurchasedEvent;
import world.bentobox.biomes.utils.Constants;
import world.bentobox.biomes.utils.Utils;


/**
 * This class contains all necessary thinks for Biomes Addon. It tries to manage and hold all necessary information.
 */
public class BiomesAddonManager
{
    /**
     * This is default constructor for Addon Manager.
     * @param addon Inits addon manager.
     */
    public BiomesAddonManager(BiomesAddon addon)
    {
        this.addon = addon;

        this.biomesDatabase = new Database<>(addon, BiomesObject.class);
        this.biomesCache = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

        this.bundleDatabase = new Database<>(addon, BiomesBundleObject.class);
        this.bundleCache = new HashMap<>();

        this.islandDatabase = new Database<>(addon, BiomesIslandDataObject.class);
        this.islandCache = new HashMap<>();

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
        this.biomesCache.clear();
        this.addon.getLogger().info("Loading biomes...");
        this.biomesDatabase.loadObjects().forEach(this::loadBiomes);
        this.bundleDatabase.loadObjects().forEach(this::loadBundle);
    }


    /**
     * This class reload
     */
    public void reload()
    {
        this.addon.getLogger().info("Reloading biomes...");

        this.biomesDatabase = new Database<>(this.addon, BiomesObject.class);
        this.biomesDatabase.loadObjects().forEach(this::loadBiomes);

        this.bundleDatabase = new Database<>(this.addon, BiomesBundleObject.class);
        this.bundleDatabase.loadObjects().forEach(this::loadBundle);
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
                Utils.sendMessage(user,
                    user.getTranslation(Constants.MESSAGES + "skipping",
                        Constants.PARAMETER_BIOME, biome.getFriendlyName()));
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

        if (this.biomesCache.containsKey(biome.getUniqueId()))
        {
            if (!overwrite)
            {
                if (!silent)
                {
                    Utils.sendMessage(user,
                        user.getTranslation(Constants.MESSAGES + "skipping",
                            Constants.PARAMETER_BIOME, biome.getFriendlyName()));
                }

                return false;
            }
            else
            {
                if (!silent)
                {
                    Utils.sendMessage(user,
                        user.getTranslation(Constants.MESSAGES + "overwriting",
                            Constants.PARAMETER_BIOME, biome.getFriendlyName()));
                }

                this.biomesCache.replace(biome.getUniqueId(), biome);
                return true;
            }
        }

        if (!silent)
        {
            Utils.sendMessage(user,
                user.getTranslation(Constants.MESSAGES + "imported",
                    Constants.PARAMETER_BIOME, biome.getFriendlyName()));
        }

        this.biomesCache.put(biome.getUniqueId(), biome);
        return true;
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
            Utils.sendMessage(user,
                user.getTranslation(Constants.MESSAGES + "migrate-start"));
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
                Utils.sendMessage(user,
                    user.getTranslation(Constants.MESSAGES + "migrate-finish"));
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
                Utils.sendMessage(user,
                    user.getTranslation(Constants.MESSAGES + "migrate-valid"));
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

        if (addonName.isBlank() || addonName.equalsIgnoreCase(world.getName()))
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
                this.biomesCache.remove(biomesObject.getUniqueId());

                biomesObject.setUniqueId(
                        addonName + "_" + biomesObject.getUniqueId().substring(world.getName().length() + 1));

                // Update world, as in some situations it was not set.
                biomesObject.setWorld(Util.getWorld(world).getName());

                this.biomesDatabase.saveObjectAsync(biomesObject);
                this.biomesCache.put(biomesObject.getUniqueId(), biomesObject);

                updated = true;
            }
        }

        return updated;
    }


    // ---------------------------------------------------------------------
    // Section: Bundles
    // ---------------------------------------------------------------------


    /**
     * Gets bundle by id.
     *
     * @param bundleId the bundle id
     * @return the bundle by id
     */
    @Nullable
    public BiomesBundleObject getBundleById(String bundleId)
    {
        return this.bundleCache.getOrDefault(bundleId, null);
    }


    /**
     * Gets bundles for given world.
     *
     * @param world the world
     * @return the bundles
     */
    public List<BiomesBundleObject> getBundles(World world)
    {
        String gameMode = this.addon.getPlugin().getIWM().getAddon(world).map(
            gameModeAddon -> gameModeAddon.getDescription().getName()).orElse("");

        if (gameMode.isEmpty())
        {
            // If not a gamemode world then return.
            return Collections.emptyList();
        }

        // Find default generator from cache.
        return this.bundleCache.values().stream().
            // Filter generators that starts with name.
            filter(bundle -> bundle.getUniqueId().startsWith(gameMode.toLowerCase())).
            // Sort in order: default generators are first, followed by lowest priority,
            // Return as list collection.
            collect(Collectors.toList());
    }


    /**
     * Save bundle to the database.
     *
     * @param bundle the bundle
     */
    public void saveBundle(BiomesBundleObject bundle)
    {
        this.bundleDatabase.saveObjectAsync(bundle);
    }


    /**
     * Loads bundles in cache silently. Used when loading.
     *
     * @param bundle that must be stored.
     * @return true if successful
     */
    private boolean loadBundle(BiomesBundleObject bundle)
    {
        return this.loadBundle(bundle, true, null);
    }


    /**
     * Load bundle in the cache.
     *
     * @param bundle - bundle that must be stored.
     * @param overwrite - true if previous bundle should be overwritten
     * @param user - user making the request
     * @return - true if imported
     */
    public boolean loadBundle(BiomesBundleObject bundle, boolean overwrite, User user)
    {
        if (this.bundleCache.containsKey(bundle.getUniqueId()))
        {
            if (!overwrite)
            {
                return false;
            }
            else
            {
                this.bundleCache.replace(bundle.getUniqueId(), bundle);
                return true;
            }
        }

        if (user != null)
        {
            Utils.sendMessage(user,
                user.getTranslation(Constants.MESSAGES + "bundle-loaded",
                    Constants.PARAMETER_BUNDLE, bundle.getFriendlyName()));
        }

        this.bundleCache.put(bundle.getUniqueId(), bundle);
        return true;
    }


    /**
     * Wipe bundle from the cache and database.
     *
     * @param bundle the bundle
     */
    public void wipeBundle(BiomesBundleObject bundle)
    {
        if (this.bundleCache.containsKey(bundle.getUniqueId()))
        {
            this.bundleCache.remove(bundle.getUniqueId());
            this.bundleDatabase.deleteID(bundle.getUniqueId());
        }
    }


    // ---------------------------------------------------------------------
    // Section: Island Data
    // ---------------------------------------------------------------------


    /**
     * Gets island data.
     *
     * @param world the world
     * @param user the user
     * @return the island data
     */
    @Nullable
    public BiomesIslandDataObject getIslandData(@Nullable World world, @Nullable User user)
    {
        if (world == null || user == null)
        {
            return null;
        }

        return this.getIslandData(this.addon.getIslandsManager().getIsland(world, user));
    }


    /**
     * Gets island data.
     *
     * @param island the island
     * @return the island data
     */
    @Nullable
    public BiomesIslandDataObject getIslandData(@Nullable Island island)
    {
        if (island == null)
        {
            return null;
        }

        this.addIslandData(island);
        return this.islandCache.get(island.getUniqueId());
    }


    /**
     * Load island from database into the cache or create new island data
     *
     * @param island - island that must be loaded
     */
    private void addIslandData(@NotNull Island island)
    {
        final String uniqueID = island.getUniqueId();

        if (this.islandCache.containsKey(uniqueID))
        {
            return;
        }

        // The island is not in the cache
        // Check if the island exists in the database

        if (this.islandDatabase.objectExists(uniqueID))
        {
            // Load player from database
            BiomesIslandDataObject data = this.islandDatabase.loadObject(uniqueID);
            // Store in cache

            if (data != null)
            {
                this.islandCache.put(uniqueID, data);
            }
            else
            {
                this.addon.logError("Could not load NULL island data object.");
            }
        }
        else
        {
            // Create the island data
            BiomesIslandDataObject pd = new BiomesIslandDataObject();
            pd.setUniqueId(uniqueID);

            // Update island data
            pd.setIslandBundle(null);

            // Update owner data.
            this.updateOwnerBundle(island, pd);

            // Save data.
            this.saveIslandData(pd);

            // Add to cache
            this.islandCache.put(uniqueID, pd);
        }
    }


    /**
     * This method updates owner bundle for island.
     *
     * @param island Island object that requires update.
     * @param dataObject Data Object that need to be populated.
     */
    private void updateOwnerBundle(@NotNull Island island, @NotNull BiomesIslandDataObject dataObject)
    {
        User owner = User.getInstance(island.getOwner());

        // Permission check can be done only to a player object.
        if (owner != null && owner.isPlayer())
        {
            // Update max island generation range.
            String permissionBundle = Utils.getPermissionValue(owner,
                Utils.getPermissionString(island.getWorld(), "[gamemode].biomes.bundle"),
                null);
            dataObject.setOwnerBundle(permissionBundle);
        }
    }


    /**
     * This method adds, validates and returns island data for given island.
     *
     * @param island Island which data must be returned.
     * @return BiomesIslandDataObject or null if failed to create.
     */
    @Nullable
    public BiomesIslandDataObject validateIslandData(@Nullable Island island)
    {
        if (island == null || island.getOwner() == null)
        {
            return null;
        }

        this.addIslandData(island);
        BiomesIslandDataObject dataObject = this.islandCache.get(island.getUniqueId());

        if (dataObject == null)
        {
            return null;
        }

        // Validate data in bundles object.
        this.updateOwnerBundle(island, dataObject);

        // Call check command which finds unlocked biomes.
        this.checkBiomesUnlockStatus(island, null, null);

        return dataObject;
    }


    /**
     * This method checks for all generators, if they are unlocked.
     *
     * @param island Island which is targeted for unlocking check.
     * @param user User who triggered check.
     * @param level New island level value.
     */
    public void checkBiomesUnlockStatus(Island island, @Nullable User user, @Nullable Long level)
    {
        if (island == null || island.getOwner() == null)
        {
            // Island or island owner is not set.
            return;
        }

        this.addIslandData(island);
        BiomesIslandDataObject dataObject = this.islandCache.get(island.getUniqueId());

        if (dataObject == null)
        {
            // Could not find any data for current island.
            return;
        }

        // Update owner bundle, as it may influence island generators.
        this.updateOwnerBundle(island, dataObject);

        // If level is null, check value from addon.
        final long islandLevel = level == null ? this.getIslandLevel(island) : level;
        final User owner = User.getInstance(island.getOwner());

        this.getIslandBiomes(island.getWorld(), dataObject).stream().
            filter(BiomesObject::isValid).
            filter(BiomesObject::isDeployed).
            filter(biome -> !dataObject.getUnlockedBiomes().contains(biome.getUniqueId())).
            filter(biome -> biome.getUnlockLevel() <= islandLevel).
            filter(biome -> biome.getUnlockPermissions().isEmpty() ||
                owner != null && owner.isOnline() && Utils.matchAllPermissions(owner, biome.getUnlockPermissions())).
            forEach(biome -> this.unlockBiome(dataObject, user, island, biome));
    }


    /**
     * Gets island biomes. It checks if island has assigned any bundle, and limits available biomes based on bundle
     * data.
     *
     * @param world the world
     * @param islandData the island data
     * @return the island biomes
     */
    public List<BiomesObject> getIslandBiomes(World world, @Nullable BiomesIslandDataObject islandData)
    {
        // Optimization could be done by generating bundles for each situation, but currently I do not
        // think it should be an actual problem here.
        Stream<BiomesObject> biomesObjects =
            this.getBiomes(world).stream().filter(BiomesObject::isDeployed).filter(BiomesObject::isValid);

        if (islandData != null)
        {
            // Owner bundle has larger priority then island bundle.
            if (islandData.getOwnerBundle() != null &&
                this.bundleCache.containsKey(islandData.getOwnerBundle()))
            {
                BiomesBundleObject bundle = this.bundleCache.get(islandData.getOwnerBundle());

                return biomesObjects.
                    filter(biomesObject -> bundle.getBiomeObjects().contains(biomesObject.getUniqueId())).
                    collect(Collectors.toList());
            }
            else if (islandData.getIslandBundle() != null &&
                this.bundleCache.containsKey(islandData.getIslandBundle()))
            {
                BiomesBundleObject bundle = this.bundleCache.get(islandData.getIslandBundle());

                return biomesObjects.
                    filter(biomesObject -> bundle.getBiomeObjects().contains(biomesObject.getUniqueId())).
                    collect(Collectors.toList());
            }
            else
            {
                return biomesObjects.collect(Collectors.toList());
            }
        }
        else
        {
            return biomesObjects.collect(Collectors.toList());
        }
    }


    /**
     * This method checks if given user can apply given biome.
     *
     * @param user User who will pay for activating.
     * @param islandData Data that stores island biomes.
     * @param biomesObject Biome that need to be changed.
     * @return {@code true} if can apply, {@code false} otherwise.
     */
    public boolean canApplyBiome(@NotNull User user,
        @NotNull BiomesIslandDataObject islandData,
        @NotNull BiomesObject biomesObject)
    {
        if (!islandData.getUnlockedBiomes().contains(biomesObject.getUniqueId()))
        {
            // Generator is not unlocked. Return false.
            Utils.sendMessage(user,
                user.getTranslation(Constants.MESSAGES + "biome-not-unlocked",
                    Constants.PARAMETER_BIOME, biomesObject.getFriendlyName()));

            return false;
        }
        else if (!islandData.getPurchasedBiomes().contains(biomesObject.getUniqueId()) &&
            this.addon.isEconomyProvided() &&
            biomesObject.getUnlockCost() > 0)
        {
            // Generator is not purchased. Return false.
            Utils.sendMessage(user,
                user.getTranslation(Constants.MESSAGES + "biome-not-purchased",
                    Constants.PARAMETER_BIOME, biomesObject.getFriendlyName()));

            return false;
        }
        else
        {
            return true;
        }
    }


    /**
     * This method unlocks given biome for given island.
     *
     * @param dataObject DataObject where all data will be saved.
     * @param user User who unlocks biome.
     * @param island Island that unlocks Biome.
     * @param biomesObject Biome that must be unlocked.
     */
    public void unlockBiome(@NotNull BiomesIslandDataObject dataObject,
        @Nullable User user,
        @NotNull Island island,
        @NotNull BiomesObject biomesObject)
    {
        if (!biomesObject.isDeployed() || !biomesObject.isValid())
        {
            // Do not add undeployed biomes and not valid biomes to the unlock list.
            if (user != null)
            {
                Utils.sendMessage(user,
                    user.getTranslation(Constants.MESSAGES + "biome-cannot-be-unlocked",
                        Constants.PARAMETER_BIOME, biomesObject.getFriendlyName()));
            }
            return;
        }

        // Create and call bukkit event to check if unlocking should be cancelled.
        BiomeUnlockEvent event = new BiomeUnlockEvent(biomesObject, user, island);
        Bukkit.getPluginManager().callEvent(event);

        if (!event.isCancelled())
        {
            // Add to unlocked biomesObject set.
            dataObject.getUnlockedBiomes().add(biomesObject.getUniqueId());
            // save data.
            this.saveIslandData(dataObject);

            if (!this.addon.getSettings().isNotifyUnlockedBiomes())
            {
                // Not necessary to notify users.
                return;
            }

            // Send message to user
            if (this.addon.isEconomyProvided() && biomesObject.getUnlockCost() > 0)
            {
                // Send message that biomesObject is available for purchase.

                island.getMemberSet().forEach(uuid ->
                    Utils.sendUnlockMessage(uuid,
                        island,
                        biomesObject,
                        this.addon,
                        false));
            }
            else
            {
                // Send message that biomesObject is available for activation.

                island.getMemberSet().forEach(uuid ->
                    Utils.sendUnlockMessage(uuid,
                        island,
                        biomesObject,
                        this.addon,
                        true));
            }
        }
    }


    /**
     * This method checks if given user can purchase given biomes object. This method includes money withdraw,
     * so it is assumed, that it is used as check before purchasing.
     *
     * @param user User who will pay for purchase.
     * @param island the island who contains data.
     * @param islandData island data object.
     * @param biomesObject biomes object that need to be purchased.
     * @return {@code true} if can purchase, {@code false} if cannot purchase.
     */
    public boolean canPurchaseGenerator(@NotNull User user,
        @NotNull Island island,
        @NotNull BiomesIslandDataObject islandData,
        @NotNull BiomesObject biomesObject)
    {
        final User owner = User.getInstance(island.getOwner());

        if (islandData.getPurchasedBiomes().contains(biomesObject.getUniqueId()))
        {
            // Biome is not unlocked. Return false.
            Utils.sendMessage(user,
                user.getTranslation(Constants.MESSAGES + "biome-already-purchased",
                    Constants.PARAMETER_BIOME, biomesObject.getFriendlyName()));
            return false;
        }
        else if (!island.isAllowed(BiomesAddon.BIOMES_ISLAND_PROTECTION))
        {
            Utils.sendMessage(user,
                user.getTranslation("general.errors.insufficient-rank",
                    TextVariables.RANK,
                    user.getTranslation(this.addon.getPlugin().getRanksManager().getRank(island.getRank(user)))));
            return false;
        }
        else if (biomesObject.getUnlockLevel() > this.getIslandLevel(island))
        {
            // Biome is not unlocked. Return false.
            Utils.sendMessage(user,
                user.getTranslation(Constants.MESSAGES + "island-level-not-reached",
                    Constants.PARAMETER_BIOME, biomesObject.getFriendlyName(),
                    TextVariables.NUMBER, String.valueOf(biomesObject.getUnlockLevel())));
            return false;
        }
        else if (!biomesObject.getUnlockPermissions().isEmpty() &&
            (owner == null || !owner.isPlayer() ||
                !biomesObject.getUnlockPermissions().stream().allMatch(owner::hasPermission)))
        {
            Optional<String> missingPermission = biomesObject.getUnlockPermissions().stream().
                filter(permission -> owner == null || !owner.isPlayer() || !owner.hasPermission(permission)).
                findAny();

            // Biome is not unlocked. Return false.
            missingPermission.ifPresent(s ->
                Utils.sendMessage(user,
                    user.getTranslation(Constants.MESSAGES + "missing-permission",
                        Constants.PARAMETER_BIOME, biomesObject.getFriendlyName(),
                        TextVariables.PERMISSION, s)));
            return false;
        }
        else
        {
            if (this.addon.isEconomyProvided() && biomesObject.getUnlockCost() > 0)
            {
                // Return true only if user has enough money and its removal was successful.
                if (this.addon.getVaultHook().has(user, biomesObject.getUnlockCost()) &&
                    this.addon.getVaultHook().withdraw(user, biomesObject.getUnlockCost()).transactionSuccess())
                {
                    return true;
                }
                else
                {
                    Utils.sendMessage(user,
                        user.getTranslation(Constants.MESSAGES + "no-credits-buy",
                            TextVariables.NUMBER, String.valueOf(biomesObject.getUnlockCost())));
                    return false;
                }
            }
            else
            {
                // Vault is not enabled or cost is not set. Allow change.
                return true;
            }
        }
    }


    /**
     * This method adds biomes object to purchased biomes.
     *
     * @param user User who will purchase.
     * @param island Island which is owned by player.
     * @param islandData island data object.
     * @param biomesObject biomes object that need to be purchased.
     */
    public void purchaseGenerator(@NotNull User user,
        @NotNull Island island,
        @NotNull BiomesIslandDataObject islandData,
        @NotNull BiomesObject biomesObject)
    {
        // Call event about successful purchase
        Bukkit.getPluginManager().callEvent(new BiomePurchasedEvent(biomesObject,
            user,
            island));

        Utils.sendMessage(user,
            user.getTranslation(Constants.MESSAGES + "biome-purchased",
                Constants.PARAMETER_BIOME, biomesObject.getFriendlyName()));
        islandData.getPurchasedBiomes().add(biomesObject.getUniqueId());

        // Save object.
        this.saveIslandData(islandData);
    }


    /**
     * Save island data.
     *
     * @param islandData the island data
     */
    public void saveIslandData(BiomesIslandDataObject islandData)
    {
        this.islandDatabase.saveObjectAsync(islandData);
    }


    /**
     * This method removes given data object from cache and database.
     *
     * @param uniqueId Object that must be removed.
     */
    public void wipeIslandData(String uniqueId)
    {
        this.islandCache.remove(uniqueId);
        this.islandDatabase.deleteID(uniqueId);
    }


    /**
     * This method removes island data object from cache and database.
     *
     * @param island Object that must be removed.
     */
    public void wipeIslandData(Island island)
    {
        this.wipeIslandData(island.getUniqueId());
    }


    /**
     * This method removes given data object from cache and database.
     *
     * @param dataObject Object that must be removed.
     */
    public void wipeIslandData(BiomesObject dataObject)
    {
        this.wipeIslandData(dataObject.getUniqueId());
    }


    /**
     * Wipe island data.
     *
     * @param optional the optional
     */
    public void wipeIslandData(Optional<GameModeAddon> optional)
    {
        if (optional.isEmpty())
        {
            // Done.
            return;
        }

        final String objectKey = optional.get().getDescription().getName();

        List<String> keySet = new ArrayList<>(this.islandCache.keySet());

        // Remove everything that starts with gamemode name.
        keySet.forEach(uniqueId ->
        {
            if (uniqueId.startsWith(objectKey))
            {
                this.islandCache.remove(uniqueId);
                this.islandDatabase.deleteID(uniqueId);
            }
        });

        this.addon.log("All island biomes data for " + objectKey + " are removed!");
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
     * This method returns biomes that is visible for user in given world.
     * @param world World in which biomes must be returned.
     * @param user User who will see biomes.
     * @return Visible biome list.
     */
    public List<BiomesObject> getBiomes(World world, User user)
    {
        List<BiomesObject> allBiomeList = this.getBiomes(world);

        List<BiomesObject> returnBiomesList = new ArrayList<>(allBiomeList.size());

        allBiomeList.stream().
            // Filter out all biomes that has a different environment then players world.
            filter(biomeObject -> user.getWorld().getEnvironment().equals(biomeObject.getEnvironment())).
            // Filter out undeployed biomes if visibility mode is set to only deployed
            filter(BiomesObject::isDeployed).
            // Filter out biomes which does user not have permissions
            filter(biomesObject -> biomesObject.getUnlockPermissions().isEmpty() ||
                biomesObject.getUnlockPermissions().stream().allMatch(user::hasPermission)).
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
        return this.biomesCache.values().stream().
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
    public BiomesObject getBiomeByID(String biomeUniqueID)
    {
        return this.biomesCache.getOrDefault(biomeUniqueID, null);
    }


    /**
     * Check if a biome exists - case insensitive
     *
     * @param name - name of biome
     * @return true if it exists, otherwise false
     */
    public boolean containsBiome(String name)
    {
        if (this.biomesCache.containsKey(name))
        {
            return true;
        }
        else
        {
            // check database.
            if (this.biomesDatabase.objectExists(name))
            {
                BiomesObject biome = this.biomesDatabase.loadObject(name);
                this.biomesCache.put(name, biome);
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
        if (this.biomesCache.containsKey(biome.getUniqueId()))
        {
            this.biomesCache.remove(biome.getUniqueId());
            this.biomesDatabase.deleteObject(biome);
        }
    }


    /**
     * Wipe game mode biomes.
     *
     * @param optional the gamemode addon
     */
    public void wipeGameModeBiomes(Optional<GameModeAddon> optional)
    {
        if (optional.isEmpty())
        {
            // Done.
            return;
        }

        final String objectKey = optional.get().getDescription().getName().toLowerCase();

        // Collect all generators
        List<String> keySet = new ArrayList<>(this.biomesCache.keySet());

        // Remove everything that starts with gamemode name.
        keySet.forEach(uniqueId ->
        {
            if (uniqueId.startsWith(objectKey))
            {
                this.biomesCache.remove(uniqueId);
                this.biomesDatabase.deleteID(uniqueId);
            }
        });

        this.addon.log("All biomes for " + objectKey + " are removed!");

        // Collect all bundles
        keySet.clear();
        keySet = new ArrayList<>(this.biomesCache.keySet());

        // Remove everything that starts with gamemode name.
        keySet.forEach(uniqueId ->
        {
            if (uniqueId.startsWith(objectKey))
            {
                this.bundleCache.remove(uniqueId);
                this.bundleDatabase.deleteID(uniqueId);
            }
        });

        this.addon.log("All biome bundles for " + objectKey + " are removed!");
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
                this.biomesCache.values().stream().anyMatch(biome -> biome.getWorld().equalsIgnoreCase(worldName));
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
    // Section: Other methods
    // ---------------------------------------------------------------------


    /**
     * This method returns long that represents given island level.
     *
     * @param island the island.
     * @return Island level
     */
    public long getIslandLevel(Island island)
    {
        if (!this.addon.isLevelProvided())
        {
            // No level addon. Return max value.
            return Long.MAX_VALUE;
        }

        return this.addon.getLevelAddon().getIslandLevel(island.getWorld(),
            island.getOwner());
    }


    /**
     * This method returns long that represents given user level.
     *
     * @param user the user.
     * @return Island level
     */
    public long getIslandLevel(User user)
    {
        if (!this.addon.isLevelProvided())
        {
            // No level addon. Return max value.
            return Long.MAX_VALUE;
        }

        return this.addon.getLevelAddon().getIslandLevel(user.getWorld(),
            user.getUniqueId());
    }


    // ---------------------------------------------------------------------
    // Section: Variables
    // ---------------------------------------------------------------------

    /**
     * Variable current addon.
     */
    private final BiomesAddon addon;

    /**
     * Variable stores map that links String to loaded biomes object.
     */
    private final Map<String, BiomesObject> biomesCache;

    /**
     * Variable stores database of biomes objects.
     */
    private Database<BiomesObject> biomesDatabase;

    /**
     * Variable stores map that links String to loaded bundle object.
     */
    private final Map<String, BiomesBundleObject> bundleCache;

    /**
     * Variable stores database of bundle objects.
     */
    private Database<BiomesBundleObject> bundleDatabase;

    /**
     * Variable stores map that links String to loaded island data object.
     */
    private final Map<String, BiomesIslandDataObject> islandCache;

    /**
     * Variable stores database of island dat objects.
     */
    private final Database<BiomesIslandDataObject> islandDatabase;
}
