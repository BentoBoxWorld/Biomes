//
// Created by BONNe
// Copyright - 2019
//


package world.bentobox.biomes.database.objects.adapters;


import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import world.bentobox.bentobox.database.objects.adapters.AdapterInterface;
import world.bentobox.biomes.config.Settings.Lore;


/**
 * This adapter allows to serialize and deserialize ChallengeLore object.
 */
public class LoreAdapter implements AdapterInterface<List<Lore>, List<String>>
{
	@SuppressWarnings("unchecked")
	@Override
	public List<Lore> deserialize(Object from)
	{
		List<Lore> result;

		if (from instanceof List)
		{
			result = ((List<String>) from).stream().
				map(Lore::getLore).
				collect(Collectors.toCollection(ArrayList::new));
		}
		else
		{
			result = new ArrayList<>(0);
		}

		return result;
	}


	@SuppressWarnings("unchecked")
	@Override
	public List<String> serialize(Object to)
	{
		List<String> result;

		if (to instanceof List)
		{
			result = ((List<Lore>) to).stream().
				map(Lore::name).
				collect(Collectors.toCollection(ArrayList::new));
		}
		else
		{
			result = new ArrayList<>(0);
		}

		return result;
	}
}
