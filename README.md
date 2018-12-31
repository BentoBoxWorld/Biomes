# addon-biomes
Biomes addon for BSkyBlock and AcidIsland. It allows to change biome on Island.

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
* Timeout: this allows to set some delay before user can change biome on his island again. **NOT IMPLEMENTED**

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
* `/bsbadmin biomes set <player> <biome> [<type>] [<size>]`: This command works the same as user biome set command, but it is necessary to provide also player, which island biome will be updated.
* `/bsbadmin biomes edit <biome> <property> <new_value>`: This command allows to edit provided biome property to new value. 
* `/bsbadmin biomes settings <property> <new_value>`: This command allows to edit current addon settings via command. 


