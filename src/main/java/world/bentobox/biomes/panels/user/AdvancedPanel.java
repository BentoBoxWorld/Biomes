///
// Created by BONNe
// Copyright - 2022
///


package world.bentobox.biomes.panels.user;


import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.TemplatedPanel;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.panels.builders.TemplatedPanelBuilder;
import world.bentobox.bentobox.api.panels.reader.ItemTemplateRecord;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.biomes.BiomesAddon;
import world.bentobox.biomes.config.Settings;
import world.bentobox.biomes.database.objects.BiomesObject;
import world.bentobox.biomes.panels.CommonPanel;
import world.bentobox.biomes.panels.ConversationUtils;
import world.bentobox.biomes.utils.Constants;
import world.bentobox.biomes.utils.Utils;


/**
 * Biomes advanced panel builder.
 */
public class AdvancedPanel extends CommonPanel
{
    /**
     * Instantiates a new Advanced panel.
     *
     * @param parentPanel the parent panel
     * @param biomesObject the biomes object
     * @param target the target
     */
    private AdvancedPanel(@NotNull CommonPanel parentPanel,
        @NotNull BiomesObject biomesObject,
        @Nullable User target)
    {
        super(parentPanel);
        this.target = target;
        this.biomeObject = biomesObject;

        this.updateMode = this.addon.getSettings().getDefaultMode();
    }


    /**
     * Builds the panel.
     */
    protected void build()
    {
        // Start building panel.
        TemplatedPanelBuilder panelBuilder = new TemplatedPanelBuilder();

        // Set main template.
        panelBuilder.template("advanced_panel", new File(this.addon.getDataFolder(), "panels"));
        panelBuilder.user(this.user);
        panelBuilder.world(this.user.getWorld());

        // Register button builders
        panelBuilder.registerTypeBuilder("MODE", this::createModeButton);

        panelBuilder.registerTypeBuilder("INCREASE", this::createIncreaseButton);
        panelBuilder.registerTypeBuilder("REDUCE", this::createReduceButton);
        panelBuilder.registerTypeBuilder("ACCEPT", this::createValueButton);
        panelBuilder.registerTypeBuilder("RETURN", this::createReturnButton);

        // Register unknown type builder.
        panelBuilder.build();
    }


    /**
     * Create mode button panel item.
     *
     * @param template the template
     * @param itemSlot the item slot
     * @return the panel item
     */
    @NonNull
    private PanelItem createModeButton(ItemTemplateRecord template, TemplatedPanel.ItemSlot itemSlot)
    {
        String update = (String) template.dataMap().getOrDefault("value",
            Settings.UpdateMode.ISLAND.name());

        PanelItemBuilder builder = new PanelItemBuilder();

        if (template.icon() != null)
        {
            ItemStack clone = template.icon().clone();
            builder.icon(clone);
        }

        if (template.title() != null)
        {
            builder.name(this.user.getTranslation(template.title()));
        }

        if (template.description() != null)
        {
            builder.description(this.user.getTranslation(template.description()));
        }

        // Add ClickHandler
        builder.clickHandler((panel, user, clickType, i) ->
        {
            this.updateMode = Settings.UpdateMode.getMode(update.toUpperCase());
            this.build();

            // Always return true.
            return true;
        });

        // Collect tooltips.
        List<String> tooltips = template.actions().stream().
            filter(action -> action.tooltip() != null).
            map(action -> this.user.getTranslation(action.tooltip())).
            filter(text -> !text.isBlank()).
            collect(Collectors.toCollection(() -> new ArrayList<>(template.actions().size())));

        // Add tooltips.
        if (!tooltips.isEmpty())
        {
            // Empty line and tooltips.
            builder.description("");
            builder.description(tooltips);
        }

        builder.glow(Settings.UpdateMode.getMode(update.toUpperCase()) == this.updateMode);

        return builder.build();
    }


