package world.bentobox.biomes.panels.util;

import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.inventory.ItemStack;

import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.biomes.panels.GuiUtils;
import world.bentobox.biomes.utils.Utils;


public class SelectBiomeGUI
{
    public SelectBiomeGUI(User user, Biome inputBiome, int lineLength, BiConsumer<Boolean, Biome> consumer)
    {
        this.consumer = consumer;
        this.user = user;
        this.inputBiome = inputBiome;
        this.lineLength = lineLength;

        // Select active group depending on input biome type.

        if (Utils.isColdBiome(inputBiome))
        {
            this.activeGroup = BiomeGroup.COLD;
        }
        else if (Utils.isDryBiome(inputBiome))
        {
            this.activeGroup = BiomeGroup.DRY;
        }
        else if (Utils.isLushBiome(inputBiome))
        {
            this.activeGroup = BiomeGroup.LUSH;
        }
        else if (Utils.isSnowyBiome(inputBiome))
        {
            this.activeGroup = BiomeGroup.SNOWY;
        }
        else if (Utils.isOceanBiome(inputBiome))
        {
            this.activeGroup = BiomeGroup.OCEAN;
        }
        else if (Utils.isNetherBiome(inputBiome))
        {
            this.activeGroup = BiomeGroup.NETHER;
        }
        else if (Utils.isTheEndBiome(inputBiome))
        {
            this.activeGroup = BiomeGroup.THE_END;
        }
        else if (Utils.isNeutralBiome(inputBiome))
        {
            this.activeGroup = BiomeGroup.NEUTRAL;
        }
        else
        {
            this.activeGroup = BiomeGroup.UNUSED;
        }

        this.build(0);
    }


    /**
     * This method builds panel that allows to select single challenge from input challenges.
     */
    private void build(int pageIndex)
    {
        PanelBuilder panelBuilder = new PanelBuilder().user(this.user).name(this.user.getTranslation("biomes.gui.title.admin.select-biome"));

        GuiUtils.fillBorder(panelBuilder, Material.BLUE_STAINED_GLASS_PANE);

        // Maximal elements in page.
        final int MAX_ELEMENTS = 21;

        List<Biome> biomeList = Utils.getBiomeNameMap().values().stream().
                filter(biome -> {
                    if (biome == this.inputBiome)
                    {
                        return false;
                    }

                    switch (activeGroup)
                    {
                    case LUSH:
                        return Utils.isLushBiome(biome);
                    case DRY:
                        return Utils.isDryBiome(biome);
                    case COLD:
                        return Utils.isColdBiome(biome);
                    case SNOWY:
                        return Utils.isSnowyBiome(biome);
                    case OCEAN:
                        return Utils.isOceanBiome(biome);
                    case NETHER:
                        return Utils.isNetherBiome(biome);
                    case THE_END:
                        return Utils.isTheEndBiome(biome);
                    case NEUTRAL:
                        return Utils.isNeutralBiome(biome);
                    default:
                        return true;
                    }
                }).
                sorted().
                collect(Collectors.toList());

        final int correctPage;

        if (pageIndex < 0)
        {
            correctPage = (biomeList.size() - 1) / MAX_ELEMENTS;
        }
        else if (pageIndex > ((biomeList.size()  - 1) / MAX_ELEMENTS))
        {
            correctPage = 0;
        }
        else
        {
            correctPage = pageIndex;
        }

        panelBuilder.item(0, this.buildButton(BiomeGroup.LUSH));
        panelBuilder.item(1, this.buildButton(BiomeGroup.DRY));
        panelBuilder.item(2, this.buildButton(BiomeGroup.COLD));
        panelBuilder.item(3, this.buildButton(BiomeGroup.SNOWY));
        panelBuilder.item(4, this.buildButton(BiomeGroup.OCEAN));
        panelBuilder.item(5, this.buildButton(BiomeGroup.NETHER));
        panelBuilder.item(6, this.buildButton(BiomeGroup.THE_END));
        panelBuilder.item(7, this.buildButton(BiomeGroup.NEUTRAL));
        panelBuilder.item(8, this.buildButton(BiomeGroup.UNUSED));

        if ((biomeList.size() - 1) > MAX_ELEMENTS)
        {
            // Navigation buttons if necessary

            panelBuilder.item(18,
                    new PanelItemBuilder().
                    icon(Material.OAK_SIGN).
                    name(this.user.getTranslation("biomes.gui.buttons.previous")).
                    clickHandler((panel, user1, clickType, slot) -> {
                        this.build(correctPage - 1);
                        return true;
                    }).build());

            panelBuilder.item(26,
                    new PanelItemBuilder().
                    icon(Material.OAK_SIGN).
                    name(this.user.getTranslation("biomes.gui.buttons.next")).
                    clickHandler((panel, user1, clickType, slot) -> {
                        this.build(correctPage + 1);
                        return true;
                    }).build());
        }

        int biomesIndex = MAX_ELEMENTS * correctPage;

        // I want first row to be only for navigation and return button.
        int index = 10;

        while (biomesIndex < ((correctPage + 1) * MAX_ELEMENTS) &&
                biomesIndex < (biomeList.size() - 1) &&
                index < 36)
        {
            if (!panelBuilder.slotOccupied(index))
            {
                if (biomeList.size() != biomesIndex)
                {
                    panelBuilder.item(index,
                            this.createBiomeIcon(biomeList.get(biomesIndex++)));
                }
            }

            index++;
        }

        panelBuilder.item(44,
                new PanelItemBuilder().
                icon(Material.OAK_DOOR).
                name(this.user.getTranslation("biomes.gui.buttons.return")).
                clickHandler( (panel, user1, clickType, slot) -> {
                    this.consumer.accept(true, this.inputBiome);
                    return true;
                }).build());

        panelBuilder.build();
    }


