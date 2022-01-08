package world.bentobox.biomes.database.objects;


import com.google.gson.annotations.Expose;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import world.bentobox.bentobox.database.objects.DataObject;
import world.bentobox.bentobox.database.objects.Table;


/**
 * This class stores necessary information for each Biomes Island Data object.
 */
@Table(name = "BiomesIslandData")
public class BiomesIslandDataObject implements DataObject
{
    /**
     * Empty constructor for loader.
     */
    public BiomesIslandDataObject()
    {
        // Empty constructor.
    }


    /**
     * @return the uniqueId
     */
    @Override
    public String getUniqueId()
    {
        return uniqueId;
    }


    /**
     * @param uniqueId the uniqueId to set
     */
    @Override
    public void setUniqueId(String uniqueId)
    {
        this.uniqueId = uniqueId;
    }


    /**
     * Gets owner bundle.
     *
     * @return the owner bundle
     */
    @Nullable
    public String getOwnerBundle()
    {
        return ownerBundle;
    }


    /**
     * Sets owner bundle.
     *
     * @param ownerBundle the owner bundle
     */
    public void setOwnerBundle(@Nullable String ownerBundle)
    {
        this.ownerBundle = ownerBundle;
    }


    /**
     * Gets island bundle.
     *
     * @return the island bundle
     */
    @Nullable
    public String getIslandBundle()
    {
        return islandBundle;
    }


    /**
     * Sets island bundle.
     *
     * @param islandBundle the island bundle
     */
    public void setIslandBundle(@Nullable String islandBundle)
    {
        this.islandBundle = islandBundle;
    }


    /**
     * Gets unlocked biomes.
     *
     * @return the unlocked biomes
     */
    public Set<String> getUnlockedBiomes()
    {
        return unlockedBiomes;
    }


    /**
     * Sets unlocked biomes.
     *
     * @param unlockedBiomes the unlocked biomes
     */
    public void setUnlockedBiomes(Set<String> unlockedBiomes)
    {
        this.unlockedBiomes = unlockedBiomes;
    }


    /**
     * Gets purchased biomes.
     *
     * @return the purchased biomes
     */
    public Set<String> getPurchasedBiomes()
    {
        return purchasedBiomes;
    }


    /**
     * Sets purchased biomes.
     *
     * @param purchasedBiomes the purchased biomes
     */
    public void setPurchasedBiomes(Set<String> purchasedBiomes)
    {
        this.purchasedBiomes = purchasedBiomes;
    }


    /**
     * Gets biome change counter.
     *
     * @return the biome change counter
     */
    public Map<String, Integer> getBiomeChangeCounter()
    {
        return biomeChangeCounter;
    }


    /**
     * Sets biome change counter.
     *
     * @param biomeChangeCounter the biome change counter
     */
    public void setBiomeChangeCounter(Map<String, Integer> biomeChangeCounter)
    {
        this.biomeChangeCounter = biomeChangeCounter;
    }


    /**
     * This method clears island data for this island.
     */
    public void clear()
    {
        this.islandBundle = null;
        this.biomeChangeCounter.clear();
        this.unlockedBiomes.clear();
        this.purchasedBiomes.clear();
    }


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------


    /**
     * Unique ID of the island.
     */
    @Expose
    private String uniqueId;

    /**
     * Stores active bundle.
     */
    @Expose
    @Nullable
    private String ownerBundle = null;

    /**
     * Stores active bundle.
     */
    @Expose
    @Nullable
    private String islandBundle = null;

    /**
     * Stores a names of unlocked biomes.
     */
    @Expose
    private Set<String> unlockedBiomes = new HashSet<>();

    /**
     * Stores a names of purchased biomes.
     */
    @Expose
    private Set<String> purchasedBiomes = new HashSet<>();

    /**
     * Stores map that links biome with how many times it is updated.
     */
    @Expose
    private Map<String, Integer> biomeChangeCounter = new HashMap<>();
}
