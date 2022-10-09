///
// Created by BONNe
// Copyright - 2022
///

package world.bentobox.biomes.tasks;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BlockVector;

import net.milkbowl.vault.economy.EconomyResponse;
import world.bentobox.bank.BankManager;
import world.bentobox.bank.BankResponse;
import world.bentobox.bank.data.Money;
import world.bentobox.bank.data.TxType;
import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.biomes.BiomesAddon;
import world.bentobox.biomes.config.Settings.UpdateMode;
import world.bentobox.biomes.database.objects.BiomesIslandDataObject;
import world.bentobox.biomes.database.objects.BiomesObject;
import world.bentobox.biomes.events.BiomeChangedEvent;
import world.bentobox.biomes.events.BiomePreChangeEvent;
import world.bentobox.biomes.utils.Constants;
import world.bentobox.biomes.utils.Utils;


/**
 * This class helps to validate if user can change biome. It also calculates how large update must be and calls update
 * task.
 */
public class BiomeUpdateHelper
{
    /**
     * Instantiates a new Biome update helper.
     *
     * @param addon the addon
     * @param callerUser the caller user
     * @param targetUser the target user
     * @param biome the biome
     * @param islandData the island data
     * @param world the world
     * @param updateMode the update mode
     * @param range the range
     * @param canWithdraw the can withdraw
     */
    public BiomeUpdateHelper(BiomesAddon addon,
        User callerUser,
        User targetUser,
        BiomesObject biome,
        BiomesIslandDataObject islandData,
        World world,
        UpdateMode updateMode,
        int range,
        boolean canWithdraw)
    {
        this.addon = addon;
        this.callerUser = callerUser;
        this.targetUser = targetUser;
        this.islandData = islandData;
        this.biome = biome;
        this.world = world;
        this.updateMode = updateMode;
        this.range = range;
        this.canWithdraw = canWithdraw;

        this.worldProtectionFlag = BiomesAddon.BIOMES_WORLD_PROTECTION.isSetForWorld(this.world);
        // Initialize standing location to be the location of the target user
        this.standingLocation = this.targetUser.getLocation();
    }