    /**
     * Create increase button panel item.
     *
     * @param template the template
     * @param itemSlot the item slot
     * @return the panel item
     */
    @NonNull
    private PanelItem createIncreaseButton(ItemTemplateRecord template, TemplatedPanel.ItemSlot itemSlot)
    {
        int increaseValue = (int) template.dataMap().getOrDefault("value", 1);

        PanelItemBuilder builder = new PanelItemBuilder();

        if (template.icon() != null)
        {
            ItemStack clone = template.icon().clone();
            clone.setAmount(increaseValue);
            builder.icon(clone);
        }

        if (template.title() != null)
        {
            builder.name(this.user.getTranslation(template.title()));
        }

        if (template.description() != null)
        {
            builder.description(this.user.getTranslation(template.description(),
                Constants.PARAMETER_NUMBER, String.valueOf(increaseValue)));
        }

        // Add ClickHandler
        builder.clickHandler((panel, user, clickType, i) ->
        {
            this.range += increaseValue;
            this.build();

            // Always return true.
            return true;
        });

        // Collect tooltips.
        List<String> tooltips = template.actions().stream().
            filter(action -> action.tooltip() != null).
            map(action -> this.user.getTranslation(action.tooltip())).
            filter(text -> !text.isBlank()).
            collect(Collectors.toCollection(() -> new ArrayList<>(template.actions().size())));

        // Add tooltips.
        if (!tooltips.isEmpty())
        {
            // Empty line and tooltips.
            builder.description("");
            builder.description(tooltips);
        }

        return builder.build();
    }


    /**
     * Create reduce button panel item.
     *
     * @param template the template
     * @param itemSlot the item slot
     * @return the panel item
     */
    @NonNull
    private PanelItem createReduceButton(ItemTemplateRecord template, TemplatedPanel.ItemSlot itemSlot)
    {
        int decreaseValue = (int) template.dataMap().getOrDefault("value", 1);

        PanelItemBuilder builder = new PanelItemBuilder();

        if (template.icon() != null)
        {
            ItemStack clone = template.icon().clone();
            clone.setAmount(decreaseValue);
            builder.icon(clone);
        }

        if (template.title() != null)
        {
            builder.name(this.user.getTranslation(template.title()));
        }

        if (template.description() != null)
        {
            builder.description(this.user.getTranslation(template.description(),
                Constants.PARAMETER_NUMBER, String.valueOf(decreaseValue)));
        }

        // Add ClickHandler
        builder.clickHandler((panel, user, clickType, i) ->
        {
            this.range = Math.max(this.range - decreaseValue, 1);
            this.build();

            // Always return true.
            return true;
        });

        // Collect tooltips.
        List<String> tooltips = template.actions().stream().
            filter(action -> action.tooltip() != null).
            map(action -> this.user.getTranslation(action.tooltip())).
            filter(text -> !text.isBlank()).
            collect(Collectors.toCollection(() -> new ArrayList<>(template.actions().size())));

        // Add tooltips.
        if (!tooltips.isEmpty())
        {
            // Empty line and tooltips.
            builder.description("");
            builder.description(tooltips);
        }

        return builder.build();
    }


    /**
     * Create value button panel item.
     *
     * @param template the template
     * @param itemSlot the item slot
     * @return the panel item
     */
    @NonNull
    private PanelItem createValueButton(ItemTemplateRecord template, TemplatedPanel.ItemSlot itemSlot)
    {
        PanelItemBuilder builder = new PanelItemBuilder();

        if (template.icon() != null)
        {
            ItemStack clone = template.icon().clone();
            clone.setAmount(this.range > 0 ? this.range : 1);
            builder.icon(clone);
        }

        if (template.title() != null)
        {
            builder.name(this.user.getTranslation(template.title()));
        }

        if (template.description() != null)
        {
            builder.description(this.user.getTranslation(template.description(),
                Constants.PARAMETER_NUMBER, String.valueOf(this.range),
                Constants.PARAMETER_BIOME, Utils.prettifyObject(this.biomeObject.getBiome(), this.user),
                Constants.PARAMETER_MODE, this.user.getTranslation(Constants.DESCRIPTIONS +
                    "update-mode." + this.updateMode.name().toLowerCase())));
        }

        // Add ClickHandler
        builder.clickHandler((panel, user, clickType, i) ->
        {
            for (ItemTemplateRecord.ActionRecords actionRecords : template.actions())
            {
                if (clickType == actionRecords.clickType())
                {
                    if ("INPUT".equalsIgnoreCase(actionRecords.actionType()))
                    {
                        // Input consumer.
                        Consumer<Number> numberConsumer = number ->
                        {
                            if (number != null)
                            {
                                this.range = number.intValue();
                            }

                            // reopen panel
                            this.build();
                        };

                        ConversationUtils.createNumericInput(numberConsumer,
                            this.user,
                            this.user.getTranslation(Constants.CONVERSATIONS + "input-number"),
                            1,
                            2000);
                    }
                    else if ("ACCEPT".equalsIgnoreCase(actionRecords.actionType()))
                    {
                        this.changeBiome();
                    }
                }
            }

            // Always return true.
            return true;
        });

        // Collect tooltips.
        List<String> tooltips = template.actions().stream().
            filter(action -> action.tooltip() != null).
            map(action -> this.user.getTranslation(action.tooltip())).
            filter(text -> !text.isBlank()).
            collect(Collectors.toCollection(() -> new ArrayList<>(template.actions().size())));

        // Add tooltips.
        if (!tooltips.isEmpty())
        {
            // Empty line and tooltips.
            builder.description("");
            builder.description(tooltips);
        }

        return builder.build();
    }


