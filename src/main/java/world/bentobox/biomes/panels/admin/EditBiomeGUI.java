package world.bentobox.biomes.panels.admin;


import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

import java.util.*;

import net.wesjd.anvilgui.AnvilGUI;
import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.biomes.BiomesAddon;
import world.bentobox.biomes.database.objects.BiomesObject;
import world.bentobox.biomes.panels.CommonGUI;
import world.bentobox.biomes.panels.util.NumberGUI;
import world.bentobox.biomes.panels.util.SelectBiomeGUI;
import world.bentobox.biomes.panels.util.StringListGUI;
import world.bentobox.biomes.panels.GuiUtils;


/**
 * This class contains methods that allows to edit specific biome object.
 */
public class EditBiomeGUI extends CommonGUI
{
	/**
	 * {@inheritDoc}
	 * @param biome Object that must be edited.
	 */
	public EditBiomeGUI(CommonGUI parentPanel, BiomesObject biome)
	{
		super(parentPanel);
		this.biome = biome;
	}


	/**
	 * {@inheritDoc}
	 * @param biome Object that must be edited.
	 */
	public EditBiomeGUI(BiomesAddon addon,
		World world,
		User user,
		String topLabel,
		String permissionPrefix,
		BiomesObject biome)
	{
		super(addon, world, user, topLabel, permissionPrefix);
		this.biome = biome;
	}


	/**
	 * This method builds all necessary elements in GUI panel.
	 */
	@Override
	public void build()
	{
		PanelBuilder panelBuilder = new PanelBuilder().user(this.user).
			name(this.user.getTranslation("biomes.gui.title.admin.edit", "[biome]", this.biome.getFriendlyName()));

		GuiUtils.fillBorder(panelBuilder, Material.PURPLE_STAINED_GLASS_PANE);

		panelBuilder.item(19, this.createButton(Button.BIOME));

		panelBuilder.item(11, this.createButton(Button.NAME));
		panelBuilder.item(20, this.createButton(Button.ICON));
		panelBuilder.item(29, this.createButton(Button.DESCRIPTION));

		panelBuilder.item(21, this.createButton(Button.ORDER));


		panelBuilder.item(14, this.createButton(Button.LEVEL));
		panelBuilder.item(23, this.createButton(Button.COST));
		panelBuilder.item(32, this.createButton(Button.PERMISSION));

		panelBuilder.item(25, this.createButton(Button.DEPLOYED));

		panelBuilder.item(44, this.returnButton);

		panelBuilder.build();
	}


