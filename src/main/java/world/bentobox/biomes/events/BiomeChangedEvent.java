///
// Created by BONNe
// Copyright - 2022
///

package world.bentobox.biomes.events;


import java.util.UUID;

import org.bukkit.block.Biome;
import org.bukkit.event.HandlerList;
import org.bukkit.util.BlockVector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import world.bentobox.bentobox.api.events.BentoBoxEvent;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.biomes.database.objects.BiomesObject;
import world.bentobox.biomes.tasks.UpdateQueue;


/**
 * This event is fired when player changed biome. It is just informative and is fired when everything is done already.
 */
public class BiomeChangedEvent extends BentoBoxEvent
{
    /**
     * Instantiates a new Biome changed event.
     *
     * @param biome the biome
     * @param user the user
     * @param island the island
     * @param minCoordinate the min coordinate
     * @param maxCoordinate the max coordinate
     * @param result the result
     */
    public BiomeChangedEvent(
        @NotNull BiomesObject biome,
        @Nullable User user,
        @NotNull Island island,
        @NotNull BlockVector minCoordinate,
        @NotNull BlockVector maxCoordinate,
        @Nullable UpdateQueue.Result result)
    {
        this.biomesObject = biome;
        this.user = user;
        this.island = island;
        this.minCoordinate = minCoordinate;
        this.maxCoordinate = maxCoordinate;

        this.result = result;
    }


// ---------------------------------------------------------------------
// Section: Getters
// ---------------------------------------------------------------------


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
     * Gets min coordinate.
     *
     * @return the min coordinate
     */
    @NotNull
    public BlockVector getMinCoordinate()
    {
        return this.minCoordinate;
    }


    /**
     * Gets min x.
     *
     * @return the min x
     */
    public int getMinX()
    {
        return this.minCoordinate.getBlockX();
    }


    /**
     * Gets min y.
     *
     * @return the min y
     */
    public int getMinY()
    {
        return this.minCoordinate.getBlockY();
    }


    /**
     * Gets min z.
     *
     * @return the min z
     */
    public int getMinZ()
    {
        return this.minCoordinate.getBlockZ();
    }


    /**
     * Gets max coordinate.
     *
     * @return the max coordinate
     */
    @NotNull
    public BlockVector getMaxCoordinate()
    {
        return this.maxCoordinate;
    }


    /**
     * Gets max x.
     *
     * @return the max x
     */
    public int getMaxX()
    {
        return this.maxCoordinate.getBlockX();
    }


    /**
     * Gets max y.
     *
     * @return the max y
     */
    public int getMaxY()
    {
        return this.maxCoordinate.getBlockY();
    }


    /**
     * Gets max z.
     *
     * @return the max z
     */
    public int getMaxZ()
    {
        return this.maxCoordinate.getBlockZ();
    }


    /**
     * Gets result.
     *
     * @return the result
     */
    @Nullable
    public UpdateQueue.Result getResult()
    {
        return this.result;
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
        return BiomeChangedEvent.handlers;
    }


    /**
     * Gets handlers.
     *
     * @return the handlers
     */
    public static HandlerList getHandlerList()
    {
        return BiomeChangedEvent.handlers;
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
     * The Biome.
     */
    @NotNull
    private final BiomesObject biomesObject;

    /**
     * The Min coordinate.
     */
    @NotNull
    private final BlockVector minCoordinate;

    /**
     * The Max coordinate.
     */
    @NotNull
    private final BlockVector maxCoordinate;

    /**
     * The result.
     */
    @Nullable
    private final UpdateQueue.Result result;

    /**
     * Event listener list for current
     */
    private static final HandlerList handlers = new HandlerList();
}