    /**
     * This method builds PanelItem for given biome.
     * @param biome Biome which PanelItem must be created.
     * @return new PanelItem for given biome.
     */
    private PanelItem createBiomeIcon(Biome biome)
    {
        return new PanelItemBuilder().
                name(biome.name()).
                description(Collections.emptyList()).
                icon(Material.MAP).
                clickHandler((panel, user1, clickType, slot) -> {
                    this.consumer.accept(true, biome);

                    return true;
                }).
                glow(false).
                build();
    }


    /**
     * This method builds icon for given BiomeGroup object.
     * @param biomeGroup object which icon must be created.
     * @return PanelItem for given BiomeGroup.
     */
    private PanelItem buildButton(BiomeGroup biomeGroup)
    {
        ItemStack icon;
        String name = this.user.getTranslation("biomes.gui.buttons.admin." + biomeGroup.name().toLowerCase());
        String description = this.user.getTranslation("biomes.gui.descriptions.admin." + biomeGroup.name().toLowerCase());

        switch (biomeGroup)
        {
        case LUSH:
            icon = new ItemStack(Material.SUNFLOWER);
            break;
        case DRY:
            icon = new ItemStack(Material.SAND);
            break;
        case COLD:
            icon = new ItemStack(Material.GRAVEL);
            break;
        case SNOWY:
            icon = new ItemStack(Material.SNOW_BLOCK);
            break;
        case OCEAN:
            icon = new ItemStack(Material.TROPICAL_FISH);
            break;
        case NETHER:
            icon = new ItemStack(Material.NETHERRACK);
            break;
        case THE_END:
            icon = new ItemStack(Material.END_STONE);
            break;
        case NEUTRAL:
            icon = new ItemStack(Material.STRUCTURE_VOID);
            break;
        case UNUSED:
            icon = new ItemStack(Material.BARRIER);
            break;
        default:
            name = "";
            icon = new ItemStack(Material.AIR);
        }

        return new PanelItemBuilder().
                icon(icon).
                name(name).
                description(GuiUtils.stringSplit(description, this.lineLength)).
                glow(this.activeGroup == biomeGroup).
                clickHandler((panel, user, clickType, slot) -> {
                    this.activeGroup = biomeGroup;
                    this.build(0);
                    return true;
                }).
                build();
    }


    // ---------------------------------------------------------------------
    // Section: Enums
    // ---------------------------------------------------------------------


    /**
     * 9 Groups for each biome group from WIKI.
     */
    private enum BiomeGroup
    {
        LUSH,
        DRY,
        COLD,
        SNOWY,
        OCEAN,
        NETHER,
        THE_END,
        NEUTRAL,
        UNUSED
    }


    // ---------------------------------------------------------------------
    // Section: Variables
    // ---------------------------------------------------------------------


    /**
     * This variable stores consumer.
     */
    private BiConsumer<Boolean, Biome> consumer;

    /**
     * User who runs GUI.
     */
    private User user;

    /**
     * Input biome.
     */
    private Biome inputBiome;

    /**
     * This variable stores how large line can be, before warp it.
     */
    private int lineLength;

    /**
     * This variable sotres currently active biome group.
     */
    private BiomeGroup activeGroup;
}
