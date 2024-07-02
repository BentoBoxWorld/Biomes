///
// Created by BONNe
// Copyright - 2022
///


package world.bentobox.biomes;


import world.bentobox.bentobox.api.addons.Addon;
import world.bentobox.bentobox.api.addons.Pladdon;


public class BiomesPladdon extends Pladdon
{
    private Addon addon;

    @Override
    public Addon getAddon() {
        if (addon == null) {
            addon = new BiomesAddon();
        }
        return addon;
    }
}
