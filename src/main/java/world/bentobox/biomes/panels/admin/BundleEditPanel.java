///
// Created by BONNe
// Copyright - 2022
///

package world.bentobox.biomes.panels.admin;


import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import lv.id.bonne.panelutils.PanelUtils;
import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.PanelListener;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.biomes.database.objects.BiomesBundleObject;
import world.bentobox.biomes.database.objects.BiomesObject;
import world.bentobox.biomes.panels.CommonPagedPanel;
import world.bentobox.biomes.panels.CommonPanel;
import world.bentobox.biomes.panels.ConversationUtils;
import world.bentobox.biomes.utils.Constants;
import world.bentobox.biomes.utils.Utils;


/**
 * This class opens GUI that shows bundle view for user.
 */
public class BundleEditPanel extends CommonPagedPanel<BiomesObject>
{
    // ---------------------------------------------------------------------
    // Section: Internal Constructor
    // ---------------------------------------------------------------------


    /**
     * This is internal constructor. It is used internally in current class to avoid creating objects everywhere.
     *
     * @param panel Parent Panel
     * @param bundle Bundle that must be displayed.
     */
    private BundleEditPanel(CommonPanel panel,
        BiomesBundleObject bundle)
    {
        super(panel);

        this.bundle = bundle;

        // By default no-filters are active.
        this.activeTab = Tab.BUNDLE_INFO;
        this.mode = Mode.VIEW;
        this.selectedBiomes = new HashSet<>();
    }


    /**
     * This method builds this GUI.
     */
    @Override
    protected void build()
    {
        if (this.bundle == null)
        {
            Utils.sendMessage(this.user, this.user.getTranslation(
                Constants.ERRORS + "no-bundle-data"));
            return;
        }

        // PanelBuilder is a BentoBox API that provides ability to easy create Panels.
        PanelBuilder panelBuilder = new PanelBuilder().
            user(this.user).
            name(this.user.getTranslation(Constants.TITLE + "edit-bundle",
                Constants.PARAMETER_BUNDLE, this.bundle.getFriendlyName()));

        PanelUtils.fillBorder(panelBuilder, 5, Material.MAGENTA_STAINED_GLASS_PANE);

        if (this.mode != Mode.ADD || this.activeTab != Tab.BUNDLE_BIOMES)
        {
            this.populateHeader(panelBuilder);
        }

        switch (this.activeTab)
        {
            case BUNDLE_INFO -> {
                this.populateInfo(panelBuilder);

                // Add listener that allows to change icons
                panelBuilder.listener(new IconChanger());

                // Reset mode as active tab is switched.
                this.mode = Mode.VIEW;
                this.selectedBiomes.clear();
            }
            case BUNDLE_BIOMES -> {
                this.populateBundles(panelBuilder);
                panelBuilder.item(39, this.createButton(Action.ADD_BUNDLE_BIOME));

                if (this.mode == Mode.VIEW)
                {
                    // Add Remove button only in view mode.
                    panelBuilder.item(41, this.createButton(Action.REMOVE_BUNDLE_BIOME));
                }
            }
        }

        panelBuilder.item(44, this.returnButton);

        // Build panel.
        panelBuilder.build();
    }


    /**
     * Update filters.
     */
    @Override
    protected void updateFilters()
    {
        // Do nothing with filters.
    }


// ---------------------------------------------------------------------
// Section: Methods
// ---------------------------------------------------------------------


    /**
     * This method populates header with buttons for switching between tabs.
     *
     * @param panelBuilder PanelBuilder that must be created.
     */
    private void populateHeader(PanelBuilder panelBuilder)
    {
        panelBuilder.item(3, this.createButton(Tab.BUNDLE_INFO));
        panelBuilder.item(5, this.createButton(Tab.BUNDLE_BIOMES));
    }


    /**
     * This method populates panel body with info blocks.
     *
     * @param panelBuilder PanelBuilder that must be created.
     */
    private void populateInfo(PanelBuilder panelBuilder)
    {
        // Users should see only icon
        panelBuilder.item(13, this.createButton(Button.BUNDLE_NAME));
        panelBuilder.item(11, this.createButton(Button.BUNDLE_ICON));
        panelBuilder.item(14, this.createButton(Button.BUNDLE_DESCRIPTION));

        panelBuilder.item(20, this.createButton(Button.BUNDLE_PERMISSION));
    }


