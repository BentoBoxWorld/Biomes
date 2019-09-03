# Biomes Addon
[![Discord](https://img.shields.io/discord/272499714048524288.svg?logo=discord)](https://discord.bentobox.world)
[![Build Status](https://ci.codemc.org/buildStatus/icon?job=BentoBoxWorld/Biomes)](https://ci.codemc.org/job/BentoBoxWorld/job/Biomes/)

Biomes addon for SkyBlock, SkyGrid, CaveBlock and AcidIsland. It allows to change biome on Island.

## Where to find

Currently Biomes Addon is in **Beta stage**, so it may or may not contain bugs... a lot of bugs. Also it means, that some features are not working or implemented. 
Latest official **Beta Release is 1.6.0.1**, and you can download it from [Release tab](https://github.com/BentoBoxWorld/Biomes/releases)

Or you can try **nightly builds** where you can check and test new features that will be implemented in next release from [Jenkins Server](https://ci.codemc.org/job/BentoBoxWorld/job/Biomes/lastStableBuild/).

If you like this addon but something is missing or is not working as you want, you can always submit an [Issue request](https://github.com/BentoBoxWorld/Biomes/issues) or get a support in Discord [BentoBox ![icon](https://avatars2.githubusercontent.com/u/41555324?s=15&v=4)](https://discord.gg/JgWKvR)

## How to use

1. Place the addon jar in the addons folder of the BentoBox plugin
2. Restart the server
3. The addon will create a data folder and inside the folder will be a *config.yml* and an example *biomes.yml*
4. Edit the config.yml and *biomes.yml* files how you want. The *biomes.yml* is for importing only.
5. Restart the server

## Config.yml

After addon is successful installed, it will create config.yml file. Every option in this file comes with comments about them. Please check file for more information.
Most of options are also editable admin via commands.

## Biomes.yml

This file contains all necessary information about default biomes. 
If you change values in biomes.yml, then to apply them, you must run **/bsb biomes import** or **/acid biomes import**.

If you want to force an overwrite of biomes via an import, add the **overwrite** option to the end of the import command.
Note that you must import biomes into both BSkyBlock and AcidIsland separately.

## User commands

Users can access to biomes addon commands with `/[gamemode] biomes` command. By default for BSkyBlock it is **/island biomes**, for AcidIsland - **/ai biomes**, for CaveBlock - **/cave biomes**, for SkyGrid - **/sg biomes**.

* `/is biomes`: This method opens GUI that allows to change biome on User island.
* `/is biomes help`: Show help for all the commands
* `/is biomes info <biome>`: This command returns information about given biome, like cost and necessary level.
* `/is biomes set <biome> [<type>] [<size>]`: This command allows to change biome on island without opening GUI. If prarameters < type> and < size> are not provided, command uses default values from addon config.

## Admin commands

There are a few admin commands and more being written. The main biomes admin command is **/bsb biomes**, **/cba biomes**, **/sgadmin biomes** & **/acid biomes**. Use 

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
