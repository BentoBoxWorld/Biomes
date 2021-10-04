package world.bentobox.biomes.panels.admin;


import org.bukkit.Material;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import lv.id.bonne.panelutils.PanelUtils;
import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.biomes.database.objects.BiomesObject;
import world.bentobox.biomes.panels.CommonPagedPanel;
import world.bentobox.biomes.panels.CommonPanel;
import world.bentobox.biomes.panels.ConversationUtils;
import world.bentobox.biomes.utils.Constants;
import world.bentobox.biomes.utils.Utils;


/**
 * This class opens GUI that allows to manage all generators for admin.
 */
public class ManageBiomesPanel extends CommonPagedPanel<BiomesObject>
{
    // ---------------------------------------------------------------------
    // Section: Internal Constructor
    // ---------------------------------------------------------------------


    /**
     * This is internal constructor. It is used internally in current class to avoid creating objects everywhere.
     *
     * @param parentPanel Parent Panel object.
     */
    private ManageBiomesPanel(CommonPanel parentPanel)
    {
        super(parentPanel);
        // Store bundles in local list to avoid building it every time.
        this.elementList = this.manager.getBiomes(this.world);
        this.filterElements = this.elementList;

        // Init set with selected bundles.
        this.selectedElements = new HashSet<>(this.elementList.size());
    }


    /**
     * This method is used to open GeneratorManagePanel outside this class. It will be much easier to open panel with
     * single method call then initializing new object.
     *
     * @param parentPanel Parent Panel object.
     */
    public static void open(CommonPanel parentPanel)
    {
        new ManageBiomesPanel(parentPanel).build();
    }


    /**
     * This method builds this GUI.
     */
    @Override
    public void build()
    {
        // PanelBuilder is a BentoBox API that provides ability to easy create Panels.
        PanelBuilder panelBuilder = new PanelBuilder().
            user(this.user).
            name(this.user.getTranslation(Constants.TITLE + "manage-biomes"));

        PanelUtils.fillBorder(panelBuilder, 5, Material.MAGENTA_STAINED_GLASS_PANE);

        panelBuilder.item(1, this.createButton(Action.ADD_BIOME));
        panelBuilder.item(2, this.createButton(Action.REMOVE_BIOME));

        this.populateElements(panelBuilder, this.filterElements);

        panelBuilder.item(44, this.returnButton);

        // Build panel.
        panelBuilder.build();
    }