    /**
     * This method returns if update tack can be called.
     *
     * @return {@code true} if user can change biome.
     */
    public boolean canChangeBiome()
    {
        // Check if environment is valid.
        if (!this.biome.getEnvironment().equals(World.Environment.NORMAL))
        {
            // Check if nether and the end islands are enabled.
            if ((
                !this.biome.getEnvironment().equals(World.Environment.NETHER) ||
                    !this.addon.getPlugin().getIWM().isNetherGenerate(this.world) ||
                    !this.addon.getPlugin().getIWM().isNetherIslands(this.world)) &&
                (
                    !this.biome.getEnvironment().equals(World.Environment.THE_END) ||
                        !this.addon.getPlugin().getIWM().isEndGenerate(this.world) ||
                        !this.addon.getPlugin().getIWM().isEndIslands(this.world)))
            {
                Utils.sendMessage(this.callerUser,
                    this.callerUser.getTranslation("general.errors.wrong-world"));
                return false;
            }
        }

        this.island = this.addon.getIslands().getIsland(this.world, this.targetUser);

        if (this.island == null)
        {
            // User has no island.
            Utils.sendMessage(this.callerUser,
                this.callerUser.getTranslation("general.errors.player-has-no-island"));
            return false;
        }

        this.calculateArea();

        if (this.callerUser == this.targetUser)
        {
            if (!this.biome.getEnvironment().equals(this.callerUser.getWorld().getEnvironment()))
            {
                // User must be in the same environment as biome require.
                Utils.sendMessage(this.callerUser,
                    this.callerUser.getTranslation("general.errors.wrong-world"));
                return false;
            }

            if (!this.checkUnlockStatus())
            {
                Utils.sendMessage(this.callerUser,
                    this.callerUser.getTranslation(Constants.ERRORS + "not-unlocked"));
                return false;
            }

            if (!this.checkPurchaseStatus())
            {
                Utils.sendMessage(this.callerUser,
                    this.callerUser.getTranslation(Constants.ERRORS + "not-purchased"));
                return false;
            }

            if (!this.hasPermissionToUpdateMode())
            {
                Utils.sendMessage(this.callerUser,
                    this.callerUser.getTranslation("general.errors.no-permission"));
                return false;
            }

            if (!this.updateMode.equals(UpdateMode.ISLAND) && this.range <= 0)
            {
                // Cannot update negative numbers.

                Utils.sendMessage(this.callerUser,
                    this.callerUser.getTranslation(Constants.ERRORS + "incorrect-range",
                        TextVariables.NUMBER,
                        Integer.toString(this.range)));
                return false;
            }

            if (this.worldProtectionFlag)
            {
                Optional<Island> onIsland =
                    this.addon.getIslands().getIslandAt(this.callerUser.getLocation());

                if (onIsland.isEmpty() || onIsland.get() != this.island)
                {
                    // User is not on his island.
                    Utils.sendMessage(this.callerUser,
                        this.callerUser.getTranslation("general.errors.not-on-island"));

                    return false;
                }

                if (!this.island.isAllowed(this.callerUser, BiomesAddon.BIOMES_ISLAND_PROTECTION))
                {
                    // This can be checked only if island exists.

                    Utils.sendMessage(this.callerUser,
                        this.callerUser.getTranslation(Constants.ERRORS + "no-rank"));
                    return false;
                }
            }
            else if (this.updateMode.equals(UpdateMode.ISLAND))
            {
                // User has no island.
                this.callerUser.sendMessage(BiomesAddon.BIOMES_WORLD_PROTECTION.getHintReference());
                return false;
            }

            // Init starting location.
            this.standingLocation = this.targetUser.getLocation();

            // Process biome mode cost
            return switch (this.biome.getCostMode())
                {
                    case PER_BLOCK -> this.checkPerBlockCost();
                    case PER_USAGE -> this.checkPerUsageCost();
                    case STATIC -> this.checkStaticCost();
                };
        }
        else
        {
            if (!this.worldProtectionFlag)
            {
                if (this.updateMode.equals(UpdateMode.ISLAND))
                {
                    // Island option is not possible for worlds without world protection.
                    if (this.callerUser.isPlayer())
                    {
                        Utils.sendMessage(this.callerUser,
                            this.callerUser.getTranslation(BiomesAddon.BIOMES_WORLD_PROTECTION.getHintReference()));
                    }
                    else
                    {
                        this.addon.logWarning("Biome change is not possible with Island mode " +
                            "for this world as BIOMES_WORLD_PROTECTION is disabled!");
                    }

                    return false;
                }
                else
                {
                    if (this.targetUser.isOnline())
                    {
                        this.standingLocation = this.targetUser.getLocation();
                    }
                    else if (this.callerUser.isPlayer())
                    {
                        this.standingLocation = this.callerUser.getLocation();
                    }
                    else
                    {
                        this.addon.logWarning("Target Player is not online. Cannot find biome change location!");
                        return false;
                    }
                }
            }
            else if (this.updateMode.equals(UpdateMode.ISLAND))
            {
                this.standingLocation = this.targetUser.getLocation();

                // Return false if targeted user has no island.
                return this.addon.getIslands().getIsland(this.world, this.targetUser) != null;
            }
            else if (this.callerUser.isPlayer())
            {
                // Chunk and square based update modes can be called only by player.

                Optional<Island> onIsland =
                    this.addon.getIslands().getIslandAt(this.callerUser.getLocation());

                if (onIsland.isEmpty() || onIsland.get() != this.island)
                {
                    // Admin is not on user island.
                    Utils.sendMessage(this.callerUser,
                        this.callerUser.getTranslation(Constants.ERRORS + "admin-not-on-island",
                            "[user]",
                            this.targetUser.getName()));

                    return false;
                }

                // Admin must be located on island to change biome, as his location will be
                // taken for update.
                this.standingLocation = this.callerUser.getLocation();
            }
            else
            {
                // Check if target user is his island.

                Optional<Island> onIsland =
                    this.addon.getIslands().getIslandAt(this.targetUser.getLocation());

                if (onIsland.isEmpty() || onIsland.get() != this.island)
                {
                    // Admin is not on user island.
                    this.addon.logWarning("Biome change for player " + this.targetUser.getName() +
                        " is not possible as he is not on his island!");
                    return false;
                }

                // Init start location
                this.standingLocation = this.targetUser.getLocation();
            }
        }

        return true;
    }


