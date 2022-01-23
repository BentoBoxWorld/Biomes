//
// Created by BONNe
// Copyright - 2020
//


package world.bentobox.biomes.panels.admin;


import org.bukkit.Material;
import org.bukkit.World;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import lv.id.bonne.panelutils.PanelUtils;
import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.biomes.BiomesAddon;
import world.bentobox.biomes.panels.CommonPanel;
import world.bentobox.biomes.panels.ConversationUtils;
import world.bentobox.biomes.utils.Constants;
import world.bentobox.biomes.utils.Utils;


/**
 * This class creates and manages Admin Panel for Biomes.
 */
public class AdminPanel extends CommonPanel
{
    /**
     * Instantiates a new Admin panel.
     *
     * @param addon the addon
     * @param user the user
     * @param world the world
     * @param topLabel the top label
     * @param permissionPrefix the permission prefix
     */
    private AdminPanel(BiomesAddon addon,
        User user,
        World world,
        String topLabel,
        String permissionPrefix)
    {
        super(addon, user, world, topLabel, permissionPrefix);
    }


    /**
     * This method allows to build panel.
     */
    @Override
    protected void build()
    {
        // PanelBuilder is a BentoBox API that provides ability to easy create Panels.
        PanelBuilder panelBuilder = new PanelBuilder().
            user(this.user).
            name(this.user.getTranslation(Constants.TITLE + "admin-panel"));

        PanelUtils.fillBorder(panelBuilder, Material.MAGENTA_STAINED_GLASS_PANE);

        panelBuilder.item(10, this.createButton(Action.MANAGE_ISLANDS));
        panelBuilder.item(28, this.createButton(Action.WIPE_ISLANDS_DATA));

        panelBuilder.item(12, this.createButton(Action.MANAGE_BIOMES));
        panelBuilder.item(21, this.createButton(Action.MANAGE_BUNDLES));

        panelBuilder.item(14, this.createButton(Action.IMPORT_TEMPLATE));

        panelBuilder.item(15, this.createButton(Action.WEB_LIBRARY));
        panelBuilder.item(24, this.createButton(Action.EXPORT_FROM_DATABASE));
        panelBuilder.item(33, this.createButton(Action.IMPORT_TO_DATABASE));

        panelBuilder.item(16, this.createButton(Action.SETTINGS));
        panelBuilder.item(34, this.createButton(Action.WIPE_BIOMES_DATA));

        panelBuilder.item(44, this.returnButton);
        panelBuilder.build();
    }


// ---------------------------------------------------------------------
// Section: Methods
// ---------------------------------------------------------------------


