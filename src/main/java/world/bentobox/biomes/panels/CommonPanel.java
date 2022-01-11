//
// Created by BONNe
// Copyright - 2021
//


package world.bentobox.biomes.panels;


import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.util.Util;
import world.bentobox.biomes.BiomesAddon;
import world.bentobox.biomes.database.objects.BiomesBundleObject;
import world.bentobox.biomes.database.objects.BiomesIslandDataObject;
import world.bentobox.biomes.database.objects.BiomesObject;
import world.bentobox.biomes.managers.BiomesAddonManager;
import world.bentobox.biomes.utils.Constants;
import world.bentobox.biomes.utils.Utils;


/**
 * This class contains common methods for all panels.
 */
public abstract class CommonPanel
{
    /**
     * Instantiates a new Common panel.
     *
     * @param addon the addon
     * @param user the user
     * @param world the world
     * @param topLabel the top label
     * @param permissionPrefix the permission prefix
     */
    protected CommonPanel(BiomesAddon addon, User user, World world, String topLabel, String permissionPrefix)
    {
        this.addon = addon;
        this.world = world;
        this.manager = addon.getAddonManager();
        this.user = user;

        this.topLabel = topLabel;
        this.permissionPrefix = permissionPrefix;

        this.parentPanel = null;

        this.returnButton = new PanelItemBuilder().
            name(this.user.getTranslation(Constants.BUTTON + "quit.name")).
            description(this.user.getTranslationOrNothing(Constants.BUTTON + "quit.description")).
            description("").
            description(this.user.getTranslationOrNothing(Constants.TIPS + "click-to-quit")).
            icon(Material.OAK_DOOR).
            clickHandler((panel, user1, clickType, i) -> {
                this.user.closeInventory();
                return true;
            }).build();
    }


    /**
     * This is default constructor for all classes that extends CommonPanel.
     *
     * @param parentPanel Parent panel of current panel.
     */
    protected CommonPanel(@NotNull CommonPanel parentPanel)
    {
        this.addon = parentPanel.addon;
        this.manager = parentPanel.manager;
        this.user = parentPanel.user;
        this.world = parentPanel.world;

        this.topLabel = parentPanel.topLabel;
        this.permissionPrefix = parentPanel.permissionPrefix;

        this.parentPanel = parentPanel;

        this.returnButton = new PanelItemBuilder().
            name(this.user.getTranslation(Constants.BUTTON + "return.name")).
            description(this.user.getTranslationOrNothing(Constants.BUTTON + "return.description")).
            description("").
            description(this.user.getTranslationOrNothing(Constants.TIPS + "click-to-return")).
            icon(Material.OAK_DOOR).
            clickHandler((panel, user1, clickType, i) -> {
                this.parentPanel.build();
                return true;
            }).build();
    }


    /**
     * This method allows building panel.
     */
    protected abstract void build();


// ---------------------------------------------------------------------
// Section: Biome Description generator
// ---------------------------------------------------------------------


    /**
     * This method generates biomesObject description.
     * @param biome Biomes Object.
     * @param target User who is targeted with biomes object.
     * @return String that contains info about biomes object.
     */
    protected String generateBiomesDescription(BiomesObject biome, @Nullable User target)
    {
        final String reference = Constants.DESCRIPTIONS + "biome.";

        // Get description from custom translations
        String description = this.user.getTranslationOrNothing(
            "biomes.custom-biome."  + biome.getUniqueId() + ".description");

        if (description.isEmpty())
        {
            // Get data from object in single string.
            description = Util.translateColorCodes(String.join("\n", biome.getDescription()));
        }

        String originalBiome = this.user.getTranslationOrNothing(reference + "original",
            "[biome]", Utils.prettifyObject(biome.getBiome(), this.user));

        String unlockDescription = this.generateBiomesUnlockDescription(biome, target);
        String changeDescription = this.generateBiomesChangeDescription(biome, target);

        String returnString = this.user.getTranslationOrNothing(reference + "lore",
            "[description]", description,
            "[biome]", originalBiome,
            "[change]", changeDescription,
            "[unlock]", unlockDescription);

        // Remove empty lines and returns as a list.
        return returnString.replaceAll("(?m)^[ \\t]*\\r?\\n", "");
    }


