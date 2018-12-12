package world.bentobox.addons.biomes.objects;


import org.bukkit.Material;
import org.bukkit.World;
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
	 * Empty constructor.
	 */
	public BiomesObject()
	{
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
	 * @return the changingRadius
	 */
	public int getChangingRadius()
	{
		return this.changingRadius;
	}


	/**
	 * @param changingRadius the changingRadius to set
	 */
	public void setChangingRadius(int changingRadius)
	{
		this.changingRadius = changingRadius;
	}


	/**
	 * @return the reqIslandlevel
	 */
	public long getReqIslandlevel()
	{
		return reqIslandlevel;
	}


	/**
	 * @param reqIslandlevel the reqIslandlevel to set
	 */
	public void setReqIslandlevel(long reqIslandlevel)
	{
		this.reqIslandlevel = reqIslandlevel;
	}


	/**
	 * @return the reqMoney
	 */
	public int getReqMoney()
	{
		return reqMoney;
	}


	/**
	 * @param reqMoney the reqMoney to set
	 */
	public void setReqMoney(int reqMoney)
	{
		this.reqMoney = reqMoney;
	}


	/**
	 * @return the reqPerms
	 */
	public Set<String> getReqPerms()
	{
		return reqPerms;
	}


	/**
	 * @param reqPerms the reqPerms to set
	 */
	public void setReqPerms(Set<String> reqPerms)
	{
		this.reqPerms = reqPerms;
	}


	/**
	 * @return the changeText
	 */
	public String getChangeText()
	{
		return this.changeText;
	}


	/**
	 * @param changeText the changeText to set
	 */
	public void setChangeText(String changeText)
	{
		this.changeText = changeText;
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
	 * @return the takeMoney
	 */
	public boolean isTakeMoney()
	{
		return takeMoney;
	}


	/**
	 * @param takeMoney the takeMoney to set
	 */
	public void setTakeMoney(boolean takeMoney)
	{
		this.takeMoney = takeMoney;
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
	 * @return the worlds
	 */
	public String getWorld()
	{
		return world;
	}


	/**
	 * @param world the worlds to set
	 */
	public void setWorld(String world)
	{
		this.world = world;
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
	 * This method sets biomes ID.
	 * @param biomeID
	 */
	public void setBiomesID(int biomeID)
	{
		this.biomeID = biomeID;
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
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.uniqueId == null) ? 0 : this.uniqueId.hashCode());
		return result;
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

		if (this.uniqueId == null)
		{
			return other.uniqueId == null;
		}
		else
		{
			return this.uniqueId.equals(other.uniqueId);
		}
	}


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------


	@ConfigComment("Whether this biome is deployed or not")
	private boolean deployed;

	// Description
	@ConfigComment("Name of the icon and biomes. May include color codes. Single line.")
	private String friendlyName = "";

	@ConfigComment("Description of the biomes. Will become the lore on the icon. Can include & color codes. String List.")
	private List<String> description = new ArrayList<>();

	@ConfigComment("The icon in the GUI for this biome. ItemStack.")
	private ItemStack icon = new ItemStack(Material.PAPER);

	@ConfigComment("Icon slot where this biomes should be placed. 0 to 49. A negative value means any slot")
	private int slot = -1;

	@ConfigComment("World where this biomes operates. List only overworld. Nether and end are automatically covered.")
	private String world = "";

	@ConfigComment("List of environments where this biome will occur: NETHER, NORMAL, THE_END. Leave blank for all.")
	private List<World.Environment> environment = new ArrayList<>();

	@ConfigComment("The required permissions to see this biome. String list.")
	private Set<String> reqPerms = new HashSet<>();

	@ConfigComment("The number of bloks around the player to change biome on an island")
	private int changingRadius = 1;

	@ConfigComment("Take the money from the player")
	private boolean takeMoney = false;

	@ConfigComment("Required island level for this biome. Only works if Level Addon is being used.")
	private long reqIslandlevel;

	@ConfigComment("Required money")
	private int reqMoney;

	@ConfigComment("If this is blank, the biome change text will be auto-generated, otherwise this will be used.")
	private String changeText = "";

	@ConfigComment("Unique id of the biome")
	private int biomeID = 0;

	@ConfigComment("Unique name of the biome")
	private String uniqueId = "";
}
