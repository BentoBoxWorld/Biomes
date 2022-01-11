package world.bentobox.biomes.database.objects;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.gson.annotations.Expose;

import world.bentobox.bentobox.database.objects.DataObject;
import world.bentobox.bentobox.database.objects.Table;
import world.bentobox.biomes.utils.Utils;


/**
 * This class stores necessary information for each Biomes object.
 */
@Table(name = "Biomes")
public class BiomesObject implements DataObject, Comparable<BiomesObject>
{
    /**
     * Empty constructor for loader.
     */
    public BiomesObject()
    {
        // Empty constructor.
    }


    /**
     * Instantiates a new Biomes object.
     *
     * @param biome the biome
     * @param world the world
     */
    public BiomesObject(Biome biome, World world)
    {
        this.biome = biome;
        this.world = world.getName();
        this.friendlyName = biome.name();
        this.setUniqueId(Utils.getGameMode(world) + "_" + this.biome.toString().toLowerCase());
    }


    // ---------------------------------------------------------------------
    // Section: Setters and Getters
    // ---------------------------------------------------------------------


    /**
     * This method returns biomes name.
     * @return Biomes name.
     */
    public Biome getBiome()
    {
        return this.biome;
    }


    /**
     * This method sets biomes name.
     * @param biome Biomes name.
     */
    public void setBiome(Biome biome)
    {
        this.biome = biome;
    }


    /**
     * @return the deployed
     */
    public boolean isDeployed()
    {
        return deployed;
    }


    /**
     * @param deployed the deployed to set
     */
    public void setDeployed(boolean deployed)
    {
        this.deployed = deployed;
    }


    /**
     * @return the description
     */
    public List<String> getDescription()
    {
        return description;
    }


    /**
     * @param description the description to set
     */
    public void setDescription(List<String> description)
    {
        this.description = description;
    }


    /**
     * @return the friendlyName
     */
    public String getFriendlyName()
    {
        return friendlyName;
    }


    /**
     * @param friendlyName the friendlyName to set
     */
    public void setFriendlyName(String friendlyName)
    {
        this.friendlyName = friendlyName;
    }


    /**
     * @return the icon
     */
    public ItemStack getIcon()
    {
        return icon != null ? icon.clone() : new ItemStack(Material.MAP);
    }


    /**
     * @param icon the icon to set
     */
    public void setIcon(ItemStack icon)
    {
        this.icon = icon;
    }


    /**
     * @return the reqIslandlevel
     */
    public long getUnlockLevel()
    {
        return this.unlockLevel == null ? 0 : this.unlockLevel;
    }


    /**
     * @param unlockLevel the requiredLevel to set
     */
    public void setUnlockLevel(@Nullable Long unlockLevel)
    {
        this.unlockLevel = unlockLevel;
    }


    /**
     * @return the cost of changing biome
     */
    public double getCost()
    {
        return this.cost == null ? 0 : this.cost;
    }


    /**
     * @param cost the reqMoney to set
     */
    public void setCost(@Nullable Double cost)
    {
        this.cost = cost;
    }


    /**
     *
     * @return world in which biome operates
     */
    public String getWorld()
    {
        return this.world;
    }


