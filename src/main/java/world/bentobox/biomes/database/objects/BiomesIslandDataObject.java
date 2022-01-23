package world.bentobox.biomes.database.objects;


import com.google.gson.annotations.Expose;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

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
    public Map<String, AtomicInteger> getBiomeChangeCounter()
    {
        return this.biomeChangeCounter;
    }


    /**
     * Sets biome change counter.
     *
     * @param biomeChangeCounter the biome change counter
     */
    public void setBiomeChangeCounter(Map<String, AtomicInteger> biomeChangeCounter)
    {
        this.biomeChangeCounter = biomeChangeCounter;
    }


// ---------------------------------------------------------------------
// Section: Useful methods
// ---------------------------------------------------------------------


    /**
     * Gets biome change counter.
     *
     * @param biomeObjectId the biome object id
     * @return the biome change counter
     */
    public int getBiomeChangeCounter(@NotNull String biomeObjectId)
    {
        return this.biomeChangeCounter.getOrDefault(biomeObjectId, dummy).get();
    }


    /**
     * Gets biome change counter.
     *
     * @param biomesObject the biome object
     * @return the biome change counter
     */
    public int getBiomeChangeCounter(@NotNull BiomesObject biomesObject)
    {
        return this.getBiomeChangeCounter(biomesObject.getUniqueId());
    }


    /**
     * Increase biome change counter.
     *
     * @param biomeObjectId the biome object id
     */
    public void increaseBiomeChangeCounter(@NotNull String biomeObjectId)
    {
        this.biomeChangeCounter.computeIfAbsent(biomeObjectId,
            id -> new AtomicInteger(0)).incrementAndGet();
    }


    /**
     * Increase biome change counter.
     *
     * @param biomesObject the biome object
     */
    public void increaseBiomeChangeCounter(@NotNull BiomesObject biomesObject)
    {
        this.increaseBiomeChangeCounter(biomesObject.getUniqueId());
    }


    /**
     * Adjust biome change counter with the new value.
     *
     * @param biomesObject the biomes object
     * @param intValue the int value
     */
    public void adjustBiomeChangeCounter(@NotNull BiomesObject biomesObject, int intValue)
    {
        this.biomeChangeCounter.computeIfAbsent(biomesObject.getUniqueId(),
            id -> new AtomicInteger(0)).set(intValue);
    }


    /**
     * Is unlocked biome.
     *
     * @param biomeObjectId the biome object id
     * @return the boolean
     */
    public boolean isUnlocked(@NotNull String biomeObjectId)
    {
        return this.unlockedBiomes.contains(biomeObjectId);
    }


    /**
     * Is unlocked biome.
     *
     * @param biomesObject the biome object
     * @return the boolean
     */
    public boolean isUnlocked(@NotNull BiomesObject biomesObject)
    {
        return this.isUnlocked(biomesObject.getUniqueId());
    }


    /**
     * Is purchased biome.
     *
     * @param biomeObjectId the biome object id
     * @return the boolean
     */
    public boolean isPurchased(@NotNull String biomeObjectId)
    {
        return this.purchasedBiomes.contains(biomeObjectId);
    }


    /**
     * Is purchased biome.
     *
     * @param biomesObject the biome object
     * @return the boolean
     */
    public boolean isPurchased(@NotNull BiomesObject biomesObject)
    {
        return this.isPurchased(biomesObject.getUniqueId());
    }


    /**
     * Unlock biome.
     *
     * @param biomeObjectId the biome object id
     */
    public void unlockBiome(@NotNull String biomeObjectId)
    {
        this.unlockedBiomes.add(biomeObjectId);
    }


    /**
     * Unlock biome.
     *
     * @param biomesObject the biome object
     */
    public void unlockBiome(@NotNull BiomesObject biomesObject)
    {
        this.unlockBiome(biomesObject.getUniqueId());
    }


    /**
     * Purchase biome.
     *
     * @param biomeObjectId the biome object id
     */
    public void purchaseBiome(@NotNull String biomeObjectId)
    {
        this.purchasedBiomes.add(biomeObjectId);
    }


    /**
     * Purchase biome.
     *
     * @param biomesObject the biome object
     */
    public void purchaseBiome(@NotNull BiomesObject biomesObject)
    {
        this.purchaseBiome(biomesObject.getUniqueId());
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
    private Map<String, AtomicInteger> biomeChangeCounter = new ConcurrentHashMap<>();

    /**
     * The constant dummy for non-existing values.
     */
    private static final AtomicInteger dummy = new AtomicInteger(0);
}
