package world.bentobox.addons.biomes.tasks;


import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import java.util.Optional;

import world.bentobox.addons.biomes.objects.BiomesObject;
import world.bentobox.addons.biomes.utils.Utils.UpdateMode;
import world.bentobox.bentobox.api.addons.Addon;
import world.bentobox.bentobox.api.addons.request.AddonRequestBuilder;
import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.hooks.VaultHook;


/**
 * This class helps to validate if user can change biome. It also calculates how large
 * update must be and calls update task.
 */
public class BiomeUpdateHelper
{
	public BiomeUpdateHelper(Addon addon,
		User callerUser,
		User targetUser,
		BiomesObject biome,
		World world,
		UpdateMode updateMode,
		int updateNumber,
		boolean canWithdraw)
	{
		this.addon = addon;
		this.callerUser = callerUser;
		this.targetUser = targetUser;
		this.biome = biome;
		this.world = world;
		this.updateMode = updateMode;
		this.updateNumber = updateNumber;
		this.canWithdraw = canWithdraw;
	}


	/**
	 * This method returns if update tack can be called.
	 * @return <code>true</code> if user can change biome.
	 */
	public boolean canChangeBiome()
	{
		if (this.callerUser == this.targetUser)
		{
			if (!this.callerUser.hasPermission(this.biome.getPermission()))
			{
				this.callerUser.sendMessage("biomes.messages.errors.missing-permission",
					"[permission]",
					this.biome.getPermission());
				return false;
			}

			if (!this.updateMode.equals(UpdateMode.ISLAND) && this.updateNumber <= 0)
			{
				// Cannot update negative numbers.

				this.callerUser.sendMessage("biomes.messages.errors.incorrect-range",
					TextVariables.NUMBER,
					Integer.toString(this.updateNumber));
				return false;
			}

			Island island = this.addon.getIslands().getIsland(this.world, this.targetUser);

			if (island == null)
			{
				// User has no island.
				this.callerUser.sendMessage("biomes.messages.errors.missing-island");
				return false;
			}

			Optional<Island> onIsland =
				this.addon.getIslands().getIslandAt(this.callerUser.getLocation());

			if (!onIsland.isPresent() || onIsland.get() != island)
			{
				// User is not on his island.

				this.callerUser.sendMessage("biomes.messages.errors.not-on-island");
				return false;
			}

			Optional<VaultHook> vaultHook = this.addon.getPlugin().getVault();

			if (vaultHook.isPresent())
			{
				if (!vaultHook.get().has(this.callerUser, this.biome.getRequiredCost()))
				{
					// Not enough money.

					this.callerUser.sendMessage("biomes.messages.errors.not-enough-money",
						TextVariables.NUMBER,
						Double.toString(this.biome.getRequiredCost()));
					return false;
				}
			}

			Optional<Addon> levelHook = this.addon.getAddonByName("Level");

			if (levelHook.isPresent())
			{
				Object levelObject = new AddonRequestBuilder().addon("Level").
					label("island-level").
					addMetaData("player", this.targetUser.getUniqueId()).
					addMetaData("world-name", this.world.getName()).
					request();

				if (levelObject != null &&
					this.biome.getRequiredLevel() > 0 &&
					(long) levelObject <= this.biome.getRequiredLevel())
				{
					// Not enough level

					this.callerUser.sendMessage("biomes.messages.errors.not-enough-level",
						TextVariables.NUMBER,
						String.valueOf(this.biome.getRequiredLevel()));
					return false;
				}
			}
		}
		else
		{
			Island island = this.addon.getIslands().getIsland(this.world, this.targetUser);

			Optional<Island> onIsland =
				this.addon.getIslands().getIslandAt(this.callerUser.getLocation());

			if (this.updateMode != UpdateMode.ISLAND &&
				(!onIsland.isPresent() || onIsland.get() != island))
			{
				// Admin is not on user island.
				this.callerUser.sendMessage("biomes.messages.errors.missing-admin-island",
					"[user]",
					this.targetUser.getName());
				return false;
			}
		}

		return true;
	}


