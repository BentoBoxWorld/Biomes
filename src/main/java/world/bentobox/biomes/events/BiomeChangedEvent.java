package world.bentobox.biomes.events;


import org.bukkit.block.Biome;
import java.util.UUID;

import world.bentobox.bentobox.api.events.BentoBoxEvent;


/**
 * This event is fired when player changed biome.
 * It is just informative and is fired when everything is done already.
 */
public class BiomeChangedEvent extends BentoBoxEvent
{
	/**
	 * Constructor BiomeChangeEvent creates a new BiomeChangeEvent instance.
	 *
	 * @param biomeID of type String that represents biome unique id. May be empty.
	 * @param biome name of biome that was applied.
	 * @param playerUUID of type UUID
	 * @param minX of type int represents minimal X coordinate
	 * @param minZ of type int represents minimal Z coordinate
	 * @param maxX of type int represents maximal X coordinate
	 * @param maxZ of type int represents maximal Z coordinate
	 */
	public BiomeChangedEvent(
		String biomeID,
		Biome biome,
		UUID playerUUID,
		int minX,
		int minZ,
		int maxX,
		int maxZ)
	{
		super(true);
		this.biomeID = biomeID;
		this.biome = biome;
		this.playerUUID = playerUUID;
		this.minX = minX;
		this.maxX = maxX;
		this.minZ = minZ;
		this.maxZ = maxZ;
	}


// ---------------------------------------------------------------------
// Section: Getters and setters
// ---------------------------------------------------------------------


	/**
	 * This method returns the biomeID value.
	 * @return the value of biomeID.
	 */
	public String getBiomeID()
	{
		return biomeID;
	}


	/**
	 * This method sets the biomeID value.
	 * @param biomeID the biomeID new value.
	 *
	 */
	public void setBiomeID(String biomeID)
	{
		this.biomeID = biomeID;
	}


	/**
	 * This method returns the biome value.
	 * @return the value of biome.
	 */
	public Biome getBiome()
	{
		return biome;
	}


	/**
	 * This method sets the biome value.
	 * @param biome the biome new value.
	 *
	 */
	public void setBiome(Biome biome)
	{
		this.biome = biome;
	}


	/**
	 * This method returns the playerUUID value.
	 * @return the value of playerUUID.
	 */
	public UUID getPlayerUUID()
	{
		return playerUUID;
	}


	/**
	 * This method sets the playerUUID value.
	 * @param playerUUID the playerUUID new value.
	 *
	 */
	public void setPlayerUUID(UUID playerUUID)
	{
		this.playerUUID = playerUUID;
	}


	/**
	 * This method returns the minX value.
	 * @return the value of minX.
	 */
	public int getMinX()
	{
		return minX;
	}


	/**
	 * This method sets the minX value.
	 * @param minX the minX new value.
	 *
	 */
	public void setMinX(int minX)
	{
		this.minX = minX;
	}


	/**
	 * This method returns the minZ value.
	 * @return the value of minZ.
	 */
	public int getMinZ()
	{
		return minZ;
	}


	/**
	 * This method sets the minZ value.
	 * @param minZ the minZ new value.
	 *
	 */
	public void setMinZ(int minZ)
	{
		this.minZ = minZ;
	}


	/**
	 * This method returns the maxX value.
	 * @return the value of maxX.
	 */
	public int getMaxX()
	{
		return maxX;
	}


	/**
	 * This method sets the maxX value.
	 * @param maxX the maxX new value.
	 *
	 */
	public void setMaxX(int maxX)
	{
		this.maxX = maxX;
	}


	/**
	 * This method returns the maxZ value.
	 * @return the value of maxZ.
	 */
	public int getMaxZ()
	{
		return maxZ;
	}


	/**
	 * This method sets the maxZ value.
	 * @param maxZ the maxZ new value.
	 *
	 */
	public void setMaxZ(int maxZ)
	{
		this.maxZ = maxZ;
	}


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------


	/**
	 * Unique ID of changed biomeObject.
	 */
	private String biomeID;

	/**
	 * Biome that is defined in Minecraft.
	 */
	private Biome biome;

	/**
	 * Player who was targeted by biome change.
	 */
	private UUID playerUUID;

	/**
	 * Minimal X coordinate of change range.
	 */
	private int minX;

	/**
	 * Minimal Z coordinate of change range.
	 */
	private int minZ;

	/**
	 * Maximal X coordinate of change range.
	 */
	private int maxX;

	/**
	 * Maximal Z coordinate of change range.
	 */
	private int maxZ;
}