    /**
     * This method calculates and sets minimal/maximal coordinates.
     */
    private void calculateArea()
    {
        int minX;
        int minZ;
        int maxX;
        int maxZ;
        int minY;
        int maxY;

        // Limit island update range
        if (this.worldProtectionFlag)
        {
            // This is implemented to fix issue when biome is changed in space between islands. #34

            if (this.addon.getSettings().isUseProtectionRange())
            {
                // Allow go outside island protection range if it is possible.

                minX = this.island.getMinProtectedX();
                minZ = this.island.getMinProtectedZ();

                maxX = this.island.getMaxProtectedX() - 1;
                maxZ = this.island.getMaxProtectedZ() - 1;
            }
            else
            {
                minX = this.island.getMinX();
                minZ = this.island.getMinZ();

                maxX = this.island.getMaxX() - 1;
                maxZ = this.island.getMaxZ() - 1;
            }

            // biome cannot be changed outside island!
            if (Utils.normalizeBy4(minX) < this.island.getMinX())
            {
                minX = Utils.normalizeBy4(minX + 4);
            }

            if (Utils.normalizeBy4(maxX) > this.island.getMaxX())
            {
                maxX = Utils.normalizeBy4(maxX - 4);
            }

            if (Utils.normalizeBy4(minZ) < this.island.getMinZ())
            {
                minZ = Utils.normalizeBy4(minZ + 4);
            }

            if (Utils.normalizeBy4(maxZ) > this.island.getMaxZ())
            {
                maxZ = Utils.normalizeBy4(maxZ - 4);
            }
        }
        else
        {
            // limit by island distance to avoid issues with long updating.
            int range = this.addon.getPlugin().getIWM().getIslandDistance(this.world);

            minX = Utils.normalizeBy4(this.standingLocation.getBlockX() - range);
            minZ = Utils.normalizeBy4(this.standingLocation.getBlockZ() - range);

            maxX = Utils.normalizeBy4(this.standingLocation.getBlockX() + range);
            maxZ = Utils.normalizeBy4(this.standingLocation.getBlockZ() + range);
        }

        switch (this.updateMode)
        {
            case CHUNK -> {
                Chunk chunk = this.standingLocation.getChunk();

                // Limit by the chunk.
                minX = Math.max(minX, (chunk.getX() - (this.range - 1)) << 4);
                maxX = Math.min(maxX, (chunk.getX() + this.range) << 4) - 1;
                minZ = Math.max(minZ, (chunk.getZ() - (this.range - 1)) << 4);
                maxZ = Math.min(maxZ, (chunk.getZ() + this.range) << 4) - 1;

                // Select whole island height.
                minY = this.world.getMinHeight();
                maxY = this.world.getMaxHeight();
            }
            case RANGE -> {
                int halfDiameter = this.range / 2;
                int x = this.standingLocation.getBlockX();

                // Calculate X location
                minX = Math.max(minX, Utils.normalizeBy4(x - halfDiameter));
                maxX = Math.min(maxX, Utils.normalizeBy4(x + halfDiameter));

                int z = this.standingLocation.getBlockZ();

                // Calculate Z location
                minZ = Math.max(minZ, Utils.normalizeBy4(z - halfDiameter));
                maxZ = Math.min(maxZ, Utils.normalizeBy4(z + halfDiameter));

                // Calculate Y location
                int y = this.standingLocation.getBlockY();
                minY = Math.max(this.world.getMinHeight(), Utils.normalizeBy4(y - halfDiameter));
                maxY = Math.min(this.world.getMaxHeight(), Utils.normalizeBy4(y + halfDiameter));
            }
            default -> {
                // Select whole island height.
                minY = this.world.getMinHeight();
                maxY = this.world.getMaxHeight();
            }
        }

        this.minCoordinate = new BlockVector(minX, minY, minZ);
        this.maxCoordinate = new BlockVector(maxX, maxY, maxZ);
    }