	/**
	 * This method calculates update region and call BiomeUpdateTask to change given biome on island.
	 */
	public void updateIslandBiome()
	{
		Island island = this.addon.getIslands().getIsland(this.world, this.targetUser);
		int range = island.getRange();

		int minX = island.getMinX();
		int minZ = island.getMinZ();

		int maxX = minX + 2 * range;
		int maxZ = minZ + 2 * range;

		Location playerLocation = this.callerUser.getLocation();

		// Calculate minimal and maximal coordinate based on update mode.

		BiomeUpdateTask task = new BiomeUpdateTask(this.callerUser, this.world, this.biome);

		switch (this.updateMode)
		{
			case ISLAND:
				task.setMinX(minX > maxX ? maxX : minX);
				task.setMaxX(minX < maxX ? maxX : minX);
				task.setMinZ(minZ > maxZ ? maxZ : minZ);
				task.setMaxZ(minZ < maxZ ? maxZ : minZ);

				break;
			case CHUNK:
				Chunk chunk = playerLocation.getChunk();

				if (chunk.getX() < 0)
				{
					task.setMaxX(Math.max(minX, chunk.getX() + 16 * (this.updateNumber - 1)));
					task.setMinX(Math.min(maxX, minX - 16 * this.updateNumber + 1));
				}
				else
				{
					task.setMinX(Math.max(minX, chunk.getX() - 16 * (this.updateNumber - 1)));
					task.setMaxX(Math.min(maxX, minX + 16 * this.updateNumber - 1));
				}

				if (chunk.getZ() < 0)
				{
					task.setMaxZ(Math.max(minZ, chunk.getZ() + 16 * (this.updateNumber - 1)));
					task.setMinZ(Math.min(maxZ, minZ - 16 * this.updateNumber + 1));
				}
				else
				{
					task.setMinZ(Math.max(minZ, chunk.getZ() - 16 * (this.updateNumber - 1)));
					task.setMaxZ(Math.min(maxZ, minZ + 16 * this.updateNumber - 1));
				}

				break;
			case SQUARE:
				int halfDiameter = this.updateNumber / 2;

				int x = playerLocation.getBlockX();

				if (x < 0)
				{
					task.setMaxX(Math.max(minX, x + halfDiameter));
					task.setMinX(Math.min(maxX, x - halfDiameter));
				}
				else
				{
					task.setMinX(Math.max(minX, x - halfDiameter));
					task.setMaxX(Math.min(maxX, x + halfDiameter));
				}

				int z = playerLocation.getBlockZ();

				if (z < 0)
				{
					task.setMaxZ(Math.max(minZ, z + halfDiameter));
					task.setMinZ(Math.min(maxZ, z - halfDiameter));
				}
				else
				{
					task.setMinZ(Math.max(minZ, z - halfDiameter));
					task.setMaxZ(Math.min(maxZ, z + halfDiameter));
				}

				break;
			default:
				// Setting all values to 0 will skip biome changing.
				// Default should never appear.
				return;
		}

		// Take Money
		if (this.canWithdraw)
		{
			this.addon.getPlugin().getVault().ifPresent(
				vaultHook -> vaultHook.withdraw(this.callerUser, this.biome.getRequiredCost()));
		}

		task.runTaskAsynchronously(this.addon.getPlugin());
	}


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------


	/**
	 * This variable stores caller addon.
	 */
	private Addon addon;

	/**
	 * This variable stores User that calls update.
	 */
	private User callerUser;

	/**
	 * This variable stores User that is targeted by update.
	 */
	private User targetUser;

	/**
	 * This variable stores BiomesObject that must be applied.
	 */
	private BiomesObject biome;

	/**
	 * This variable stores update mode.
	 */
	private UpdateMode updateMode;

	/**
	 * This variable stores how large update region must be.
	 */
	private int updateNumber;

	/**
	 * This variable stores update world.
	 */
	private World world;

	/**
	 * This variable stores if money from caller can be withdrawn.
	 */
	private boolean canWithdraw;
}