    /**
     * This method populates panel body with blocks.
     *
     * @param panelBuilder PanelBuilder that must be created.
     */
    private void populateBundles(PanelBuilder panelBuilder)
    {
        List<BiomesObject> bundleBiomeList = this.bundle.getBiomeObjects().stream().
            map(this.addon.getAddonManager()::getBiomeByID).
            filter(Objects::nonNull).
            sorted(Comparator.comparing(BiomesObject::getOrder).
                thenComparing(BiomesObject::getFriendlyName).
                thenComparing(BiomesObject::getBiome)).
            collect(Collectors.toList());

        if (this.mode == Mode.ADD)
        {
            // Need a list that does not contains current generators.
            final Set<BiomesObject> currentSet = new HashSet<>(bundleBiomeList);

            // Assign new list to generator tier objects.
            bundleBiomeList = this.addon.getAddonManager().getBiomes(this.world).stream().
                filter(biome -> !currentSet.contains(biome)).
                collect(Collectors.toList());
        }

        this.populateElements(panelBuilder, bundleBiomeList);
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
        description.add(this.user.getTranslationOrNothing(reference + "description"));

        PanelItem.ClickHandler clickHandler = (panel, user, clickType, i) -> true;

        boolean glow = false;
        ItemStack itemStack = new ItemStack(Material.AIR);

        switch (button)
        {
            case BUNDLE_NAME -> {
                itemStack = new ItemStack(Material.NAME_TAG);

                clickHandler = (panel, user, clickType, i) ->
                {
                    // Create consumer that process description change
                    Consumer<String> consumer = value ->
                    {
                        if (value != null)
                        {
                            this.bundle.setFriendlyName(value);
                            this.manager.saveBundle(this.bundle);
                        }

                        this.build();
                    };

                    // start conversation
                    ConversationUtils.createStringInput(consumer,
                        user,
                        user.getTranslation(Constants.CONVERSATIONS + "write-name"),
                        user.getTranslation(Constants.CONVERSATIONS + "name-changed"));

                    return true;
                };

                description.add(this.user.getTranslation(reference + "value",
                    Constants.PARAMETER_BUNDLE, this.bundle.getFriendlyName()));
                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-change"));
            }
            case BUNDLE_ICON -> {
                itemStack = this.bundle.getBundleIcon();

                clickHandler = (panel, user, clickType, i) ->
                {
                    // TODO: implement GUI for block selection
                    this.isIconSelected = !this.isIconSelected;
                    panel.getInventory().setItem(i, this.createButton(button).getItem());

                    return true;
                };

                if (!this.isIconSelected)
                {
                    description.add("");
                    description.add(this.user.getTranslation(Constants.TIPS + "click-to-change"));
                }
                else
                {
                    description.add("");
                    description.add(this.user.getTranslation(Constants.TIPS + "click-on-item"));
                }

                glow = this.isIconSelected;
            }
            case BUNDLE_DESCRIPTION -> {
                itemStack = new ItemStack(Material.WRITTEN_BOOK);

                clickHandler = (panel, user, clickType, i) ->
                {
                    // Create consumer that process description change
                    Consumer<List<String>> consumer = value ->
                    {
                        if (value != null)
                        {
                            this.bundle.setDescription(value);
                            this.manager.saveBundle(this.bundle);
                        }

                        this.build();
                    };

                    // start conversation
                    ConversationUtils.createStringListInput(consumer,
                        user,
                        user.getTranslation(Constants.CONVERSATIONS + "write-description"),
                        user.getTranslation(Constants.CONVERSATIONS + "description-changed"));

                    return true;
                };

                if (this.bundle.getDescription().isEmpty())
                {
                    description.add(this.user.getTranslation(reference + "none"));
                }
                else
                {
                    description.add(this.user.getTranslation(reference + "value"));
                    description.addAll(this.bundle.getDescription());
                }

                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-change"));
            }
            case BUNDLE_PERMISSION -> {
                itemStack = new ItemStack(Material.REDSTONE_TORCH);

                description.add(this.user.getTranslation(reference + "value",
                    Constants.PARAMETER_GAMEMODE, Utils.getGameMode(this.world),
                    Constants.PARAMETER_ID, this.bundle.getUniqueId()));
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
     * This method creates panel item for given button type.
     *
     * @param button Button type.
     * @return Clickable PanelItem button.
     */
    private PanelItem createButton(Tab button)
    {
        final String reference = Constants.BUTTON + button.name().toLowerCase();
        String name = this.user.getTranslation(reference + ".name");
        List<String> description = new ArrayList<>();
        description.add(this.user.getTranslationOrNothing(reference + ".description"));
        description.add("");
        description.add(this.user.getTranslation(Constants.TIPS + "click-to-view"));

        PanelItem.ClickHandler clickHandler = (panel, user, clickType, i) ->
        {
            this.activeTab = button;
            this.build();
            return true;
        };

        Material material = switch (button)
            {
                case BUNDLE_INFO -> Material.WRITTEN_BOOK;
                case BUNDLE_BIOMES -> Material.CHEST;
            };

        return new PanelItemBuilder().
            name(name).
            description(description).
            icon(material).
            clickHandler(clickHandler).
            glow(this.activeTab == button).
            build();
    }


    /**
     * This method creates panel item for given button type.
     *
     * @param button Button type.
     * @return Clickable PanelItem button.
     */
    private PanelItem createButton(Action button)
    {
        final String reference = Constants.BUTTON + button.name().toLowerCase() + ".";
        String name = this.user.getTranslation(reference + "name");
        List<String> description = new ArrayList<>();
        PanelItem.ClickHandler clickHandler = (panel, user, clickType, i) -> true;

        boolean glow = false;
        Material icon = Material.PAPER;
        int count = 1;

        switch (button)
        {
            case ADD_BUNDLE_BIOME -> {
                description.add(this.user.getTranslationOrNothing(reference + "description"));

                if (this.mode == Mode.VIEW)
                {
                    description.add("");
                    description.add(this.user.getTranslation(Constants.TIPS + "click-to-choose"));
                }
                else
                {
                    if (this.selectedBiomes.isEmpty())
                    {
                        description.add("");
                        description.add(this.user.getTranslation(Constants.TIPS + "select-before"));
                    }
                    else
                    {
                        description.add(this.user.getTranslation(reference + "title"));
                        this.selectedBiomes.forEach(biome ->
                            description.add(this.user.getTranslation(reference + "value",
                                Constants.PARAMETER_BIOME, biome.getFriendlyName())));

                        description.add("");
                        description.add(this.user.getTranslation(Constants.TIPS + "click-to-add"));
                    }
                }

                icon = Material.WRITABLE_BOOK;
                glow = !this.selectedBiomes.isEmpty() && this.mode.equals(Mode.ADD);
                clickHandler = (panel, user1, clickType, slot) ->
                {

                    if (this.mode == Mode.VIEW)
                    {
                        this.mode = Mode.ADD;
                    }
                    else
                    {
                        // Switch mode to view.
                        this.mode = Mode.VIEW;

                        // Add all selected generators to the current bundle
                        this.selectedBiomes.forEach(biomes ->
                            this.bundle.getBiomeObjects().add(biomes.getUniqueId()));

                        // clear selected generator list.
                        this.selectedBiomes.clear();
                    }

                    this.build();
                    return true;
                };
            }
            case REMOVE_BUNDLE_BIOME -> {
                description.add(this.user.getTranslationOrNothing(reference + "description"));

                icon = this.selectedBiomes.isEmpty() ? Material.BARRIER : Material.LAVA_BUCKET;
                glow = !this.selectedBiomes.isEmpty() && this.mode.equals(Mode.VIEW);

                if (!this.selectedBiomes.isEmpty())
                {
                    description.add(this.user.getTranslation(reference + "title"));
                    this.selectedBiomes.forEach(generator ->
                        description.add(this.user.getTranslation(reference + "value",
                            Constants.PARAMETER_BIOME, generator.getFriendlyName())));

                    description.add("");
                    description.add(this.user.getTranslation(Constants.TIPS + "click-to-remove"));

                    clickHandler = (panel, user1, clickType, slot) ->
                    {

                        // Should be active only in view mode.
                        if (this.mode == Mode.VIEW)
                        {
                            // Remove all selected generators to the current bundle
                            this.selectedBiomes.forEach(biomes ->
                                this.bundle.getBiomeObjects().remove(biomes.getUniqueId()));

                            // clear selected generator list.
                            this.selectedBiomes.clear();
                            this.build();
                        }

                        return true;
                    };
                }
                else
                {
                    description.add("");
                    description.add(this.user.getTranslation(Constants.TIPS + "select-before"));
                }
            }
        }

        return new PanelItemBuilder().
            name(name).
            description(description).
            icon(icon).
            amount(count).
            clickHandler(clickHandler).
            glow(glow).
            build();
    }


    /**
     * This method creates button for BiomesObject
     *
     * @param biomesObject BiomesObject which button must be created.
     * @return PanelItem for BiomesObject.
     */
    protected PanelItem createElementButton(BiomesObject biomesObject)
    {
        boolean glow = this.selectedBiomes.contains(biomesObject);

        List<String> description = new ArrayList<>();
        description.add(this.generateBiomesDescription(biomesObject, null));

        if (this.selectedBiomes.contains(biomesObject))
        {
            description.add(this.user.getTranslation(Constants.DESCRIPTIONS + "selected"));
        }

        description.add("");

        if (this.selectedBiomes.contains(biomesObject))
        {
            description.add(this.user.getTranslation(Constants.TIPS + "click-to-deselect"));
        }
        else
        {
            description.add(this.user.getTranslation(Constants.TIPS + "click-to-select"));
        }


        PanelItem.ClickHandler clickHandler = (panel, user, clickType, i) ->
        {
            if (this.selectedBiomes.contains(biomesObject))
            {
                this.selectedBiomes.remove(biomesObject);
            }
            else
            {
                this.selectedBiomes.add(biomesObject);
            }

            // Build necessary as multiple icons are changed.
            this.build();

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
     * This class allows to change icon for Generator Tier
     */
    private class IconChanger implements PanelListener
    {
        /**
         * Process inventory click. If generator icon is selected and user clicks on item in his inventory, then change
         * icon to the item from inventory.
         *
         * @param user the user
         * @param event the event
         */
        @Override
        public void onInventoryClick(User user, InventoryClickEvent event)
        {
            // Handle icon changing
            if (BundleEditPanel.this.isIconSelected &&
                event.getCurrentItem() != null &&
                !event.getCurrentItem().getType().equals(Material.AIR) &&
                event.getRawSlot() > 44)
            {
                // set material and amount only. Other data should be removed.
                BundleEditPanel.this.bundle.setBundleIcon(event.getCurrentItem().clone());
                // save change
                BundleEditPanel.this.manager.saveBundle(BundleEditPanel.this.bundle);
                // Deselect icon
                BundleEditPanel.this.isIconSelected = false;

                event.getInventory().setItem(11,
                    BundleEditPanel.this.createButton(Button.BUNDLE_ICON).getItem());
            }
        }


        /**
         * On inventory close.
         *
         * @param event the event
         */
        @Override
        public void onInventoryClose(InventoryCloseEvent event)
        {
            // Do nothing
        }


        /**
         * Setup current listener.
         */
        @Override
        public void setup()
        {
            // Do nothing
        }
    }


    // ---------------------------------------------------------------------
    // Section: Private Classes
    // ---------------------------------------------------------------------


    /**
     * This method is used to open UserPanel outside this class. It will be much easier to open panel with single method
     * call then initializing new object.
     *
     * @param panel Parent Panel
     * @param bundle Bundle that must be displayed.
     */
    public static void open(CommonPanel panel, BiomesBundleObject bundle)
    {
        new BundleEditPanel(panel, bundle).build();
    }


    // ---------------------------------------------------------------------
    // Section: Enums
    // ---------------------------------------------------------------------


    /**
     * This enum holds variable that allows to switch between button creation.
     */
    private enum Action
    {
        /**
         * Return button that exists GUI.
         */
        RETURN,
        /**
         * Allows to select previous generators in multi-page situation.
         */
        PREVIOUS,
        /**
         * Allows to select next generators in multi-page situation.
         */
        NEXT,
        /**
         * Allows to add a new generator to the generator list.
         */
        ADD_BUNDLE_BIOME,
        /**
         * Allows to remove selected generator from generator list
         */
        REMOVE_BUNDLE_BIOME
    }


    /**
     * This enum holds possible tabs for current gui.
     */
    private enum Tab
    {
        /**
         * General Information Tab
         */
        BUNDLE_INFO,
        /**
         * Generators Tab.
         */
        BUNDLE_BIOMES
    }


    /**
     * This enum holds possible actions that this gui allows.
     */
    private enum Button
    {
        /**
         * Holds Name type that allows to interact with bundle name.
         */
        BUNDLE_NAME,
        /**
         * Holds Name type that allows to interact with bundle icon.
         */
        BUNDLE_ICON,
        /**
         * Holds Name type that allows to interact with bundle description.
         */
        BUNDLE_DESCRIPTION,

        /**
         * Bundle permission button.
         */
        BUNDLE_PERMISSION
    }


    /**
     * This allows to separate between adding and viewing/removing assigned generators to current bundle.
     */
    private enum Mode
    {
        /**
         * Holds generator view mode that allows to see currently assigned generators to viewed bundle.
         */
        VIEW,
        /**
         * Holds generator view mode that allows to see see generators which are not assigned to viewed bundle.
         */
        ADD
    }


    // ---------------------------------------------------------------------
    // Section: Variables
    // ---------------------------------------------------------------------

    /**
     * This variable stores bundle that is viewed.
     */
    private final BiomesBundleObject bundle;

    /**
     * Thi map stores selected biomes.
     */
    private final Set<BiomesObject> selectedBiomes;

    /**
     * This variable stores which tab currently is active.
     */
    private Tab activeTab;

    /**
     * This variable stores if icon is selected for changing.
     */
    private boolean isIconSelected;

    /**
     * This variable stores currently used mode.
     */
    private Mode mode;
}