    /**
     * This method calculates update region and call BiomeUpdateTask to change given biome on island.
     */
    public void updateIslandBiome()
    {
        // Calculate minimal and maximal coordinate based on update mode.

        BiomeUpdateTask task = new BiomeUpdateTask(this.addon,
            this.callerUser,
            this.biome);

        // Select world depending on environment.

        if (World.Environment.NETHER.equals(this.biome.getEnvironment()))
        {
            task.setWorld(this.addon.getPlugin().getIWM().getNetherWorld(this.world));
        }
        else if (World.Environment.THE_END.equals(this.biome.getEnvironment()))
        {
            task.setWorld(this.addon.getPlugin().getIWM().getEndWorld(this.world));
        }
        else
        {
            task.setWorld(this.world);
        }

        task.setMinCoordinate(this.minCoordinate);
        task.setMaxCoordinate(this.maxCoordinate);

        Bukkit.getPluginManager().callEvent(new BiomePreChangeEvent(this.biome,
            this.targetUser,
            this.island,
            this.minCoordinate,
            this.maxCoordinate));

        // Take required cost.

        CompletableFuture<Boolean> changeBiomeStage = new CompletableFuture<>();
        changeBiomeStage.thenAccept((runTask) -> {
            if (runTask)
            {
                this.runBiomeChangeTask(task);
            }
        });

        if (this.canWithdraw)
        {
            switch (this.biome.getCostMode())
            {
                case PER_BLOCK -> this.withdrawPerBlock(changeBiomeStage);
                case PER_USAGE -> this.withdrawPerUsage(changeBiomeStage);
                case STATIC -> this.withdrawStatic(changeBiomeStage);
            }
        }
        else
        {
            changeBiomeStage.complete(true);
        }
    }


    /**
     * Run biome change task.
     *
     * @param task the task
     */
    private void runBiomeChangeTask(BiomeUpdateTask task)
    {
        task.updateChunkQueue();

        // Increase counter.
        this.islandData.increaseBiomeChangeCounter(this.biome);
        this.addon.getAddonManager().saveIslandData(this.islandData);

        this.addon.getUpdateQueue().addUpdateTask(task).thenAccept((result) ->
        {
            switch (result)
            {
                case FINISHED -> {
                    Utils.sendMessage(this.callerUser,
                        this.callerUser.getTranslation(Constants.MESSAGES + "update-done",
                            "[biome]", this.biome.getFriendlyName()));

                    this.addon.log(this.callerUser.getName() + " changed biome in loaded chunks to " +
                        this.biome.getFriendlyName() + " from" +
                        " min=" + this.minCoordinate +
                        " max=" + this.maxCoordinate +
                        " while standing on" +
                        " location=" + this.standingLocation.toVector());
                }
                case TIMEOUT -> {
                    Utils.sendMessage(this.callerUser,
                        this.callerUser.getTranslation(Constants.ERRORS + "timeout"));

                    this.addon.logWarning(this.callerUser.getName() + " timeout while changing biome to " +
                        this.biome.getFriendlyName() + " from" +
                        " min=" + this.minCoordinate +
                        " max=" + this.maxCoordinate +
                        " while standing on" +
                        " location=" + this.standingLocation.toVector());
                }
                default -> {
                    Utils.sendMessage(this.callerUser, this.callerUser.getTranslation(Constants.ERRORS + "failed"));

                    this.addon.logError(this.callerUser.getName() + " failed to changed biome to " +
                        this.biome.getFriendlyName() + " from" +
                        " min=" + this.minCoordinate +
                        " max=" + this.maxCoordinate +
                        " while standing on" +
                        " location=" + this.standingLocation.toVector());
                }
            }

            Bukkit.getPluginManager().callEvent(new BiomeChangedEvent(this.biome,
                this.targetUser,
                this.island,
                this.minCoordinate,
                this.maxCoordinate,
                result));
        });
    }


