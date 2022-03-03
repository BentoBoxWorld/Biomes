///
// Created by BONNe
// Copyright - 2022
///

package world.bentobox.biomes.panels.util;


import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import world.bentobox.bentobox.api.panels.Panel;
import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.biomes.utils.Constants;
import world.bentobox.biomes.utils.Utils;


/**
 * This class creates panel that allows to select and deselect World Environments. On save it runs input consumer with
 * true and selected values.
 */
public record EnvironmentSelector(User user, World.Environment values, BiConsumer<Boolean, World.Environment> consumer)
{
    /**
     * This method builds GUI that allows to select challenge type.
     */
    private void build()
    {
        PanelBuilder panelBuilder = new PanelBuilder().
            user(this.user).
            type(Panel.Type.HOPPER).
            name(this.user.getTranslation(Constants.TITLE + "environment-selector"));

        panelBuilder.item(0, this.getButton(World.Environment.NORMAL));
        panelBuilder.item(1, this.getButton(World.Environment.NETHER));
        panelBuilder.item(2, this.getButton(World.Environment.THE_END));
        panelBuilder.item(4, this.getButton(Button.CANCEL));

        panelBuilder.build();
    }


    /**
     * This method create button that does some functionality in current gui.
     *
     * @param environment Environment
     * @return PanelItem.
     */
    private PanelItem getButton(World.Environment environment)
    {
        final String reference = Constants.BUTTON + "environment_element.";

        String name = this.user.getTranslation(reference + "name",
            "[environment]", Utils.prettifyObject(environment, this.user));
        final List<String> description = new ArrayList<>(3);
        description.add(this.user.getTranslationOrNothing(reference + "description",
            "[description]", Utils.prettifyDescription(environment, this.user)));

        description.add("");
        description.add(this.user.getTranslation(Constants.TIPS + "click-to-choose"));

        PanelItem.ClickHandler clickHandler = (panel, user, clickType, slot) ->
        {
            this.consumer.accept(true, environment);
            return true;
        };

        ItemStack icon;

        switch (environment)
        {
            case NORMAL -> icon = new ItemStack(Material.DIRT);
            case NETHER -> icon = new ItemStack(Material.NETHERRACK);
            case THE_END -> icon = new ItemStack(Material.END_STONE);
            default -> icon = new ItemStack(Material.PAPER);
        }

        return new PanelItemBuilder().
            icon(icon).
            name(name).
            description(description).
            clickHandler(clickHandler).
            glow(this.values == environment).
            build();
    }


    /**
     * This method create button that does some functionality in current gui.
     *
     * @param button Button functionality.
     * @return PanelItem.
     */
    private PanelItem getButton(Button button)
    {
        final String reference = Constants.BUTTON + button.name().toLowerCase() + ".";

        String name = this.user.getTranslation(reference + "name");
        final List<String> description = new ArrayList<>(3);
        description.add(this.user.getTranslation(reference + "description"));

        ItemStack icon;
        PanelItem.ClickHandler clickHandler;

        if (button == Button.CANCEL)
        {
            icon = new ItemStack(Material.IRON_DOOR);
            clickHandler = (panel, user, clickType, slot) ->
            {
                this.consumer.accept(false, null);
                return true;
            };

            description.add("");
            description.add(this.user.getTranslation(Constants.TIPS + "click-to-cancel"));
        }
        else
        {
            icon = new ItemStack(Material.PAPER);
            clickHandler = null;
        }

        return new PanelItemBuilder().
            icon(icon).
            name(name).
            description(description).
            clickHandler(clickHandler).
            build();
    }


    /**
     * This method opens GUI that allows to select challenge type.
     *
     * @param user User who opens GUI.
     * @param values the values
     * @param consumer Consumer that allows to get clicked type.
     */
    public static void open(User user, World.Environment values, BiConsumer<Boolean, World.Environment> consumer)
    {
        new EnvironmentSelector(user, values, consumer).build();
    }


    /**
     * This enum holds all button values in current gui.
     */
    private enum Button
    {
        /**
         * Cancel button.
         */
        CANCEL,
    }
}
