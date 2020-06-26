package world.bentobox.biomes.database.objects;


import java.util.*;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.inventory.ItemStack;

import com.google.gson.annotations.Expose;

import world.bentobox.bentobox.api.configuration.ConfigComment;
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
     * Default constructor.
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

        if (!(obj instanceof BiomesObject))
        {
            return false;
        }

        BiomesObject other = (BiomesObject) obj;

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
    public int compareTo(BiomesObject object)
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


    // ---------------------------------------------------------------------
    // Section: Variables
    // ---------------------------------------------------------------------


    @ConfigComment("Official minecraft biome name.")
    @Expose
    private Biome biome;

    @ConfigComment("Whether this biome is deployed or not")
    @Expose
    private boolean deployed;

    @ConfigComment("Name of the icon and biomes. May include color codes. Single line.")
    @Expose
    private String friendlyName = "";

    @ConfigComment("Description of the biomes. Will become the lore on the icon. Can include & color codes. String List.")
    @Expose
    private List<String> description = new ArrayList<>();

    @ConfigComment("The icon in the GUI for this biome. ItemStack.")
    @Expose
    private ItemStack icon = new ItemStack(Material.PAPER);

    @ConfigComment("Order of biome. Biomes will be ordered in ascending order.")
    @Expose
    private int order = -1;

    @ConfigComment("Required island level for this biome. Only works if Level Addon is being used.")
    @Expose
    private long requiredLevel;

    @ConfigComment("Cost of changing biome.")
    @Expose
    private double requiredCost;

    @ConfigComment("Set of String permission that is required for this biome to be activated.")
    @Expose
    private Set<String> requiredPermissions = new HashSet<>();

    @ConfigComment("World where this biome operates. List only NORMAL. NETHER and THE_END are automatically covered.")
    @Expose
    private String world;

    @ConfigComment("Allows to specify environment for biome. This allows to split overworld, nether and the end biomes.")
    @Expose
    private World.Environment environment;

    @ConfigComment("Unique StringName of the biome")
    @Expose
    private String uniqueId;
}
