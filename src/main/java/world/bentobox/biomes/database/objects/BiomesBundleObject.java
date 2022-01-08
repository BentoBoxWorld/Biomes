//
// Created by BONNe
// Copyright - 2020
//


package world.bentobox.biomes.database.objects;


import com.google.gson.annotations.Expose;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import java.util.*;

import world.bentobox.bentobox.database.objects.DataObject;
import world.bentobox.bentobox.database.objects.Table;


/**
 * The Biomes Bundle Object.
 */
@Table(name = "BiomesBundle")
public class BiomesBundleObject implements DataObject
{
    /**
     * Instantiates a new Generator bundle object.
     */
    public BiomesBundleObject()
    {
    }


// ---------------------------------------------------------------------
// Section: Getters and Setters
// ---------------------------------------------------------------------


    /**
     * Gets uniqueId
     *
     * @return the unique id
     */
    @Override
    public String getUniqueId()
    {
        return uniqueId;
    }


    /**
     * Sets the uniqueId
     *
     * @param uniqueId the uniqueId
     */
    @Override
    public void setUniqueId(String uniqueId)
    {
        this.uniqueId = uniqueId;
    }


    /**
     * Gets friendly name.
     *
     * @return the friendly name
     */
    public String getFriendlyName()
    {
        return friendlyName;
    }


    /**
     * Sets friendly name.
     *
     * @param friendlyName the friendly name
     */
    public void setFriendlyName(String friendlyName)
    {
        this.friendlyName = friendlyName;
    }


    /**
     * Gets description.
     *
     * @return the description
     */
    public List<String> getDescription()
    {
        return description;
    }


    /**
     * Sets description.
     *
     * @param description the description
     */
    public void setDescription(List<String> description)
    {
        this.description = description;
    }


    /**
     * Gets generator icon.
     *
     * @return the generator icon
     */
    public ItemStack getBundleIcon()
    {
        return bundleIcon.clone();
    }


    /**
     * Sets generator icon.
     *
     * @param bundleIcon the generator icon
     */
    public void setBundleIcon(ItemStack bundleIcon)
    {
        this.bundleIcon = bundleIcon;
    }


    /**
     * Gets generator tiers.
     *
     * @return the generator tiers
     */
    public Set<String> getBiomeObjects()
    {
        return biomeObjects;
    }


    /**
     * Sets generator tiers.
     *
     * @param biomeObjects the generator tiers
     */
    public void setBiomeObjects(Set<String> biomeObjects)
    {
        this.biomeObjects = biomeObjects;
    }


// ---------------------------------------------------------------------
// Section: Clone
// ---------------------------------------------------------------------


    /**
     * Creates and returns a copy of this object.  The precise meaning of "copy" may depend on the class of the object.
     *
     * @return a clone of this instance.
     */
    public BiomesBundleObject copy()
    {
        BiomesBundleObject clone = new BiomesBundleObject();

        clone.setUniqueId(this.uniqueId);
        clone.setFriendlyName(this.friendlyName);
        clone.setBundleIcon(this.bundleIcon.clone());
        clone.setDescription(new ArrayList<>(this.description));
        clone.setBiomeObjects(new HashSet<>(this.biomeObjects));

        return clone;
    }


// ---------------------------------------------------------------------
// Section: Static dummy bundle.
// ---------------------------------------------------------------------

    /**
     * The Unique id.
     */
    @Expose
    private String uniqueId;

    /**
     * The Friendly name.
     */
    @Expose
    private String friendlyName;


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------

    /**
     * The Description.
     */
    @Expose
    private List<String> description = Collections.emptyList();

    /**
     * The Generator icon.
     */
    @Expose
    private ItemStack bundleIcon = new ItemStack(Material.STONE);

    /**
     * The Generator tiers.
     */
    @Expose
    private Set<String> biomeObjects = new HashSet<>();

    /**
     * Dummy bundle is used to display a bundle that contains all generators.
     */
    public final static BiomesBundleObject dummyBundle;

    /**
     * Populate dummy bundle with some values.
     */
    static
    {
        dummyBundle = new BiomesBundleObject();
        dummyBundle.setBundleIcon(new ItemStack(Material.STRUCTURE_VOID));
        dummyBundle.setFriendlyName("&f&l No Bundle");
        dummyBundle.setUniqueId(null);

        List<String> description = new ArrayList<>();
        description.add("&cThis is not an actual");
        description.add("&cbundle.");

        dummyBundle.setDescription(description);
    }
}