    @Override
    protected void updateFilters()
    {
        if (this.searchString == null || this.searchString.isBlank())
        {
            this.filterElements = this.elementList;
        }
        else
        {
            this.filterElements = this.elementList.stream().
                filter(element -> {
                    // If element name is set and name contains search field, then do not filter out.
                    return element.getBiome().name().toLowerCase().
                        contains(this.searchString.toLowerCase()) ||
                        element.getFriendlyName().toLowerCase().
                            contains(this.searchString.toLowerCase());
                }).
                distinct().
                collect(Collectors.toList());
        }
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
            case ADD_BIOME -> {
                description.add(this.user.getTranslationOrNothing(reference + ".description"));
                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-create"));

                icon = Material.WRITABLE_BOOK;
                clickHandler = (panel, user1, clickType, slot) ->
                {
                    String gameModePrefix = Utils.getGameMode(this.world).toLowerCase() + "_";

                    // This consumer process new bundle creating with a name and id from given
                    // consumer value.
                    Consumer<String> bundleIdConsumer = value ->
                    {
                        if (value != null)
                        {
                            BiomesObject newObject = this.addon.getAddonManager().createBiome(
                                gameModePrefix + Utils.sanitizeInput(value), world.getName());

                            if (newObject != null)
                            {
                                newObject.setFriendlyName(value);
                                EditBiomeGUI.open(this, newObject);

                                // Add new generator to generatorList.
                                this.elementList.add(newObject);
                            }
                        }
                        else
                        {
                            // Operation is canceled. Open this panel again.
                            this.build();
                        }
                    };

                    // This function checks if generator with a given ID already exist.
                    Function<String, Boolean> validationFunction = objectId ->
                        !this.manager.containsBiome(gameModePrefix + Utils.sanitizeInput(objectId));

                    // Call a conversation API to get input string.
                    ConversationUtils.createIDStringInput(bundleIdConsumer,
                        validationFunction,
                        this.user,
                        this.user.getTranslation(Constants.CONVERSATIONS + "write-name"),
                        this.user.getTranslation(Constants.CONVERSATIONS + "new-object-created",
                            Constants.PARAMETER_WORLD, world.getName()),
                        Constants.CONVERSATIONS + "object-already-exists");

                    return true;
                };
            }
            case REMOVE_BIOME -> {
                icon = this.selectedElements.isEmpty() ? Material.BARRIER : Material.LAVA_BUCKET;
                glow = !this.selectedElements.isEmpty();

                description.add(this.user.getTranslationOrNothing(reference + ".description"));

                if (!this.selectedElements.isEmpty())
                {
                    description.add(this.user.getTranslation(reference + ".title"));
                    this.selectedElements.forEach(object ->
                        description.add(this.user.getTranslation(reference + ".value",
                            "[biome]", object.getFriendlyName())));

                    description.add("");
                    description.add(this.user.getTranslation(Constants.TIPS + "click-to-remove"));

                    clickHandler = (panel, user1, clickType, slot) ->
                    {

                        // Create consumer that accepts value from conversation.
                        Consumer<Boolean> consumer = value ->
                        {
                            if (value)
                            {
                                this.selectedElements.forEach(biomesObject ->
                                {
                                    this.manager.removeBiome(biomesObject);
                                    this.elementList.remove(biomesObject);
                                });

                                this.selectedElements.clear();
                            }

                            this.build();
                        };

                        String stickString;

                        if (!this.selectedElements.isEmpty())
                        {
                            Iterator<BiomesObject> iterator = this.selectedElements.iterator();

                            StringBuilder builder = new StringBuilder();
                            builder.append(iterator.next().getFriendlyName());

                            while (iterator.hasNext())
                            {
                                builder.append(", ").append(iterator.next().getFriendlyName());
                            }

                            stickString = builder.toString();
                        }
                        else
                        {
                            stickString = "";
                        }

                        // Create conversation that gets user acceptance to delete selected generator data.
                        ConversationUtils.createConfirmation(
                            consumer,
                            this.user,
                            this.user.getTranslation(Constants.CONVERSATIONS + "confirm-deletion",
                                TextVariables.NUMBER, String.valueOf(this.selectedElements.size()),
                                "[value]", stickString),
                            this.user.getTranslation(Constants.CONVERSATIONS + "data-removed",
                                "[gamemode]", Utils.getGameMode(this.world)));


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
     * This method creates button for biomes.
     *
     * @param biomesObject biomes which button must be created.
     * @return PanelItem for biomes object.
     */
    protected PanelItem createElementButton(BiomesObject biomesObject)
    {
        boolean glow = this.selectedElements.contains(biomesObject);

        List<String> description = new ArrayList<>();
        description.addAll(biomesObject.getDescription());

        if (this.selectedElements.contains(biomesObject))
        {
            description.add(this.user.getTranslation(Constants.DESCRIPTIONS + "selected"));
        }

        description.add("");
        description.add(this.user.getTranslation(Constants.TIPS + "left-click-to-edit"));

        if (this.selectedElements.contains(biomesObject))
        {
            description.add(this.user.getTranslation(Constants.TIPS + "right-click-to-deselect"));
        }
        else
        {
            description.add(this.user.getTranslation(Constants.TIPS + "right-click-to-select"));
        }


        PanelItem.ClickHandler clickHandler = (panel, user, clickType, i) -> {
            // Click handler should work only if user has a permission to change anything.
            // Otherwise just to view.

            if (clickType.isRightClick())
            {
                // Open edit panel.
                if (this.selectedElements.contains(biomesObject))
                {
                    this.selectedElements.remove(biomesObject);
                }
                else
                {
                    this.selectedElements.add(biomesObject);
                }

                // Build necessary as multiple icons are changed.
                this.build();
            }
            else
            {
                EditBiomeGUI.open(this, biomesObject);
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


    // ---------------------------------------------------------------------
    // Section: Enums
    // ---------------------------------------------------------------------


    /**
     * This enum holds variable that allows to switch between button creation.
     */
    private enum Action
    {
        /**
         * Allows to add new biome.
         */
        ADD_BIOME,
        /**
         * Allows to delete selected biome.
         */
        REMOVE_BIOME
    }


    // ---------------------------------------------------------------------
    // Section: Variables
    // ---------------------------------------------------------------------

    /**
     * This variable stores all biomes in the given world.
     */
    private final List<BiomesObject> elementList;

    /**
     * This variable stores all biomes in the given world.
     */
    private List<BiomesObject> filterElements;

    /**
     * This variable stores all selected biomes.
     */
    private final Set<BiomesObject> selectedElements;
}
