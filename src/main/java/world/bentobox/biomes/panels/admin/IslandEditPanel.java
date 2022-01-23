///
// Created by BONNe
// Copyright - 2022
///

package world.bentobox.biomes.panels.admin;


import com.google.common.collect.ImmutableSet;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import lv.id.bonne.panelutils.PanelUtils;
import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.biomes.database.objects.BiomesBundleObject;
import world.bentobox.biomes.database.objects.BiomesIslandDataObject;
import world.bentobox.biomes.database.objects.BiomesObject;
import world.bentobox.biomes.panels.CommonPagedPanel;
import world.bentobox.biomes.panels.CommonPanel;
import world.bentobox.biomes.panels.ConversationUtils;
import world.bentobox.biomes.panels.user.AdvancedPanel;
import world.bentobox.biomes.panels.util.BundleSelector;
import world.bentobox.biomes.utils.Constants;
import world.bentobox.biomes.utils.Utils;


/**
 * This class opens GUI that shows bundle view for user.
 */
public class IslandEditPanel extends CommonPagedPanel<BiomesObject>
{
    // ---------------------------------------------------------------------
    // Section: Internal Constructor
    // ---------------------------------------------------------------------


    /**
     * This is internal constructor. It is used internally in current class to avoid creating objects everywhere.
     *
     * @param panel Parent Panel
     * @param island Island that must be displayed.
     */
    private IslandEditPanel(CommonPanel panel,
        Island island)
    {
        super(panel);

        this.island = island;
        this.islandData = this.addon.getAddonManager().getIslandData(island);

        this.title = this.island.getName();

        if (this.title == null || this.title.equals(""))
        {
            // Deal with situations when island name is not set.

            if (island.getOwner() != null)
            {
                User user = User.getInstance(island.getOwner());

                this.title = this.user.getTranslation(Constants.DESCRIPTIONS + "island-owner",
                    Constants.PARAMETER_PLAYER, user.getName());
            }
            else
            {
                this.title = this.user.getTranslation(Constants.DESCRIPTIONS + "island-owner",
                    Constants.PARAMETER_PLAYER, this.user.getTranslation(Constants.DESCRIPTIONS + "unknown"));
            }
        }

        // Store generators in local list to avoid building it every time.
        this.elementList = this.manager.getIslandBiomes(world, this.islandData);
    }


    /**
     * This method builds this GUI.
     */
    @Override
    public void build()
    {
        // Do not enable this GUI if there is an issue with getting data.
        if (this.islandData == null || this.island == null)
        {
            Utils.sendMessage(this.user, this.user.getTranslation(
                Constants.ERRORS + "no-island-data"));
            return;
        }

        // PanelBuilder is a BentoBox API that provides ability to easy create Panels.
        PanelBuilder panelBuilder = new PanelBuilder().
            user(this.user).
            name(this.user.getTranslation(Constants.TITLE + "edit-island",
                Constants.PARAMETER_ISLAND, this.title));

        PanelUtils.fillBorder(panelBuilder, Material.MAGENTA_STAINED_GLASS_PANE);

        // Add buttons somewhere at the top.
        panelBuilder.item(1, this.createButton(Button.ISLAND_NAME));

        panelBuilder.item(3, this.createButton(Button.ISLAND_BUNDLE));
        panelBuilder.item(4, this.createButton(Button.OWNER_BUNDLE));

        panelBuilder.item(8, this.createButton(Button.RESET_TO_DEFAULT));

        this.populateElements(panelBuilder, this.elementList);

        panelBuilder.item(44, this.returnButton);

        // Build panel.
        panelBuilder.build();
    }


// ---------------------------------------------------------------------
// Section: Methods
// ---------------------------------------------------------------------


    @Override
    protected void updateFilters()
    {
        // Do nothing with filters.
    }