    /**
     * Method that withdraws the money for changing biome.
     *
     * @param changeBiomeStage the change biome stage
     * @param money the money
     */
    private void withdrawMoney(CompletableFuture<Boolean> changeBiomeStage, double money)
    {
        if (!this.addon.isEconomyProvided())
        {
            Utils.sendMessage(this.callerUser,
                this.callerUser.getTranslation(Constants.ERRORS + "could-not-remove-money"));
            this.addon.logError("Economy Addon not provided.");
            changeBiomeStage.complete(false);
            return;
        }

        if (this.addon.getSettings().isUseBankAccount() && this.addon.isBankProvided())
        {
            BankManager bankManager = this.addon.getBankAddon().getBankManager();
            bankManager.withdraw(this.callerUser, this.island, new Money(money), TxType.WITHDRAW).
                thenAccept(response -> {
                    if (response != BankResponse.SUCCESS)
                    {
                        Utils.sendMessage(this.callerUser,
                            this.callerUser.getTranslation(Constants.ERRORS + "could-not-remove-money"));
                        changeBiomeStage.complete(false);
                    }
                });
        }
        else
        {
            EconomyResponse withdraw = this.addon.getVaultHook().withdraw(this.callerUser, money);

            if (!withdraw.transactionSuccess())
            {
                // Something went wrong on withdraw.

                Utils.sendMessage(this.callerUser,
                    this.callerUser.getTranslation(Constants.ERRORS + "could-not-remove-money"));
                this.addon.logError(withdraw.errorMessage);
                changeBiomeStage.complete(false);
            }
        }
    }


    /**
     * Method that withdraws items for changing biome.
     *
     * @param changeBiomeStage the change biome stage
     * @param requiredItemList the required item list
     * @param ignoreMetaData items that can ignore metadata.
     */
    private void withdrawItems(CompletableFuture<Boolean> changeBiomeStage,
        List<ItemStack> requiredItemList,
        Set<Material> ignoreMetaData)
    {
        if (this.callerUser.getPlayer().getGameMode() == GameMode.CREATIVE)
        {
            // No point to check items from creative inventory.
            return;
        }

        for (ItemStack required : requiredItemList)
        {
            int amountToBeRemoved = required.getAmount();
            List<ItemStack> itemsInInventory;

            if (ignoreMetaData.contains(required.getType()))
            {
                // Use collecting method that ignores item meta.
                itemsInInventory = Arrays.stream(this.callerUser.getInventory().getContents()).
                    filter(Objects::nonNull).
                    filter(i -> i.getType().equals(required.getType())).
                    collect(Collectors.toList());
            }
            else
            {
                // Use collecting method that compares item meta.
                itemsInInventory = Arrays.stream(this.callerUser.getInventory().getContents()).
                    filter(Objects::nonNull).
                    filter(i -> i.isSimilar(required)).
                    collect(Collectors.toList());
            }

            for (ItemStack itemStack : itemsInInventory)
            {
                if (amountToBeRemoved > 0)
                {
                    ItemStack dummy = itemStack.clone();
                    dummy.setAmount(1);

                    // Remove either the full amount or the remaining amount
                    if (itemStack.getAmount() >= amountToBeRemoved)
                    {
                        itemStack.setAmount(itemStack.getAmount() - amountToBeRemoved);
                        amountToBeRemoved = 0;
                    }
                    else
                    {
                        amountToBeRemoved -= itemStack.getAmount();
                        itemStack.setAmount(0);
                    }
                }
            }

            if (amountToBeRemoved > 0)
            {
                Utils.sendMessage(this.callerUser,
                    this.callerUser.getTranslation(Constants.ERRORS + "could-not-remove-items"));

                this.addon.logError("Could not remove " + amountToBeRemoved + " of " + required.getType() +
                    " from player's inventory!");

                changeBiomeStage.complete(false);
                return;
            }
        }
    }


    /**
     * Withdraw per block boolean.
     *
     * @param changeBiomeStage the change biome stage
     */
    private void withdrawPerBlock(CompletableFuture<Boolean> changeBiomeStage)
    {
        int blockCount = this.getBlockCount();

        if (this.addon.isEconomyProvided())
        {
            this.withdrawMoney(changeBiomeStage, this.biome.getCost() * blockCount);
        }

        if (changeBiomeStage.isDone())
        {
            // Return if already processed.
            return;
        }

        if (!this.biome.getItemCost().isEmpty())
        {
            List<ItemStack> itemCost = Utils.groupEqualItems(this.biome.getItemCost(), Collections.emptySet());
            itemCost.forEach(itemStack -> itemStack.setAmount(itemStack.getAmount() * blockCount));

            this.withdrawItems(changeBiomeStage, itemCost, Collections.emptySet());
        }

        if (changeBiomeStage.isDone())
        {
            // Return if already processed.
            return;
        }

        changeBiomeStage.complete(true);
    }