    /**
     * This method generates biomesObject unlock description.
     * @param biome Biomes object.
     * @param target User who is targeted with biomes object.
     * @return String that contains unlock message.
     */
    private String generateBiomesUnlockDescription(BiomesObject biome, @Nullable User target)
    {
        final String reference = Constants.DESCRIPTIONS + "biome.unlock.";

        if (target != null)
        {
            BiomesIslandDataObject islandData = this.addon.getAddonManager().getIslandData(this.world, target);

            if (islandData != null && islandData.getUnlockedBiomes().contains(biome.getUniqueId()))
            {
                // Do not generate message for unlocked biomes.
                return "";
            }
        }

        String items = this.generateUnlockItemPrice(biome, target);
        String price = this.generateUnlockPrice(biome, target);

        String level;

        if (!this.addon.isLevelProvided() || biome.getUnlockLevel() <= 0)
        {
            level = "";
        }
        else
        {
            if (target == null ||
                this.addon.getLevelAddon().getIslandLevel(world, target.getUniqueId()) < biome.getUnlockLevel())
            {
                level = this.user.getTranslationOrNothing(reference + "level",
                    "[number]", String.valueOf(biome.getUnlockLevel()));
            }
            else
            {
                level = "";
            }
        }

        String permissions = this.generatePermissions(biome, target);

        return this.user.getTranslationOrNothing(reference + "lore",
            "[items]", items,
            "[money]", price,
            "[level]", level,
            "[permissions]", permissions);
    }


    /**
     * This method generates price description for changing biome.
     * @param biome Biome that will be changed.
     * @param target User who is targeted.
     * @return Price description.
     */
    private String generateUnlockPrice(BiomesObject biome, @Nullable User target)
    {
        final String reference = Constants.DESCRIPTIONS + "biomes.unlock.";

        return !this.addon.isEconomyProvided() || biome.getUnlockCost() <= 0 ? "" :
            this.user.getTranslationOrNothing(reference + "money",
                "[number]", String.valueOf(biome.getUnlockCost()));
    }


    /**
     * This method generates item price description for changing biome.
     * @param biome Biome that will be changed.
     * @param target User who is targeted.
     * @return Price description.
     */
    private String generateUnlockItemPrice(BiomesObject biome, @Nullable User target)
    {
        final String reference = Constants.DESCRIPTIONS + "biome.unlock.";

        if (!biome.getUnlockItems().isEmpty())
        {
            // Yes list duplication for complete menu.
            List<ItemStack> missingItems = Utils.groupEqualItems(biome.getUnlockItems(), Collections.emptySet());

            StringBuilder itemBuilder = new StringBuilder();

            if (missingItems.size() == 1)
            {
                itemBuilder.append(this.user.getTranslationOrNothing(reference + "item-single",
                        "[number]", String.valueOf(missingItems.get(0).getAmount()),
                    "[item]", Utils.prettifyObject(missingItems.get(0), this.user)));
            }
            else if (!missingItems.isEmpty())
            {
                itemBuilder.append(this.user.getTranslationOrNothing(reference + "item-title"));
                missingItems.forEach(item ->
                {
                    itemBuilder.append("\n");
                    itemBuilder.append(this.user.getTranslationOrNothing(reference + "item-list",
                        "[number]", String.valueOf(item.getAmount()),
                        "[item]", Utils.prettifyObject(item, this.user)));
                });
            }

            return itemBuilder.toString();
        }
        else
        {
            return "";
        }
    }


    /**
     * This method generates permission description for changing biome.
     * @param biome Biome that will be changed.
     * @param target User who is targeted.
     * @return Permission description.
     */
    private String generatePermissions(BiomesObject biome, @Nullable User target)
    {
        final String reference = Constants.DESCRIPTIONS + "biome.unlock.";

        if (!biome.getUnlockPermissions().isEmpty())
        {
            // Yes list duplication for complete menu.
            List<String> missingPermissions = biome.getUnlockPermissions().stream().
                filter(permission -> target == null || !target.hasPermission(permission)).
                sorted().
                collect(Collectors.toList());

            StringBuilder permissionBuilder = new StringBuilder();

            if (missingPermissions.size() == 1)
            {
                permissionBuilder.append(this.user.getTranslationOrNothing(reference + "permission-single",
                    Constants.PARAMETER_PERMISSION, missingPermissions.get(0)));
            }
            else if (!missingPermissions.isEmpty())
            {
                permissionBuilder.append(this.user.getTranslationOrNothing(reference + "permissions-title"));
                missingPermissions.forEach(permission ->
                {
                    permissionBuilder.append("\n");
                    permissionBuilder.append(this.user.getTranslationOrNothing(reference + "permissions-list",
                        Constants.PARAMETER_PERMISSION, permission));
                });
            }

            return permissionBuilder.toString();
        }
        else
        {
            return "";
        }
    }


