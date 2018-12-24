package world.bentobox.addons.biomes.commands.user;


import java.util.*;

import world.bentobox.addons.biomes.BiomesAddon;
import world.bentobox.addons.biomes.objects.BiomesObject;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.util.Util;


/**
 * This method returns information about provided biome in chat.
 */
public class BiomesInfoCommand extends CompositeCommand
{
	public BiomesInfoCommand(BiomesAddon addon, BiomesCommand command)
	{
		super(addon, command, "info");
	}

	@Override
	public void setup()
	{
		this.setPermission("island.biomes");
		this.setParametersHelp("biomes.info.parameters");
		this.setDescription("biomes.info.description");

		this.setOnlyPlayer(true);
	}


	@Override
	public boolean execute(User user, String label, List<String> args)
	{
		BiomesObject biomesObject = this.getBiomeObject(args, user);

		if (biomesObject != null)
		{
			user.sendMessage("biomes.info.message.header", "[name]", biomesObject.getFriendlyName());
			user.sendMessage("biomes.info.message.type", "[type]", biomesObject.getBiomeName());
			user.sendMessage("biomes.info.message.description", "[description]", this.getSingleLineDescription(biomesObject.getDescription()));
			user.sendMessage("biomes.info.message.level", "[level]", Long.toString(biomesObject.getRequiredLevel()));
			user.sendMessage("biomes.info.message.cost","[cost]", Integer.toString(biomesObject.getRequiredCost()));

			return true;
		}
		else
		{
			this.showHelp(this, user);
			return false;
		}
	}


	@Override
	public Optional<List<String>> tabComplete(User user, String alias, List<String> args)
	{
 		if (args.size() > 3)
		{
			// Show biome for first tab.
			return Optional.of(new ArrayList<>());
		}

		String lastString = args.get(args.size() - 1);

		final List<String> returnList = new ArrayList<>();

		List<BiomesObject> biomes =
			((BiomesAddon) this.getParent().getAddon()).getAddonManager().getBiomes();

		// Create suggestions with all biomes that is available for users.

		biomes.forEach(biomesObject -> {
			returnList.addAll(Util.tabLimit(
				Collections.singletonList(biomesObject.getBiomeName()), lastString));
		});

		return Optional.of(returnList);
	}


// ---------------------------------------------------------------------
// Section: Methods
// ---------------------------------------------------------------------


	/**
	 * This method returns BiomesObject or null.
	 * @param args Args that contains all command arguments
	 * @param user Caller user.
	 * @return BiomesObject or null.
	 */
	private BiomesObject getBiomeObject(List<String> args, User user)
	{
		if (!args.isEmpty())
		{
			BiomesObject biome = ((BiomesAddon) this.getParent().getAddon()).getAddonManager().
				getBiomeFromString(args.get(0));

			if (biome == null)
			{
				user.sendMessage(user.getTranslation("biomes.command.error.wrong-biome-name",
					"[biome]",
					args.get(0)));
			}

			return biome;
		}
		else
		{
			user.sendMessage(user.getTranslation("biomes.command.error.biome-not-defined"));
			return null;
		}
	}


	/**
	 * This method transforms stringList to string by appending each list at the end.
	 * @param stringList List with strings that must be concasted.
	 * @return String from stringList.
	 */
	private String getSingleLineDescription(List<String> stringList)
	{
		if (stringList.size() > 1)
		{
			Iterator<String> iter = stringList.iterator();

			StringBuilder returnString = new StringBuilder(iter.next());

			while (iter.hasNext())
			{
				returnString.append(" ").append(iter.next());
			}

			return returnString.toString();
		}
		else
		{
			return stringList.get(0);
		}
	}
}