    /**
     * Create return button panel item.
     *
     * @param template the template
     * @param slot the slot
     * @return the panel item
     */
    @Nullable
    private PanelItem createReturnButton(@NonNull ItemTemplateRecord template, TemplatedPanel.ItemSlot slot)
    {
        PanelItemBuilder builder = new PanelItemBuilder();

        if (template.icon() != null)
        {
            builder.icon(template.icon().clone());
        }
        else
        {
            builder.icon(this.returnButton.getItem());
        }

        if (template.title() != null)
        {
            builder.name(this.user.getTranslation(this.world, template.title()));
        }
        else
        {
            builder.name(this.returnButton.getName());
        }

        if (template.description() != null)
        {
            builder.description(this.user.getTranslation(this.world, template.description()));
        }
        else
        {
            builder.description(this.returnButton.getDescription());
        }

        // Add ClickHandler
        if (this.returnButton.getClickHandler().isPresent())
        {
            builder.clickHandler(this.returnButton.getClickHandler().get());
        }

        // Collect tooltips.
        List<String> tooltips = template.actions().stream().
            filter(action -> action.tooltip() != null).
            map(action -> this.user.getTranslation(this.world, action.tooltip())).
            filter(text -> !text.isBlank()).
            collect(Collectors.toCollection(() -> new ArrayList<>(template.actions().size())));

        // Add tooltips.
        if (!tooltips.isEmpty())
        {
            // Empty line and tooltips.
            builder.description("");
            builder.description(tooltips);
        }

        return builder.build();
    }


    /**
     * Change biome with given parameters.
     */
    private void changeBiome()
    {
        List<String> arguments = new ArrayList<>();

        if (this.target != null)
        {
            arguments.add("set");
            arguments.add(this.target.getName());
        }

        arguments.add(this.biomeObject.getUniqueId());

        if (BiomesAddon.BIOMES_WORLD_PROTECTION.isSetForWorld(this.world))
        {
            arguments.add(this.updateMode.name());
            arguments.add(String.valueOf(this.range));
        }
        else
        {
            // This fix issues when admin disables Advanced GUI and sets
            // incompatible options

            if (this.updateMode.equals(Settings.UpdateMode.ISLAND))
            {
                arguments.add(Settings.UpdateMode.RANGE.name());
                arguments.add(String.valueOf(this.addon.getPlugin().getIWM().getIslandDistance(this.world)));
            }
            else
            {
                arguments.add(this.updateMode.name());
                arguments.add(String.valueOf(this.range));
            }
        }

        if (this.target == null)
        {
            // Target is not specified. Use player command.
            this.callCommand(true, this.addon.getSettings().getPlayerSetCommand().split(" ")[0], arguments);
        }
        else
        {
            // Target is specified. It means command must be called by the admin.
            this.callCommand(false, "set", arguments);
        }
    }


    /**
     * Opens Biomes Advanced Change Panel.
     *
     * @param parentPanel the parent panel
     * @param biomesObject the biomes object
     * @param target the target
     */
    public static void open(@NotNull CommonPanel parentPanel,
        @NotNull BiomesObject biomesObject,
        @Nullable User target)
    {
        new AdvancedPanel(parentPanel, biomesObject, target).build();
    }


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------

    /**
     * The biome that user can change.
     */
    @NotNull
    private final BiomesObject biomeObject;

    /**
     * Target player. Most of the time it will be equal user, but if admin changes, target will be different user.
     */
    @Nullable
    private final User target;

    /**
     * This variable stores update range
     */
    private int range = 1;

    /**
     * This variable stores update mode
     */
    private Settings.UpdateMode updateMode;
}
