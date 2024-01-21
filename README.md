# Biomes Addon
[![Discord](https://img.shields.io/discord/272499714048524288.svg?logo=discord)](https://discord.bentobox.world)
[![Build Status](https://ci.codemc.org/buildStatus/icon?job=BentoBoxWorld/Biomes)](https://ci.codemc.org/job/BentoBoxWorld/job/Biomes/)

Biomes addon for SkyBlock, SkyGrid, CaveBlock and AcidIsland. It allows island biome changing.

## Where to find

Download the latest release from the BentoBox Downloads site (https://download.bentobox.world) or 
you can try **snapshot builds** where you can check and test new features that will be implemented in next release from [Jenkins Server](https://ci.bentobox.world/Biomes/lastStableBuild/).

If you like this addon but something is missing or is not working as you want, you can always submit an [Issue request](https://github.com/BentoBoxWorld/Biomes/issues) or get a support in Discord [BentoBox ![icon](https://avatars2.githubusercontent.com/u/41555324?s=15&v=4)](https://discord.gg/JgWKvR)

More information about the Biomes Addon you can find here: [Wiki Biomes](https://docs.bentobox.world/en/latest/addons/Biomes/)

## Translations

As most of BentoBox projects, Biomes Addon is translatable in any language. Everyone can contribute, and translate some parts of the addon in their language via [GitLocalize](https://gitlocalize.com/repo/2894).
If your language is not in the list, please contact to developers via Discord and it will be added there.

## How to use

1. Place the addon jar in the addons folder of the BentoBox plugin
2. Restart the server
3. The addon will create a data folder and inside the folder will be a *config.yml* and an example *biomesTemplate.yml*
4. Edit the config.yml and *biomesTemplate.yml* files how you want. The *biomesTemplate.yml* is for importing only.
5. Restart the server

## Config.yml

After the addon is successful installed, it will create a config.yml file. Every option in this file comes with comments.
Please check the file for more information.
Most options are also editable admin via commands.

## Biomes.yml

This file contains all necessary information about default biomes. 
If you change values in biomes.yml, then to apply them, you must run **/bsb biomes import** or **/acid biomes import**.

If you want to force an overwrite of the biomes via an import, add the **overwrite** option to the end of the import command.
Note that you must import biomes into both BSkyBlock and AcidIsland separately.

## User commands

Users can access to biomes addon commands with `/[gamemode] biomes` command. By default for BSkyBlock it is **/island biomes**, for AcidIsland - **/ai biomes**, for CaveBlock - **/cave biomes**, for SkyGrid - **/sg biomes**.

* `/is biomes`: This method opens GUI that allows to change biome on User island.
* `/is biomes help`: Show help for all the commands
* `/is biomes buy <biome>`: This command allows to buy given biome.
* `/is biomes set <biome> [<type>] [<size>]`: This command allows to change biome on island without opening GUI. If prarameters < type> and < size> are not provided, command uses default values from addon config.

## Admin commands

There are a few admin commands and more being written. The main biomes admin command is **/bsb biomes**, **/cba biomes**, **/sgadmin biomes** & **/acid biomes**. Use 

* `/bsbadmin biomes`: To open Admin GUI. 
* `/bsbadmin biomes help` : Show help for all the commands
* `/bsbadmin biomes import <file>`: import biomes from biomes.yml
* `/bsbadmin biomes set <player> <biome> [<type>] [<size>]`: This command works the same as user biome set command, but it is necessary to provide also player, which island biome will be updated.
* `/bsbadmin biomes unlock <player> <biome> [true]`: This command unlocks passed biome to a player island..

## Permissions

`{GameModePrefix}` is permission prefix for each Game Mode addon. F.e. BSkyBlock addon permission prefix is 'bskyblock' but AcidIsland permission prefix is 'acidisland'.

###### User permissions:
- `{GameModePrefix}.biomes`: player can use biomes command that opens GUI.
- `{GameModePrefix}.biomes.buy`: player can use biomes buy command.
- `{GameModePrefix}.biomes.set`: player can use biomes set command.

###### Admin permissions:
- `{GameModePrefix}.admin.biomes`: player can use admin biomes command that opens GUI.