    /**
     * This method generates biomesObject change description.
     * @param biome Biomes object.
     * @param target User who is targeted with biomes object.
     * @return String that contains change message.
     */
    private String generateBiomesChangeDescription(BiomesObject biome, @Nullable User target)
    {
        final String reference = Constants.DESCRIPTIONS + "biome.change.";

        BiomesIslandDataObject islandData = this.addon.getAddonManager().getIslandData(this.world, target);

        if (islandData != null && !islandData.getUnlockedBiomes().contains(biome.getUniqueId()))
        {
            // Do not generate message for locked biomes.
            return "";
        }

        String mode = this.user.getTranslationOrNothing(reference + "mode." + biome.getCostMode().name().toLowerCase());
        String items = this.generateChangeItemPrice(biome, islandData);
        String price = this.generateChangePrice(biome, islandData);

        return this.user.getTranslationOrNothing(reference + "lore",
            "[mode]", mode,
            "[items]", items,
            "[money]", price);
    }


    /**
     * This method generates price description for changing biome.
     * @param biome Biome that will be changed.
     * @param target island who is targeted.
     * @return Price description.
     */
    private String generateChangePrice(BiomesObject biome, @Nullable BiomesIslandDataObject target)
    {
        final String reference = Constants.DESCRIPTIONS + "biomes.change.";

        double cost = biome.getCost();

        if (target != null &&
            BiomesObject.CostMode.PER_USAGE.equals(biome.getCostMode()) &&
            biome.getCostIncrement() > 0 &&
            target.getBiomeChangeCounter().containsKey(biome.getUniqueId()))
        {
            int counter = target.getBiomeChangeCounter().get(biome.getUniqueId());

            cost = cost + biome.getCostIncrement() * cost * counter;
        }

        return !this.addon.isEconomyProvided() ||cost <= 0 ? "" :
            this.user.getTranslationOrNothing(reference + "money",
                "[number]", String.valueOf(cost));
    }


    /**
     * This method generates item description for changing biome.
     * @param biome Biome that will be changed.
     * @param target island who is targeted.
     * @return Item description.
     */
    private String generateChangeItemPrice(BiomesObject biome, @Nullable BiomesIslandDataObject target)
    {
        final String reference = Constants.DESCRIPTIONS + "biome.change.";

        if (!biome.getUnlockItems().isEmpty())
        {
            double increment;

            if (target != null &&
                BiomesObject.CostMode.PER_USAGE.equals(biome.getCostMode()) &&
                biome.getCostIncrement() > 0 &&
                target.getBiomeChangeCounter().containsKey(biome.getUniqueId()))
            {
                increment = biome.getCostIncrement() * target.getBiomeChangeCounter().get(biome.getUniqueId());
            }
            else
            {
                increment = 0;
            }

            // Yes list duplication for complete menu.
            List<ItemStack> missingItems = Utils.groupEqualItems(biome.getUnlockItems(), Collections.emptySet());

            StringBuilder itemBuilder = new StringBuilder();

            if (missingItems.size() == 1)
            {
                int amount = missingItems.get(0).getAmount();

                itemBuilder.append(this.user.getTranslationOrNothing(reference + "item-single",
                    "[number]", String.valueOf((int) (amount + amount * increment)),
                    "[item]", Utils.prettifyObject(missingItems.get(0), this.user)));
            }
            else if (!missingItems.isEmpty())
            {
                itemBuilder.append(this.user.getTranslationOrNothing(reference + "item-title"));
                missingItems.forEach(item ->
                {
                    int amount = item.getAmount();

                    itemBuilder.append("\n");
                    itemBuilder.append(this.user.getTranslationOrNothing(reference + "item-list",
                        "[number]", String.valueOf((int) (amount + amount * increment)),
                        "[item]", Utils.prettifyObject(item, this.user)));
                });
            }

            return itemBuilder.toString();
        }
        else
        {
            return "";
        }
    }


// ---------------------------------------------------------------------
// Section: Bundle description
// ---------------------------------------------------------------------