    /**
     * This method creates button for BiomesObject.
     *
     * @param biomesObject BiomesObject which button must be created.
     * @return PanelItem for BiomesObject.
     */
    @Override
    protected PanelItem createElementButton(BiomesObject biomesObject)
    {
        boolean glow = false;

        List<String> description = new ArrayList<>();
        description.add(this.generateBiomesDescription(biomesObject,
            this.island.getOwner() != null ? User.getInstance(this.island.getOwner()) : null));

        description.add("");
        description.add(this.user.getTranslation(Constants.TIPS + "left-click-to-apply"));

        if (!this.islandData.isUnlocked(biomesObject))
        {
            description.add(this.user.getTranslation(Constants.TIPS + "right-click-to-unlock"));
        }
        else if (!this.islandData.isPurchased(biomesObject))
        {
            description.add(this.user.getTranslation(Constants.TIPS + "right-click-to-buy"));
        }
        else
        {
            description.add(this.user.getTranslation(Constants.TIPS + "right-click-to-change"));
        }

        PanelItem.ClickHandler clickHandler = (panel, user, clickType, i) ->
        {

            if (clickType.isRightClick())
            {
                if (!this.islandData.isUnlocked(biomesObject))
                {
                    this.islandData.unlockBiome(biomesObject);
                    this.manager.saveIslandData(this.islandData);
                    this.build();
                }
                else if (!this.manager.isPurchased(this.islandData, biomesObject))
                {
                    this.islandData.purchaseBiome(biomesObject);
                    this.manager.saveIslandData(this.islandData);
                    this.build();
                }
                else
                {
                    Consumer<Number> numberConsumer = number ->
                    {
                        if (number != null)
                        {
                            this.islandData.adjustBiomeChangeCounter(biomesObject, number.intValue());
                            this.manager.saveIslandData(this.islandData);
                        }

                        // reopen panel
                        this.build();
                    };

                    ConversationUtils.createNumericInput(numberConsumer,
                        this.user,
                        this.user.getTranslation(Constants.CONVERSATIONS + "input-number"),
                        0,
                        Integer.MAX_VALUE);
                }
            }
            else
            {
                AdvancedPanel.open(this, biomesObject, User.getInstance(this.island.getOwner()));
            }

            // Always return true.
            return true;
        };

        return new PanelItemBuilder().
            name(biomesObject.getFriendlyName()).
            description(description).
            icon(biomesObject.getIcon()).
            clickHandler(clickHandler).
            glow(glow).
            build();
    }


