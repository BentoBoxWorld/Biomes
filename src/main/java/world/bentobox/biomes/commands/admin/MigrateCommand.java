package world.bentobox.biomes.commands.admin;


import java.util.List;

import world.bentobox.bentobox.api.addons.Addon;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.biomes.BiomesAddon;


public class MigrateCommand extends CompositeCommand {

    /**
     * Migrates challenges
     * @param addon - addon
     * @param cmd - command
     */
    public MigrateCommand(Addon addon, CompositeCommand cmd) {
        super(addon, cmd, "migrate");
    }

    @Override
    public boolean execute(User user, String label, List<String> args) {
        ((BiomesAddon)getAddon()).getAddonManager().migrateDatabase(user, getWorld());

        return true;
    }


    @Override
    public void setup() {
        this.setPermission("biomes.admin");
        this.setParametersHelp("biomes.commands.admin.migrate.parameters");
        this.setDescription("biomes.commands.admin.migrate.description");
    }
}
