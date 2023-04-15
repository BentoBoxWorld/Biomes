///
// Created by BONNe
// Copyright - 2022
///


package world.bentobox.biomes;


import world.bentobox.bentobox.api.addons.Addon;
import world.bentobox.bentobox.api.addons.Pladdon;


public class BiomesPladdon extends Pladdon
{
    @Override
    public Addon getAddon()
    {
        return new BiomesAddon();
    }
}
