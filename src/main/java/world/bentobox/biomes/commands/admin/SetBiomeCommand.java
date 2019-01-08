package world.bentobox.biomes.commands.admin;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import world.bentobox.bentobox.api.addons.Addon;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.util.Util;
import world.bentobox.biomes.commands.ExpandedCompositeCommand;
import world.bentobox.biomes.objects.BiomesObject;
import world.bentobox.biomes.objects.Settings.UpdateMode;
import world.bentobox.biomes.panel.admin.AdminUserListPanel;
import world.bentobox.biomes.tasks.BiomeUpdateHelper;

/**
 * This is admin command that allows to change user biomes.
 */
public class SetBiomeCommand extends ExpandedCompositeCommand
{
	public SetBiomeCommand(Addon addon, CompositeCommand parent)
	{
		super(addon, parent, "set");
	}

	@Override
	public void setup()
	{
		this.setPermission("admin.biomes.set");
		this.setParametersHelp("biomes.commands.admin.set.parameters");
		this.setDescription("biomes.commands.admin.set.description");
	}


	@Override
	public boolean execute(User user, String label, List<String> args)
	{
		if (user.isPlayer() && args.isEmpty())
		{
			// Shows admin panel
			new AdminUserListPanel(this.addon,
				this.getWorld(),
				user,
				this.getTopLabel(),
				this.getPermissionPrefix()).build();
			return true;
		}
		else if (args.isEmpty())
		{
			this.showHelp(this, user);
			return false;
		}
		else
		{
			User targetUser = this.getPlayer(args, 0, user);
			BiomesObject biome = this.getBiomeObject(args, 1, user);
			UpdateMode updateMode = this.getUpdateMode(args, 2, user);
			int size = this.getUpdateRange(args, 3, user);

			if (targetUser == null || biome == null || updateMode == null || size < 1)
			{
				// Show help if something fails.
				this.showHelp(this, user);
				return false;
			}
			else
			{
				// Use BiomeUpdateHelper to change biome for user.

				BiomeUpdateHelper helper = new BiomeUpdateHelper(this.addon,
					user,
					targetUser,
					biome,
					user.getWorld(),
					updateMode,
					size,
					false);

				if (helper.canChangeBiome())
				{
					helper.updateIslandBiome();
					return true;
				}

				return false;
			}
		}
	}


	@Override
	public Optional<List<String>> tabComplete(User user, String alias, List<String> args)
	{
		String lastString = args.get(args.size() - 1);

		final List<String> returnList = new ArrayList<>();
		final int size = args.size();

		switch (size)
		{
			case 3:
				returnList.addAll(Util.tabLimit(new ArrayList<>(Util.getOnlinePlayerList(user)), lastString));
				break;
			case 4:
				List<BiomesObject> biomes = this.addon.getAddonManager().getBiomes(this.getWorld());

				// Create suggestions with all biomes that is available for users.

				biomes.forEach(biomesObject -> {
					returnList.addAll(Util.tabLimit(
						Collections.singletonList(biomesObject.getBiomeName()), lastString));
				});

				break;
			case 5:
				// Create suggestions with all biomes that is available for users.

				returnList.addAll(Util.tabLimit(Collections.singletonList("ISLAND"), lastString));
				returnList.addAll(Util.tabLimit(Collections.singletonList("CHUNK"), lastString));
				returnList.addAll(Util.tabLimit(Collections.singletonList("SQUARE"), lastString));

				break;
			case 6:
				if (lastString.isEmpty() || lastString.matches("[0-9]*"))
				{
					returnList.addAll(Util.tabLimit(Collections.singletonList("<number>"), lastString));
				}

				break;
			default:
			{
				break;
			}
		}

		return Optional.of(returnList);
	}
}
