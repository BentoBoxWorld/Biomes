//
// Created by BONNe
// Copyright - 2021
//


package world.bentobox.biomes.panels;


import org.bukkit.Material;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.List;
import java.util.stream.Collectors;

import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.util.Util;
import world.bentobox.biomes.BiomesAddon;
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


    /**
     * This method generates biomesObject description.
     * @param biome Biomes Object.
     * @param target User who will view biomes object.
     * @return String that contains info about biomes object.
     */
    protected String generateBiomesDescription(BiomesObject biome, @Nullable User target)
    {
        final String reference = Constants.DESCRIPTIONS + "biomes.";

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

        String price = this.generatePrice(biome, this.user);

        String level;

        if (!this.addon.isLevelProvided() || biome.getRequiredLevel() <= 0)
        {
            level = "";
        }
        else
        {
            if (target == null ||
                this.addon.getLevelAddon().getIslandLevel(world, target.getUniqueId()) < biome.getRequiredLevel())
            {
                level = this.user.getTranslationOrNothing(reference + "level",
                    "[number]", String.valueOf(biome.getRequiredLevel()));
            }
            else
            {
                level = "";
            }
        }

        String permissions = this.generatePermissions(biome, target);

        String returnString = this.user.getTranslationOrNothing(reference + "biome.lore",
            "[description]", description,
            "[biome]", originalBiome,
            "[price]", price,
            "[level]", level,
            "[permissions]", permissions);

        // Remove empty lines and returns as a list.

        return returnString.replaceAll("(?m)^[ \\t]*\\r?\\n", "");
    }


    /**
     * This method generates price description for changing biome.
     * @param biome Biome that will be changed.
     * @param target User who is targeted.
     * @return Price description.
     */
    private String generatePrice(BiomesObject biome, @Nullable User target)
    {
        final String reference = Constants.DESCRIPTIONS + "biomes.";

        return !this.addon.isEconomyProvided() || biome.getRequiredCost() <= 0 ? "" :
            this.user.getTranslationOrNothing(reference + "money",
                "[number]", String.valueOf(biome.getRequiredCost()));
    }


    /**
     * This method generates permission description for changing biome.
     * @param biome Biome that will be changed.
     * @param target User who is targeted.
     * @return Permission description.
     */
    private String generatePermissions(BiomesObject biome, @Nullable User target)
    {
        final String reference = Constants.DESCRIPTIONS + "biome.";

        if (!biome.getRequiredPermissions().isEmpty())
        {
            // Yes list duplication for complete menu.
            List<String> missingPermissions = biome.getRequiredPermissions().stream().
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
     * This method reopens given panel.
     * @param panel Panel that must be reopened.
     */
    public static void reopen(CommonPanel panel)
    {
        panel.build();
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
