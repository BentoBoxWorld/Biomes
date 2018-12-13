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
	public long getLevel()
	{
		return this.requiredLevel;
	}


	/**
	 * @param reqIslandlevel the reqIslandlevel to set
	 */
	public void setLevel(long reqIslandlevel)
	{
		this.requiredLevel = reqIslandlevel;
	}


	/**
	 * @return the cost of changing biome
	 */
	public int getCost()
	{
		return this.requiredCost;
	}


	/**
	 * @param requiredCost the reqMoney to set
	 */
	public void setCost(int requiredCost)
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
	 * @return the environment
	 */
	public List<World.Environment> getEnvironment()
	{
		return environment;
	}


	/**
	 * @param environment the environment to set
	 */
	public void setEnvironment(List<World.Environment> environment)
	{
		this.environment = environment;
	}


	/**
	 * This method returns biomes ID.
	 * @return
	 */
	public int getBiomesID()
	{
		return this.biomeID;
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

		return this.biomeID == other.getBiomesID();
	}


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------


	@ConfigComment("Official minecraft biome name.")
	private final String biomeName;

	@ConfigComment("Unique biome ID.")
	private final int biomeID;

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

	@ConfigComment("List of environments where this biome will occur: NETHER, NORMAL, THE_END. Leave blank for all.")
	private List<World.Environment> environment = new ArrayList<>();

	@ConfigComment("Required island level for this biome. Only works if Level Addon is being used.")
	private long requiredLevel;

	@ConfigComment("Cost of changing biome.")
	private int requiredCost;

	@ConfigComment("Unique StringName of the biome")
	private String uniqueId = "";
}