    /**
     * Admin should see simplified view. It is not necessary to view all unnecessary things.
     *
     * @param bundle Bundle which description must be generated.
     * @return List of strings that describes bundle.
     */
    protected String generateBundleDescription(BiomesBundleObject bundle)
    {
        final String reference = Constants.DESCRIPTIONS + "bundle.";

        StringBuilder descriptionBuilder = new StringBuilder();
        bundle.getDescription().forEach(line ->
            descriptionBuilder.append(Util.translateColorCodes(line)).append("\n"));

        String permission;

        if (bundle != BiomesBundleObject.dummyBundle)
        {
            permission = this.user.getTranslation(reference + "permission",
                Constants.PARAMETER_ID, bundle.getUniqueId(),
                Constants.PARAMETER_GAMEMODE, Utils.getGameMode(this.world).toLowerCase());
        }
        else
        {
            permission = "";
        }

        StringBuilder biomesBuilder = new StringBuilder();

        // Add missing permissions
        if (!bundle.getBiomeObjects().isEmpty())
        {
            biomesBuilder.append(this.user.getTranslation(reference + "title"));

            bundle.getBiomeObjects().stream().
                map(this.manager::getBiomeByID).
                filter(Objects::nonNull).
                forEach(biome -> biomesBuilder.append("\n").
                    append(this.user.getTranslation(reference + "value",
                        Constants.PARAMETER_BIOME, biome.getFriendlyName())));
        }
        else
        {
            biomesBuilder.append(this.user.getTranslation(reference + "no-biomes"));
        }


        String returnString = this.user.getTranslationOrNothing(reference + "lore",
            "[description]", descriptionBuilder.toString(),
            "[biomes]", biomesBuilder.toString(),
            "[permission]", permission);

        // Remove empty lines and returns as a list.

        return returnString.replaceAll("(?m)^[ \\t]*\\r?\\n", "");
    }


// ---------------------------------------------------------------------
// Section: Methods
// ---------------------------------------------------------------------


    /**
     * This method reopens given panel.
     * @param panel Panel that must be reopened.
     */
    public static void reopen(CommonPanel panel)
    {
        panel.build();
    }


    /**
     * This method finds and try to execute given sub command with given arguments.
     * @param subCommand Sub Command that need to be called.
     * @param arguments List of arguments for current command.
     */
    protected void callCommand(String subCommand, List<String> arguments)
    {
        CompositeCommand command = this.addon.getPlugin().getCommandsManager().getCommand(this.topLabel);

        if (command == null)
        {
            // TODO: Throw error that top command not found.
            return;
        }

        Optional<CompositeCommand> commandOptional =
            command.getSubCommand(this.addon.getSettings().getPlayerCommand().split(" ")[0]);

        if (commandOptional.isEmpty())
        {
            // TODO: Throw error that biomes command not found.
            return;
        }

        commandOptional = commandOptional.get().getSubCommand(subCommand);

        if (commandOptional.isEmpty())
        {
            // TODO: Throw error that biomes sub-command not found.
            return;
        }

        command = commandOptional.get();

        if (command.canExecute(this.user, subCommand, arguments))
        {
            command.execute(this.user, subCommand, arguments);
        }

        this.user.closeInventory();
    }


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------


    /**
     * This variable stores parent gui.
     */
    @Nullable
    protected final CommonPanel parentPanel;

    /**
     * Variable stores Challenges addon.
     */
    protected final BiomesAddon addon;

    /**
     * Variable stores Challenges addon manager.
     */
    protected final BiomesAddonManager manager;

    /**
     * Variable stores world in which panel is referred to.
     */
    protected final World world;

    /**
     * Variable stores user who created this panel.
     */
    protected final User user;

    /**
     * Variable stores top label of command from which panel was called.
     */
    protected final String topLabel;

    /**
     * Variable stores permission prefix of command from which panel was called.
     */
    protected final String permissionPrefix;

    /**
     * This object holds PanelItem that allows to return to previous panel.
     */
    protected PanelItem returnButton;
}
