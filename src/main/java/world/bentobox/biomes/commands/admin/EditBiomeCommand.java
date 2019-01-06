package world.bentobox.biomes.commands.admin;


import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.inventory.ItemStack;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import world.bentobox.bentobox.api.addons.Addon;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.util.Util;
import world.bentobox.biomes.commands.ExpandedCompositeCommand;
import world.bentobox.biomes.objects.BiomesObject;
import world.bentobox.biomes.panel.admin.AdminBiomeListPanel;
import world.bentobox.biomes.utils.Utils;


/**
 * This is admin command that allows to change user biomes.
 */
public class EditBiomeCommand extends ExpandedCompositeCommand
{
	public EditBiomeCommand(Addon addon, CompositeCommand parent)
	{
		super(addon, parent, "edit");
	}

	@Override
	public void setup()
	{
		this.setPermission("admin.biomes.edit");
		this.setParametersHelp("biomes.commands.admin.edit.parameters");
		this.setDescription("biomes.commands.admin.edit.description");
	}


	@Override
	public boolean execute(User user, String label, List<String> args)
	{
		if (user.isPlayer() && args.isEmpty())
		{
			// Shows BiomesPanel in Edit mode.
			new AdminBiomeListPanel(this.addon,
				this.getWorld(),
				user,
				true,
				this.getTopLabel(),
				this.getPermissionPrefix()).build();
			return true;
		}
		else if (args.isEmpty())
		{
			this.showHelp(this, user);
			return false;
		}
		else if (args.size() < 3)
		{
			user.sendMessage("biomes.messages.errors.missing-arguments");
			this.showHelp(this, user);
			return false;
		}
		else
		{
			BiomesObject biomesObject = this.getBiomeObject(args, 0, user);

			if (biomesObject == null)
			{
				return false;
			}

			// Use proper setter based on 2. argument.

			switch (args.get(1))
			{
				case BIOME:
					String newBiomeString = args.get(2);

					user.sendMessage("biomes.messages.warnings.may-break-others",
						"[biome]",
						newBiomeString);

					Biome newBiome = Utils.getBiomeNameMap().getOrDefault(newBiomeString.toUpperCase(), null);

					if (newBiome == null)
					{
						user.sendMessage("biomes.messages.errors.incorrect-biome",
							"[biome]",
							newBiomeString);
						return false;
					}
					else
					{
						biomesObject.setBiomeName(newBiome.name());
						biomesObject.setBiomeID(newBiome.ordinal());
					}

					break;
				case FRIENDLY_NAME:
					biomesObject.setFriendlyName(this.buildStringFromValue(args));
					break;
				case DESCRIPTION:
					biomesObject.setDescription(Utils.splitString(this.buildStringFromValue(args)));
					break;
				case ICON:
					Material newIcon = Material.getMaterial(args.get(2).toUpperCase());

					if (newIcon == null)
					{
						user.sendMessage("biomes.messages.errors.incorrect-icon",
							"[icon]",
							args.get(2));
						return false;
					}
					else
					{
						biomesObject.setIcon(new ItemStack(newIcon));
					}

					break;
				case DEPLOYED:

					if (args.get(2).equalsIgnoreCase("true"))
					{
						biomesObject.setDeployed(true);
						break;
					}
					else if (args.get(2).equalsIgnoreCase("false"))
					{
						biomesObject.setDeployed(false);
						break;
					}
					else
					{
						user.sendMessage("biomes.messages.errors.incorrect-boolean",
							"[boolean]",
							args.get(2));
						return false;
					}

				case REQUIRED_COST:

					try
					{
						biomesObject.setRequiredCost(Integer.parseUnsignedInt(args.get(2)));
						break;
					}
					catch (Exception e)
					{
						user.sendMessage("biomes.messages.errors.incorrect-range",
							"[number]",
							args.get(2));
						return false;
					}

				case REQUIRED_LEVEL:
					try
					{
						biomesObject.setRequiredLevel(Integer.parseUnsignedInt(args.get(2)));
						break;
					}
					catch (Exception e)
					{
						user.sendMessage("biomes.messages.errors.incorrect-range",
							"[number]",
							args.get(2));
						return false;
					}
				case PERMISSION:
					// TODO: probably validation?
					biomesObject.setPermission(args.get(2));
					break;
				default:
					user.sendMessage("biomes.messages.errors.incorrect-parameter",
						"[property]",
						args.get(1));
					return false;
			}


			this.addon.getAddonManager().saveBiome(biomesObject);
			user.sendMessage("biomes.messages.information.saved",
				"[biome]",
				biomesObject.getFriendlyName());
			return true;
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
				List<BiomesObject> biomes = this.addon.getAddonManager().getBiomes(this.getWorld());

				// Create suggestions with all biomes that is available for users.

				biomes.forEach(biomesObject -> {
					returnList.add(biomesObject.getBiomeName());
				});

				break;
			case 4:
				// Create list with all biome properties values.

				returnList.add(BIOME);
				returnList.add(FRIENDLY_NAME);
				returnList.add(DESCRIPTION);
				returnList.add(ICON);

				returnList.add(DEPLOYED);

				returnList.add(REQUIRED_COST);
				returnList.add(REQUIRED_LEVEL);
				returnList.add(PERMISSION);

				break;
			case 5:
				// Change last variable depending on previous value.

				switch (args.get(3))
				{
					case BIOME:
						returnList.addAll(Utils.getBiomeNameMap().keySet());
						break;
					case FRIENDLY_NAME:
						returnList.add("<name>");
						break;
					case DESCRIPTION:
						returnList.add("<description>");
						break;
					case ICON:
						// Add all possible materials
						for (Material material : Material.values())
						{
							returnList.add(material.name());
						}

						break;
					case DEPLOYED:
						returnList.add("<boolean>");
						break;
					case REQUIRED_COST:
						returnList.add("<number>");
						break;
					case REQUIRED_LEVEL:
						returnList.add("<number>");
						break;
					case PERMISSION:
						returnList.add("<permission>");
						break;
				}

				break;
			default:
			{
				break;
			}
		}

		return Optional.of(Util.tabLimit(returnList, lastString));
	}


	/**
	 * This method builds single string from args.
	 * @param args List of strings that contains values which must be merged.
	 * @return String that contains all values from input list after 2. index.
	 */
	private String buildStringFromValue(List<String> args)
	{
		StringBuilder builder = new StringBuilder(args.get(2));

		for (int i = 3; i < args.size(); i++)
		{
			builder.append(" ");
			builder.append(args.get(i));
		}

		return builder.toString();
	}


// ---------------------------------------------------------------------
// Section: Constants
// ---------------------------------------------------------------------


	private static final String BIOME = "biomeName";

	private static final String DEPLOYED = "deployed";

	private static final String FRIENDLY_NAME = "friendlyName";

	private static final String DESCRIPTION = "description";

	private static final String ICON = "icon";

	private static final String REQUIRED_COST = "requiredCost";

	private static final String REQUIRED_LEVEL = "requiredLevel";

	private static final String PERMISSION = "permission";
}