    /**
     * Withdraw per usage boolean.
     *
     * @param changeBiomeStage the change biome stage
     */
    private void withdrawPerUsage(CompletableFuture<Boolean> changeBiomeStage)
    {
        double increment = this.getUsageIncrement();

        if (this.addon.isEconomyProvided())
        {
            this.withdrawMoney(changeBiomeStage,
                this.biome.getCost() + increment * this.biome.getCost());
        }

        if (changeBiomeStage.isDone())
        {
            // Return if already processed.
            return;
        }

        if (!this.biome.getItemCost().isEmpty())
        {
            List<ItemStack> itemCost = Utils.groupEqualItems(this.biome.getItemCost(), Collections.emptySet());
            itemCost.forEach(itemStack -> itemStack.setAmount(
                itemStack.getAmount() + (int) increment * itemStack.getAmount()));

            this.withdrawItems(changeBiomeStage,
                itemCost,
                Collections.emptySet());
        }

        if (changeBiomeStage.isDone())
        {
            // Return if already processed.
            return;
        }

        changeBiomeStage.complete(true);
    }


    /**
     * Withdraw static boolean.
     *
     * @param changeBiomeStage the change biome stage
     */
    private void withdrawStatic(CompletableFuture<Boolean> changeBiomeStage)
    {
        if (this.addon.isEconomyProvided())
        {
            this.withdrawMoney(changeBiomeStage, this.biome.getCost());
        }

        if (changeBiomeStage.isDone())
        {
            // Return if already processed.
            return;
        }

        if (!this.biome.getItemCost().isEmpty())
        {
            this.withdrawItems(changeBiomeStage,
                Utils.groupEqualItems(this.biome.getItemCost(), Collections.emptySet()),
                Collections.emptySet());
        }

        if (changeBiomeStage.isDone())
        {
            // Return if already processed.
            return;
        }

        changeBiomeStage.complete(true);
    }


    /**
     * This method checks if given biome object is unlocked for island.
     *
     * @return {@code true} if biomeObject is unlocked, {@code false} otherwise.
     */
    private boolean checkUnlockStatus()
    {
        return this.islandData.isUnlocked(this.biome);
    }


    /**
     * This method checks if given biome object is purchased for island.
     *
     * @return {@code true} if biomeObject is unlocked, {@code false} otherwise.
     */
    private boolean checkPurchaseStatus()
    {
        return this.addon.getAddonManager().isPurchased(this.islandData, this.biome);
    }


    /**
     * Check static cost of changing biome.
     *
     * @return {@code true} if player has requirements, {@code false} otherwise.
     */
    private boolean checkStaticCost()
    {
        if (this.addon.isEconomyProvided())
        {
            if (this.addon.getSettings().isUseBankAccount() && this.addon.isBankProvided())
            {
                if (this.addon.getBankAddon().getBankManager().getBalance(this.island).getValue() < this.biome.getCost())
                {
                    // Not enough money.
                    Utils.sendMessage(this.callerUser,
                        this.callerUser.getTranslation(Constants.ERRORS + "not-enough-money-bank",
                            TextVariables.NUMBER,
                            String.valueOf(this.biome.getCost())));
                    return false;
                }
            }
            else if (!this.addon.getVaultHook().has(this.callerUser, this.biome.getCost()))
            {
                // Not enough money.

                Utils.sendMessage(this.callerUser,
                    this.callerUser.getTranslation(Constants.ERRORS + "not-enough-money",
                        TextVariables.NUMBER,
                        String.valueOf(this.biome.getCost())));
                return false;
            }
        }

        if (!this.biome.getItemCost().isEmpty())
        {
            List<ItemStack> missingItemList = new ArrayList<>();

            Utils.groupEqualItems(this.biome.getItemCost(), Collections.emptySet()).forEach(item ->
            {
                if (!Utils.hasRequiredItem(this.callerUser, item, Collections.emptySet()))
                {
                    missingItemList.add(item.clone());
                }
            });

            if (!missingItemList.isEmpty())
            {
                // Not enough items
                StringBuilder missingItems = new StringBuilder();
                missingItemList.forEach(itemStack -> missingItems.
                    append(itemStack.getAmount()).
                    append(" ").
                    append(Utils.prettifyObject(itemStack, this.callerUser)).
                    append(" "));

                Utils.sendMessage(this.callerUser,
                    this.callerUser.getTranslation(Constants.ERRORS + "not-enough-items",
                        "[items]", missingItems.toString()));
                return false;
            }
        }

        return true;
    }


