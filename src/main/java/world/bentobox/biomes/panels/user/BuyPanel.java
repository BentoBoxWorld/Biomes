///
// Created by BONNe
// Copyright - 2022
///


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

import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.TemplatedPanel;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.panels.builders.TemplatedPanelBuilder;
import world.bentobox.bentobox.api.panels.reader.ItemTemplateRecord;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.util.Util;
import world.bentobox.biomes.BiomesAddon;
import world.bentobox.biomes.database.objects.BiomesIslandDataObject;
import world.bentobox.biomes.database.objects.BiomesObject;
import world.bentobox.biomes.panels.CommonPanel;
import world.bentobox.biomes.utils.Constants;
import world.bentobox.biomes.utils.Utils;


/**
 * Biomes panel builder.
 */
public class BuyPanel extends CommonPanel
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
    private BuyPanel(BiomesAddon addon,
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
        // Remove wrong environment biomes.
        this.biomeList = this.manager.getIslandBiomes(world, this.islandData).stream().
            filter(biome -> biome.getEnvironment().equals(user.getWorld().getEnvironment())).
            filter(biomesObject -> this.islandData != null && !this.manager.isPurchased(this.islandData, biomesObject)).
            collect(Collectors.toList());

    }


    /**
     * Instantiates a new Buy panel.
     *
     * @param parentPanel the parent panel
     */
    private BuyPanel(CommonPanel parentPanel)
    {
        super(parentPanel);

        this.island = this.addon.getIslands().getIsland(world, user);
        // Get valid user island data
        this.islandData = this.manager.validateIslandData(this.island);
        // Store biomes in local list to avoid building it every time.
        // Remove wrong environment biomes.
        this.biomeList = this.manager.getIslandBiomes(world, this.islandData).stream().
            filter(biome -> biome.getEnvironment().equals(user.getWorld().getEnvironment())).
            filter(biomesObject -> this.islandData != null && !this.manager.isPurchased(this.islandData, biomesObject)).
            collect(Collectors.toList());

    }


    protected void build()
    {
        if (this.island == null || this.islandData == null)
        {
            Utils.sendMessage(this.user, this.user.getTranslation("general.errors.no-island"));
            return;
        }

        // Do not open gui if there is no magic sticks.
        if (this.biomeList.isEmpty())
        {
            this.addon.logError("There are no available biomes!");
            Utils.sendMessage(this.user, this.user.getTranslation(Constants.MESSAGES + "everything-already-bought",
                Constants.PARAMETER_GAMEMODE, Utils.getGameMode(this.world)));
            return;
        }

        // Start building panel.
        TemplatedPanelBuilder panelBuilder = new TemplatedPanelBuilder();

        // Set main template.
        panelBuilder.template("buy_panel", new File(this.addon.getDataFolder(), "panels"));
        panelBuilder.user(this.user);
        panelBuilder.world(this.user.getWorld());

        // Register button builders
        panelBuilder.registerTypeBuilder("BIOME", this::createBiomeButton);

        // Register next and previous builders
        panelBuilder.registerTypeBuilder("NEXT", this::createNextButton);
        panelBuilder.registerTypeBuilder("PREVIOUS", this::createPreviousButton);
        panelBuilder.registerTypeBuilder("RETURN", this::createReturnButton);

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

        activeActions.removeIf(action ->
        {
            if ("BUY".equalsIgnoreCase(action.actionType()))
            {
                return this.addon.getAddonManager().isPurchased(this.islandData, biomesObject);
            }
            else
            {
                return false;
            }
        });

        // Add Click handler
        builder.clickHandler((panel, user, clickType, i) ->
        {
            for (ItemTemplateRecord.ActionRecords action : activeActions)
            {
                if (clickType == action.clickType() &&
                    "BUY".equalsIgnoreCase(action.actionType()))
                {
                    this.buyBiome(biomesObject);
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
        if (!tooltips.isEmpty() && this.islandData.isUnlocked(biomesObject))
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
     * Buy biome.
     *
     * @param biomesObject the biomes object
     */
    private void buyBiome(BiomesObject biomesObject)
    {
        this.callCommand(true, this.addon.getSettings().getPlayerBuyCommand().split(" ")[0],
            Collections.singletonList(biomesObject.getUniqueId()));
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
        new BuyPanel(addon, world, user, topLabel, permissionPrefix).build();
    }


    /**
     * Open new buy panel.
     *
     * @param parentPanel the parent panel
     */
    public static void open(CommonPanel parentPanel)
    {
        new BuyPanel(parentPanel).build();
    }


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------

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

    /**
     * This will be used for paging.
     */
    private int biomesIndex;
}
