package world.bentobox.biomes.panels.admin;


import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

import lv.id.bonne.panelutils.PanelUtils;
import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.biomes.database.objects.BiomesBundleObject;
import world.bentobox.biomes.panels.CommonPagedPanel;
import world.bentobox.biomes.panels.CommonPanel;
import world.bentobox.biomes.panels.ConversationUtils;
import world.bentobox.biomes.utils.Constants;
import world.bentobox.biomes.utils.Utils;


/**
 * This class opens GUI that allows to manage all generators for admin.
 */
public class BundleManagePanel extends CommonPagedPanel<BiomesBundleObject>
{
    // ---------------------------------------------------------------------
    // Section: Internal Constructor
    // ---------------------------------------------------------------------


    /**
     * This is internal constructor. It is used internally in current class to avoid creating objects everywhere.
     *
     * @param parentPanel Parent Panel object.
     */
    private BundleManagePanel(CommonPanel parentPanel)
    {
        super(parentPanel);
        // Store bundles in local list to avoid building it every time.
        this.elementList = this.manager.getBundles(this.world);

        // Init set with selected bundles.
        this.selectedElements = new HashSet<>(this.elementList.size());
    }


    /**
     * This method builds this GUI.
     */
    @Override
    protected void build()
    {
        // PanelBuilder is a BentoBox API that provides ability to easy create Panels.
        PanelBuilder panelBuilder = new PanelBuilder().
            user(this.user).
            name(this.user.getTranslation(Constants.TITLE + "manage-bundles"));

        PanelUtils.fillBorder(panelBuilder, 5, Material.MAGENTA_STAINED_GLASS_PANE);

        panelBuilder.item(1, this.createButton(Action.CREATE_BUNDLE));
        panelBuilder.item(2, this.createButton(Action.DELETE_BUNDLE));

        this.populateElements(panelBuilder, this.elementList);

        panelBuilder.item(44, this.returnButton);

        // Build panel.
        panelBuilder.build();
    }


// ---------------------------------------------------------------------
// Section: Methods
// ---------------------------------------------------------------------


