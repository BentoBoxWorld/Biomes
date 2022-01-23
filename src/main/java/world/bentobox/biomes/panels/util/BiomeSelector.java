///
// Created by BONNe
// Copyright - 2021
///


package world.bentobox.biomes.panels.util;


import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.inventory.ItemStack;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import lv.id.bonne.panelutils.PanelUtils;
import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.biomes.utils.Constants;
import world.bentobox.biomes.utils.Utils;


/**
 * The BiomeSelector for GUIs.
 */
public class BiomeSelector extends PagedSelector<Biome>
{
    /**
     * Instantiates a new Biome selector.
     *
     * @param user the user
     * @param excluded the excluded
     * @param consumer the consumer
     */
    private BiomeSelector(User user, Set<Biome> excluded, BiConsumer<Boolean, Biome> consumer)
    {
        super(user);
        this.consumer = consumer;

        this.elements = Arrays.stream(Biome.values()).
            filter(material -> !excluded.contains(material)).
            // Sort by name
                sorted(Comparator.comparing(Biome::name)).
            collect(Collectors.toList());
        // Init without filters applied.
        this.filterElements = this.elements;

        this.mode = null;
    }


    /**
     * This method builds all necessary elements in GUI panel.
     */
    @Override
    protected void build()
    {
        PanelBuilder panelBuilder = new PanelBuilder().user(this.user);
        panelBuilder.name(this.user.getTranslation(Constants.TITLE + "biome-selector"));

        PanelUtils.fillBorder(panelBuilder, Material.BLUE_STAINED_GLASS_PANE);

        panelBuilder.item(0, this.buildButton(Mode.TEMPERATE));
        panelBuilder.item(1, this.buildButton(Mode.WARM));
        panelBuilder.item(2, this.buildButton(Mode.COLD));
        panelBuilder.item(3, this.buildButton(Mode.SNOWY));
        panelBuilder.item(4, this.buildButton(Mode.AQUATIC));
        panelBuilder.item(5, this.buildButton(Mode.NETHER));
        panelBuilder.item(6, this.buildButton(Mode.THE_END));
        panelBuilder.item(7, this.buildButton(Mode.NEUTRAL));
        panelBuilder.item(8, this.buildButton(Mode.CAVE));

        this.populateElements(panelBuilder, this.filterElements);

        panelBuilder.item(44, this.createButton());

        panelBuilder.build();
    }


    /**
     * This method is called when filter value is updated.
     */
    @Override
    protected void updateFilters()
    {
        if (this.searchString == null || this.searchString.isBlank())
        {
            this.filterElements = this.elements;
        }
        else
        {
            this.filterElements = this.elements.stream().
                filter(element ->
                {
                    // If element name is set and name contains search field, then do not filter out.
                    return element.name().toLowerCase().contains(this.searchString.toLowerCase());
                }).
                distinct().
                collect(Collectors.toList());
        }

        if (this.mode != null)
        {
            // Filter biomes according selected mode.

            this.filterElements.removeIf(biome ->
            {
                switch (this.mode)
                {
                    case TEMPERATE -> {
                        return !Utils.isTemperateBiome(biome);
                    }
                    case WARM -> {
                        return !Utils.isWarmBiome(biome);
                    }
                    case COLD -> {
                        return !Utils.isColdBiome(biome);
                    }
                    case SNOWY -> {
                        return !Utils.isSnowyBiome(biome);
                    }
                    case AQUATIC -> {
                        return !Utils.isAquaticBiome(biome);
                    }
                    case NETHER -> {
                        return !Utils.isNetherBiome(biome);
                    }
                    case THE_END -> {
                        return !Utils.isTheEndBiome(biome);
                    }
                    case NEUTRAL -> {
                        return !Utils.isNeutralBiome(biome);
                    }
                    case CAVE -> {
                        return !Utils.isCaveBiome(biome);
                    }
                    default -> {
                        return false;
                    }
                }
            });
        }
    }


// ---------------------------------------------------------------------
// Section: Methods
// ---------------------------------------------------------------------


    /**
     * This method creates PanelItem button of requested type.
     *
     * @return new PanelItem with requested functionality.
     */
    private PanelItem createButton()
    {
        final String reference = Constants.BUTTON + "cancel.";

        final String name = this.user.getTranslation(reference + "name");
        final List<String> description = new ArrayList<>(3);
        description.add(this.user.getTranslation(reference + "description"));

        ItemStack icon = new ItemStack(Material.IRON_DOOR);
        PanelItem.ClickHandler clickHandler = (panel, user1, clickType, slot) ->
        {
            this.consumer.accept(false, null);
            return true;
        };

        description.add("");
        description.add(this.user.getTranslation(Constants.TIPS + "click-to-cancel"));

        return new PanelItemBuilder().
            icon(icon).
            name(name).
            description(description).
            clickHandler(clickHandler).
            build();
    }