    /**
     * Create button panel item with a given button type.
     *
     * @param button the button
     * @return the panel item
     */
    private PanelItem createButton(Action button)
    {
        final String reference = Constants.BUTTON + button.name().toLowerCase();
        String name = this.user.getTranslation(reference + ".name");
        List<String> description = new ArrayList<>();
        description.add(this.user.getTranslationOrNothing(reference + ".description"));

        Material material;
        PanelItem.ClickHandler clickHandler;
        boolean glow = false;

        switch (button)
        {
            case MANAGE_ISLANDS:
            {
                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-open"));

                clickHandler = (panel, user1, clickType, slot) ->
                {
                    IslandManagePanel.open(this);
                    return true;
                };
                material = Material.PLAYER_HEAD;
                break;
            }
            case MANAGE_BIOMES:
            {
                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-open"));

                clickHandler = (panel, user1, clickType, slot) ->
                {
                    BiomeManagePanel.open(this);
                    return true;
                };
                material = Material.COBBLESTONE;
                break;
            }
            case MANAGE_BUNDLES:
            {
                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-open"));

                clickHandler = (panel, user1, clickType, slot) ->
                {
                    BundleManagePanel.open(this);
                    return true;
                };
                material = Material.CHEST;
                break;
            }
            case SETTINGS:
            {
                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-open"));

                clickHandler = (panel, user1, clickType, slot) ->
                {
                    SettingsPanel.open(this);
                    return true;
                };
                material = Material.CRAFTING_TABLE;
                break;
            }
            case IMPORT_TEMPLATE:
            {
                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-open"));

                clickHandler = (panel, user1, clickType, slot) ->
                {
                    LibraryPanel.open(this, LibraryPanel.Library.TEMPLATE);
                    return true;
                };
                material = Material.BOOKSHELF;
                break;
            }
            case WEB_LIBRARY:
            {
                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-open"));

                clickHandler = (panel, user1, clickType, slot) ->
                {
                    LibraryPanel.open(this, LibraryPanel.Library.WEB);
                    return true;
                };
                material = Material.COBWEB;
                break;
            }
            case EXPORT_FROM_DATABASE:
            {
                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-export"));

                clickHandler = (panel, user1, clickType, slot) ->
                {
                    // This consumer process file exporting after user input is returned.
                    Consumer<String> fileNameConsumer = value ->
                    {
                        if (value != null)
                        {
                            this.addon.getImportManager().generateDatabaseFile(this.user,
                                this.world,
                                Utils.sanitizeInput(value));
                        }

                        this.build();
                    };

                    // This function checks if file can be created.
                    Function<String, Boolean> validationFunction = fileName ->
                    {
                        String sanitizedName = Utils.sanitizeInput(fileName);
                        return !new File(this.addon.getDataFolder(),
                            sanitizedName.endsWith(".json") ? sanitizedName : sanitizedName + ".json").exists();
                    };

                    // Call a conversation API to get input string.
                    ConversationUtils.createIDStringInput(fileNameConsumer,
                        validationFunction,
                        this.user,
                        this.user.getTranslation(Constants.CONVERSATIONS + "exported-file-name"),
                        this.user.getTranslation(Constants.CONVERSATIONS + "database-export-completed",
                            Constants.PARAMETER_WORLD, world.getName()),
                        Constants.CONVERSATIONS + "file-name-exist");

                    return true;
                };
                material = Material.HOPPER;
                break;
            }
            case IMPORT_TO_DATABASE:
            {
                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-open"));

                clickHandler = (panel, user1, clickType, slot) ->
                {
                    LibraryPanel.open(this, LibraryPanel.Library.DATABASE);
                    return true;
                };
                material = Material.BOOKSHELF;
                glow = true;
                break;
            }
            case WIPE_ISLANDS_DATA:
            {
                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-wipe"));

                clickHandler = (panel, user1, clickType, slot) ->
                {
                    // Create consumer that accepts value from conversation.
                    Consumer<Boolean> consumer = value ->
                    {
                        if (value)
                        {
                            this.addon.getAddonManager().
                                wipeIslandData(this.addon.getPlugin().getIWM().getAddon(this.world));
                        }

                        this.build();
                    };
                    // Create conversation that gets user acceptance to delete island data.
                    ConversationUtils.createConfirmation(
                        consumer,
                        this.user,
                        this.user.getTranslation(Constants.CONVERSATIONS + "confirm-island-data-deletion",
                            Constants.PARAMETER_GAMEMODE, Utils.getGameMode(this.world)),
                        this.user.getTranslation(Constants.CONVERSATIONS + "user-data-removed",
                            Constants.PARAMETER_GAMEMODE, Utils.getGameMode(this.world)));

                    return true;
                };

                material = Material.TNT;
                break;
            }
            case WIPE_BIOMES_DATA:
            {
                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-wipe"));

                clickHandler = (panel, user1, clickType, slot) ->
                {
                    // Create consumer that accepts value from conversation.
                    Consumer<Boolean> consumer = value ->
                    {
                        if (value)
                        {
                            this.addon.getAddonManager().
                                wipeGameModeBiomes(this.addon.getPlugin().getIWM().getAddon(this.world));
                        }

                        this.build();
                    };
                    // Create conversation that gets user acceptance to delete generator data.
                    ConversationUtils.createConfirmation(
                        consumer,
                        this.user,
                        this.user.getTranslation(Constants.CONVERSATIONS + "confirm-biome-data-deletion",
                            Constants.PARAMETER_GAMEMODE, Utils.getGameMode(this.world)),
                        this.user.getTranslation(Constants.CONVERSATIONS + "biome-data-removed",
                            Constants.PARAMETER_GAMEMODE, Utils.getGameMode(this.world)));

                    return true;
                };

                material = Material.TNT;
                break;
            }
            default:
                return PanelItem.empty();
        }

        return new PanelItemBuilder().
            name(name).
            description(description).
            icon(material).
            clickHandler(clickHandler).
            glow(glow).
            build();
    }


    /**
     * Open panel from outside.
     *
     * @param addon the addon
     * @param world the world
     * @param user the user
     * @param topLabel the top label
     * @param permissionPrefix the permission prefix
     */
    public static void open(BiomesAddon addon,
        World world,
        User user,
        String topLabel,
        String permissionPrefix)
    {
        new AdminPanel(addon, user, world, topLabel, permissionPrefix).build();
    }


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------


    /**
     * This enum holds variable that allows to switch between button creation.
     */
    private enum Action
    {
        /**
         * Manage islands action.
         */
        MANAGE_ISLANDS,
        /**
         * Manage biomes action.
         */
        MANAGE_BIOMES,
        /**
         * Manage bundles action.
         */
        MANAGE_BUNDLES,
        /**
         * Settings action.
         */
        SETTINGS,
        /**
         * Import template action.
         */
        IMPORT_TEMPLATE,
        /**
         * Web library action.
         */
        WEB_LIBRARY,
        /**
         * Export from database action.
         */
        EXPORT_FROM_DATABASE,
        /**
         * Import to database action.
         */
        IMPORT_TO_DATABASE,
        /**
         * Wipe island data action.
         */
        WIPE_ISLANDS_DATA,
        /**
         * Wipe biome data action.
         */
        WIPE_BIOMES_DATA
    }
}