    /**
     * This method creates panel item for given button type.
     *
     * @param button Button type.
     * @return Clickable PanelItem button.
     */
    private PanelItem createButton(Action button)
    {
        final String reference = Constants.BUTTON + button.name().toLowerCase();
        String name = this.user.getTranslation(reference + ".name");
        List<String> description = new ArrayList<>();

        PanelItem.ClickHandler clickHandler;
        boolean glow = false;

        Material icon = Material.PAPER;
        int count = 1;

        switch (button)
        {
            case CREATE_BUNDLE -> {
                description.add(this.user.getTranslationOrNothing(reference + ".description"));
                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-create"));
                icon = Material.WRITABLE_BOOK;
                clickHandler = (panel, user1, clickType, slot) ->
                {
                    String gameModePrefix = Utils.getGameMode(this.world).toLowerCase() + "_";

                    // This consumer process new bundle creating with a name and id from given
                    // consumer value..
                    Consumer<String> bundleIdConsumer = value ->
                    {
                        if (value != null)
                        {
                            BiomesBundleObject newBundle = new BiomesBundleObject();
                            newBundle.setFriendlyName(value);
                            newBundle.setUniqueId(gameModePrefix + Utils.sanitizeInput(value));
                            // Add PAPER as new icon.
                            newBundle.setBundleIcon(new ItemStack(Material.PAPER));
                            newBundle.setDescription(new ArrayList<>());
                            newBundle.setBiomeObjects(new HashSet<>());

                            this.manager.saveBundle(newBundle);
                            this.manager.loadBundle(newBundle, false, this.user);

                            // Add new generator to generatorList.
                            this.elementList.add(newBundle);

                            // Open bundle edit panel.
                            BundleEditPanel.open(this, newBundle);
                        }
                        else
                        {
                            // Operation is canceled. Open this panel again.
                            this.build();
                        }
                    };

                    // This function checks if generator with a given ID already exist.
                    Function<String, Boolean> validationFunction = bundleId ->
                        this.manager.getBundleById(gameModePrefix + Utils.sanitizeInput(bundleId)) == null;

                    // Call a conversation API to get input string.
                    ConversationUtils.createIDStringInput(bundleIdConsumer,
                        validationFunction,
                        this.user,
                        this.user.getTranslation(Constants.CONVERSATIONS + "write-name"),
                        this.user.getTranslation(Constants.CONVERSATIONS + "new-object-created",
                            Constants.PARAMETER_WORLD, world.getName()),
                        Constants.ERRORS + "object-already-exists");

                    return true;
                };
            }
            case DELETE_BUNDLE -> {
                icon = this.selectedElements.isEmpty() ? Material.BARRIER : Material.LAVA_BUCKET;
                glow = !this.selectedElements.isEmpty();
                description.add(this.user.getTranslationOrNothing(reference + ".description"));
                if (!this.selectedElements.isEmpty())
                {
                    description.add(this.user.getTranslation(reference + ".title"));
                    this.selectedElements.forEach(bundle ->
                        description.add(this.user.getTranslation(reference + ".value",
                            Constants.PARAMETER_BUNDLE, bundle.getFriendlyName())));

                    description.add("");
                    description.add(this.user.getTranslation(Constants.TIPS + "click-to-remove"));

                    clickHandler = (panel, user1, clickType, slot) ->
                    {

                        // Create consumer that accepts value from conversation.
                        Consumer<Boolean> consumer = value ->
                        {
                            if (value)
                            {
                                this.selectedElements.forEach(bundle ->
                                {
                                    this.manager.wipeBundle(bundle);
                                    this.elementList.remove(bundle);
                                });
                            }

                            this.build();
                        };

                        String generatorString;

                        if (!this.selectedElements.isEmpty())
                        {
                            Iterator<BiomesBundleObject> iterator = this.selectedElements.iterator();

                            StringBuilder builder = new StringBuilder();
                            builder.append(iterator.next().getFriendlyName());

                            while (iterator.hasNext())
                            {
                                builder.append(", ").append(iterator.next().getFriendlyName());
                            }

                            generatorString = builder.toString();
                        }
                        else
                        {
                            generatorString = "";
                        }

                        // Create conversation that gets user acceptance to delete selected generator data.
                        ConversationUtils.createConfirmation(
                            consumer,
                            this.user,
                            this.user.getTranslation(Constants.CONVERSATIONS + "confirm-deletion",
                                TextVariables.NUMBER, String.valueOf(this.selectedElements.size()),
                                Constants.PARAMETER_VALUE, generatorString),
                            this.user.getTranslation(Constants.CONVERSATIONS + "data-removed",
                                Constants.PARAMETER_GAMEMODE, Utils.getGameMode(this.world)));


                        return true;
                    };
                }
                else
                {
                    description.add("");
                    description.add(this.user.getTranslation(Constants.TIPS + "select-before"));

                    // Do nothing as no generators are selected.
                    clickHandler = (panel, user1, clickType, slot) -> true;
                }
            }
            default -> clickHandler = (panel, user1, clickType, slot) -> true;
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
     * Update filters.
     */
    @Override
    protected void updateFilters()
    {
        // There are no filters that should be updated.
    }


    /**
     * This method creates button for bundle.
     *
     * @param bundle bundle which button must be created.
     * @return PanelItem for bundle object.
     */
    protected PanelItem createElementButton(BiomesBundleObject bundle)
    {
        boolean glow = this.selectedElements.contains(bundle);

        List<String> description = new ArrayList<>();
        description.add(this.generateBundleDescription(bundle));

        if (this.selectedElements.contains(bundle))
        {
            description.add(this.user.getTranslation(Constants.DESCRIPTIONS + "selected"));
        }

        description.add("");
        description.add(this.user.getTranslation(Constants.TIPS + "left-click-to-edit"));

        if (this.selectedElements.contains(bundle))
        {
            description.add(this.user.getTranslation(Constants.TIPS + "right-click-to-deselect"));
        }
        else
        {
            description.add(this.user.getTranslation(Constants.TIPS + "right-click-to-select"));
        }


        PanelItem.ClickHandler clickHandler = (panel, user, clickType, i) ->
        {
            // Click handler should work only if user has a permission to change anything.
            // Otherwise just to view.

            if (clickType.isRightClick())
            {
                // Open edit panel.
                if (this.selectedElements.contains(bundle))
                {
                    this.selectedElements.remove(bundle);
                }
                else
                {
                    this.selectedElements.add(bundle);
                }

                // Build necessary as multiple icons are changed.
                this.build();
            }
            else
            {
                BundleEditPanel.open(this, bundle);
            }

            // Always return true.
            return true;
        };

        return new PanelItemBuilder().
            name(bundle.getFriendlyName()).
            description(description).
            icon(bundle.getBundleIcon()).
            clickHandler(clickHandler).
            glow(glow).
            build();
    }


    /**
     * This method is used to open GeneratorManagePanel outside this class. It will be much easier to open panel with
     * single method call then initializing new object.
     *
     * @param parentPanel Parent Panel object.
     */
    public static void open(CommonPanel parentPanel)
    {
        new BundleManagePanel(parentPanel).build();
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
         * Allows to add new bundles to the bundleList.
         */
        CREATE_BUNDLE,
        /**
         * Allows to delete selected bundles from bundleList.
         */
        DELETE_BUNDLE
    }


    // ---------------------------------------------------------------------
    // Section: Variables
    // ---------------------------------------------------------------------

    /**
     * This variable stores all bundles in the given world.
     */
    private final List<BiomesBundleObject> elementList;

    /**
     * This variable stores all selected bundles.
     */
    private final Set<BiomesBundleObject> selectedElements;
}
