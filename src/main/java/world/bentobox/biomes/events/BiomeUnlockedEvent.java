//
// Created by BONNe
// Copyright - 2020
//


package world.bentobox.biomes.events;


import org.bukkit.block.Biome;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.UUID;

import world.bentobox.bentobox.api.events.BentoBoxEvent;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.biomes.database.objects.BiomesObject;


/**
 * This event is fired when user is trying to unlock biome. It is cancellable.
 */
public class BiomeUnlockedEvent extends BentoBoxEvent implements Cancellable
{
    /**
     * Instantiates a new biome unlock event.
     *
     * @param biome the biome
     * @param user the user
     * @param island the island
     */
    public BiomeUnlockedEvent(@NotNull BiomesObject biome,
        @Nullable User user,
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
    @Nullable
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
        return this.user == null ? null : this.user.getUniqueId();
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


    /**
     * Gets the cancellation state of this event. A cancelled event will not be executed in the server, but will still
     * pass to other plugins
     *
     * @return true if this event is cancelled
     */
    @Override
    public boolean isCancelled()
    {
        return this.cancelled;
    }


    /**
     * Sets the cancellation state of this event. A cancelled event will not be executed in the server, but will still
     * pass to other plugins.
     *
     * @param cancel true if you wish to cancel this event
     */
    @Override
    public void setCancelled(boolean cancel)
    {
        this.cancelled = cancel;
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
        return BiomeUnlockedEvent.handlers;
    }


    /**
     * Gets handlers.
     *
     * @return the handlers
     */
    public static HandlerList getHandlerList()
    {
        return BiomeUnlockedEvent.handlers;
    }


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------

    /**
     * The User.
     */
    @Nullable
    private final User user;

    /**
     * The Island.
     */
    @NotNull
    private final Island island;

    /**
     * The Biomes object.
     */
    @NotNull
    private final BiomesObject biomesObject;

    /**
     * Boolean that indicates if event is cancelled.
     */
    private boolean cancelled;

    /**
     * Event listener list for current
     */
    private static final HandlerList handlers = new HandlerList();
}
