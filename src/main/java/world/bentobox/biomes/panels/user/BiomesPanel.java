//
// Created by BONNe
// Copyright - 2021
//


package world.bentobox.biomes.panels.user;


import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import world.bentobox.bentobox.api.panels.Panel;
import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.TemplatedPanel;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.panels.builders.TemplatedPanelBuilder;
import world.bentobox.bentobox.api.panels.reader.ItemTemplateRecord;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.util.Util;
import world.bentobox.biomes.BiomesAddon;
import world.bentobox.biomes.config.Settings;
import world.bentobox.biomes.database.objects.BiomesIslandDataObject;
import world.bentobox.biomes.database.objects.BiomesObject;
import world.bentobox.biomes.panels.CommonPanel;
import world.bentobox.biomes.utils.Constants;
import world.bentobox.biomes.utils.Utils;


/**
 * Biomes panel builder.
 */
public class BiomesPanel extends CommonPanel
{
    /**
     * Instantiates a new Biomes panel.
     *
     * @param addon the addon
     * @param world the world
     * @param user the user
     * @param topLabel the top label
     * @param permissionPrefix the permission prefix
     */
    private BiomesPanel(BiomesAddon addon,
        World world,
        User user,
        String topLabel,
        String permissionPrefix)
    {
        super(addon, user, world, topLabel, permissionPrefix);

        this.island = this.addon.getIslands().getIsland(world, user);
        // Get valid user island data
        this.islandData = this.manager.validateIslandData(this.island);
        // Store biomes in local list to avoid building it every time.
        this.biomeList = this.manager.getIslandBiomes(world, this.islandData);
    }


    /**
     * Open the Biomes GUI.
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
        new BiomesPanel(addon, world, user, topLabel, permissionPrefix).build();
    }


    protected void build()
    {
        // Do not open gui if there is no magic sticks.
        if (this.biomeList.isEmpty())
        {
            this.addon.logError("There are no available biomes!");
            Utils.sendMessage(this.user, this.user.getTranslation(Constants.ERRORS + "no-biomes",
                Constants.PARAMETER_GAMEMODE, Utils.getGameMode(this.world)));
            return;
        }

        if (this.island == null || this.islandData == null)
        {
            Utils.sendMessage(this.user, this.user.getTranslation("general.errors.no-island"));
            return;
        }

        // Start building panel.
        TemplatedPanelBuilder panelBuilder = new TemplatedPanelBuilder();

        // Set main template.
        panelBuilder.template("main_panel", new File(this.addon.getDataFolder(), "panels"));
        panelBuilder.user(this.user);
        panelBuilder.world(this.user.getWorld());

        // Register button builders
        panelBuilder.registerTypeBuilder("BIOME", this::createBiomeButton);

        // Register next and previous builders
        panelBuilder.registerTypeBuilder("NEXT", this::createNextButton);
        panelBuilder.registerTypeBuilder("PREVIOUS", this::createPreviousButton);

        // Register unknown type builder.
        panelBuilder.build();
    }


    /**
     * Create biome button panel item.
     *
     * @param template the template
     * @param slot the slot
     * @return the panel item
     */
    @Nullable
    private PanelItem createBiomeButton(ItemTemplateRecord template, TemplatedPanel.ItemSlot slot)
    {
        if (this.biomeList.isEmpty())
        {
            // Does not contain any sticks.
            return null;
        }

        BiomesObject biomesObject;

        // Check if that is a specific sticks
        if (template.dataMap().containsKey("id"))
        {
            String id = (String) template.dataMap().get("id");

            // Find a challenge with given id;
            biomesObject = this.biomeList.stream().
                filter(biomeId -> biomeId.getUniqueId().equals(id)).
                findFirst().
                orElse(null);

            if (biomesObject == null)
            {
                // There is no stick in the list with specific id.
                return null;
            }
        }
        else
        {
            int index = this.biomesIndex * slot.amountMap().getOrDefault("BIOME", 1) + slot.slot();

            if (index >= this.biomeList.size())
            {
                // Out of index.
                return null;
            }

            biomesObject = this.biomeList.get(index);
        }

        return this.createBiomeButton(template, biomesObject);
    }


    /**
     * Create biome button panel item.
     *
     * @param template the template
     * @param biomesObject the biomes object
     * @return the panel item
     */
    @NonNull
    private PanelItem createBiomeButton(ItemTemplateRecord template, BiomesObject biomesObject)
    {
        PanelItemBuilder builder = new PanelItemBuilder();

        // Template specification are always more important than dynamic content.
        builder.icon(template.icon() != null ? template.icon().clone() : biomesObject.getIcon());

        // Template specific title is always more important than biomesObject name.
        if (template.title() != null && !template.title().isBlank())
        {
            builder.name(this.user.getTranslation(this.world, template.title(),
                Constants.PARAMETER_NAME, biomesObject.getFriendlyName()));
        }
        else
        {
            builder.name(Util.translateColorCodes(biomesObject.getFriendlyName()));
        }

        if (template.description() != null && !template.description().isBlank())
        {
            // TODO: adding parameters could be useful.
            builder.description(this.user.getTranslation(this.world, template.description()));
        }
        else
        {
            builder.description(this.generateBiomesDescription(biomesObject, this.user));
        }

        // Get only possible actions, by removing all inactive ones.
        List<ItemTemplateRecord.ActionRecords> activeActions = new ArrayList<>(template.actions());

        activeActions.removeIf(action -> {
            switch (action.actionType().toUpperCase())
            {
                case "BUY" -> {
                    return this.islandData.isPurchased(biomesObject);
                }
                case "CHANGE", "ADVANCED_PANEL" -> {
                    return !this.islandData.isUnlocked(biomesObject) ||
                        !this.islandData.isPurchased(biomesObject);
                }
                default -> {
                    return false;
                }
            }
        });

        // Add Click handler
        builder.clickHandler((panel, user, clickType, i) -> {
            for (ItemTemplateRecord.ActionRecords action : activeActions)
            {
                if (clickType == action.clickType())
                {
                    switch (action.actionType().toUpperCase())
                    {
                        case "BUY" -> {
                            this.buyBiome(biomesObject);
                        }
                        case "CHANGE" -> {
                            // Biome change is done via commands, because that is the only way how
                            // to apply timeouts reliably.

                            String[] split = action.content().split(":");

                            this.changeBiome(biomesObject, split[0], split.length > 1 ? split[1] : "1");
                        }
                        case "ADVANCED_PANEL" -> {
                            AdvancedPanel.open(this, biomesObject, null);
                        }
                    }
                }
            }

            return true;
        });

        // Collect tooltips.
        List<String> tooltips = activeActions.stream().
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

        // Click Handlers are managed by custom addon buttons.
        return builder.build();
    }


