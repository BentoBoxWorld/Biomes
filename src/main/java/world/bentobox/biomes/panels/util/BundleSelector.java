///
// Created by BONNe
// Copyright - 2022
///


package world.bentobox.biomes.panels.util;


import org.bukkit.Material;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import lv.id.bonne.panelutils.PanelUtils;
import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.biomes.database.objects.BiomesBundleObject;
import world.bentobox.biomes.database.objects.BiomesObject;
import world.bentobox.biomes.panels.CommonPagedPanel;
import world.bentobox.biomes.panels.CommonPanel;
import world.bentobox.biomes.utils.Constants;


/**
 * The Bundle Selector GUI.
 */
public class BundleSelector extends CommonPagedPanel<BiomesBundleObject>
{
    /**
     * Instantiates a new Select bundle panel.
     *
     * @param panel the parent panel
     * @param bundle the input bundle
     * @param consumer the consumer
     */
    private BundleSelector(CommonPanel panel, BiomesBundleObject bundle, Consumer<BiomesBundleObject> consumer)
    {
        super(panel);
        this.consumer = consumer;
        this.selectedBundle = bundle == null ? BiomesBundleObject.dummyBundle : bundle;

        this.bundleList = this.addon.getAddonManager().getBundles(this.world);

        // Add dummy bundle that contains all generators from GameMode.
        this.bundleList.add(0, BiomesBundleObject.dummyBundle);
        // Add all generators to the dummy bundle
        BiomesBundleObject.dummyBundle.setBiomeObjects(
            this.addon.getAddonManager().getBiomes(this.world).stream().
                map(BiomesObject::getUniqueId).
                collect(Collectors.toSet()));
    }


    /**
     * Update filters.
     */
    @Override
    protected void updateFilters()
    {
        // Not implemented for this gui.
    }


    /**
     * This method builds panel that allows to select single challenge from input challenges.
     */
    protected void build()
    {
        PanelBuilder panelBuilder =
            new PanelBuilder().user(this.user).name(this.user.getTranslation(Constants.TITLE + "bundle-selector"));

        PanelUtils.fillBorder(panelBuilder, Material.BLUE_STAINED_GLASS_PANE);

        this.populateElements(panelBuilder, this.bundleList);
        panelBuilder.item(44, this.returnButton);

        panelBuilder.build();
    }


    @Override
    protected PanelItem createElementButton(BiomesBundleObject bundle)
    {
        boolean glow = this.selectedBundle == bundle;

        List<String> description = new ArrayList<>();

        description.add(this.generateBundleDescription(bundle));

        if (this.selectedBundle == bundle)
        {
            description.add(this.user.getTranslation(Constants.DESCRIPTIONS + "selected"));
        }

        description.add("");
        description.add(this.user.getTranslation(Constants.TIPS + "click-to-choose"));

        PanelItem.ClickHandler clickHandler = (panel, user, clickType, i) ->
        {
            this.consumer.accept(bundle);
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
     * Opens panel for this class without necessity to create new class instance.
     *
     * @param panel the parent panel
     * @param bundle the input bundle
     * @param consumer the consumer
     */
    public static void open(CommonPanel panel, BiomesBundleObject bundle, Consumer<BiomesBundleObject> consumer)
    {
        new BundleSelector(panel, bundle, consumer).build();
    }


    // ---------------------------------------------------------------------
    // Section: Variables
    // ---------------------------------------------------------------------

    /**
     * This variable stores consumer.
     */
    private final Consumer<BiomesBundleObject> consumer;

    /**
     * Bundle which was selected previously.
     */
    private final BiomesBundleObject selectedBundle;

    /**
     * List of bundles that will be listed in this panel.
     */
    private final List<BiomesBundleObject> bundleList;
}
