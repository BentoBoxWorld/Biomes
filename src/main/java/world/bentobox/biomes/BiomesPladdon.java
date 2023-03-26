///
// Created by BONNe
// Copyright - 2022
///


package world.bentobox.biomes;


import org.bukkit.plugin.java.annotation.plugin.ApiVersion;
import org.bukkit.plugin.java.annotation.plugin.Plugin;

import world.bentobox.bentobox.api.addons.Addon;
import world.bentobox.bentobox.api.addons.Pladdon;


/**
 * @author tastybento
 */
@Plugin(name = "Biomes", version = "1.0")
@ApiVersion(ApiVersion.Target.v1_19)
public class BiomesPladdon extends Pladdon
{
    @Override
    public Addon getAddon()
    {
        return new BiomesAddon();
    }
}
