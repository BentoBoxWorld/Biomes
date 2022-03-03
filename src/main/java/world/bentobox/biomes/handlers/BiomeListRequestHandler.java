///
// Created by BONNe
// Copyright - 2022
///

package world.bentobox.biomes.handlers;


import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;

import world.bentobox.bentobox.api.addons.request.AddonRequestHandler;
import world.bentobox.biomes.BiomesAddon;
import world.bentobox.biomes.database.objects.BiomesObject;


/**
 * This handler returns all biomes uniqueIDs that is operating in given world.
 */
public class BiomeListRequestHandler extends AddonRequestHandler
{
    /**
     * Constructor creates a new BiomeListRequestHandler instance.
     *
     * @param addon of type BiomesAddon
     */
    public BiomeListRequestHandler(BiomesAddon addon)
    {
        super("biomes-list");
        this.addon = addon;
    }


    /**
     * Handle object.
     *
     * @param metaData Required meta data.
     * @return Set of strings that contains completed challenges.
     * @see AddonRequestHandler#handle(Map AddonRequestHandler#handle(MapltString,Objectgt)
     */
    @Override
    public Object handle(Map<String, Object> metaData)
    {
		/*
            What we need in the metaData:
				0. "world-name" -> String
            What we will return:
				- List of biomes in given world.
         */

        if (metaData == null ||
            metaData.isEmpty() ||
            metaData.get(WORLD_NAME) == null ||
            !(metaData.get(WORLD_NAME) instanceof String) ||
            Bukkit.getWorld((String) metaData.get(WORLD_NAME)) == null)
        {
            return Collections.emptyList();
        }

        // Return list of biomes unique IDs from given world.

        return this.addon.getAddonManager().
            getBiomes(Bukkit.getWorld((String) metaData.get(WORLD_NAME))).
            stream().map(BiomesObject::getUniqueId).collect(Collectors.toList());
    }


    /**
     * Variable stores challenges addon.
     */
    private final BiomesAddon addon;


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------

    /**
     * The constant WORLD_NAME.
     */
    private static final String WORLD_NAME = "world-name";
}
