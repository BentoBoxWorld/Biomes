package world.bentobox.biomes.tasks;


import java.util.Optional;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;

import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.util.Util;
import world.bentobox.biomes.BiomesAddon;
import world.bentobox.biomes.config.Settings.UpdateMode;
import world.bentobox.biomes.database.objects.BiomesObject;
import world.bentobox.biomes.utils.Utils;
import world.bentobox.level.objects.LevelsData;


/**
 * This class helps to validate if user can change biome. It also calculates how large
 * update must be and calls update task.
 */
public class BiomeUpdateHelper
{
	public BiomeUpdateHelper(BiomesAddon addon,
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

		this.worldProtectionFlag = BiomesAddon.BIOMES_WORLD_PROTECTION.isSetForWorld(this.world);
	}


	/**
	 * This method returns if update tack can be called.
	 * @return <code>true</code> if user can change biome.
	 */
	public boolean canChangeBiome()
	{
		// Check if environment is valid.
		if (!this.biome.getEnvironment().equals(World.Environment.NORMAL))
		{
			// Check if nether and the end islands are enabled.
			if ((!this.biome.getEnvironment().equals(World.Environment.NETHER) ||
				!this.addon.getPlugin().getIWM().isNetherGenerate(this.world) ||
				!this.addon.getPlugin().getIWM().isNetherIslands(this.world)) &&
				(!this.biome.getEnvironment().equals(World.Environment.THE_END) ||
					!this.addon.getPlugin().getIWM().isEndGenerate(this.world) ||
					!this.addon.getPlugin().getIWM().isEndIslands(this.world)))
			{
				this.callerUser.sendMessage("general.errors.wrong-world");
				return false;
			}
		}

		if (this.callerUser == this.targetUser)
		{
			if (!this.checkPermissions())
			{
				this.callerUser.sendMessage("general.errors.no-permission");
				return false;
			}

			if (!this.updateMode.equals(UpdateMode.ISLAND) && this.updateNumber <= 0)
			{
				// Cannot update negative numbers.

				this.callerUser.sendMessage("biomes.errors.incorrect-range",
					TextVariables.NUMBER,
					Integer.toString(this.updateNumber));
				return false;
			}

			if (this.worldProtectionFlag)
			{
				Island island =
					this.addon.getIslands().getIsland(this.world, this.targetUser);

				if (island == null)
				{
					// User has no island.
					this.callerUser.sendMessage("general.errors.player-has-no-island");
					return false;
				}

				Optional<Island> onIsland =
					this.addon.getIslands().getIslandAt(this.callerUser.getLocation());

				if (!onIsland.isPresent() || onIsland.get() != island)
				{
					// User is not on his island.

					this.callerUser.sendMessage("biomes.errors.not-on-island");
					return false;
				}

				if (!island.isAllowed(this.callerUser, BiomesAddon.BIOMES_ISLAND_PROTECTION))
				{
					// This can be checked only if island exists.

					this.callerUser.sendMessage("biomes.errors.no-rank");
					return false;
				}

				if (this.addon.isLevelProvided())
				{
					// This is here as I am not sure if Level addon can calculate island level
					// if players can build anywhere.

					LevelsData data = this.addon.getLevelAddon().getLevelsData(this.targetUser.getUniqueId());

					if (data == null ||
						!data.getLevels().containsKey(Util.getWorld(this.world).getName()) ||
						this.biome.getRequiredLevel() > 0 &&
							data.getLevel(Util.getWorld(this.world)) <= this.biome.getRequiredLevel())
					{
						// Not enough level

						this.callerUser.sendMessage("biomes.errors.not-enough-level",
							TextVariables.NUMBER,
							String.valueOf(this.biome.getRequiredLevel()));
						return false;
					}
				}
			}
			else if (this.updateMode.equals(UpdateMode.ISLAND))
			{
				// User has no island.
				this.callerUser.sendMessage(BiomesAddon.BIOMES_WORLD_PROTECTION.getHintReference());
				return false;
			}


			if (this.addon.isEconomyProvided())
			{
				if (!this.addon.getVaultHook().has(this.callerUser, this.biome.getRequiredCost()))
				{
					// Not enough money.

					this.callerUser.sendMessage("biomes.errors.not-enough-money",
						TextVariables.NUMBER,
						Double.toString(this.biome.getRequiredCost()));
					return false;
				}
			}

			// Init starting location.
			this.standingLocation = this.targetUser.getLocation();
		}
		else
		{
			if (!this.worldProtectionFlag)
			{
				if (this.updateMode.equals(UpdateMode.ISLAND))
				{
					// Island option is not possible for worlds without world protection.
					if (this.callerUser.isPlayer())
					{
						this.callerUser.sendMessage(BiomesAddon.BIOMES_WORLD_PROTECTION.getHintReference());
					}
					else
					{
						this.addon.logWarning("Biome change is not possible with Island mode " +
							"for this world as BIOMES_WORLD_PROTECTION is disabled!");
					}

					return false;
				}
				else
				{
					if (this.targetUser.isOnline())
					{
						this.standingLocation = this.targetUser.getLocation();
					}
					else if (this.callerUser.isPlayer())
					{
						this.standingLocation = this.callerUser.getLocation();
					}
					else
					{
						this.addon.logWarning("Target Player is not online. Cannot find biome change location!");
						return false;
					}
				}
			}
			else if (this.updateMode.equals(UpdateMode.ISLAND))
			{
				this.standingLocation = this.targetUser.getLocation();

				// Return false if targeted user has no island.
				return this.addon.getIslands().getIsland(this.world, this.targetUser) != null;
			}
			else if (this.callerUser.isPlayer())
			{
				// Chunk and square based update modes can be called only by player.

				Island island = this.addon.getIslands().getIsland(this.world, this.targetUser);

				Optional<Island> onIsland =
					this.addon.getIslands().getIslandAt(this.callerUser.getLocation());

				if (this.updateMode != UpdateMode.ISLAND &&
					(!onIsland.isPresent() || onIsland.get() != island))
				{
					// Admin is not on user island.
					this.callerUser.sendMessage("biomes.errors.admin-not-on-island",
						"[user]",
						this.targetUser.getName());

					return false;
				}

				// Admin must be located on island to change biome, as his location will be
				// taken for update.
				this.standingLocation = this.callerUser.getLocation();
			}
			else
			{
				// Check if target user is his island.
				Island island = this.addon.getIslands().getIsland(this.world, this.targetUser);

				Optional<Island> onIsland =
					this.addon.getIslands().getIslandAt(this.targetUser.getLocation());

				if (!onIsland.isPresent() || onIsland.get() != island)
				{
					// Admin is not on user island.
					this.addon.logWarning("Biome change for player " + this.targetUser.getName() + " is not possible as he is not on his island!");
					return false;
				}

				// Init start location
				this.standingLocation = this.targetUser.getLocation();
			}
		}

		return true;
	}


