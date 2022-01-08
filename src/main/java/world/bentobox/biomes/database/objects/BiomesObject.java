package world.bentobox.biomes.database.objects;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

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
    public long getRequiredLevel()
    {
        return this.requiredLevel;
    }


    /**
     * @param requiredLevel the requiredLevel to set
     */
    public void setRequiredLevel(long requiredLevel)
    {
        this.requiredLevel = requiredLevel;
    }


    /**
     * @return the cost of changing biome
     */
    public double getRequiredCost()
    {
        return this.requiredCost;
    }


    /**
     * @param requiredCost the reqMoney to set
     */
    public void setRequiredCost(double requiredCost)
    {
        this.requiredCost = requiredCost;
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
    public Set<String> getRequiredPermissions()
    {
        return requiredPermissions;
    }


    /**
     * This method sets the permissions object value.
     * @param requiredPermissions the permissions object new value.
     *
     */
    public void setRequiredPermissions(Set<String> requiredPermissions)
    {
        this.requiredPermissions = requiredPermissions;
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
        object.setRequiredLevel(this.requiredLevel);
        object.setRequiredCost(this.requiredCost);
        object.setRequiredPermissions(new HashSet<>(this.requiredPermissions));
        object.setWorld(this.world);
        object.setEnvironment(this.environment);
        object.setUniqueId(this.uniqueId);

        return object;
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
     * The Required level.
     */
    @Expose
    private long requiredLevel;

    /**
     * The Required cost.
     */
    @Expose
    private double requiredCost;

    /**
     * The Required permissions.
     */
    @Expose
    private Set<String> requiredPermissions = new HashSet<>();

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
}
