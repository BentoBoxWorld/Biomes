package world.bentobox.biomes.commands.admin;


import java.util.List;

import net.wesjd.anvilgui.AnvilGUI;
import world.bentobox.bentobox.api.addons.Addon;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.util.Util;
import world.bentobox.biomes.commands.ExpandedCompositeCommand;
import world.bentobox.biomes.database.objects.BiomesObject;
import world.bentobox.biomes.panel.admin.EditBiomeGUI;


/**
 * This command allows to add new biome.
 * This will be dummy biome, without proper name and settings. All could be edited via /biomes edit command.
 */
public class AddBiomeCommand extends ExpandedCompositeCommand
{
	/**
	 * Default constructor. Inits command with "add" parameter.
	 */
	public AddBiomeCommand(Addon addon, CompositeCommand parent)
	{
		super(addon, parent, "add");
	}


	/**
	 * This method setup this command permissions, parameters and descriptions.
	 * {@inheritDoc}
	 */
	@Override
	public void setup()
	{
		this.setPermission("admin.biomes.add");
		this.setParametersHelp("biomes.commands.admin.add.parameters");
		this.setDescription("biomes.commands.admin.add.description");
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean canExecute(User user, String label, List<String> args)
	{
		if (user.isPlayer() && args.isEmpty())
		{
			return true;
		}
		else if (args.size() > 1)
		{
			user.sendMessage("biomes.errors.too-many-arguments");
		}
		else if (args.size() == 1)
		{
			// If biome with given ID already exist, then show error. Otherwise process command.
			if (!this.addon.getAddonManager().containsBiome(Util.getWorld(this.getWorld()).getName() + "-" + args.get(0).toLowerCase()))
			{
				return true;
			}
			else
			{
				user.sendMessage("biomes.errors.unique-id", "[id]", args.get(0).toLowerCase());
				return false;
			}
		}

		this.showHelp(this, user);
		return false;
	}


	/**
	 *
	 * {@inheritDoc}
	 */
	@Override
	public boolean execute(User user, String label, List<String> args)
	{
		if (user.isPlayer() && args.isEmpty())
		{
			// Shows BiomesPanel in Edit mode.
			new AnvilGUI(this.addon.getPlugin(),
				user.getPlayer(),
				"unique_id",
				(player, reply) -> {
					String newName = Util.getWorld(this.getWorld()).getName() + "-" + reply.toLowerCase();

					if (!this.addon.getAddonManager().containsBiome(newName))
					{
						new EditBiomeGUI(this.addon,
							this.getWorld(),
							user,
							this.getTopLabel(),
							this.getPermissionPrefix(),
							this.addon.getAddonManager().createBiome(newName)).build();
					}
					else
					{
						user.sendMessage("biomes.errors.unique-id", "[id]", reply);
					}

					return reply;
				});

			return true;
		}
		else
		{
			BiomesObject biome = this.addon.getAddonManager().createBiome(
				Util.getWorld(this.getWorld()).getName() + "-" + args.get(0).toLowerCase());

			if (user.isPlayer())
			{
				new EditBiomeGUI(this.addon,
					this.getWorld(),
					user,
					this.getTopLabel(),
					this.getPermissionPrefix(),
					biome).build();
			}
			else
			{
				user.sendMessage("biomes.messages.biome-created",
					"[id]",
					args.get(0).toLowerCase());
			}

			return true;
		}
	}
}