	/**
	 * This method calculates update region and call BiomeUpdateTask to change given biome on island.
	 */
	public void updateIslandBiome()
	{
		int minX;
		int minZ;
		int maxX;
		int maxZ;

		// Limit island update range
		if (this.worldProtectionFlag)
		{
			Island island = this.addon.getIslands().getIsland(this.world, this.targetUser);

			// This is implemented to fix issue when biome is changed in space between islands. #34

			if (this.addon.getSettings().isUseProtectionRange())
			{
				// Allow to go outside island protection range if it is possible.

				minX = island.getMinProtectedX();
				minZ = island.getMinProtectedZ();

				maxX = island.getMaxProtectedX() - 1;
				maxZ = island.getMaxProtectedZ() - 1;
			}
			else
			{
				minX = island.getMinX();
				minZ = island.getMinZ();

				maxX = island.getMaxX() - 1;
				maxZ = island.getMaxZ() - 1;
			}

			// biome cannot be changed outside island!
			if (Utils.normalizeBy4(minX) < island.getMinX())
			{
				minX = Utils.normalizeBy4(minX + 4);
			}

			if (Utils.normalizeBy4(maxX) > island.getMaxX())
			{
				maxX = Utils.normalizeBy4(maxX - 4);
			}

			if (Utils.normalizeBy4(minZ) < island.getMinZ())
			{
				minZ = Utils.normalizeBy4(minZ + 4);
			}

			if (Utils.normalizeBy4(maxZ) > island.getMaxZ())
			{
				maxZ = Utils.normalizeBy4(maxZ - 4);
			}
		}
		else
		{
			// limit by island distance to avoid issues with long updating.
			int range = this.addon.getPlugin().getIWM().getIslandDistance(this.world);

			minX = Utils.normalizeBy4(this.standingLocation.getBlockX() - range);
			minZ = Utils.normalizeBy4(this.standingLocation.getBlockZ() - range);

			maxX = Utils.normalizeBy4(this.standingLocation.getBlockX() + range);
			maxZ = Utils.normalizeBy4(this.standingLocation.getBlockZ() + range);
		}

		// Calculate minimal and maximal coordinate based on update mode.

		BiomeUpdateTask task = new BiomeUpdateTask(this.addon, this.callerUser, this.standingLocation, this.biome);

		// Select world depending on environment.

		if (World.Environment.NETHER.equals(this.biome.getEnvironment()))
		{
			task.setWorld(this.addon.getPlugin().getIWM().getNetherWorld(this.world));
		}
		else if (World.Environment.THE_END.equals(this.biome.getEnvironment()))
		{
			task.setWorld(this.addon.getPlugin().getIWM().getEndWorld(this.world));
		}
		else
		{
			task.setWorld(this.world);
		}

		switch (this.updateMode)
		{
			case ISLAND:
				task.setMinX(minX);
				task.setMaxX(maxX);
				task.setMinZ(minZ);
				task.setMaxZ(maxZ);

				// Select whole island height.
				task.setMinY(0);
				task.setMaxY(this.world.getMaxHeight());

				break;
			case CHUNK:
				Chunk chunk = this.standingLocation.getChunk();

				task.setMinX(Math.max(minX, (chunk.getX() - (this.updateNumber - 1)) << 4));
				task.setMaxX(Math.min(maxX, (chunk.getX() + this.updateNumber) << 4) - 1);

				task.setMinZ(Math.max(minZ, (chunk.getZ() - (this.updateNumber - 1)) << 4));
				task.setMaxZ(Math.min(maxZ, (chunk.getZ() + this.updateNumber) << 4) - 1);

				// Select whole island height.
				task.setMinY(0);
				task.setMaxY(this.world.getMaxHeight());

				break;
			case RANGE:
				int halfDiameter = this.updateNumber / 2;

				int x = this.standingLocation.getBlockX();

				if (x < 0)
				{
					task.setMaxX(Math.max(minX, Utils.normalizeBy4(x + halfDiameter)));
					task.setMinX(Math.min(maxX, Utils.normalizeBy4(x - halfDiameter)));
				}
				else
				{
					task.setMinX(Math.max(minX, Utils.normalizeBy4(x - halfDiameter)));
					task.setMaxX(Math.min(maxX, Utils.normalizeBy4(x + halfDiameter)));
				}

				int z = this.standingLocation.getBlockZ();

				if (z < 0)
				{
					task.setMaxZ(Math.max(minZ, Utils.normalizeBy4(z + halfDiameter)));
					task.setMinZ(Math.min(maxZ, Utils.normalizeBy4(z - halfDiameter)));
				}
				else
				{
					task.setMinZ(Math.max(minZ, Utils.normalizeBy4(z - halfDiameter)));
					task.setMaxZ(Math.min(maxZ, Utils.normalizeBy4(z + halfDiameter)));
				}

				// Calculate Y location
				int y = this.standingLocation.getBlockY();

				task.setMinY(Math.max(0, Utils.normalizeBy4(y - halfDiameter)));
				task.setMaxY(Math.min(this.world.getMaxHeight(), Utils.normalizeBy4(y + halfDiameter)));

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


	/**
	 * This method checks if user has all required permissions.
	 * @return true if user has all required permissions, otherwise false.
	 */
	private boolean checkPermissions()
	{
		return this.biome.getRequiredPermissions().isEmpty() ||
			this.biome.getRequiredPermissions().stream().allMatch(this.callerUser::hasPermission);
	}


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------


	/**
	 * This variable stores caller addon.
	 */
	private BiomesAddon addon;

	/**
	 * This variable stores User that calls update.
	 */
	private User callerUser;

	/**
	 * This variable stores User that is targeted by update.
	 */
	private User targetUser;

	/**
	 * This variable holds from which location Update process should start.
	 */
	private Location standingLocation;

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

	/**
	 * This variable stores if world protection flag is enabled. Avoids checking it each
	 * time as flag will not change its value while updating.
	 */
	private final boolean worldProtectionFlag;
}