    /**
     * This method creates panel item for given button type.
     *
     * @param button Button type.
     * @return Clickable PanelItem button.
     */
    private PanelItem createButton(Button button)
    {
        final String reference = Constants.BUTTON + button.name().toLowerCase() + ".";
        String name = this.user.getTranslation(reference + "name");
        List<String> description = new ArrayList<>();

        PanelItem.ClickHandler clickHandler = (panel, user, clickType, i) -> true;

        boolean glow = false;
        ItemStack itemStack = new ItemStack(Material.AIR);

        switch (button)
        {
            case ISLAND_NAME -> {
                // Create owner name translated string.
                String ownerName = this.addon.getPlayers().getName(this.island.getOwner());

                if (ownerName.equals(""))
                {
                    ownerName = this.user.getTranslation(Constants.DESCRIPTIONS + "unknown");
                }

                ownerName = this.user.getTranslation(reference + "owner", Constants.PARAMETER_PLAYER, ownerName);

                // Create island members translated string.

                StringBuilder builder = new StringBuilder();

                ImmutableSet<UUID> members = this.island.getMemberSet();
                if (members.size() > 1)
                {
                    builder.append(this.user.getTranslation(reference + "list"));

                    for (UUID uuid : members)
                    {
                        if (uuid != this.island.getOwner())
                        {
                            builder.append("\n").append(this.user.getTranslation(reference + "value",
                                Constants.PARAMETER_PLAYER, this.addon.getPlayers().getName(uuid)));
                        }
                    }
                }

                // Get descriptionLine that contains [members]
                description.add(this.user.getTranslation(reference + "description",
                    Constants.PARAMETER_OWNER, ownerName,
                    Constants.PARAMETER_MEMBERS, builder.toString(),
                    Constants.PARAMETER_ID, this.island.getUniqueId()));

                itemStack = new ItemStack(Material.NAME_TAG);

                // Transform name into button title.
                name = this.user.getTranslation(reference + "name",
                    Constants.PARAMETER_NAME, this.title);
            }
            case ISLAND_BUNDLE -> {
                description.add(this.user.getTranslationOrNothing(reference + "description"));

                BiomesBundleObject ownerBundle = this.islandData.getOwnerBundle() != null ?
                    this.addon.getAddonManager().getBundleById(this.islandData.getOwnerBundle()) : null;

                final BiomesBundleObject islandBundle = this.islandData.getIslandBundle() != null ?
                    this.addon.getAddonManager().getBundleById(this.islandData.getIslandBundle()) : null;

                if (ownerBundle != null)
                {
                    itemStack = new ItemStack(Material.STRUCTURE_VOID);
                    description.add(this.user.getTranslation(reference + "overwritten"));
                }
                else
                {
                    if (islandBundle != null)
                    {
                        itemStack = islandBundle.getBundleIcon();

                        description.add(this.user.getTranslation(reference + "value",
                            Constants.PARAMETER_BUNDLE, islandBundle.getFriendlyName()));
                    }
                    else
                    {
                        itemStack = new ItemStack(Material.NAME_TAG);
                    }
                }

                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-change"));

                clickHandler = (panel, user, clickType, slot) ->
                {
                    BundleSelector.open(this, islandBundle, bundle ->
                    {
                        if (bundle == null || bundle == BiomesBundleObject.dummyBundle)
                        {
                            this.islandData.setIslandBundle(null);
                        }
                        else
                        {
                            this.islandData.setIslandBundle(bundle.getUniqueId());
                        }

                        this.manager.saveIslandData(this.islandData);

                        // Recreate list based on new bundle.
                        this.elementList = this.manager.getIslandBiomes(world, this.islandData);

                        this.build();
                    });

                    return true;
                };
            }
            case OWNER_BUNDLE -> {
                description.add(this.user.getTranslationOrNothing(reference + "description",
                    Constants.PARAMETER_GAMEMODE, Utils.getGameMode(this.world).toLowerCase()));

                BiomesBundleObject bundle = this.islandData.getOwnerBundle() != null ?
                    this.addon.getAddonManager().getBundleById(this.islandData.getOwnerBundle()) : null;

                if (bundle != null)
                {
                    itemStack = bundle.getBundleIcon();

                    description.add(this.user.getTranslation(reference + "value",
                        Constants.PARAMETER_BUNDLE, bundle.getFriendlyName()));
                }
                else
                {
                    itemStack = new ItemStack(Material.STRUCTURE_VOID);
                }
            }
            case RESET_TO_DEFAULT -> {
                description.add(this.user.getTranslationOrNothing(reference + "description"));
                // add empty line
                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-perform"));

                clickHandler = (panel, user, clickType, i) ->
                {
                    this.islandData.setIslandBundle(null);
                    this.islandData.clear();

                    this.manager.saveIslandData(this.islandData);

                    this.build();
                    return true;
                };

                itemStack = new ItemStack(Material.OBSERVER);
            }
        }

        return new PanelItemBuilder().
            name(name).
            description(description).
            icon(itemStack).
            clickHandler(clickHandler).
            glow(glow).
            build();
    }


    /**
     * This method is used to open UserPanel outside this class. It will be much easier to open panel with single method
     * call then initializing new object.
     *
     * @param panel Parent Panel
     * @param island Island that must be displayed.
     */
    public static void open(CommonPanel panel,
        Island island)
    {
        new IslandEditPanel(panel, island).build();
    }


    // ---------------------------------------------------------------------
    // Section: Variables
    // ---------------------------------------------------------------------


    /**
     * This enum holds possible actions that this gui allows.
     */
    private enum Button
    {
        /**
         * Holds name of the island that is edited.
         */
        ISLAND_NAME,
        /**
         * Displays island active bundle.
         */
        ISLAND_BUNDLE,
        /**
         * Displays owner active bundle.
         */
        OWNER_BUNDLE,
        /**
         * Action that allows to reset to default values.
         */
        RESET_TO_DEFAULT
    }


    /**
     * This variable stores island that is viewed.
     */
    private final Island island;

    /**
     * This variable stores generator data for this island.
     */
    private final BiomesIslandDataObject islandData;

    /**
     * Stores island title.
     */
    private String title;

    /**
     * This variable stores all biomes in the given world.
     */
    private List<BiomesObject> elementList;
}
