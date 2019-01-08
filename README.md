# Biomes Addon
[![Build Status](https://ci.codemc.org/buildStatus/icon?job=BentoBoxWorld/Biomes)](https://ci.codemc.org/job/BentoBoxWorld/job/Biomes/)

Biomes addon for SkyBlock and AcidIsland. It allows to change biome on Island.

## Where to find

Currently Biomes Addon is in **Alpha stage**, so it may or may not contain bugs... a lot of bugs. Also it means, that some features are not working or implemented. 
Latest official **Alpha Release is 0.4.0**, and you can download it from [Release tab](https://github.com/BentoBoxWorld/Biomes/releases)

Or you can try **nightly builds** where you can check and test new features that will be implemented in next release from [Jenkins Server](https://ci.codemc.org/job/BentoBoxWorld/job/Biomes/lastStableBuild/).

If you like this addon but something is missing or is not working as you want, you can always submit an [Issue request](https://github.com/BentoBoxWorld/Biomes/issues) or get a support in Discord [BentoBox ![icon](https://avatars2.githubusercontent.com/u/41555324?s=15&v=4)](https://discord.gg/JgWKvR)

## How to use

1. Place the addon jar in the addons folder of the BentoBox plugin
2. Restart the server
3. The addon will create a data folder and inside the folder will be a *config.yml* and an example *biomes.yml*
4. Edit the config.yml and *biomes.yml* files how you want. The *biomes.yml* is for importing only.
5. Restart the server

## Config.yml

The *config.yml* has following sections:

* Advanced Menu: if this is *true*, users will be able to choose all options how the biome can be updated.
* Default Type: this allows to choose default selected type that will be available when users opens Biome GUI.
	Valid values are *'ISLAND', 'CHUNK' and 'SQUARE'*
* Default Size: this allows to set default value that will be set when Biome GUI is opened. It must be integer larger then 0.
* Reset Biomes:  if this is *true*, on owner change, addon will reset biome on whole island to default biome, if user does not have biomes.set permission. 
* Timeout: this allows to set some delay before user can change biome on his island again.
* biomes-visibility: this variable allows to choose which biomes users can see in Biomes GUI.
    Valid values are *'ALL', 'DEPLOYED' , 'ACCESSIBLE' and 'TOGGLEABLE'*
* disabled-gamemodes: this allows to disable Biomes Addon in certain GameModes. To disable GameMode just need to add new line with "-" in front of GameMode name, f.e.:
```
disabled-gamemodes:
  - BSkyBlock
```

## Biomes.yml

This file contains all necessary information about default biomes. 
If you change values in biomes.yml, then to apply them, you must run **/bsb biomes import** or **/acid biomes import**.

If you want to force an overwrite of biomes via an import, add the **overwrite** option to the end of the import command.
Note that you must import biomes into both BSkyBlock and AcidIsland separately.

## User commands

* `/is biomes`: This method opens GUI that allows to change biome on User island.
* `/is biomes help`: Show help for all the commands
* `/is biomes info <biome>`: This command returns information about given biome, like cost and necessary level.
* `/is biomes set <biome> [<type>] [<size>]`: This command allows to change biome on island without opening GUI. If prarameters < type> and < size> are not provided, command uses default values from addon config.

## Admin commands

There are a few admin commands and more being written. The main biomes admin command is **/bsb biomes** or **/acid biomes**. Use 

* `/bsbadmin biomes`: To open Admin GUI. 
* `/bsbadmin biomes help` : Show help for all the commands
* `/bsbadmin biomes import [overwrite]`: import biomes from biomes.yml
* `/bsbadmin biomes add <biome>`: add a new biome what can be edited via GUI or `biomes edit` command. Biome will not be deployed. To do it, you should enable it in GUI or via `biomes edit <biome> deployed true` command.
* `/bsbadmin biomes set <player> <biome> [<type>] [<size>]`: This command works the same as user biome set command, but it is necessary to provide also player, which island biome will be updated.
* `/bsbadmin biomes edit <biome> <property> <new_value>`: This command allows to edit provided biome property to new value. 
* `/bsbadmin biomes settings <property> <new_value>`: This command allows to edit current addon settings via command. 

## Permissions

`{GameModePrefix}` is permission prefix for each Game Mode addon. F.e. BSkyBlock addon permission prefix is 'bskyblock' but AcidIsland permission prefix is 'acidisland'.

###### User permissions:
- `{GameModePrefix}.biomes`: player can use biomes command that opens GUI.
- `{GameModePrefix}.biomes.info`: player can use biomes info command.
- `{GameModePrefix}.biomes.set`: player can use biomes set command.

###### Admin permissions:
- `{GameModePrefix}.admin.biomes`: player can use admin biomes command that opens GUI.
- `{GameModePrefix}.admin.biomes.add`: player can use admin biomes add command that adds new biome.
- `{GameModePrefix}.admin.biomes.edit`: player can use admin biomes edit command that edits existing biomes parameters.
- `{GameModePrefix}.admin.biomes.set`: player can use admin biomes set command that allows to change other player biomes.
- `{GameModePrefix}.admin.biomes.import`: player can use admin biomes import command allows to import biomes in world.
- `{GameModePrefix}.admin.biomes.settings`: player can use admin biomes settings command that allows to change addon settings.