	/**
	 * This method returns button for edit panel of given type.
	 * @param button Type of button.
	 * @return new panel button with requested type.
	 */
	private PanelItem createButton(Button button)
	{
		PanelItemBuilder itemBuilder = new PanelItemBuilder();
		final int lineLength = this.addon.getSettings().getLoreLineLength();

		switch (button)
		{
			case BIOME:
			{
				itemBuilder.name(this.user.getTranslation("biomes.gui.buttons.admin.change-biome"));

				List<String> description = new ArrayList<>(2);
				description.add(this.user.getTranslation("biomes.gui.descriptions.admin.change-biome"));
				description.add(this.user.getTranslation("biomes.gui.descriptions.current-value",
					"[value]", this.biome.getBiome().name()));
				itemBuilder.description(GuiUtils.stringSplit(description, this.addon.getSettings().getLoreLineLength()));

				itemBuilder.icon(Material.WATER_BUCKET);
				itemBuilder.clickHandler((panel, user, clickType, slot) -> {
					// Open select gui
					new SelectBiomeGUI(this.user, this.biome.getBiome(), lineLength, (status, value) -> {
						this.biome.setBiome(value);
						this.build();
					});

					return true;
				});
				break;
			}
			case PERMISSION:
			{
				itemBuilder.name(this.user.getTranslation("biomes.gui.buttons.admin.required-permissions"));

				List<String> description = new ArrayList<>(this.biome.getRequiredPermissions().size() + 1);
				description.add(this.user.getTranslation(
					"biomes.gui.descriptions.admin.required-permissions"));

				for (String permission : this.biome.getRequiredPermissions())
				{
					description.add(this.user.getTranslation("biomes.gui.descriptions.permission",
						"[permission]", permission));
				}

				itemBuilder.description(GuiUtils.stringSplit(description, this.addon.getSettings().getLoreLineLength()));

				itemBuilder.icon(Material.REDSTONE_LAMP);
				itemBuilder.clickHandler((panel, user, clickType, slot) -> {
					new StringListGUI(this.user, this.biome.getRequiredPermissions(), lineLength, (status, value) -> {
						if (status)
						{
							this.biome.setRequiredPermissions(new HashSet<>(value));
						}

						this.build();
					});

					return true;
				});
				break;
			}
			case LEVEL:
			{
				itemBuilder.name(this.user.getTranslation("biomes.gui.buttons.admin.required-level"));

				List<String> description = new ArrayList<>(2);
				description.add(this.user.getTranslation("biomes.gui.descriptions.admin.required-level"));
				description.add(this.user.getTranslation("biomes.gui.descriptions.current-value",
					"[value]", Long.toString(this.biome.getRequiredLevel())));
				itemBuilder.description(GuiUtils.stringSplit(description, this.addon.getSettings().getLoreLineLength()));

				itemBuilder.icon(this.addon.isLevelProvided() ? Material.BEACON : Material.BARRIER);
				itemBuilder.clickHandler((panel, user, clickType, slot) -> {
					new NumberGUI(this.user, (int) this.biome.getRequiredLevel(), lineLength, (status, value) -> {
						if (status)
						{
							this.biome.setRequiredLevel(value);
						}

						this.build();
					});

					return true;
				});

				break;
			}
			case COST:
			{
				itemBuilder.name(this.user.getTranslation("biomes.gui.buttons.admin.required-money"));

				List<String> description = new ArrayList<>(2);
				description.add(this.user.getTranslation("biomes.gui.descriptions.admin.required-money"));
				description.add(this.user.getTranslation("biomes.gui.descriptions.current-value",
					"[value]", Long.toString(this.biome.getRequiredCost())));
				itemBuilder.description(GuiUtils.stringSplit(description, this.addon.getSettings().getLoreLineLength()));

				itemBuilder.icon(this.addon.isEconomyProvided() ? Material.GOLD_INGOT : Material.BARRIER);
				itemBuilder.clickHandler((panel, user, clickType, slot) -> {
					new NumberGUI(this.user, this.biome.getRequiredCost(), 0, lineLength, (status, value) -> {
						if (status)
						{
							this.biome.setRequiredCost(value);
						}

						this.build();
					});
					return true;
				});

				break;
			}
			case NAME:
			{
				itemBuilder.name(this.user.getTranslation("biomes.gui.buttons.admin.name"));

				List<String> description = new ArrayList<>(2);
				description.add(this.user.getTranslation("biomes.gui.descriptions.admin.name"));
				description.add(this.user.getTranslation("biomes.gui.descriptions.current-value",
					"[value]", this.biome.getFriendlyName()));
				itemBuilder.description(GuiUtils.stringSplit(description, this.addon.getSettings().getLoreLineLength()));

				itemBuilder.icon(Material.BOOK);
				itemBuilder.clickHandler((panel, user, clickType, slot) -> {
					new AnvilGUI(this.addon.getPlugin(),
						this.user.getPlayer(),
						this.biome.getFriendlyName(),
						(player, reply) -> {
							this.biome.setFriendlyName(reply);
							this.build();
							return reply;
						});

					return true;
				});
				break;
			}
			case DEPLOYED:
			{
				itemBuilder.name(this.user.getTranslation("biomes.gui.buttons.admin.deployment"));

				List<String> description = new ArrayList<>(2);
				description.add(this.user.getTranslation("biomes.gui.descriptions.admin.deployment"));
				description.add(this.user.getTranslation("biomes.gui.descriptions.current-value",
					"[value]",
					this.biome.isDeployed() ?
						this.user.getTranslation("biomes.gui.descriptions.enabled") :
						this.user.getTranslation("biomes.gui.descriptions.disabled")));
				itemBuilder.description(GuiUtils.stringSplit(description, this.addon.getSettings().getLoreLineLength()));
				itemBuilder.icon(Material.LEVER);
				itemBuilder.clickHandler((panel, user, clickType, slot) -> {
					this.biome.setDeployed(!this.biome.isDeployed());
					panel.getInventory().setItem(slot, this.createButton(button).getItem());
					return true;
				});
				itemBuilder.glow(this.biome.isDeployed());
				break;
			}
			case ICON:
			{
				itemBuilder.name(this.user.getTranslation("biomes.gui.buttons.admin.icon"));

				itemBuilder.description(GuiUtils.stringSplit(
					this.user.getTranslation("biomes.gui.descriptions.admin.icon"),
					this.addon.getSettings().getLoreLineLength()));

				itemBuilder.icon(this.biome.getIcon());
				itemBuilder.clickHandler((panel, user, clickType, slot) -> {
					new AnvilGUI(this.addon.getPlugin(),
						this.user.getPlayer(),
						this.biome.getIcon().getType().name(),
						(player, reply) -> {
							Material material = Material.getMaterial(reply);

							if (material != null)
							{
								this.biome.setIcon(new ItemStack(material));
								this.build();
							}
							else
							{
								this.user.sendMessage("biomes.errors.wrong-icon", "[value]", reply);
							}

							return reply;
						});

					return true;
				});
				break;
			}
			case DESCRIPTION:
			{
				itemBuilder.name(this.user.getTranslation("biomes.gui.buttons.admin.description"));

				List<String> values = new ArrayList<>();
				values.add(this.user.getTranslation("biomes.gui.descriptions.admin.description"));
				values.add(this.user.getTranslation("biomes.gui.descriptions.current-value", "[value]", ""));
				values.addAll(this.generateBiomesDescription(this.biome));

				itemBuilder.description(GuiUtils.stringSplit(values, this.addon.getSettings().getLoreLineLength()));
				itemBuilder.icon(Material.WRITTEN_BOOK);
				itemBuilder.clickHandler((panel, user, clickType, slot) -> {
					new StringListGUI(this.user, this.biome.getDescription(), lineLength, (status, value) -> {
						if (status)
						{
							this.biome.setDescription(value);
						}

						this.build();
					});

					return true;
				});
				break;
			}
			case ORDER:
			{
				itemBuilder.name(this.user.getTranslation("biomes.gui.buttons.admin.order"));

				List<String> description = new ArrayList<>(2);
				description.add(this.user.getTranslation("biomes.gui.descriptions.admin.order"));
				description.add(this.user.getTranslation("biomes.gui.descriptions.current-value",
					"[value]", Integer.toString(this.biome.getOrder())));
				itemBuilder.description(description);

				itemBuilder.icon(Material.DROPPER);
				itemBuilder.clickHandler((panel, user, clickType, slot) -> {
					new NumberGUI(this.user, this.biome.getOrder(), -1, 54, lineLength, (status, value) -> {
						if (status)
						{
							this.biome.setOrder(value);
						}

						this.build();
					});

					return true;
				});
				break;
			}
		}

		return itemBuilder.build();
	}


// ---------------------------------------------------------------------
// Section: Enums
// ---------------------------------------------------------------------


	/**
	 * This enum shows which button should be created.
	 */
	private enum Button
	{
		BIOME,
		PERMISSION,
		COST,
		LEVEL,
		ORDER,
		DESCRIPTION,
		ICON,
		NAME,
		DEPLOYED
	}


// ---------------------------------------------------------------------
// Section: Variable
// ---------------------------------------------------------------------


	/**
	 * Holds a biome object that is edited.
	 */
	private BiomesObject biome;
}
