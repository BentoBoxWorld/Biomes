//
// Created by BONNe
// Copyright - 2020
//


package world.bentobox.biomes.events;


import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.Nullable;
import java.util.UUID;

import world.bentobox.bentobox.api.events.BentoBoxEvent;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.biomes.database.objects.BiomesObject;


/**
 * This event is fired when user is trying to unlock biome. It is cancellable.
 */
public class BiomeUnlockEvent extends BentoBoxEvent implements Cancellable
{
    /**
     * Instantiates a new biome unlock event.
     *
     * @param biome the biome
     * @param user the user
     * @param island the island
     */
    public BiomeUnlockEvent(BiomesObject biome, @Nullable User user, Island island)
    {
        this.biome = biome.getFriendlyName();
        this.biomeId = biome.getUniqueId();

        this.targetPlayer = user == null ? null : user.getUniqueId();
        this.islandUUID = island.getUniqueId();
    }


    /**
     * Gets target player.
     *
     * @return the target player
     */
    public UUID getTargetPlayer()
    {
        return targetPlayer;
    }


    /**
     * Sets target player.
     *
     * @param targetPlayer the target player
     */
    public void setTargetPlayer(UUID targetPlayer)
    {
        this.targetPlayer = targetPlayer;
    }


    /**
     * Gets island uuid.
     *
     * @return the island uuid
     */
    public String getIslandUUID()
    {
        return islandUUID;
    }


    /**
     * Sets island uuid.
     *
     * @param islandUUID the island uuid
     */
    public void setIslandUUID(String islandUUID)
    {
        this.islandUUID = islandUUID;
    }


    /**
     * Gets biome.
     *
     * @return the biome
     */
    public String getBiome()
    {
        return biome;
    }


    /**
     * Sets biome.
     *
     * @param biome the biome
     */
    public void setBiome(String biome)
    {
        this.biome = biome;
    }


    /**
     * Gets biome id.
     *
     * @return the biome id
     */
    public String getBiomeId()
    {
        return biomeId;
    }


    /**
     * Sets biome id.
     *
     * @param biomeId the biome id
     */
    public void setBiomeId(String biomeId)
    {
        this.biomeId = biomeId;
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
        return BiomeUnlockEvent.handlers;
    }


    /**
     * Gets handlers.
     *
     * @return the handlers
     */
    public static HandlerList getHandlerList()
    {
        return BiomeUnlockEvent.handlers;
    }


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------

    /**
     * Player who unlocks biome.
     */
    private UUID targetPlayer;

    /**
     * Island Id.
     */
    private String islandUUID;

    /**
     * Friendly name for biome.
     */
    private String biome;

    /**
     * Biome ID.
     */
    private String biomeId;

    /**
     * Boolean that indicates if event is cancelled.
     */
    private boolean cancelled;

    /**
     * Event listener list for current
     */
    private static final HandlerList handlers = new HandlerList();
}
