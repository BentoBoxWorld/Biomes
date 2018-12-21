package world.bentobox.addons.biomes.objects;


import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.inventory.ItemStack;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import world.bentobox.bentobox.api.configuration.ConfigComment;
import world.bentobox.bentobox.database.objects.DataObject;


/**
 * This class stores necessary information for each Biomes object.
 * TODO: Fix naming / Review parameters
 */
public class BiomesObject implements DataObject
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
	public BiomesObject(Biome biome)
	{
		this.biomeName = biome.name();
		this.biomeID = biome.ordinal();
	}


// ---------------------------------------------------------------------
// Section: Setters and Getters
// ---------------------------------------------------------------------


	/**
	 * This method returns biomes name.
	 * @return Biomes name.
	 */
	public String getBiomeName()
	{
		return this.biomeName;
	}


	/**
	 * This method sets biomes name.
	 * @param biomeName Biomes name.
	 */
	public void setBiomeName(String biomeName)
	{
		this.biomeName = biomeName;
	}


	/**
	 * This method returns biomes ID.
	 * @return Biomes ID.b
	 */
	public int getBiomeID()
	{
		return this.biomeID;
	}


	/**
	 * This method sets biomes ID.
	 * @param biomeID biomes ID.
	 */
	public void setBiomeID(int biomeID)
	{
		this.biomeID = biomeID;
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
	public int getRequiredCost()
	{
		return this.requiredCost;
	}


	/**
	 * @param requiredCost the reqMoney to set
	 */
	public void setRequiredCost(int requiredCost)
	{
		this.requiredCost = requiredCost;
	}


	/**
	 * @return the slot
	 */
	public int getSlot()
	{
		return slot;
	}


	/**
	 * @param slot the slot to set
	 */
	public void setSlot(int slot)
	{
		this.slot = slot;
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


// ---------------------------------------------------------------------
// Section: Other methods
// ---------------------------------------------------------------------


	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		return this.biomeID;
	}


	/* (non-Javadoc)
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

		return this.biomeID == other.getBiomeID();
	}


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------


	@ConfigComment("Official minecraft biome name.")
	private String biomeName;

	@ConfigComment("Unique biome ID.")
	private int biomeID;

	@ConfigComment("Whether this biome is deployed or not")
	private boolean deployed;

	@ConfigComment("Name of the icon and biomes. May include color codes. Single line.")
	private String friendlyName = "";

	@ConfigComment("Description of the biomes. Will become the lore on the icon. Can include & color codes. String List.")
	private List<String> description = new ArrayList<>();

	@ConfigComment("The icon in the GUI for this biome. ItemStack.")
	private ItemStack icon = new ItemStack(Material.PAPER);

	@ConfigComment("Icon slot where this biomes should be placed. 0 to 49. A negative value means any slot")
	private int slot = -1;

	@ConfigComment("Required island level for this biome. Only works if Level Addon is being used.")
	private long requiredLevel;

	@ConfigComment("Cost of changing biome.")
	private int requiredCost;

	@ConfigComment("Unique StringName of the biome")
	private String uniqueId = "";
}