    /**
     * This method creates button for given biome.
     *
     * @param biome biome which button must be created.
     * @return new Button for biome.
     */
    @Override
    protected PanelItem createElementButton(Biome biome)
    {
        final String reference = Constants.BUTTON + "biome.";

        List<String> description = new ArrayList<>();
        description.add(this.user.getTranslation(reference + "description",
            "[id]", biome.name()));
        description.add("");
        description.add(this.user.getTranslation(Constants.TIPS + "click-to-choose"));

        return new PanelItemBuilder().
            name(this.user.getTranslation(reference + "name", "[biome]",
                Utils.prettifyObject(biome, this.user))).
            icon(Material.MAP).
            description(description).
            clickHandler((panel, user1, clickType, slot) ->
            {
                this.consumer.accept(true, biome);
                return true;
            }).
            build();
    }


    /**
     * This method builds icon for given BiomeGroup object.
     *
     * @param filterMode object which icon must be created.
     * @return PanelItem for given BiomeGroup.
     */
    private PanelItem buildButton(Mode filterMode)
    {
        final String reference = Constants.BUTTON + "biome-filter.";

        List<String> description = new ArrayList<>();
        description.add("");
        description.add(this.user.getTranslation(Constants.TIPS + "click-to-filter"));

        ItemStack icon = switch (filterMode)
            {
                case TEMPERATE -> new ItemStack(Material.SUNFLOWER);
                case WARM -> new ItemStack(Material.SAND);
                case COLD -> new ItemStack(Material.GRAVEL);
                case SNOWY -> new ItemStack(Material.SNOW_BLOCK);
                case AQUATIC -> new ItemStack(Material.TROPICAL_FISH);
                case NETHER -> new ItemStack(Material.NETHERRACK);
                case THE_END -> new ItemStack(Material.END_STONE);
                case NEUTRAL -> new ItemStack(Material.STRUCTURE_VOID);
                case CAVE -> new ItemStack(Material.BARRIER);
            };

        return new PanelItemBuilder().
            name(this.user.getTranslation(reference + "name", "[filter]",
                this.user.getTranslation(reference + filterMode.name().toLowerCase()))).
            icon(icon).
            description(description).
            clickHandler((panel, user1, clickType, slot) ->
            {
                this.mode = filterMode;
                this.updateFilters();
                this.build();
                return true;
            }).
            build();
    }


    /**
     * This method opens GUI that allows to select challenge type.
     *
     * @param user User who opens GUI.
     * @param excluded the excluded
     * @param consumer Consumer that allows to get clicked type.
     */
    public static void open(User user, Set<Biome> excluded, BiConsumer<Boolean, Biome> consumer)
    {
        new BiomeSelector(user, excluded, consumer).build();
    }


    /**
     * This method opens GUI that allows to select challenge type.
     *
     * @param user User who opens GUI.
     * @param consumer Consumer that allows to get clicked type.
     */
    public static void open(User user, BiConsumer<Boolean, Biome> consumer)
    {
        new BiomeSelector(user, new HashSet<>(), consumer).build();
    }


// ---------------------------------------------------------------------
// Section: Mode
// ---------------------------------------------------------------------


    /**
     * Biome filters.
     */
    private enum Mode
    {
        /**
         * Temperate mode.
         */
        TEMPERATE,
        /**
         * Warm mode.
         */
        WARM,
        /**
         * Cold mode.
         */
        COLD,
        /**
         * Snowy mode.
         */
        SNOWY,
        /**
         * Aquatic mode.
         */
        AQUATIC,
        /**
         * Nether mode.
         */
        NETHER,
        /**
         * The end mode.
         */
        THE_END,
        /**
         * Neutral mode.
         */
        NEUTRAL,
        /**
         * Cave mode.
         */
        CAVE
    }


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------

    /**
     * List with elements that will be displayed in current GUI.
     */
    private final List<Biome> elements;

    /**
     * This variable stores consumer.
     */
    private final BiConsumer<Boolean, Biome> consumer;

    /**
     * Mode of filter.
     */
    private Mode mode;

    /**
     * Stores filtered items.
     */
    private List<Biome> filterElements;
}
