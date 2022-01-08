//
// Created by BONNe
// Copyright - 2020
//


package world.bentobox.biomes.events;


import org.bukkit.event.HandlerList;
import java.util.UUID;

import world.bentobox.bentobox.api.events.BentoBoxEvent;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.biomes.database.objects.BiomesObject;


/**
 * This event is called after player bought the given biome.
 */
public class BiomesBuyEvent extends BentoBoxEvent
{
    /**
     * Instantiates a new biome buy event.
     *
     * @param biome the biome
     * @param user the user
     * @param island the island
     */
    public BiomesBuyEvent(BiomesObject biome, User user, String island)
    {
        this.biome = biome.getFriendlyName();
        this.biomeId = biome.getUniqueId();

        this.targetPlayer = user.getUniqueId();
        this.islandUUID = island;
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
        return BiomesBuyEvent.handlers;
    }


    /**
     * Gets handlers.
     *
     * @return the handlers
     */
    public static HandlerList getHandlerList()
    {
        return BiomesBuyEvent.handlers;
    }


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------

    /**
     * Player who bought biome.
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
     * biome ID.
     */
    private String biomeId;

    /**
     * Event listener list for current
     */
    private static final HandlerList handlers = new HandlerList();
}