    /**
     * Check per usage cost of changing biome.
     *
     * @return {@code true} if player has requirements, {@code false} otherwise.
     */
    private boolean checkPerUsageCost()
    {
        double increment = this.getUsageIncrement();

        if (this.addon.isEconomyProvided())
        {
            double cost = this.biome.getCost() + increment * this.biome.getCost();

            if (this.addon.getSettings().isUseBankAccount() && this.addon.isBankProvided())
            {
                if (this.addon.getBankAddon().getBankManager().getBalance(this.island).getValue() < cost)
                {
                    // Not enough money.
                    Utils.sendMessage(this.callerUser,
                        this.callerUser.getTranslation(Constants.ERRORS + "not-enough-money-bank",
                            TextVariables.NUMBER,
                            String.valueOf(cost)));
                    return false;
                }
            }
            else if (!this.addon.getVaultHook().has(this.callerUser, cost))
            {
                // Not enough money.

                Utils.sendMessage(this.callerUser,
                    this.callerUser.getTranslation(Constants.ERRORS + "not-enough-money",
                        TextVariables.NUMBER,
                        String.valueOf(cost)));
                return false;
            }
        }

        if (!this.biome.getItemCost().isEmpty())
        {
            List<ItemStack> missingItemList = new ArrayList<>();

            Utils.groupEqualItems(this.biome.getItemCost(), Collections.emptySet()).forEach(item ->
            {
                ItemStack clone = item.clone();
                // Increase items by increment.
                clone.setAmount(clone.getAmount() + clone.getAmount() * (int) increment);

                if (!Utils.hasRequiredItem(this.callerUser, clone, Collections.emptySet()))
                {
                    missingItemList.add(clone);
                }
            });

            if (!missingItemList.isEmpty())
            {
                // Not enough items
                StringBuilder missingItems = new StringBuilder();
                missingItemList.forEach(itemStack -> missingItems.
                    append(itemStack.getAmount()).
                    append(" ").
                    append(Utils.prettifyObject(itemStack, this.callerUser)).
                    append(" "));

                Utils.sendMessage(this.callerUser,
                    this.callerUser.getTranslation(Constants.ERRORS + "not-enough-items",
                        "[items]", missingItems.toString()));
                return false;
            }
        }

        return true;
    }


    /**
     * Gets usage increment.
     *
     * @return the usage increment
     */
    private double getUsageIncrement()
    {
        return this.biome.getCostIncrement() * this.islandData.getBiomeChangeCounter(this.biome);
    }