    /**
     * Create next button panel item.
     *
     * @param template the template
     * @param slot the slot
     * @return the panel item
     */
    @Nullable
    private PanelItem createNextButton(@NonNull ItemTemplateRecord template, TemplatedPanel.ItemSlot slot)
    {
        int size = this.biomeList.size();

        if (size <= slot.amountMap().getOrDefault("BIOME", 1) ||
            1.0 * size / slot.amountMap().getOrDefault("BIOME", 1) <= this.biomesIndex + 1)
        {
            // There are no next elements
            return null;
        }

        int nextPageIndex = this.biomesIndex + 2;

        PanelItemBuilder builder = new PanelItemBuilder();

        if (template.icon() != null)
        {
            ItemStack clone = template.icon().clone();

            if ((Boolean) template.dataMap().getOrDefault("indexing", false))
            {
                clone.setAmount(nextPageIndex);
            }

            builder.icon(clone);
        }

        if (template.title() != null)
        {
            builder.name(this.user.getTranslation(this.world, template.title()));
        }

        if (template.description() != null)
        {
            builder.description(this.user.getTranslation(this.world, template.description(),
                Constants.PARAMETER_NUMBER, String.valueOf(nextPageIndex)));
        }

        // Add ClickHandler
        builder.clickHandler((panel, user, clickType, i) ->
        {
            // Next button ignores click type currently.
            this.biomesIndex++;
            this.build();
            // Always return true.
            return true;
        });

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
     * Create previous button panel item.
     *
     * @param template the template
     * @param slot the slot
     * @return the panel item
     */
    @Nullable
    private PanelItem createPreviousButton(@NonNull ItemTemplateRecord template, TemplatedPanel.ItemSlot slot)
    {
        if (this.biomesIndex == 0)
        {
            // There are no next elements
            return null;
        }

        int previousPageIndex = this.biomesIndex;

        PanelItemBuilder builder = new PanelItemBuilder();

        if (template.icon() != null)
        {
            ItemStack clone = template.icon().clone();

            if ((Boolean) template.dataMap().getOrDefault("indexing", false))
            {
                clone.setAmount(previousPageIndex);
            }

            builder.icon(clone);
        }

        if (template.title() != null)
        {
            builder.name(this.user.getTranslation(this.world, template.title()));
        }

        if (template.description() != null)
        {
            builder.description(this.user.getTranslation(this.world, template.description(),
                Constants.PARAMETER_NUMBER, String.valueOf(previousPageIndex)));
        }

        // Add ClickHandler
        builder.clickHandler((panel, user, clickType, i) ->
        {
            // Next button ignores click type currently.
            this.biomesIndex--;
            this.build();

            // Always return true.
            return true;
        });

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
     *
     * @param biomesObject the biomes object
     * @param mode the mode
     * @param range the range
     */
    private void changeBiome(BiomesObject biomesObject, String mode, String range)
    {
        List<String> arguments = new ArrayList<>();

        arguments.add(biomesObject.getUniqueId());


        if (BiomesAddon.BIOMES_WORLD_PROTECTION.isSetForWorld(this.world))
        {
            arguments.add(mode);
            arguments.add(range);
        }
        else
        {
            // This fix issues when admin disables Advanced GUI and sets
            // incompatible options

            if (mode.equalsIgnoreCase(Settings.UpdateMode.ISLAND.name()))
            {
                arguments.add(Settings.UpdateMode.RANGE.name());
                arguments.add(Integer.toString(this.addon.getPlugin().getIWM().getIslandDistance(this.world)));
            }
            else
            {
                arguments.add(mode);
                arguments.add(range);
            }
        }

        this.callCommand(this.addon.getSettings().getPlayerSetCommand().split(" ")[0], arguments);
    }


    /**
     * Buy biome.
     *
     * @param biomesObject the biomes object
     */
    private void buyBiome(BiomesObject biomesObject)
    {
        this.callCommand(this.addon.getSettings().getPlayerBuyCommand().split(" ")[0],
            Collections.singletonList(biomesObject.getUniqueId()));
    }


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------

    /**
     * This will be used for paging.
     */
    private int biomesIndex;

    /**
     * List of biomes that user can change.
     */
    private final List<BiomesObject> biomeList;

    /**
     * The island which biome is changed.
     */
    private final Island island;

    /**
     * Target island data object.
     */
    private final BiomesIslandDataObject islandData;
}