    /**
     * @param world where biome must operate.
     */
    public void setWorld(String world)
    {
        this.world = world;
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
     * This method returns the order object.
     * @return the order object.
     */
    public int getOrder()
    {
        return order;
    }


    /**
     * This method sets the order object value.
     * @param order the order object new value.
     *
     */
    public void setOrder(int order)
    {
        this.order = order;
    }


    /**
     * This method returns the permissions object.
     * @return the permissions object.
     */
    @NotNull
    public Set<String> getUnlockPermissions()
    {
        return this.unlockPermissions == null ? Collections.emptySet() : this.unlockPermissions;
    }


    /**
     * This method sets the permissions object value.
     * @param unlockPermissions the permissions object new value.
     *
     */
    public void setUnlockPermissions(@Nullable Set<String> unlockPermissions)
    {
        this.unlockPermissions = unlockPermissions;
    }


    /**
     * This method returns the environment value.
     * @return the value of environment.
     */
    public World.Environment getEnvironment()
    {
        return this.environment;
    }


    /**
     * This method sets the environment value.
     * @param environment the environment new value.
     *
     */
    public void setEnvironment(World.Environment environment)
    {
        this.environment = environment;
    }


    /**
     * Gets unlock cost.
     *
     * @return the unlock cost
     */
    public double getUnlockCost()
    {
        return this.unlockCost == null ? 0 : this.unlockCost;
    }


    /**
     * Sets unlock cost.
     *
     * @param unlockCost the unlock cost
     */
    public void setUnlockCost(@Nullable Double unlockCost)
    {
        this.unlockCost = unlockCost;
    }


    /**
     * Gets unlock items.
     *
     * @return the unlock items
     */
    @NotNull
    public List<ItemStack> getUnlockItems()
    {
        return this.unlockItems == null ? Collections.emptyList() : this.unlockItems;
    }


    /**
     * Sets unlock items.
     *
     * @param unlockItems the unlock items
     */
    public void setUnlockItems(@Nullable List<ItemStack> unlockItems)
    {
        this.unlockItems = unlockItems;
    }


    /**
     * Gets change item cost.
     *
     * @return the change item cost
     */
    @NotNull
    public List<ItemStack> getItemCost()
    {
        return this.itemCost == null ? Collections.emptyList() : this.itemCost;
    }


    /**
     * Sets change item cost.
     *
     * @param itemCost the change item cost
     */
    public void setItemCost(@Nullable List<ItemStack> itemCost)
    {
        this.itemCost = itemCost;
    }


    /**
     * Gets cost mode.
     *
     * @return the cost mode
     */
    @NotNull
    public CostMode getCostMode()
    {
        return costMode;
    }


    /**
     * Sets cost mode.
     *
     * @param costMode the cost mode
     */
    public void setCostMode(@NotNull CostMode costMode)
    {
        this.costMode = costMode;
    }


    /**
     * Gets cost increment.
     *
     * @return the cost increment
     */
    public double getCostIncrement()
    {
        return costIncrement == null ? 0 : this.costIncrement;
    }


    /**
     * Sets cost increment.
     *
     * @param costIncrement the cost increment
     */
    public void setCostIncrement(@Nullable Double costIncrement)
    {
        this.costIncrement = costIncrement;
    }


    // ---------------------------------------------------------------------
    // Section: Other methods
    // ---------------------------------------------------------------------


    /*
     * (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        return 31 + ((this.uniqueId == null) ? 0 : this.uniqueId.hashCode());
    }


    /*
     *(non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }

        if (!(obj instanceof BiomesObject other))
        {
            return false;
        }

        if (this.uniqueId == null && other.getUniqueId() == null)
        {
            return this.biome == other.getBiome();
        }
        else if (this.uniqueId == null || other.getUniqueId() == null)
        {
            return false;
        }
        else
        {
            return this.uniqueId.equals(other.getUniqueId());
        }
    }


    /*
     * (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public int compareTo(@NotNull BiomesObject object)
    {
        // Compare by order
        return Comparator.comparingInt(BiomesObject::getOrder).
            // Compare by environment
            thenComparing(BiomesObject::getEnvironment).
            // compare by biome
            thenComparing(BiomesObject::getBiome).
            // compare by friendly name
            thenComparing(BiomesObject::getFriendlyName).
            // Provide objects
            compare(this, object);
    }


    /**
     * This method checks if this object is valid.
     * @return {@code true} if biome is valid, {@code false} otherwise.
     */
    public boolean isValid()
    {
        return this.biome != null;
    }


    /**
     * Copy biomes object.
     *
     * @return the biomes object
     */
    public BiomesObject copy()
    {
        BiomesObject object = new BiomesObject();
        object.setBiome(this.biome);
        object.setDeployed(this.deployed);
        object.setFriendlyName(this.friendlyName);
        object.setDescription(new ArrayList<>(this.description));
        object.setIcon(this.icon.clone());
        object.setOrder(this.order);
        object.setWorld(this.world);
        object.setEnvironment(this.environment);
        object.setUniqueId(this.uniqueId);

        object.setUnlockLevel(this.unlockLevel);
        object.setUnlockCost(this.unlockCost);

        if (this.unlockPermissions != null && !this.unlockPermissions.isEmpty())
        {
            object.setUnlockPermissions(new HashSet<>(this.unlockPermissions));
        }

        if (this.unlockItems != null && !this.unlockItems.isEmpty())
        {
            object.setUnlockItems(this.unlockItems.stream().map(ItemStack::clone).collect(Collectors.toList()));
        }

        object.setCost(this.cost);

        if (this.itemCost != null && !this.itemCost.isEmpty())
        {
            object.setItemCost(this.itemCost.stream().map(ItemStack::clone).collect(Collectors.toList()));
        }

        object.setCostMode(this.costMode);
        object.setCostIncrement(this.costIncrement);

        return object;
    }


    // ---------------------------------------------------------------------
    // Section: Classes
    // ---------------------------------------------------------------------


    /**
     * The enum Cost mode.
     */
    public enum CostMode
    {
        /**
         * Per block cost mode.
         */
        PER_BLOCK,
        /**
         * Per usage cost mode.
         */
        PER_USAGE,
        /**
         * Static cost mode.
         */
        STATIC
    }


    // ---------------------------------------------------------------------
    // Section: Variables
    // ---------------------------------------------------------------------


    /**
     * The Biome.
     */
    @Expose
    private Biome biome;

    /**
     * The Deployed.
     */
    @Expose
    private boolean deployed;

    /**
     * The Friendly name.
     */
    @Expose
    private String friendlyName = "";

    /**
     * The Description.
     */
    @Expose
    private List<String> description = new ArrayList<>();

    /**
     * The Icon.
     */
    @Expose
    private ItemStack icon = new ItemStack(Material.PAPER);

    /**
     * The Order.
     */
    @Expose
    private int order = -1;

    /**
     * The World.
     */
    @Expose
    private String world;

    /**
     * The Environment.
     */
    @Expose
    private World.Environment environment;

    /**
     * The Unique id.
     */
    @Expose
    private String uniqueId;

    // ---------------------------------------------------------------------
    // Section: Unlock/Purchase Cost
    // ---------------------------------------------------------------------

    /**
     * The Required level.
     */
    @Expose
    @Nullable
    private Long unlockLevel = null;

    /**
     * The Unlock cost.
     */
    @Expose
    @Nullable
    private Double unlockCost = null;

    /**
     * The Unlock items.
     */
    @Expose
    @Nullable
    private List<ItemStack> unlockItems = null;

    /**
     * The Required permissions.
     */
    @Expose
    @Nullable
    private Set<String> unlockPermissions = null;

    // ---------------------------------------------------------------------
    // Section: Change Cost
    // ---------------------------------------------------------------------

    /**
     * The Change cost.
     */
    @Expose
    @Nullable
    private Double cost = null;

    /**
     * The Change item cost.
     */
    @Expose
    @Nullable
    private List<ItemStack> itemCost = null;

    /**
     * The Cost mode.
     */
    @Expose
    @NotNull
    private CostMode costMode = CostMode.STATIC;

    /**
     * The Cost increment.
     */
    @Expose
    @Nullable
    private Double costIncrement = null;
}