    /**
     * Check per block cost boolean.
     *
     * @return {@code true} if player has requirements, {@code false} otherwise.
     */
    private boolean checkPerBlockCost()
    {
        int blockCount = this.getBlockCount();

        if (this.addon.isEconomyProvided())
        {
            double cost = this.biome.getCost() * blockCount;

            if (this.addon.getSettings().isUseBankAccount() && this.addon.isBankProvided())
            {
                if (this.addon.getBankAddon().getBankManager().getBalance(this.island).getValue() < cost)
                {
                    // Not enough money.
                    Utils.sendMessage(this.callerUser,
                        this.callerUser.getTranslation(Constants.ERRORS + "not-enough-money-bank",
                            TextVariables.NUMBER,
                            String.valueOf(cost)));
                    return false;
                }
            }
            else if (!this.addon.getVaultHook().has(this.callerUser, cost))
            {
                // Not enough money.

                Utils.sendMessage(this.callerUser,
                    this.callerUser.getTranslation(Constants.ERRORS + "not-enough-money",
                        TextVariables.NUMBER,
                        String.valueOf(cost)));
                return false;
            }
        }

        if (!this.biome.getItemCost().isEmpty())
        {
            List<ItemStack> missingItemList = new ArrayList<>();

            Utils.groupEqualItems(this.biome.getItemCost(), Collections.emptySet()).forEach(item ->
            {
                ItemStack clone = item.clone();
                // Increase items by increment.
                clone.setAmount(clone.getAmount() * blockCount);

                if (!Utils.hasRequiredItem(this.callerUser, clone, Collections.emptySet()))
                {
                    missingItemList.add(clone);
                }
            });

            if (!missingItemList.isEmpty())
            {
                // Not enough items
                StringBuilder missingItems = new StringBuilder();
                missingItemList.forEach(itemStack -> missingItems.
                    append(itemStack.getAmount()).
                    append(" ").
                    append(Utils.prettifyObject(itemStack, this.callerUser)).
                    append(" "));

                Utils.sendMessage(this.callerUser,
                    this.callerUser.getTranslation(Constants.ERRORS + "not-enough-items",
                        "[items]", missingItems.toString()));
                return false;
            }
        }

        return true;
    }


    /**
     * Gets block count.
     *
     * @return the block count
     */
    private int getBlockCount()
    {
        return (this.maxCoordinate.getBlockX() - this.minCoordinate.getBlockX()) *
            (this.maxCoordinate.getBlockZ() - this.minCoordinate.getBlockZ()) *
            (this.maxCoordinate.getBlockY() - this.minCoordinate.getBlockY());
    }


    /**
     * This method checks if user has all required permissions.
     *
     * @return true if user has all required permissions, otherwise false.
     */
    private boolean checkPermissions()
    {
        return this.biome.getUnlockPermissions().isEmpty() ||
            this.biome.getUnlockPermissions().stream().allMatch(this.callerUser::hasPermission);
    }


    /**
     * This method returns if the caller user have a permission to change biome with a given update mode for given
     * biomeObject.
     *
     * @return {@code true} if caller has permission to use given update mode on given biomeObject {@code false}
     * otherwise.
     */
    public boolean hasPermissionToUpdateMode()
    {
        // TODO: probably passing permission string to helper would be more efficient.
        return Utils.hasUserUpdateModePermission(this.callerUser,
            this.addon.getPlugin().getIWM().getPermissionPrefix(this.world),
            this.updateMode,
            this.biome.getUniqueId());
    }


    // ---------------------------------------------------------------------
    // Section: Variables
    // ---------------------------------------------------------------------


    /**
     * This variable stores caller addon.
     */
    private final BiomesAddon addon;

    /**
     * This variable stores User that calls update.
     */
    private final User callerUser;

    /**
     * This variable stores User that is targeted by update.
     */
    private final User targetUser;

    /**
     * IslandDataObject that stores island related data.
     */
    private final BiomesIslandDataObject islandData;

    /**
     * This variable stores BiomesObject that must be applied.
     */
    private final BiomesObject biome;

    /**
     * This variable stores update mode.
     */
    private final UpdateMode updateMode;

    /**
     * This variable stores how large update region must be.
     */
    private final int range;

    /**
     * This variable stores update world.
     */
    private final World world;

    /**
     * This variable stores if money from caller can be withdrawn.
     */
    private final boolean canWithdraw;

    /**
     * This variable stores if world protection flag is enabled. Avoids checking it each time as flag will not change
     * its value while updating.
     */
    private final boolean worldProtectionFlag;

    /**
     * The Island.
     */
    private Island island;

    /**
     * This variable holds from which location Update process should start.
     */
    private Location standingLocation;

    /**
     * This variable stores minCoordinate for area where biome is changed.
     */
    private BlockVector minCoordinate;

    /**
     * This variable stores maxCoordinate for area where biome is changed.
     */
    private BlockVector maxCoordinate;
}
