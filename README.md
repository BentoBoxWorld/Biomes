# addon-biomes
Biomes addon for BSkyBlock and AcidIsland. It allows to change biome on Island.

## How to use

1. Place the addon jar in the addons folder of the BentoBox plugin
2. Restart the server
3. The addon will create a data folder and inside the folder will be a *config.yml* and an example *biomes.yml*
4. Edit the config.yml and *biomes.yml* files how you want. The *biomes.yml* is for importing only.
5. Restart the server
6. To import biomes into BSkyBlock do */bsb biomes import*. To import into AcidIsland do */acid biomes import*.

## Config.yml

The *config.yml* has following sections:

* Advanced Menu: if this is *true*, users will be able to choose all options how the biome can be updated.
* Default Type: this allows to choose default selected type that will be available when users opens Biome GUI.
	Valid values are *'ISLAND', 'CHUNK' and 'SQUARE'*
* Default Size: this allows to set default value that will be set when Biome GUI is opened. It must be integer larger then 0.
* Timeout: this allows to set some delay before user can change biome on his island again. **NOT IMPLEMENTED**

## Biomes.yml

This file contains all necessary information about default biomes. It is necessary just for first import. 
If you change values in biomes.yml, then to apply them, you must run **/bsb biomes import** or **/acid biomes import**.

If you want to force an overwrite of biomes via an import, add the **overwrite** option to the end of the import command.
Note that you must import biomes into both BSkyBlock and AcidIsland separately.

## Admin commands

There are a few admin commands and more being written. The main biomes admin command is **/bsb biomes** or **/acid biomes**. Use 

* /bsbadmin biomes help : Show help for all the commands
* /bsbadmin biomes import [overwrite]: import challenges from challenges.yml
* /bsbadmin biomes: To open Admin GUI. 

