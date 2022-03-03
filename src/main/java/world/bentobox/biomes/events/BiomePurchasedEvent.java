///
// Created by BONNe
// Copyright - 2022
///


package world.bentobox.biomes.events;


import org.bukkit.block.Biome;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import java.util.UUID;

import world.bentobox.bentobox.api.events.BentoBoxEvent;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.biomes.database.objects.BiomesObject;


/**
 * This event is called after player bought the given biome.
 */
public class BiomePurchasedEvent extends BentoBoxEvent
{
    /**
     * Instantiates a new biome buy event.
     *
     * @param biome the biome
     * @param user the user
     * @param island the island
     */
    public BiomePurchasedEvent(@NotNull BiomesObject biome,
        @NotNull User user,
        @NotNull Island island)
    {
        this.biomesObject = biome;
        this.user = user;
        this.island = island;
    }


    /**
     * Gets user.
     *
     * @return the user
     */
    @NotNull
    public User getUser()
    {
        return this.user;
    }


    /**
     * Gets user uuid.
     *
     * @return the user uuid
     */
    public UUID getUserUUID()
    {
        return this.user.getUniqueId();
    }


    /**
     * Gets island.
     *
     * @return the island
     */
    @NotNull
    public Island getIsland()
    {
        return this.island;
    }


    /**
     * Gets island uuid.
     *
     * @return the island uuid
     */
    public String getIslandUUID()
    {
        return this.island.getUniqueId();
    }


    /**
     * Gets biomes object.
     *
     * @return the biomes object
     */
    @NotNull
    public BiomesObject getBiomesObject()
    {
        return this.biomesObject;
    }


    /**
     * Gets biome.
     *
     * @return the biome
     */
    public Biome getBiome()
    {
        return this.biomesObject.getBiome();
    }


    /**
     * Gets biome id.
     *
     * @return the biome id
     */
    public String getBiomeId()
    {
        return this.biomesObject.getUniqueId();
    }


// ---------------------------------------------------------------------
// Section: Handler methods
// ---------------------------------------------------------------------


    /**
     * Gets handlers.
     *
     * @return the handlers
     */
    @Override
    public HandlerList getHandlers()
    {
        return BiomePurchasedEvent.handlers;
    }


    /**
     * Gets handlers.
     *
     * @return the handlers
     */
    public static HandlerList getHandlerList()
    {
        return BiomePurchasedEvent.handlers;
    }


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------


    /**
     * The User.
     */
    @NotNull
    private final User user;

    /**
     * The Island.
     */
    @NotNull
    private final Island island;


    /**
     * The Biome.
     */
    @NotNull
    private final BiomesObject biomesObject;

    /**
     * Event listener list for current
     */
    private static final HandlerList handlers = new HandlerList();
}
